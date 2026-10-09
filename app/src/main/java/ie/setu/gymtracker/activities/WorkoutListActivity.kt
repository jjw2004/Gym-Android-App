package ie.setu.gymtracker.activities

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

class WorkoutListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var adapter: WorkoutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_workout_list)

        recyclerView =
            findViewById(R.id.workoutsRecyclerView)

        emptyText =
            findViewById(R.id.emptyText)

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
        return AppData.workouts.findAll().sortedByDescending { it.date }
    }

    private fun refreshList() {

        val workouts = loadWorkouts()

        adapter.updateWorkouts(workouts)

        emptyText.visibility =
            if (workouts.isEmpty()) View.VISIBLE else View.GONE
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
}
