package ie.setu.gymtracker

import android.app.Application
import timber.log.Timber

// Runs once when the app starts, BEFORE any screen - whichever screen Android opens first.
// (Android can close the app while the camera is open and then reopen straight into the form,
// so setting things up here is safer than in MainActivity)
// It's registered in the Manifest with android:name=".GymTrackerApp"
class GymTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // turn on logging (shows in Logcat)
        Timber.plant(Timber.DebugTree())

        // load the saved workouts from the JSON file
        AppData.initialize(this)

        Timber.i("GymTracker started")
    }
}
