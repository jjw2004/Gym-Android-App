package ie.setu.gymtracker.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import ie.setu.gymtracker.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val viewWorkoutsButton =
            findViewById<Button>(R.id.viewWorkoutsButton)

        val logWorkoutButton =
            findViewById<Button>(R.id.logWorkoutButton)

        viewWorkoutsButton.setOnClickListener {
            startActivity(
                Intent(this, WorkoutListActivity::class.java)
            )
        }

        logWorkoutButton.setOnClickListener {
            startActivity(
                Intent(this, AddEditActivity::class.java)
            )
        }
    }
}
