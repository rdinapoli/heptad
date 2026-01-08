package com.heptad.app.domain

import com.heptad.app.data.models.Puzzle

/**
 * Two-letter prefix hint data
 */
data class TwoLetterHint(
    val prefix: String,
    val totalCount: Int,
    val foundCount: Int
) {
    val remainingCount: Int get() = totalCount - foundCount
    val isComplete: Boolean get() = foundCount >= totalCount
}

/**
 * Grid cell for the letter/length matrix
 */
data class GridCell(
    val letter: Char,
    val length: Int,
    val totalCount: Int,
    val foundCount: Int
) {
    val remainingCount: Int get() = totalCount - foundCount
    val isComplete: Boolean get() = foundCount >= totalCount
}

/**
 * Letter reveal hint - shows partial word
 */
data class LetterRevealHint(
    val partialWord: String,
    val fullWord: String,
    val fullLength: Int,
    val isPangram: Boolean
)

/**
 * Generates hint data from puzzle and game state
 */
class HintGenerator(private val puzzle: Puzzle) {

    /**
     * Generate two-letter prefix hints
     * Groups all valid words by their first two letters and shows counts
     */
    fun generateTwoLetterHints(foundWords: Set<String>): List<TwoLetterHint> {
        val foundLower = foundWords.map { it.lowercase() }.toSet()

        // Group all valid words by first two letters
        val allWordsByPrefix = puzzle.validWords.groupBy {
            it.take(2).uppercase()
        }

        // Group found words by first two letters
        val foundWordsByPrefix = foundLower.filter { it in puzzle.validWords }
            .groupBy { it.take(2).uppercase() }

        return allWordsByPrefix.map { (prefix, words) ->
            TwoLetterHint(
                prefix = prefix,
                totalCount = words.size,
                foundCount = foundWordsByPrefix[prefix]?.size ?: 0
            )
        }.sortedBy { it.prefix }
    }

    /**
     * Generate grid view hints
     * Matrix of starting letter × word length with counts
     */
    fun generateGridHints(foundWords: Set<String>): List<GridCell> {
        val foundLower = foundWords.map { it.lowercase() }.toSet()

        // Get all unique starting letters and lengths
        val startingLetters = puzzle.validWords.map { it.first().uppercaseChar() }.toSet().sorted()
        val lengths = puzzle.validWords.map { it.length }.toSet().sorted()

        val cells = mutableListOf<GridCell>()

        for (letter in startingLetters) {
            for (length in lengths) {
                val matchingWords = puzzle.validWords.filter {
                    it.first().uppercaseChar() == letter && it.length == length
                }
                val foundMatching = matchingWords.count { it in foundLower }

                if (matchingWords.isNotEmpty()) {
                    cells.add(GridCell(
                        letter = letter,
                        length = length,
                        totalCount = matchingWords.size,
                        foundCount = foundMatching
                    ))
                }
            }
        }

        return cells
    }

    /**
     * Get unique word lengths in the puzzle (for grid header)
     */
    fun getWordLengths(): List<Int> {
        return puzzle.validWords.map { it.length }.toSet().sorted()
    }

    /**
     * Get unique starting letters in the puzzle (for grid row headers)
     */
    fun getStartingLetters(): List<Char> {
        return puzzle.validWords.map { it.first().uppercaseChar() }.toSet().sorted()
    }

    /**
     * Generate letter reveal hints for unfound words
     * @param foundWords Words already found
     * @param revealCount Number of letters to reveal (1-3)
     */
    fun generateLetterRevealHints(
        foundWords: Set<String>,
        revealCount: Int = 2
    ): List<LetterRevealHint> {
        val foundLower = foundWords.map { it.lowercase() }.toSet()
        val unfoundWords = puzzle.validWords.filter { it !in foundLower }

        return unfoundWords.map { word ->
            val revealed = word.take(revealCount).uppercase()
            val hidden = "·".repeat(word.length - revealCount)
            LetterRevealHint(
                partialWord = revealed + hidden,
                fullWord = word,
                fullLength = word.length,
                isPangram = word in puzzle.pangrams
            )
        }.sortedWith(compareBy({ it.partialWord }, { it.fullLength }))
    }

    /**
     * Get a random unfound word for definition hint
     */
    fun getRandomUnfoundWord(foundWords: Set<String>, excludeWords: Set<String> = emptySet()): String? {
        val foundLower = foundWords.map { it.lowercase() }.toSet()
        val excludeLower = excludeWords.map { it.lowercase() }.toSet()
        val candidates = puzzle.validWords.filter {
            it !in foundLower && it !in excludeLower
        }
        return candidates.randomOrNull()
    }

    /**
     * Get count of remaining words
     */
    fun getRemainingWordCount(foundWords: Set<String>): Int {
        val foundLower = foundWords.map { it.lowercase() }.toSet()
        return puzzle.validWords.count { it !in foundLower }
    }

    /**
     * Get progress percentage (0.0 to 1.0)
     */
    fun getProgressPercentage(foundWords: Set<String>): Float {
        if (puzzle.validWords.isEmpty()) return 0f
        return foundWords.size.toFloat() / puzzle.validWords.size.toFloat()
    }
}
