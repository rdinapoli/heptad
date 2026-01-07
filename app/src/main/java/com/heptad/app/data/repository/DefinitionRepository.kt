package com.heptad.app.data.repository

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API response models for Free Dictionary API
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
 * Repository for fetching word definitions from Free Dictionary API
 */
@Singleton
class DefinitionRepository @Inject constructor() {

    private val gson = Gson()
    private val baseUrl = "https://api.dictionaryapi.dev/api/v2/entries/en"

    // Simple in-memory cache
    private val cache = mutableMapOf<String, WordDefinition>()

    /**
     * Fetch definition for a word
     */
    suspend fun getDefinition(word: String): DefinitionResult = withContext(Dispatchers.IO) {
        val normalizedWord = word.lowercase().trim()

        // Check cache first
        cache[normalizedWord]?.let {
            return@withContext DefinitionResult.Success(it)
        }

        try {
            val url = URL("$baseUrl/$normalizedWord")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val apiResponses = gson.fromJson(response, Array<DictionaryApiResponse>::class.java)

                if (apiResponses.isNotEmpty()) {
                    val definition = parseDefinition(apiResponses.first())
                    cache[normalizedWord] = definition
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
            DefinitionResult.Error(e.message ?: "Network error")
        }
    }

    private fun parseDefinition(response: DictionaryApiResponse): WordDefinition {
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
            meanings = meanings.take(3) // Limit to 3 meanings
        )
    }

    /**
     * Clear the definition cache
     */
    fun clearCache() {
        cache.clear()
    }
}
