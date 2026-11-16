package ie.setu.gymtracker.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.LocalDate

class WorkoutJSONStoreTest {

    // a temporary folder for the test JSON file - deleted after each test
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var file: File

    private val bench = ExerciseModel(name = "Bench Press", sets = 3, reps = 10, weightKg = 60.0)

    @Before
    fun setup() {
        file = File(tempFolder.root, "workouts.json")
    }

    // a brand new store reading the same file = what happens when the app is reopened
    private fun reopen() = WorkoutJSONStore(file)

    @Test
    fun startsEmptyWhenThereIsNoFile() {
        assertTrue(WorkoutJSONStore(file).findAll().isEmpty())
    }

    @Test
    fun createdWorkoutIsStillThereAfterReopening() {
        WorkoutJSONStore(file).create(
            WorkoutModel(gymName = "Xtreme CSC", date = LocalDate.of(2026, 11, 16), exercises = listOf(bench))
        )

        val loaded = reopen().findAll()
        assertEquals(1, loaded.size)
        assertEquals("Xtreme CSC", loaded[0].gymName)
        assertEquals(LocalDate.of(2026, 11, 16), loaded[0].date)
        assertEquals(listOf(bench), loaded[0].exercises)
    }

    @Test
    fun photosAreSaved() {
        WorkoutJSONStore(file).create(
            WorkoutModel(gymName = "Flyefit", beforePhotoUri = "content://photos/before.jpg")
        )

        val loaded = reopen().findAll()[0]
        assertEquals("content://photos/before.jpg", loaded.beforePhotoUri)
        assertNull(loaded.afterPhotoUri)
    }

    @Test
    fun updateIsSaved() {
        val store = WorkoutJSONStore(file)
        store.create(WorkoutModel(gymName = "Flyefit"))
        store.update(WorkoutModel(id = 1, gymName = "SETU Arena"))

        assertEquals("SETU Arena", reopen().findOne(1)!!.gymName)
    }

    @Test
    fun deleteIsSaved() {
        val store = WorkoutJSONStore(file)
        store.create(WorkoutModel(gymName = "Flyefit"))
        store.create(WorkoutModel(gymName = "SETU Arena"))
        assertTrue(store.delete(1))

        val loaded = reopen().findAll()
        assertEquals(1, loaded.size)
        assertEquals("SETU Arena", loaded[0].gymName)
    }

    @Test
    fun idsCarryOnAfterReopening() {
        val store = WorkoutJSONStore(file)
        store.create(WorkoutModel(gymName = "Flyefit"))
        store.create(WorkoutModel(gymName = "SETU Arena"))

        val reopened = reopen()
        val third = WorkoutModel(gymName = "Xtreme CSC")
        reopened.create(third)
        assertEquals(3L, third.id)   // not 1 again
    }

    @Test
    fun searchWorksOnLoadedWorkouts() {
        WorkoutJSONStore(file).create(WorkoutModel(gymName = "Xtreme CSC", exercises = listOf(bench)))

        val reopened = reopen()
        assertEquals(1, reopened.search("xtreme", null).size)
        assertEquals(listOf("Bench Press"), reopened.exerciseNames())
    }

    @Test
    fun damagedFileStartsEmptyInsteadOfCrashing() {
        file.writeText("this is not json {{{")
        assertTrue(WorkoutJSONStore(file).findAll().isEmpty())
    }

    @Test
    fun invalidWorkoutIsNotSaved() {
        val store = WorkoutJSONStore(file)
        try {
            store.create(WorkoutModel(gymName = ""))
        } catch (_: IllegalArgumentException) {
        }
        assertFalse(file.exists())   // nothing was written
    }

    @Test
    fun fileIsReadableJson() {
        WorkoutJSONStore(file).create(WorkoutModel(gymName = "Xtreme CSC", date = LocalDate.of(2026, 11, 16)))

        val text = file.readText()
        assertTrue(text.contains("\"gymName\": \"Xtreme CSC\""))
        assertTrue(text.contains("\"date\": \"2026-11-16\""))   // the date is saved as plain text
    }
}
