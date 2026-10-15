package ie.setu.gymtracker.models

import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong

// Keeps every workout in a list in memory (they are lost when the app closes)
class WorkoutMemStore : WorkoutStore {

    // the list of saved workouts
    private val workouts = ArrayList<WorkoutModel>()

    // counter for giving each new workout the next id (1, 2, 3...)
    private val lastId = AtomicLong(0L)

    // Returns every workout
    override fun findAll(): List<WorkoutModel> {
        return workouts.toList()   // return a copy so callers can't modify the store directly
    }

    // Finds the workout with this id, or null if there isn't one
    override fun findOne(id: Long): WorkoutModel? {
        return workouts.find { w -> w.id == id }
    }

    // Returns workouts whose gym name contains the text (ignoring capitals)
    override fun findByGym(gymName: String): List<WorkoutModel> {
        return workouts.filter { w -> w.gymName.contains(gymName, ignoreCase = true) }
    }

    // Saves a new workout and gives it the next id
    override fun create(workout: WorkoutModel) {
        workout.id = lastId.incrementAndGet()
        workouts.add(workout)
    }

    // Replaces an existing workout's details. Returns false if the id wasn't found
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

    // Removes the workout with this id. Returns true if one was removed
    override fun delete(id: Long): Boolean {
        return workouts.removeIf { w -> w.id == id }
    }

    // Returns all workouts on this exact date
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

    // Returns every exercise name logged so far, with no duplicates, A to Z
    override fun exerciseNames(): List<String> {
        return workouts
            .flatMap { w -> w.exercises } // all exercises from all workouts, in one list
            .map { e -> e.name } // just the names
            .distinctBy { name -> name.lowercase() } // "Squat" and "squat" count as one
            .sorted()// A to Z
    }
}
