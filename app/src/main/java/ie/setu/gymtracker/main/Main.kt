package ie.setu.gymtracker.main

import ie.setu.gymtracker.models.ExerciseModel
import ie.setu.gymtracker.models.WorkoutMemStore
import ie.setu.gymtracker.models.WorkoutModel
import java.time.LocalDate
import java.time.format.DateTimeParseException

// the store used by the console menu
val store = WorkoutMemStore()

// Sprint 1 console version: shows the menu over and over until you enter 0.
// Run it with: right-click Main.kt > More Run/Debug > Run 'MainKt' with Coverage
fun main() {
    println("=== GymTracker Console App ===")
    var input: Int
    do {
        input = menu()
        when (input) {
            1 -> addWorkout()
            2 -> listWorkouts()
            3 -> updateWorkout()
            4 -> addExerciseToWorkout()
            5 -> deleteWorkout()
            6 -> searchWorkout()
            7 -> searchByGym()
            0 -> println("\nExiting GymTracker. Goodbye!")
            else -> println("\nInvalid option. Please try again.")
        }
    } while (input != 0)
}

// Prints the menu and returns the number typed (-1 if it isn't a number)
fun menu(): Int {
    println("\n----------------------------------")
    println(" MAIN MENU")
    println("----------------------------------")
    println(" 1. Log a Workout")
    println(" 2. List All Workouts")
    println(" 3. Update a Workout")
    println(" 4. Add Exercise to a Workout")
    println(" 5. Delete a Workout")
    println(" 6. Search Workout by ID")
    println(" 7. Search Workouts by Gym")
    println(" 0. Exit")
    print("\nEnter option: ")
    return readlnOrNull()?.toIntOrNull() ?: -1
}

// Asks for a gym, date and exercises, then saves a new workout
fun addWorkout() {
    println("\n--- Log a Workout ---")
    print("Enter Gym Name: ")
    val gymName = readlnOrNull()?.trim().orEmpty()

    if (gymName.isEmpty()) {
        println("Gym name cannot be empty. Creation cancelled.")
        return
    }

    val date = readDate(LocalDate.now())
    val exercises = readExercises()

    val workout = WorkoutModel(gymName = gymName, date = date, exercises = exercises)
    store.create(workout)
    println("Workout logged successfully with ID: ${workout.id}")
}

// Prints every workout
fun listWorkouts() {
    println("\n--- All Workouts ---")
    val workouts = store.findAll()
    if (workouts.isEmpty()) {
        println("No workouts logged yet.")
    } else {
        workouts.forEach { printWorkout(it) }
    }
}

// Changes a workout's gym name and date (leave blank to keep the old value)
fun updateWorkout() {
    println("\n--- Update Workout ---")
    listWorkouts()
    if (store.findAll().isEmpty()) return

    print("\nEnter ID of Workout to update: ")
    val id = readlnOrNull()?.toLongOrNull()
    val workout = id?.let { store.findOne(it) }

    if (workout != null) {
        print("Enter New Gym Name [${workout.gymName}]: ")
        val gymName = readlnOrNull()?.trim().orEmpty().ifEmpty { workout.gymName }
        val date = readDate(workout.date)

        val updated = store.update(workout.copy(gymName = gymName, date = date))
        if (updated) println("Workout updated successfully.")
    } else {
        println("Workout with ID $id not found.")
    }
}

// Adds more exercises to a workout that is already saved
fun addExerciseToWorkout() {
    println("\n--- Add Exercise to Workout ---")
    listWorkouts()
    if (store.findAll().isEmpty()) return

    print("\nEnter ID of Workout: ")
    val id = readlnOrNull()?.toLongOrNull()
    val workout = id?.let { store.findOne(it) }

    if (workout != null) {
        val newExercises = readExercises()
        if (newExercises.isNotEmpty()) {
            store.update(workout.copy(exercises = workout.exercises + newExercises))
            println("${newExercises.size} exercise(s) added to workout ${workout.id}.")
        } else {
            println("No exercises added.")
        }
    } else {
        println("Workout with ID $id not found.")
    }
}

// Removes a workout by its id
fun deleteWorkout() {
    println("\n--- Delete Workout ---")
    listWorkouts()
    if (store.findAll().isEmpty()) return

    print("\nEnter ID of Workout to delete: ")
    val id = readlnOrNull()?.toLongOrNull()

    if (id != null) {
        val deleted = store.delete(id)
        if (deleted) {
            println("Workout with ID $id deleted successfully.")
        } else {
            println("Workout with ID $id not found.")
        }
    } else {
        println("Invalid ID entered.")
    }
}

// Finds and prints one workout by its id
fun searchWorkout() {
    println("\n--- Search Workout ---")
    print("Enter ID: ")
    val id = readlnOrNull()?.toLongOrNull()

    if (id != null) {
        val workout = store.findOne(id)
        if (workout != null) {
            printWorkout(workout)
        } else {
            println("No workout found with ID $id.")
        }
    } else {
        println("Invalid ID entered.")
    }
}

// Prints every workout at a gym (part of the name is enough)
fun searchByGym() {
    println("\n--- Search Workouts by Gym ---")
    print("Enter Gym Name: ")
    val gymName = readlnOrNull()?.trim().orEmpty()

    val workouts = store.findByGym(gymName)
    if (workouts.isEmpty()) {
        println("No workouts found at \"$gymName\".")
    } else {
        workouts.forEach { printWorkout(it) }
    }
}

// ---------- helpers ----------

// Prints one workout and its exercises
fun printWorkout(workout: WorkoutModel) {
    println("ID: ${workout.id} | Gym: ${workout.gymName} | Date: ${workout.date}")
    if (workout.exercises.isEmpty()) {
        println("    (no exercises)")
    } else {
        workout.exercises.forEach {
            println("    - ${it.name}: ${it.sets} sets x ${it.reps} reps @ ${it.weightKg}kg")
        }
    }
}

/** Prompts for a date (yyyy-MM-dd). Blank or invalid input keeps [default]. */
fun readDate(default: LocalDate): LocalDate {
    print("Enter Date (yyyy-MM-dd) [$default]: ")
    val input = readlnOrNull()?.trim().orEmpty()
    if (input.isEmpty()) return default
    return try {
        LocalDate.parse(input)
    } catch (e: DateTimeParseException) {
        println("Invalid date, using $default.")
        default
    }
}

/** Keeps asking for exercises until the user leaves the name blank. */
fun readExercises(): List<ExerciseModel> {
    val exercises = ArrayList<ExerciseModel>()
    println("Add exercises (leave name blank to finish):")
    while (true) {
        print("  Exercise Name: ")
        val name = readlnOrNull()?.trim().orEmpty()
        if (name.isEmpty()) break

        print("  Sets: ")
        val sets = readlnOrNull()?.toIntOrNull() ?: 0
        print("  Reps: ")
        val reps = readlnOrNull()?.toIntOrNull() ?: 0
        print("  Weight (kg): ")
        val weightKg = readlnOrNull()?.toDoubleOrNull() ?: 0.0

        exercises.add(ExerciseModel(name = name, sets = sets, reps = reps, weightKg = weightKg))
    }
    return exercises
}
