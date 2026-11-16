package ie.setu.gymtracker

import android.content.Context
import ie.setu.gymtracker.models.WorkoutJSONStore
import ie.setu.gymtracker.models.WorkoutStore
import java.io.File

/**
 * Singleton so every Activity shares the same workout store.
 */
object AppData {

    // the one store every screen uses. "lateinit" because it needs a Context to find the file,
    // so it's set in initialize(). "private set" = only AppData can change it
    lateinit var workouts: WorkoutStore
        private set

    // Creates the store that saves to workouts.json in the app's private storage.
    // Called once when the app starts (GymTrackerApp)
    fun initialize(context: Context) {
        if (::workouts.isInitialized) return   // already set up - don't load the file twice

        // the type is still the WorkoutStore interface, so the screens didn't need to change
        workouts = WorkoutJSONStore(File(context.filesDir, "workouts.json"))
    }
}
