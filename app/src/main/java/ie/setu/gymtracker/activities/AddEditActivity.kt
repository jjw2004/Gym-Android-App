package ie.setu.gymtracker.activities

import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ie.setu.gymtracker.AppData
import ie.setu.gymtracker.R
import ie.setu.gymtracker.api.WgerApi
import ie.setu.gymtracker.helpers.PhotoHelpers
import ie.setu.gymtracker.models.ExerciseModel
import ie.setu.gymtracker.models.WorkoutModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.time.LocalDate

// Screen for logging a new workout or editing an existing one
class AddEditActivity : AppCompatActivity() {

    /** The input boxes for one exercise row (item_exercise_input.xml). */
    private class ExerciseRow(view: View) {
        val nameInput: AutoCompleteTextView = view.findViewById(R.id.exerciseNameInput)
        val setsInput: EditText = view.findViewById(R.id.setsInput)
        val repsInput: EditText = view.findViewById(R.id.repsInput)
        val weightInput: EditText = view.findViewById(R.id.weightInput)
        val removeButton: Button = view.findViewById(R.id.removeExerciseButton)
    }

    // views from the layout, set in onCreate
    private lateinit var gymNameInput: EditText
    private lateinit var dateButton: Button
    private lateinit var exercisesContainer: LinearLayout

    // every exercise row currently on the form
    private val exerciseRows = ArrayList<ExerciseRow>()

    // the date picked with the date button (today by default)
    private var selectedDate: LocalDate = LocalDate.now()

    // id of the workout being edited, or null when logging a new one
    private var editingId: Long? = null

    // exercise names downloaded from wger.de - empty until the download finishes
    private var wgerNames: List<String> = emptyList()

    // ---------- progress photos ----------
    private lateinit var beforePhotoImage: ImageView
    private lateinit var afterPhotoImage: ImageView

    // addresses of the chosen photos (null = no photo)
    private var beforePhotoUri: String? = null
    private var afterPhotoUri: String? = null

    // which box was tapped - true = before, false = after
    private var choosingBefore = true

    // the file the camera is saving into right now
    private var cameraFile: File? = null

    // Opens the camera app. It saves the photo into the file we give it.
    // Must be set up before the screen starts, so it's created here and not in onCreate
    private val takePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { photoTaken: Boolean ->
            val file = cameraFile
            if (photoTaken && file != null) {
                Timber.d("Photo taken")
                setPhoto(PhotoHelpers.uriFor(this, file).toString())
            } else {
                Timber.d("Camera cancelled")
                file?.delete()   // nothing was saved, so remove the empty file
            }
            cameraFile = null
        }

