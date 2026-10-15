package ie.setu.gymtracker

import ie.setu.gymtracker.models.WorkoutMemStore
import ie.setu.gymtracker.models.WorkoutStore

/**
 * Singleton so every Activity shares the same workout store.
 */
object AppData {
    // the one store every screen uses - change this line to use a different kind of store
    val workouts: WorkoutStore = WorkoutMemStore()
}
