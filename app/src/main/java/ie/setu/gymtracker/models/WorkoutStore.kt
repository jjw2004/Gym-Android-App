package ie.setu.gymtracker.models

import java.time.LocalDate

interface WorkoutStore {
    fun findAll(): List<WorkoutModel>
    fun findOne(id: Long): WorkoutModel?
    fun findByGym(gymName: String): List<WorkoutModel>
    fun create(workout: WorkoutModel)
    fun update(workout: WorkoutModel): Boolean
    fun delete(id: Long): Boolean

    // all workouts on one exact day
    fun findByDate(date: LocalDate): List<WorkoutModel>

    // workouts whose gym name contains gymName, and (if date isn't null) are on that day
    fun search(gymName: String, date: LocalDate?): List<WorkoutModel>

    // every exercise name logged so far, no duplicates, A-Z
    fun exerciseNames(): List<String>
}