    // Opens the gallery / files so the user can pick a photo they already have
    private val pickPhoto =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                // keep permission to read this photo later, when the workout is opened again
                try {
                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: SecurityException) {
                    // some apps don't allow it - the photo still works for now
                }
                Timber.d("Photo picked from gallery")
                setPhoto(uri.toString())
            } else {
                Timber.d("Gallery cancelled")
            }
        }

    // Runs once when the screen opens: finds the views, works out if we are
    // adding or editing, and sets up what each button does
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_edit)

        gymNameInput =
            findViewById(R.id.gymNameInput)

        dateButton =
            findViewById(R.id.dateButton)

        exercisesContainer =
            findViewById(R.id.exercisesContainer)

        beforePhotoImage =
            findViewById(R.id.beforePhotoImage)

        afterPhotoImage =
            findViewById(R.id.afterPhotoImage)

        // tap a photo box to take or pick a photo
        beforePhotoImage.setOnClickListener { choosePhoto(before = true) }
        afterPhotoImage.setOnClickListener { choosePhoto(before = false) }

        val addExerciseButton =
            findViewById<Button>(R.id.addExerciseButton)

        val saveButton =
            findViewById<Button>(R.id.saveButton)

        val cancelButton =
            findViewById<Button>(R.id.cancelButton)

        // an id was sent = edit mode, no id (-1) = new workout
        editingId =
            intent.getLongExtra("id", -1L)
                .takeIf { it != -1L }

        if (editingId != null) {

            findViewById<TextView>(R.id.formTitle)
                .text = getString(R.string.edit_workout)

            loadExistingWorkout(editingId!!)
        } else {
            addExerciseRow()   // start with one empty exercise row
        }

        // the screen was rebuilt (e.g. phone rotated, or the camera was open) - put the photos back
        if (savedInstanceState != null) {
            beforePhotoUri = savedInstanceState.getString("beforePhotoUri")
            afterPhotoUri = savedInstanceState.getString("afterPhotoUri")
            choosingBefore = savedInstanceState.getBoolean("choosingBefore", true)
            cameraFile = savedInstanceState.getString("cameraFile")?.let { File(it) }
        }

        loadWgerNames()
        updateDateButton()
        showPhotos()

        dateButton.setOnClickListener {
            showDatePicker()
        }

        addExerciseButton.setOnClickListener {
            addExerciseRow()
        }

        saveButton.setOnClickListener {
            saveWorkout()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    // Opens a calendar so the user can pick the workout date
    private fun showDatePicker() {
        // DatePickerDialog months are 0-based, LocalDate months are 1-based
        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDate = LocalDate.of(year, month + 1, day)
                updateDateButton()
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        ).show()
    }

    // Shows the picked date on the date button
    private fun updateDateButton() {
        dateButton.text = getString(R.string.date_label, selectedDate.toString())
    }

    // Adds one exercise row (name, sets, reps, weight, remove button) to the form.
    // If an exercise is passed in, its values are filled in (used when editing)
    private fun addExerciseRow(exercise: ExerciseModel? = null) {

        val view = LayoutInflater.from(this)
            .inflate(
                R.layout.item_exercise_input,
                exercisesContainer,
                false
            )

        val row = ExerciseRow(view)

        row.nameInput.setAdapter(makeSuggestionsAdapter())

        if (exercise != null) {
            row.nameInput.setText(exercise.name)
            row.setsInput.setText(exercise.sets.toString())
            row.repsInput.setText(exercise.reps.toString())
            if (exercise.weightKg > 0) {
                row.weightInput.setText(exercise.weightKg.toString())
            }
        }

        row.removeButton.setOnClickListener {
            exercisesContainer.removeView(view)
            exerciseRows.remove(row)
        }

        exercisesContainer.addView(view)
        exerciseRows.add(row)
    }

    // Edit mode: finds the workout by its id and fills the form with its details
    private fun loadExistingWorkout(id: Long) {

        val workout =
            AppData.workouts.findOne(id)

        if (workout == null) {

            Toast.makeText(
                this,
                getString(R.string.workout_not_found),
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        gymNameInput.setText(workout.gymName)
        selectedDate = workout.date
        beforePhotoUri = workout.beforePhotoUri
        afterPhotoUri = workout.afterPhotoUri

        workout.exercises.forEach { addExerciseRow(it) }
    }

    /** Reads the exercise rows. Returns null if any row has invalid input. */
    private fun readExercises(): List<ExerciseModel>? {

        val exercises = ArrayList<ExerciseModel>()

        for (row in exerciseRows) {

            val name = row.nameInput.text.toString().trim()

            if (name.isEmpty()) continue   // skip blank rows

            val sets = row.setsInput.text.toString().toIntOrNull()

            if (sets == null || sets <= 0) {
                row.setsInput.error = getString(R.string.error_enter_sets)
                return null
            }

            val reps = row.repsInput.text.toString().toIntOrNull()

            if (reps == null || reps <= 0) {
                row.repsInput.error = getString(R.string.error_enter_reps)
                return null
            }

            // weight is optional (e.g. bodyweight exercises)
            val weightText = row.weightInput.text.toString()
            val weightKg = if (weightText.isEmpty()) 0.0 else weightText.toDoubleOrNull()

            if (weightKg == null) {
                row.weightInput.error = getString(R.string.error_invalid_number)
                return null
            }

            exercises.add(ExerciseModel(name = name, sets = sets, reps = reps, weightKg = weightKg))
        }

        return exercises
    }

    // Checks the form, then saves a new workout or updates the one being edited,
    // and closes the screen
    private fun saveWorkout() {

        val gymName =
            gymNameInput.text.toString().trim()

        if (gymName.isEmpty()) {
            gymNameInput.error = getString(R.string.error_gym_name_required)
            return
        }

        val exercises = readExercises() ?: return

        val workout = WorkoutModel(
            id = editingId ?: 0L,      // 0 for a new workout - the store gives it a real id
            gymName = gymName,
            date = selectedDate,
            exercises = exercises,
            beforePhotoUri = beforePhotoUri,
            afterPhotoUri = afterPhotoUri
        )

        // the store calls validate() - if anything is wrong it throws an error instead of saving
        try {
            if (editingId == null) {
                AppData.workouts.create(workout)
                Toast.makeText(this, getString(R.string.workout_logged), Toast.LENGTH_SHORT).show()
            } else {
                AppData.workouts.update(workout)
                Toast.makeText(this, getString(R.string.workout_updated), Toast.LENGTH_SHORT).show()
            }

            finish()

        } catch (e: IllegalArgumentException) {
            Timber.w("Validation failed: ${e.message}")
            Toast.makeText(
                this,
                e.message ?: getString(R.string.error_check_workout),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /** Downloads wger exercise names in the background, then refreshes every name box. */
    private fun loadWgerNames() {
        lifecycleScope.launch {
            wgerNames = WgerApi.exerciseNames()

            // rows made before the download finished still have the old suggestions
            for (row in exerciseRows) {
                row.nameInput.setAdapter(makeSuggestionsAdapter())
            }
        }
    }

    /** Builds the suggestions for an exercise name box: your own exercises first, then wger's, no duplicates. */
    private fun makeSuggestionsAdapter(): ArrayAdapter<String> {
        val names = (AppData.workouts.exerciseNames() + wgerNames)
            .distinctBy { it.lowercase() }

        return ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names)
    }

    // Asks: take a photo, pick one from the gallery, or remove the current one
    private fun choosePhoto(before: Boolean) {
        choosingBefore = before
        val current = if (before) beforePhotoUri else afterPhotoUri

        val options = mutableListOf(getString(R.string.take_photo), getString(R.string.choose_from_gallery))
        if (current != null) {
            options.add(getString(R.string.remove_photo))   // only offer "remove" if there's a photo
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(if (before) R.string.before_photo_title else R.string.after_photo_title)
            .setItems(options.toTypedArray()) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> pickPhoto.launch(arrayOf("image/*"))   // only show images
                    2 -> setPhoto(null)
                }
            }
            .show()
    }

    // Makes an empty file, then opens the camera app to save a photo into it
    private fun openCamera() {
        val file = PhotoHelpers.newPhotoFile(this)
        cameraFile = file
        try {
            takePhoto.launch(PhotoHelpers.uriFor(this, file))
        } catch (e: ActivityNotFoundException) {
            Timber.w("No camera app: ${e.message}")
            Toast.makeText(this, getString(R.string.no_camera_app), Toast.LENGTH_SHORT).show()
            file.delete()
            cameraFile = null
        }
    }

    // Saves the new photo address in the box that was tapped (null = remove it)
    private fun setPhoto(uri: String?) {
        if (choosingBefore) {
            beforePhotoUri = uri
        } else {
            afterPhotoUri = uri
        }
        showPhotos()
    }

    // Shows both photos, or a camera icon in a box with no photo
    private fun showPhotos() {
        PhotoHelpers.showPhoto(beforePhotoImage, beforePhotoUri)
        PhotoHelpers.showPhoto(afterPhotoImage, afterPhotoUri)
    }

    // Android can rebuild this screen (rotation, or while the camera app is open),
    // which would lose the photos - so save them here and read them back in onCreate
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("beforePhotoUri", beforePhotoUri)
        outState.putString("afterPhotoUri", afterPhotoUri)
        outState.putBoolean("choosingBefore", choosingBefore)
        outState.putString("cameraFile", cameraFile?.absolutePath)
    }
}
