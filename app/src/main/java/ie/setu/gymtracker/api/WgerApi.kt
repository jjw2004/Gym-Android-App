package ie.setu.gymtracker.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Downloads exercise names from the free wger.de API (no key needed). */
object WgerApi {

    // web address of the exercise list (up to 1000 per page)
    private const val EXERCISES_URL =
        "https://wger.de/api/v2/exercise-translation/?language=2&limit=1000"

    // wger's language number for English
    private const val ENGLISH = 2

    // remembered after the first download, so we only download once
    private var cachedNames: List<String>? = null

    // Gets the English exercise names: gives back the saved copy if we have one,
    // otherwise downloads them. Returns an empty list if anything goes wrong
    suspend fun exerciseNames(): List<String> {
        // already downloaded? give back the saved copy
        cachedNames?.let { return it }

        // network work must happen on a background thread
        return withContext(Dispatchers.IO) {
            try {
                val names = ArrayList<String>()
                var nextUrl: String? = EXERCISES_URL

                // keep downloading pages until there's no "next" page
                while (nextUrl != null) {
                    val json = JSONObject(download(nextUrl))
                    val results = json.getJSONArray("results")

                    for (i in 0 until results.length()) {
                        val exercise = results.getJSONObject(i)
                        // the API ignores ?language=2, so keep only English here
                        if (exercise.getInt("language") == ENGLISH) {
                            names.add(exercise.getString("name").trim())
                        }
                    }

                    nextUrl = if (json.isNull("next")) null else json.getString("next")
                }

                val sorted = names.distinctBy { it.lowercase() }.sorted()
                cachedNames = sorted
                sorted
            } catch (e: Exception) {
                // no internet, server down, bad data... just suggest nothing from wger
                emptyList()
            }
        }
    }

    /** Fetches a web address and returns what it sends back, as text. */
    private fun download(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000   // give up after 10 seconds
        connection.readTimeout = 10_000
        try {
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}