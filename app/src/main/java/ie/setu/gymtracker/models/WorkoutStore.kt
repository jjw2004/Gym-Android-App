package ie.setu.gymtracker.models
import java.time.LocalDate
interface WorkoutStore {
    fun findAll(): List<WorkoutModel>
    fun findOne(id: Long): WorkoutModel?
    fun findByGym(gymName: String): List<WorkoutModel>
    fun create(workout: WorkoutModel)
    fun update(workout: WorkoutModel): Boolean
    fun delete(id: Long): Boolean
    fun findByDate(date: LocalDate): List<WorkoutModel>
}
