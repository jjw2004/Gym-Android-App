package ie.setu.gymtracker.activities

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ie.setu.gymtracker.AppData

class MainActivity : AppCompatActivity() {

    private lateinit var listLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createUserInterface()
    }

    override fun onResume() {
        super.onResume()

        if (::listLayout.isInitialized) {
            displayWorkouts()
        }
    }

    private fun createUserInterface() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "My Workouts"
            textSize = 28f
            gravity = Gravity.CENTER
        }

        val addButton = Button(this).apply {
            text = "Log Workout"
            setOnClickListener {
                val intent = Intent(this@MainActivity, AddEditActivity::class.java)
                startActivity(intent)
            }
        }

        listLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        root.addView(title, matchWidth())
        root.addView(addButton, matchWidth())
        root.addView(listLayout, matchWidth())

        // ScrollView so a long workout history doesn't run off the screen.
        // fitsSystemWindows keeps content clear of the status and navigation bars.
        val scrollView = ScrollView(this).apply {
            fitsSystemWindows = true
            addView(root)
        }

        setContentView(scrollView)

        displayWorkouts()
    }

    private fun displayWorkouts() {

        listLayout.removeAllViews()

        val workouts = AppData.workouts.findAll().sortedByDescending { it.date }

        if (workouts.isEmpty()) {

            val emptyText = TextView(this).apply {
                text = "No workouts logged yet."
                textSize = 18f
                setPadding(0, 40, 0, 40)
            }

            listLayout.addView(emptyText)

            return
        }

        for (workout in workouts) {

            val workoutLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 20, 0, 20)
            }

            val workoutTitle = TextView(this).apply {
                text = "${workout.gymName} – ${workout.date}"
                textSize = 20f
            }

            workoutLayout.addView(workoutTitle)

            if (workout.exercises.isEmpty()) {
                workoutLayout.addView(TextView(this).apply {
                    text = "(no exercises)"
                    textSize = 16f
                })
            } else {
                for (exercise in workout.exercises) {
                    val weight = if (exercise.weightKg > 0) "@ ${exercise.weightKg}kg" else "(bodyweight)"
                    workoutLayout.addView(TextView(this).apply {
                        text = "• ${exercise.name}: ${exercise.sets} x ${exercise.reps} $weight"
                        textSize = 16f
                    })
                }
            }

            val editButton = Button(this).apply {
                text = "Edit"

                setOnClickListener {
                    val intent = Intent(
                        this@MainActivity,
                        AddEditActivity::class.java
                    )

                    intent.putExtra("id", workout.id)

                    startActivity(intent)
                }
            }

            val deleteButton = Button(this).apply {
                text = "Delete"

                setOnClickListener {
                    AppData.workouts.delete(workout.id)
                    displayWorkouts()
                }
            }

            workoutLayout.addView(editButton)
            workoutLayout.addView(deleteButton)

            listLayout.addView(workoutLayout)
        }
    }

    private fun matchWidth() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
}
