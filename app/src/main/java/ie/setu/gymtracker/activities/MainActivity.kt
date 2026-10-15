package ie.setu.gymtracker.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import ie.setu.gymtracker.R

// The menu screen - the first screen you see when the app opens
class MainActivity : AppCompatActivity() {

    // Shows the menu and makes the two buttons open the other screens
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val viewWorkoutsButton =
            findViewById<Button>(R.id.viewWorkoutsButton)

        val logWorkoutButton =
            findViewById<Button>(R.id.logWorkoutButton)

        // open the list of workouts
        viewWorkoutsButton.setOnClickListener {
            startActivity(
                Intent(this, WorkoutListActivity::class.java)
            )
        }

        // open the form with no id, so it's a new workout
        logWorkoutButton.setOnClickListener {
            startActivity(
                Intent(this, AddEditActivity::class.java)
            )
        }
    }
}
