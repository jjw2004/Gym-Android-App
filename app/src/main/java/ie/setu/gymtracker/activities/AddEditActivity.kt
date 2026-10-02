package ie.setu.gymtracker.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ie.setu.gymtracker.AppData
import ie.setu.gymtracker.models.ExerciseModel
import ie.setu.gymtracker.models.WorkoutModel
import java.time.LocalDate

class AddEditActivity : AppCompatActivity() {

    /** The input boxes for one exercise row on the form. */
    private class ExerciseRow(
        val layout: LinearLayout,
        val nameInput: EditText,
        val setsInput: EditText,
        val repsInput: EditText,
        val weightInput: EditText
    )

    private lateinit var gymNameInput: EditText
    private lateinit var dateButton: Button
    private lateinit var exercisesLayout: LinearLayout

    private val exerciseRows = ArrayList<ExerciseRow>()
    private var selectedDate: LocalDate = LocalDate.now()

    private var editingId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createUserInterface()

        editingId = intent.getLongExtra("id", -1L)

        if (editingId != -1L) {
            loadExistingWorkout(editingId)
        } else {
            addExerciseRow()   // start with one empty exercise row
        }
    }

    private fun createUserInterface() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        gymNameInput = EditText(this).apply {
            hint = "Gym name"
        }

        dateButton = Button(this).apply {
            setOnClickListener {
                showDatePicker()
            }
        }
        updateDateButton()

        val exercisesTitle = TextView(this).apply {
            text = "Exercises"
            textSize = 20f
            setPadding(0, 32, 0, 8)
        }

        exercisesLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val addExerciseButton = Button(this).apply {
            text = "Add Exercise"

            setOnClickListener {
                addExerciseRow()
            }
        }

        val saveButton = Button(this).apply {
            text = "Save"

            setOnClickListener {
                saveWorkout()
            }
        }

        val cancelButton = Button(this).apply {
            text = "Cancel"

            setOnClickListener {
                finish()
            }
        }

        root.addView(gymNameInput)
        root.addView(dateButton)
        root.addView(exercisesTitle)
        root.addView(exercisesLayout)
        root.addView(addExerciseButton)
        root.addView(saveButton)
        root.addView(cancelButton)

        val scrollView = ScrollView(this).apply {
            fitsSystemWindows = true
            addView(root)
        }

        setContentView(scrollView)
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
        dateButton.text = "Date: $selectedDate"
    }

    private fun addExerciseRow(exercise: ExerciseModel? = null) {

        val rowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16, 0, 16)
        }

        val nameInput = EditText(this).apply {
            hint = "Exercise (e.g. Bench Press)"
            setText(exercise?.name.orEmpty())
        }

        val setsInput = numberInput("Sets", exercise?.sets?.toString())
        val repsInput = numberInput("Reps", exercise?.reps?.toString())
        val weightInput = numberInput("Weight (kg)", exercise?.weightKg?.takeIf { it > 0 }?.toString(), decimal = true)

        // sets, reps and weight side by side on one line
        val numbersLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(setsInput, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(repsInput, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(weightInput, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }

        val row = ExerciseRow(rowLayout, nameInput, setsInput, repsInput, weightInput)

        val removeButton = Button(this).apply {
            text = "Remove Exercise"

            setOnClickListener {
                exercisesLayout.removeView(rowLayout)
                exerciseRows.remove(row)
            }
        }

        rowLayout.addView(nameInput)
        rowLayout.addView(numbersLayout)
        rowLayout.addView(removeButton)

        exercisesLayout.addView(rowLayout)
        exerciseRows.add(row)
    }

    private fun numberInput(hint: String, value: String?, decimal: Boolean = false): EditText {
        return EditText(this).apply {
            this.hint = hint
            setText(value.orEmpty())
            inputType = if (decimal) {
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            } else {
                InputType.TYPE_CLASS_NUMBER
            }
        }
    }

    private fun loadExistingWorkout(id: Long) {

        val workout = AppData.workouts.findOne(id)

        if (workout == null) {
            Toast.makeText(
                this,
                "Workout not found",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        gymNameInput.setText(workout.gymName)
        selectedDate = workout.date
        updateDateButton()

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
                row.setsInput.error = "Enter sets"
                return null
            }

            val reps = row.repsInput.text.toString().toIntOrNull()

            if (reps == null || reps <= 0) {
                row.repsInput.error = "Enter reps"
                return null
            }

            // weight is optional (e.g. bodyweight exercises)
            val weightText = row.weightInput.text.toString()
            val weightKg = if (weightText.isEmpty()) 0.0 else weightText.toDoubleOrNull()

            if (weightKg == null) {
                row.weightInput.error = "Enter a valid number"
                return null
            }

            exercises.add(ExerciseModel(name = name, sets = sets, reps = reps, weightKg = weightKg))
        }

        return exercises
    }

    private fun saveWorkout() {

        val gymName = gymNameInput.text.toString().trim()

        if (gymName.isEmpty()) {
            gymNameInput.error = "Gym name is required"
            return
        }

        val exercises = readExercises() ?: return

        if (editingId == -1L) {

            val workout = WorkoutModel(
                gymName = gymName,
                date = selectedDate,
                exercises = exercises
            )

            AppData.workouts.create(workout)

            Toast.makeText(
                this,
                "Workout logged",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            val workout = WorkoutModel(
                id = editingId,
                gymName = gymName,
                date = selectedDate,
                exercises = exercises
            )

            AppData.workouts.update(workout)

            Toast.makeText(
                this,
                "Workout updated",
                Toast.LENGTH_SHORT
            ).show()
        }

        finish()
    }
}
