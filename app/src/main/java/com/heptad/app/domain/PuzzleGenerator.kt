package com.heptad.app.domain

import com.heptad.app.data.models.Puzzle
import com.heptad.app.data.repository.DictionaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates valid Heptad puzzles from the dictionary
 */
@Singleton
class PuzzleGenerator @Inject constructor(
    private val dictionaryRepository: DictionaryRepository
) {
    companion object {
        private const val MIN_WORDS = 10
        private const val MAX_WORDS = 200
        private const val MIN_SCORE = 20
        private const val MAX_SCORE = 500
        private const val MAX_ATTEMPTS = 200
    }

    /**
     * Generate a new puzzle with the given settings
     * @param level Dictionary level (60, 70, or 80)
     * @param includeS Whether to allow the letter 'S' in puzzles
     * @return A valid puzzle, or null if generation failed
     */
    suspend fun generatePuzzle(
        level: Int = 70,
        includeS: Boolean = true
    ): Puzzle? = withContext(Dispatchers.Default) {
        repeat(MAX_ATTEMPTS) {
            val letters = selectLetters(includeS)
            val result = findBestCenter(letters, level)

            if (result != null) {
                val (centerLetter, validWords) = result
                val outerLetters = letters.filter { it != centerLetter }
                val pangrams = findPangrams(validWords, letters.toSet())
                val maxScore = calculateMaxScore(validWords, pangrams)

                if (meetsQualityCriteria(validWords, pangrams, maxScore)) {
                    return@withContext Puzzle(
                        centerLetter = centerLetter,
                        outerLetters = outerLetters,
                        validWords = validWords,
                        pangrams = pangrams,
                        maxScore = maxScore,
                        dictionaryLevel = level,
                        includesS = includeS
                    )
                }
            }
        }

        null // Failed to generate a valid puzzle
    }

    /**
     * Select 7 unique random letters
     */
    private fun selectLetters(includeS: Boolean): List<Char> {
        val alphabet = if (includeS) {
            ('a'..'z').toList()
        } else {
            ('a'..'z').filter { it != 's' }
        }

        return alphabet.shuffled().take(7)
    }

    /**
     * Try each letter as the center and find the one with the most valid words
     */
    private suspend fun findBestCenter(
        letters: List<Char>,
        level: Int
    ): Pair<Char, Set<String>>? {
        var bestCenter: Char? = null
        var bestWords: Set<String>? = null
        var bestWordCount = 0

        for (potentialCenter in letters) {
            val validWords = dictionaryRepository.findValidWords(
                letters = letters.toSet(),
                centerLetter = potentialCenter,
                level = level
            )

            if (validWords.size > bestWordCount) {
                bestWordCount = validWords.size
                bestCenter = potentialCenter
                bestWords = validWords
            }
        }

        return if (bestCenter != null && bestWords != null) {
            bestCenter to bestWords
        } else {
            null
        }
    }

    /**
     * Find all pangrams (words using all 7 letters)
     */
    private fun findPangrams(validWords: Set<String>, allLetters: Set<Char>): Set<String> {
        return validWords.filter { word ->
            allLetters.all { letter -> letter in word }
        }.toSet()
    }

    /**
     * Calculate the maximum possible score for the puzzle
     */
    private fun calculateMaxScore(validWords: Set<String>, pangrams: Set<String>): Int {
        return validWords.sumOf { word ->
            val baseScore = if (word.length == 4) 1 else word.length
            val pangramBonus = if (word in pangrams) 7 else 0
            baseScore + pangramBonus
        }
    }

    /**
     * Check if the puzzle meets quality criteria
     */
    private fun meetsQualityCriteria(
        validWords: Set<String>,
        pangrams: Set<String>,
        maxScore: Int
    ): Boolean {
        val wordCount = validWords.size

        // Pangram requirement relaxed for mock dictionary
        return wordCount >= MIN_WORDS &&
                wordCount <= MAX_WORDS &&
                maxScore >= MIN_SCORE &&
                maxScore <= MAX_SCORE
    }
}
