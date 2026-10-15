package ie.setu.gymtracker.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ie.setu.gymtracker.AppData
import ie.setu.gymtracker.R
import ie.setu.gymtracker.models.ExerciseModel
import ie.setu.gymtracker.models.WorkoutModel
import java.time.LocalDate

class AddEditActivity : AppCompatActivity() {

    /** The input boxes for one exercise row (item_exercise_input.xml). */
    private class ExerciseRow(view: View) {
        val nameInput: AutoCompleteTextView = view.findViewById(R.id.exerciseNameInput)
        val setsInput: EditText = view.findViewById(R.id.setsInput)
        val repsInput: EditText = view.findViewById(R.id.repsInput)
        val weightInput: EditText = view.findViewById(R.id.weightInput)
        val removeButton: Button = view.findViewById(R.id.removeExerciseButton)
    }

    private lateinit var gymNameInput: EditText
    private lateinit var dateButton: Button
    private lateinit var exercisesContainer: LinearLayout

    private val exerciseRows = ArrayList<ExerciseRow>()
    private var selectedDate: LocalDate = LocalDate.now()

    private var editingId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_edit)

        gymNameInput =
            findViewById(R.id.gymNameInput)

        dateButton =
            findViewById(R.id.dateButton)

        exercisesContainer =
            findViewById(R.id.exercisesContainer)

        val addExerciseButton =
            findViewById<Button>(R.id.addExerciseButton)

        val saveButton =
            findViewById<Button>(R.id.saveButton)

        val cancelButton =
            findViewById<Button>(R.id.cancelButton)

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

        updateDateButton()

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

    private fun updateDateButton() {
        dateButton.text = getString(R.string.date_label, selectedDate.toString())
    }

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

    private fun saveWorkout() {

        val gymName =
            gymNameInput.text.toString().trim()

        if (gymName.isEmpty()) {
            gymNameInput.error = getString(R.string.error_gym_name_required)
            return
        }

        val exercises = readExercises() ?: return

        if (editingId == null) {

            val workout = WorkoutModel(
                gymName = gymName,
                date = selectedDate,
                exercises = exercises
            )

            AppData.workouts.create(workout)

            Toast.makeText(
                this,
                getString(R.string.workout_logged),
                Toast.LENGTH_SHORT
            ).show()

        } else {

            val workout = WorkoutModel(
                id = editingId!!,
                gymName = gymName,
                date = selectedDate,
                exercises = exercises
            )

            AppData.workouts.update(workout)

            Toast.makeText(
                this,
                getString(R.string.workout_updated),
                Toast.LENGTH_SHORT
            ).show()
        }

        finish()
    }
    /** Builds the list of suggestions for an exercise name box. */
    private fun makeSuggestionsAdapter(): ArrayAdapter<String> {
        val names = AppData.workouts.exerciseNames()
        return ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names)
    }
}
