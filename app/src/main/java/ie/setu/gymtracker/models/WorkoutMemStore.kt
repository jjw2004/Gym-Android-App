package ie.setu.gymtracker.models

import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong

class WorkoutMemStore : WorkoutStore {

    private val workouts = ArrayList<WorkoutModel>()
    private val lastId = AtomicLong(0L)

    override fun findAll(): List<WorkoutModel> {
        return workouts.toList()   // return a copy so callers can't modify the store directly
    }

    override fun findOne(id: Long): WorkoutModel? {
        return workouts.find { w -> w.id == id }
    }

    override fun findByGym(gymName: String): List<WorkoutModel> {
        return workouts.filter { w -> w.gymName.contains(gymName, ignoreCase = true) }
    }

    override fun create(workout: WorkoutModel) {
        workout.id = lastId.incrementAndGet()
        workouts.add(workout)
    }

    override fun update(workout: WorkoutModel): Boolean {
        val foundIndex = workouts.indexOfFirst { w -> w.id == workout.id }
        return if (foundIndex != -1) {
            workouts[foundIndex] = workouts[foundIndex].copy(
                gymName = workout.gymName,
                date = workout.date,
                exercises = workout.exercises
            )
            true
        } else {
            false
        }
    }

    override fun delete(id: Long): Boolean {
        return workouts.removeIf { w -> w.id == id }
    }

    override fun findByDate(date: LocalDate): List<WorkoutModel> {
        // filter keeps only the workouts where the { } part is true
        return workouts.filter { w -> w.date == date }
    }

    // LocalDate? means the date is optional, so null means "any date".
    // An empty gymName matches every gym, because "Xtreme CSC".contains("") is true,
    // so search("", null) just returns everything.
    override fun search(gymName: String, date: LocalDate?): List<WorkoutModel> {
        return workouts.filter { w ->
            // keep it if the gym name matches (ignoring capitals) AND
            // either no date was picked or the dates are the same
            w.gymName.contains(gymName, ignoreCase = true) &&
                (date == null || w.date == date)
        }
    }
}
