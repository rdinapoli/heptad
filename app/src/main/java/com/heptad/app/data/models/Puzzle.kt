package com.heptad.app.data.models

import java.util.UUID

/**
 * Represents a Heptad puzzle configuration.
 *
 * @property id Unique identifier for the puzzle
 * @property centerLetter The mandatory center letter (must be included in all valid words)
 * @property outerLetters The 6 outer/orbital letters
 * @property validWords All valid words that can be formed with this puzzle
 * @property pangrams Words that use all 7 letters at least once
 * @property maxScore Maximum achievable score for this puzzle
 * @property createdAt Timestamp when the puzzle was created
 * @property dictionaryLevel The SCOWL dictionary level used (60, 70, or 80)
 * @property includesS Whether the letter 'S' was allowed during generation
 */
data class Puzzle(
    val id: String = UUID.randomUUID().toString(),
    val centerLetter: Char,
    val outerLetters: List<Char>,
    val validWords: Set<String>,
    val pangrams: Set<String>,
    val maxScore: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val dictionaryLevel: Int = 70,
    val includesS: Boolean = true
) {
    /**
     * All 7 letters in the puzzle (center + outer)
     */
    val allLetters: Set<Char>
        get() = (outerLetters + centerLetter).toSet()

    /**
     * Total number of valid words
     */
    val wordCount: Int
        get() = validWords.size

    /**
     * Check if a word is a pangram
     */
    fun isPangram(word: String): Boolean = word.lowercase() in pangrams

    /**
     * Check if a word is valid for this puzzle
     */
    fun isValidWord(word: String): Boolean = word.lowercase() in validWords

    /**
     * Calculate score for a word
     * - 4-letter words: 1 point
     * - 5+ letter words: 1 point per letter
     * - Pangram bonus: +7 points
     */
    fun calculateWordScore(word: String): Int {
        val normalizedWord = word.lowercase()
        if (normalizedWord !in validWords) return 0

        val baseScore = if (normalizedWord.length == 4) 1 else normalizedWord.length
        val pangramBonus = if (normalizedWord in pangrams) 7 else 0
        return baseScore + pangramBonus
    }

    companion object {
        /**
         * Create an empty/placeholder puzzle
         */
        fun empty(): Puzzle = Puzzle(
            id = "empty",
            centerLetter = 'A',
            outerLetters = listOf('B', 'C', 'D', 'E', 'F', 'G'),
            validWords = emptySet(),
            pangrams = emptySet(),
            maxScore = 0
        )
    }
}
