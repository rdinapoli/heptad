package com.heptad.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local definition entry from bundled dictionary
 */
data class LocalDefinitionEntry(
    val pos: String,
    val def: String
)

/**
 * API response models for Free Dictionary API (fallback)
 */
data class DictionaryApiResponse(
    val word: String,
    val phonetic: String? = null,
    val phonetics: List<Phonetic>? = null,
    val meanings: List<Meaning>? = null
)

data class Phonetic(
    val text: String? = null,
    val audio: String? = null
)

data class Meaning(
    val partOfSpeech: String,
    val definitions: List<DefinitionEntry>
)

data class DefinitionEntry(
    val definition: String,
    val example: String? = null,
    val synonyms: List<String>? = null
)

/**
 * Simplified definition for UI display
 */
data class WordDefinition(
    val word: String,
    val phonetic: String?,
    val meanings: List<MeaningDisplay>
)

data class MeaningDisplay(
    val partOfSpeech: String,
    val definitions: List<String>,
    val example: String?
)

sealed class DefinitionResult {
    data class Success(val definition: WordDefinition) : DefinitionResult()
    data class Error(val message: String) : DefinitionResult()
    object Loading : DefinitionResult()
    object NotFound : DefinitionResult()
}

/**
 * Repository for fetching word definitions.
 * Uses bundled offline dictionary first, falls back to Free Dictionary API.
 */
@Singleton
class DefinitionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    private val apiBaseUrl = "https://api.dictionaryapi.dev/api/v2/entries/en"

    // Local definitions loaded from bundled file
    private var localDefinitions: Map<String, List<LocalDefinitionEntry>>? = null
    private var localLoaded = false

    // In-memory cache for API results
    private val apiCache = mutableMapOf<String, WordDefinition>()

    /**
     * Load local definitions from bundled gzipped JSON file
     */
    private suspend fun ensureLocalDefinitionsLoaded() = withContext(Dispatchers.IO) {
        if (localLoaded) return@withContext

        try {
            // Load definitions.gz from raw resources
            val resourceId = context.resources.getIdentifier(
                "definitions",  // Android strips .gz extension
                "raw",
                context.packageName
            )

            if (resourceId != 0) {
                context.resources.openRawResource(resourceId).use { inputStream ->
                    GZIPInputStream(inputStream).use { gzipStream ->
                        InputStreamReader(gzipStream, Charsets.UTF_8).use { reader ->
                            val type = object : TypeToken<Map<String, List<LocalDefinitionEntry>>>() {}.type
                            localDefinitions = gson.fromJson(reader, type)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // If loading fails, we'll just use the API
            localDefinitions = emptyMap()
        }

        localLoaded = true
    }

    /**
     * Get definition from local dictionary
     */
    private fun getLocalDefinition(word: String): WordDefinition? {
        val entries = localDefinitions?.get(word.lowercase()) ?: return null

        if (entries.isEmpty()) return null

        val meanings = entries.map { entry ->
            MeaningDisplay(
                partOfSpeech = entry.pos,
                definitions = listOf(entry.def),
                example = null  // Local dictionary doesn't include examples
            )
        }

        return WordDefinition(
            word = word,
            phonetic = null,  // Local dictionary doesn't include phonetics
            meanings = meanings
        )
    }

    /**
     * Fetch definition for a word.
     * Tries local dictionary first, then falls back to API.
     */
    suspend fun getDefinition(word: String): DefinitionResult = withContext(Dispatchers.IO) {
        val normalizedWord = word.lowercase().trim()

        // Ensure local definitions are loaded
        ensureLocalDefinitionsLoaded()

        // Try local dictionary first
        getLocalDefinition(normalizedWord)?.let {
            return@withContext DefinitionResult.Success(it)
        }

        // Check API cache
        apiCache[normalizedWord]?.let {
            return@withContext DefinitionResult.Success(it)
        }

        // Fall back to API
        try {
            val url = URL("$apiBaseUrl/$normalizedWord")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val apiResponses = gson.fromJson(response, Array<DictionaryApiResponse>::class.java)

                if (apiResponses.isNotEmpty()) {
                    val definition = parseApiDefinition(apiResponses.first())
                    apiCache[normalizedWord] = definition
                    DefinitionResult.Success(definition)
                } else {
                    DefinitionResult.NotFound
                }
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                DefinitionResult.NotFound
            } else {
                DefinitionResult.Error("Failed to fetch definition (code: $responseCode)")
            }
        } catch (e: Exception) {
            // If API fails and we don't have local definition, return NotFound
            DefinitionResult.NotFound
        }
    }

    private fun parseApiDefinition(response: DictionaryApiResponse): WordDefinition {
        val meanings = response.meanings?.map { meaning ->
            MeaningDisplay(
                partOfSpeech = meaning.partOfSpeech,
                definitions = meaning.definitions.take(2).map { it.definition },
                example = meaning.definitions.firstOrNull { it.example != null }?.example
            )
        } ?: emptyList()

        return WordDefinition(
            word = response.word,
            phonetic = response.phonetic ?: response.phonetics?.firstOrNull { it.text != null }?.text,
            meanings = meanings.take(3)
        )
    }

    /**
     * Clear caches
     */
    fun clearCache() {
        apiCache.clear()
    }

    /**
     * Check if local definitions are loaded
     */
    fun isLocalDictionaryLoaded(): Boolean = localLoaded && !localDefinitions.isNullOrEmpty()

    /**
     * Get count of local definitions
     */
    fun getLocalDefinitionCount(): Int = localDefinitions?.size ?: 0
}
