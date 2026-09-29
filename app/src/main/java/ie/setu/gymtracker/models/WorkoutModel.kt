package ie.setu.gymtracker.models

import java.time.LocalDate

/**
 * A single gym session: which gym you went to, when, and the exercises you did.
 * Kotlin automatically generates toString(), equals(), hashCode(), and copy().
 */
data class WorkoutModel(
    var id: Long = 0L,
    val gymName: String = "",
    val date: LocalDate = LocalDate.now(),
    val exercises: List<ExerciseModel> = emptyList()
    // TODO add more stuff here such as notes, duration, gym location
)
