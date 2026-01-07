package com.heptad.app.data.repository

import android.content.Context
import com.heptad.app.R
import com.heptad.app.data.models.Definition
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for loading and managing the word dictionary
 */
@Singleton
class DictionaryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Cache for loaded dictionaries by level
    private val dictionaryCache = mutableMapOf<Int, Set<String>>()

    // Cache for definitions (to be populated when definitions are added)
    private var definitionsCache: Map<String, Definition>? = null

    /**
     * Load the dictionary for the given level
     * Currently only mock dictionary is available (level 70)
     */
    suspend fun loadDictionary(level: Int = 70): Set<String> = withContext(Dispatchers.IO) {
        // Check cache first
        dictionaryCache[level]?.let { return@withContext it }

        // Load SCOWL dictionary for the specified level
        val resourceId = when (level) {
            60 -> R.raw.scowl_60
            80 -> R.raw.scowl_80
            else -> R.raw.scowl_70  // Default to level 70
        }

        val words = context.resources.openRawResource(resourceId)
            .bufferedReader()
            .useLines { lines ->
                lines
                    .map { it.trim().lowercase() }
                    .filter { it.isNotBlank() && it.length >= 4 && it.all { c -> c.isLetter() } }
                    .toSet()
            }

        // Cache the result
        dictionaryCache[level] = words
        words
    }

    /**
     * Check if a word exists in the dictionary
     */
    suspend fun isValidWord(word: String, level: Int = 70): Boolean {
        val dictionary = loadDictionary(level)
        return word.lowercase() in dictionary
    }

    /**
     * Get all words that can be formed with the given letters
     * @param letters All available letters
     * @param centerLetter The mandatory center letter
     * @param level Dictionary level to use
     */
    suspend fun findValidWords(
        letters: Set<Char>,
        centerLetter: Char,
        level: Int = 70
    ): Set<String> = withContext(Dispatchers.Default) {
        val dictionary = loadDictionary(level)
        val lowercaseLetters = letters.map { it.lowercaseChar() }.toSet()
        val lowercaseCenter = centerLetter.lowercaseChar()

        dictionary.filter { word ->
            // Word must contain the center letter
            lowercaseCenter in word &&
            // All letters in the word must be from available letters
            word.all { it in lowercaseLetters }
        }.toSet()
    }

    /**
     * Get definition for a word (placeholder - returns unavailable for now)
     */
    suspend fun getDefinition(word: String): Definition {
        definitionsCache?.get(word.lowercase())?.let { return it }
        return Definition.unavailable(word)
    }

    /**
     * Clear the dictionary cache (useful for testing or when changing settings)
     */
    fun clearCache() {
        dictionaryCache.clear()
        definitionsCache = null
    }

    /**
     * Get the number of words in the dictionary
     */
    suspend fun getWordCount(level: Int = 70): Int {
        return loadDictionary(level).size
    }
}
