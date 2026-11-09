package ie.setu.gymtracker.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutModelTest {

    private fun bench() = ExerciseModel(name = "Bench Press", sets = 3, reps = 10, weightKg = 60.0)

    private fun valid() = WorkoutModel(gymName = "Xtreme CSC", exercises = listOf(bench()))

    @Test
    fun validWorkoutIsAccepted() {
        valid().validate()
    }

    @Test
    fun bodyweightExerciseIsAccepted() {
        valid().copy(exercises = listOf(bench().copy(weightKg = 0.0))).validate()
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankGymNameIsRejected() {
        valid().copy(gymName = "   ").validate()
    }

    @Test(expected = IllegalArgumentException::class)
    fun zeroSetsIsRejected() {
        valid().copy(exercises = listOf(bench().copy(sets = 0))).validate()
    }

    @Test(expected = IllegalArgumentException::class)
    fun zeroRepsIsRejected() {
        valid().copy(exercises = listOf(bench().copy(reps = 0))).validate()
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeWeightIsRejected() {
        valid().copy(exercises = listOf(bench().copy(weightKg = -5.0))).validate()
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankExerciseNameIsRejected() {
        valid().copy(exercises = listOf(bench().copy(name = ""))).validate()
    }

    @Test(expected = IllegalArgumentException::class)
    fun storeRejectsInvalidCreate() {
        WorkoutMemStore().create(valid().copy(gymName = ""))
    }

    @Test
    fun storeSavesNothingWhenInvalid() {
        val store = WorkoutMemStore()
        try {
            store.create(valid().copy(gymName = ""))
        } catch (_: IllegalArgumentException) {
        }
        assertTrue(store.findAll().isEmpty())
    }

    @Test
    fun storeAcceptsValidWorkout() {
        val store = WorkoutMemStore()
        store.create(valid())
        assertEquals(1, store.findAll().size)
    }
}
