package ie.setu.gymtracker.models

/**
 * A single exercise performed during a workout, e.g. Bench Press 3 x 10 @ 60kg.
 */
data class ExerciseModel(
    val name: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val weightKg: Double = 0.0
)
