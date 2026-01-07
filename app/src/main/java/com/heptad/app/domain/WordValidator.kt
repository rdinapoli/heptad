package com.heptad.app.domain

import com.heptad.app.data.models.Puzzle

/**
 * Result of word validation
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    object TooShort : ValidationResult()
    object MissingCenter : ValidationResult()
    object InvalidLetters : ValidationResult()
    object NotInWordList : ValidationResult()
    object AlreadyFound : ValidationResult()

    /**
     * Convert validation result to user-friendly message
     */
    fun toMessage(): String {
        return when (this) {
            is Valid -> "Valid word"
            is TooShort -> "Too short - words must be 4+ letters"
            is MissingCenter -> "Missing center letter"
            is InvalidLetters -> "Uses letters not in the puzzle"
            is NotInWordList -> "Not in word list"
            is AlreadyFound -> "Already found"
        }
    }
}

/**
 * Validates words against puzzle rules
 */
class WordValidator(private val puzzle: Puzzle) {

    private val allLetters: Set<Char> = puzzle.allLetters

    /**
     * Validate a word against the puzzle rules and found words
     * @param word The word to validate
     * @param foundWords Set of words already found
     * @return The validation result
     */
    fun validate(word: String, foundWords: Set<String>): ValidationResult {
        val normalizedWord = word.lowercase().trim()

        return when {
            normalizedWord.length < 4 ->
                ValidationResult.TooShort

            puzzle.centerLetter !in normalizedWord ->
                ValidationResult.MissingCenter

            normalizedWord.any { it !in allLetters } ->
                ValidationResult.InvalidLetters

            normalizedWord !in puzzle.validWords ->
                ValidationResult.NotInWordList

            normalizedWord in foundWords ->
                ValidationResult.AlreadyFound

            else ->
                ValidationResult.Valid
        }
    }

    /**
     * Calculate the score for a word
     * @param word The word to score
     * @return The score for the word (0 if invalid)
     */
    fun calculateScore(word: String): Int {
        val normalizedWord = word.lowercase().trim()
        if (normalizedWord !in puzzle.validWords) return 0

        val baseScore = if (normalizedWord.length == 4) 1 else normalizedWord.length
        val pangramBonus = if (normalizedWord in puzzle.pangrams) 7 else 0
        return baseScore + pangramBonus
    }

    /**
     * Check if a word is a pangram
     */
    fun isPangram(word: String): Boolean {
        return word.lowercase().trim() in puzzle.pangrams
    }

    /**
     * Get a message for a successful word submission
     */
    fun getSuccessMessage(word: String): String {
        val score = calculateScore(word)
        return if (isPangram(word)) {
            "PANGRAM! +$score points"
        } else {
            "+$score points"
        }
    }
}
