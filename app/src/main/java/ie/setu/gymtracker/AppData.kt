package ie.setu.gymtracker

import ie.setu.gymtracker.models.WorkoutMemStore
import ie.setu.gymtracker.models.WorkoutStore

/**
 * Singleton so every Activity shares the same workout store.
 */
object AppData {
    val workouts: WorkoutStore = WorkoutMemStore()
}
