package ie.setu.gymtracker

import ie.setu.gymtracker.models.ExerciseModel
import ie.setu.gymtracker.models.WorkoutMemStore
import ie.setu.gymtracker.models.WorkoutModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WorkoutMemStoreTest {

    private lateinit var store: WorkoutMemStore

    @Before
    fun setup() {
        store = WorkoutMemStore()
        store.create(WorkoutModel(gymName = "Flyefit Waterford"))
        store.create(WorkoutModel(gymName = "SETU Arena"))
    }

    @Test
    fun createAssignsIds() {
        assertEquals(listOf(1L, 2L), store.findAll().map { it.id })
    }

    @Test
    fun updateReplacesFields() {
        val bench = ExerciseModel(name = "Bench Press", sets = 3, reps = 10, weightKg = 60.0)
        assertTrue(store.update(WorkoutModel(id = 1, gymName = "Flyefit", exercises = listOf(bench))))
        val updated = store.findOne(1)!!
        assertEquals("Flyefit", updated.gymName)
        assertEquals(listOf(bench), updated.exercises)
    }

    @Test
    fun updateMissingReturnsFalse() {
        assertFalse(store.update(WorkoutModel(id = 99, gymName = "Nowhere")))
    }

    @Test
    fun deleteRemovesWorkout() {
        assertTrue(store.delete(1))
        assertNull(store.findOne(1))
        assertFalse(store.delete(1))
    }

    @Test
    fun findByGymIsCaseInsensitive() {
        assertEquals(1, store.findByGym("setu").size)
    }
}
