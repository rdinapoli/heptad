package com.heptad.app.domain

import kotlin.random.Random

/**
 * Stateless utility for puzzle generation logic shared between
 * PuzzleGenerator (freeplay) and DailyPuzzleRepository (daily fallback).
 */
object PuzzleGenerationHelper {

    private val RARE_LETTERS = setOf('q', 'x', 'z', 'j', 'k')
    private const val MAX_RARE_LETTERS = 1

    /**
     * Select 7 unique random letters, avoiding bad combinations.
     * Uses the provided [random] for determinism.
     */
    fun selectLetters(includeS: Boolean, random: Random): List<Char> {
        val alphabet = if (includeS) {
            ('a'..'z').toList()
        } else {
            ('a'..'z').filter { it != 's' }
        }

        repeat(10) {
            val candidate = alphabet.shuffled(random).take(7)
            if (scoreLetterCombination(candidate) >= 0) {
                return candidate
            }
        }

        return alphabet.shuffled(random).take(7)
    }

    /**
     * Score a letter combination for playability.
     * Returns negative score for bad combinations.
     */
    fun scoreLetterCombination(letters: List<Char>): Int {
        var score = 0

        val rareCount = letters.count { it in RARE_LETTERS }
        if (rareCount > MAX_RARE_LETTERS) {
            score -= (rareCount - MAX_RARE_LETTERS) * 50
        }

        if ('q' in letters && 'u' !in letters) {
            score -= 100
        }

        return score
    }

    /**
     * Try each letter as center and return the one with the most valid words.
     * Accepts a pre-loaded dictionary set to avoid DI dependencies.
     */
    fun findBestCenter(
        letters: List<Char>,
        dictionary: Set<String>
    ): Pair<Char, Set<String>>? {
        val letterSet = letters.toSet()
        var bestCenter: Char? = null
        var bestWords: Set<String>? = null
        var bestWordCount = 0

        for (potentialCenter in letters) {
            val validWords = dictionary.filter { word ->
                word.length >= 4 &&
                    potentialCenter in word &&
                    word.all { it in letterSet }
            }.toSet()

            if (validWords.size > bestWordCount) {
                bestWordCount = validWords.size
                bestCenter = potentialCenter
                bestWords = validWords
            }
        }

        return if (bestCenter != null && bestWords != null && bestWordCount > 0) {
            bestCenter to bestWords
        } else {
            null
        }
    }

    /**
     * Find all pangrams (words using all 7 letters).
     */
    fun findPangrams(validWords: Set<String>, allLetters: Set<Char>): Set<String> {
        return validWords.filter { word ->
            allLetters.all { letter -> letter in word }
        }.toSet()
    }

    /**
     * Calculate the maximum possible score for the puzzle.
     */
    fun calculateMaxScore(validWords: Set<String>, pangrams: Set<String>): Int {
        return validWords.sumOf { word ->
            val baseScore = if (word.length == 4) 1 else word.length
            val pangramBonus = if (word in pangrams) 7 else 0
            baseScore + pangramBonus
        }
    }
}
