package ie.setu.gymtracker.models

/**
 * A single exercise performed during a workout, e.g. Bench Press 3 x 10 @ 60kg.
 */
data class ExerciseModel(
    val name: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val weightKg: Double = 0.0   // 0.0 means bodyweight (e.g. pull ups)
) {
    // Checks the exercise makes sense. require() throws an IllegalArgumentException
    // with the message if the check is false
    fun validate() {
        require(name.isNotBlank()) {
            "Exercise name must not be empty."
        }
        require(sets > 0) {
            "Sets must be more than 0 ($name)."
        }
        require(reps > 0) {
            "Reps must be more than 0 ($name)."
        }
        require(weightKg.isFinite() && weightKg >= 0.0) {
            "Weight can't be negative ($name)."
        }
    }
}
