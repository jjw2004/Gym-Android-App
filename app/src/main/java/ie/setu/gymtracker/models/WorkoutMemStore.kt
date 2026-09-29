package ie.setu.gymtracker.models

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
}
