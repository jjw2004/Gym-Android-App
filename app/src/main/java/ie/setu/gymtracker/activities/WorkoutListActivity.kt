package ie.setu.gymtracker.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ie.setu.gymtracker.AppData
import ie.setu.gymtracker.R
import ie.setu.gymtracker.adapters.WorkoutAdapter
import ie.setu.gymtracker.models.WorkoutModel
import java.time.LocalDate
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder

// Screen that shows every workout in a list, with search, a date filter,
// and edit / delete buttons on each workout
class WorkoutListActivity : AppCompatActivity() {

    // views from the layout, set in onCreate
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var adapter: WorkoutAdapter
    private lateinit var filterDateButton: Button
    private lateinit var clearFilterButton: Button
    private lateinit var filterText: TextView

    private lateinit var searchInput: EditText

    // the two filters - the list only shows workouts that match both
    private var filterDate: LocalDate? = null   // null = no date picked, any date
    private var searchText = ""                 // "" = nothing typed, any gym

    // Runs once when the screen opens: finds the views, sets up the search box
    // and buttons, and connects the adapter to the list
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_workout_list)

        recyclerView =
            findViewById(R.id.workoutsRecyclerView)

        emptyText =
            findViewById(R.id.emptyText)

        filterDateButton = findViewById(R.id.filterDateButton)
        clearFilterButton = findViewById(R.id.clearFilterButton)
        filterText = findViewById(R.id.filterText)

        searchInput = findViewById(R.id.searchInput)

        // doAfterTextChanged runs this code every time the text in the search bar changes,
        // so the list updates as you type
        searchInput.doAfterTextChanged { text ->
            // text can be null: ?. skips the call if it is, and orEmpty() turns null into ""
            searchText = text?.toString()?.trim().orEmpty()
            refreshList()
        }

        filterDateButton.setOnClickListener {
            showDatePicker()
        }

        // "Show All" turns off both filters
        clearFilterButton.setOnClickListener {
            filterDate = null
            searchInput.setText("")   // emptying the box also clears searchText via the listener above
            refreshList()
        }

        adapter = WorkoutAdapter(
            workouts = loadWorkouts(),

            onEdit = { workout ->
                editWorkout(workout)
            },

            onDelete = { workout ->
                deleteWorkout(workout)
            }
        )

        recyclerView.layoutManager =
            LinearLayoutManager(this)

        recyclerView.adapter = adapter

        val returnButton =
            findViewById<Button>(R.id.returnButton)

        returnButton.setOnClickListener {
            // MainActivity is still underneath us, so just close this screen
            finish()
        }
    }

    // Runs every time this screen comes back into view (e.g. after editing a workout),
    // so the list is always up to date
    override fun onResume() {
        super.onResume()

        if (::adapter.isInitialized) {
            refreshList()
        }
    }

    /** Workouts matching the search and date filter, newest first. */
    private fun loadWorkouts(): List<WorkoutModel> {
        // the store does the filtering - with no search and no date it returns everything
        return AppData.workouts.search(searchText, filterDate)
            .sortedByDescending { it.date }
    }

    /** Reloads the list and shows/hides the label, Show All button and empty message. */
    private fun refreshList() {
        val workouts = loadWorkouts()
        adapter.updateWorkouts(workouts)

        // copy into a val so Kotlin knows it can't change between the null check and using it
        val date = filterDate
        if (date == null) {
            filterText.visibility = View.GONE
        } else {
            filterText.text = getString(R.string.showing_date, date.toString())
            filterText.visibility = View.VISIBLE
        }

        // true if either filter is switched on
        val filtering = date != null || searchText.isNotEmpty()

        // only show "Show All" when there's something to clear
        clearFilterButton.visibility = if (filtering) View.VISIBLE else View.GONE

        // different empty message depending on whether you're filtering or not
        emptyText.text = getString(
            if (filtering) R.string.no_matching_workouts else R.string.no_workouts
        )
        emptyText.visibility = if (workouts.isEmpty()) View.VISIBLE else View.GONE
    }

    // Opens the form in edit mode by sending the workout's id
    private fun editWorkout(workout: WorkoutModel) {

        val intent =
            Intent(this, AddEditActivity::class.java)

        intent.putExtra("id", workout.id)

        startActivity(intent)
    }

    /** Asks "are you sure?" first, and only deletes if the user taps Delete. */
    private fun deleteWorkout(workout: WorkoutModel) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_title)
            .setMessage(getString(R.string.delete_message, workout.gymName, workout.date.toString()))

            // Cancel: null means "do nothing" - the dialog just closes
            .setNegativeButton(R.string.cancel, null)

            // Delete: this code only runs if the user taps Delete
            .setPositiveButton(R.string.delete) { _, _ ->
                AppData.workouts.delete(workout.id)
                refreshList()
            }

            .show()
    }

    /** Opens a calendar; picking a day sets the date filter. */
    private fun showDatePicker() {
        // open the calendar on the current filter date, or today if there isn't one
        val start = filterDate ?: LocalDate.now()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                filterDate = LocalDate.of(year, month + 1, day)   // months start at 0!
                refreshList()
            },
            start.year,
            start.monthValue - 1,
            start.dayOfMonth
        ).show()
    }
}
