package ie.setu.gymtracker.models

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.io.File
import java.time.LocalDate

// A WorkoutMemStore that also saves every workout to a JSON file, so they're still there
// next time the app opens. All the finding / searching code is reused from WorkoutMemStore -
// this class only adds loading (when it's created) and saving (after every change)
class WorkoutJSONStore(private val file: File) : WorkoutMemStore() {

    // Gson turns objects into JSON text and back.
    // It doesn't know how to store a LocalDate, so we tell it: save it as text like "2026-11-16"
    private val gson = GsonBuilder()
        .setPrettyPrinting()   // easy-to-read JSON with line breaks
        .registerTypeAdapter(LocalDate::class.java, JsonSerializer<LocalDate> { date, _, _ ->
            JsonPrimitive(date.toString())
        })
        .registerTypeAdapter(LocalDate::class.java, JsonDeserializer { json, _, _ ->
            LocalDate.parse(json.asString)
        })
        .create()

    // runs when the store is created - read whatever was saved last time
    init {
        load()
    }

    // same as WorkoutMemStore, then save the file
    override fun create(workout: WorkoutModel) {
        super.create(workout)   // "super" = the WorkoutMemStore version
        save()
    }

    override fun update(workout: WorkoutModel): Boolean {
        val updated = super.update(workout)
        if (updated) save()     // only save if something actually changed
        return updated
    }

    override fun delete(id: Long): Boolean {
        val removed = super.delete(id)
        if (removed) save()
        return removed
    }

    // Reads the workouts from the JSON file into the list
    private fun load() {
        if (!file.exists()) {
            Timber.i("No ${file.name} yet - starting with no workouts")
            return
        }

        // tells Gson the file holds a list of WorkoutModel
        val type = object : TypeToken<ArrayList<WorkoutModel>>() {}.type

        // a damaged file shouldn't stop the app from starting
        val loaded: ArrayList<WorkoutModel> = try {
            gson.fromJson(file.readText(), type) ?: arrayListOf()
        } catch (e: Exception) {
            Timber.e(e, "Could not read ${file.name}, starting with no workouts")
            arrayListOf()
        }

        workouts.clear()
        workouts.addAll(loaded)

        // carry on numbering after the biggest id, so new workouts don't reuse an old id
        lastId.set(workouts.maxOfOrNull { it.id } ?: 0L)

        Timber.i("Loaded ${workouts.size} workouts from ${file.name}")
    }

    // Writes every workout to the JSON file (replacing what was there)
    private fun save() {
        file.parentFile?.mkdirs()
        file.writeText(gson.toJson(workouts))
        Timber.d("Saved ${workouts.size} workouts to ${file.name}")
    }
}
