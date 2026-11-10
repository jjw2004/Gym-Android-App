package ie.setu.gymtracker.adapters

import ie.setu.gymtracker.models.WorkoutModel

// What the workout list can tell its screen about.
// WorkoutListActivity implements this, so the adapter never needs to know
// what editing or deleting actually does - it just calls these
interface WorkoutListener {

    // Edit button, or tapping the row
    fun onEditWorkout(workout: WorkoutModel)

    // Delete button, or long-pressing the row
    fun onDeleteWorkout(workout: WorkoutModel)
}
