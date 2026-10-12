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

class WorkoutListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var adapter: WorkoutAdapter
    private lateinit var filterDateButton: Button
    private lateinit var clearFilterButton: Button
    private lateinit var filterText: TextView

    private var filterDate: LocalDate? = null   // null = no filter, show everything

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

        filterDateButton.setOnClickListener {
            showDatePicker()
        }

        clearFilterButton.setOnClickListener {
            filterDate = null
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

    override fun onResume() {
        super.onResume()

        if (::adapter.isInitialized) {
            refreshList()
        }
    }

    /** Newest workouts first. */
    private fun loadWorkouts(): List<WorkoutModel> {
        val date = filterDate
        val workouts =
            if (date == null) AppData.workouts.findAll()
            else AppData.workouts.findByDate(date)

        return workouts.sortedByDescending { it.date }
    }

    private fun refreshList() {
        val workouts = loadWorkouts()
        adapter.updateWorkouts(workouts)

        val date = filterDate
        if (date == null) {
            filterText.visibility = View.GONE
            clearFilterButton.visibility = View.GONE
            emptyText.text = getString(R.string.no_workouts)
        } else {
            filterText.text = getString(R.string.showing_date, date.toString())
            filterText.visibility = View.VISIBLE
            clearFilterButton.visibility = View.VISIBLE
            emptyText.text = getString(R.string.no_workouts_on_date)
        }

        emptyText.visibility = if (workouts.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun editWorkout(workout: WorkoutModel) {

        val intent =
            Intent(this, AddEditActivity::class.java)

        intent.putExtra("id", workout.id)

        startActivity(intent)
    }

    private fun deleteWorkout(workout: WorkoutModel) {

        AppData.workouts.delete(workout.id)

        refreshList()
    }

    private fun showDatePicker() {
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
