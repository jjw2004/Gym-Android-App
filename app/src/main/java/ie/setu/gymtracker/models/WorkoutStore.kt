package ie.setu.gymtracker.models

import java.time.LocalDate

// The list of things any workout store must be able to do.
// WorkoutMemStore does them in memory - a different store (e.g. one that saves
// to a file) could be swapped in by changing one line in AppData
interface WorkoutStore {
    // get every workout
    fun findAll(): List<WorkoutModel>
    // get one workout by id (null if not found)
    fun findOne(id: Long): WorkoutModel?
    // workouts whose gym name contains this text
    fun findByGym(gymName: String): List<WorkoutModel>
    // save a new workout
    fun create(workout: WorkoutModel)
    // change a saved workout's details (false if not found)
    fun update(workout: WorkoutModel): Boolean
    // remove a workout (true if it was removed)
    fun delete(id: Long): Boolean

    // all workouts on one exact day
    fun findByDate(date: LocalDate): List<WorkoutModel>

    // workouts whose gym name contains gymName, and (if date isn't null) are on that day
    fun search(gymName: String, date: LocalDate?): List<WorkoutModel>

    // every exercise name logged so far, no duplicates, A-Z
    fun exerciseNames(): List<String>
}
