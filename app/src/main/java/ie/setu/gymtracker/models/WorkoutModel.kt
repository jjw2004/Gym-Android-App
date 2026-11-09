package ie.setu.gymtracker.models

import java.time.LocalDate

/**
 * A single gym session: which gym you went to, when, and the exercises you did.
 * Kotlin automatically generates toString(), equals(), hashCode(), and copy().
 */
data class WorkoutModel(
    var id: Long = 0L,                                  // set by the store when the workout is saved
    val gymName: String = "",
    val date: LocalDate = LocalDate.now(),              // today if not set
    val exercises: List<ExerciseModel> = emptyList(),   // one workout can have many exercises
    val beforePhotoUri: String? = null,                 // progress photo before the workout (also proof you went!)
    val afterPhotoUri: String? = null                   // progress photo after the workout
    // TODO add more stuff here such as notes, duration, gym location
) {
    // Checks the workout makes sense before it's saved - throws an IllegalArgumentException if not
    fun validate() {
        require(gymName.isNotBlank()) {
            "Gym name must not be empty."
        }
        exercises.forEach { it.validate() }   // every exercise must be valid too
    }
}
