package com.heptad.app.data.models

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PuzzleTest {

    private lateinit var puzzle: Puzzle

    @Before
    fun setup() {
        puzzle = Puzzle(
            id = "test-puzzle",
            centerLetter = 'a',
            outerLetters = listOf('b', 'c', 'd', 'e', 'f', 'g'),
            validWords = setOf("bade", "badge", "cabbage", "face", "fade", "deaf", "cafe"),
            pangrams = setOf("cabbage"),
            maxScore = 50
        )
    }

    @Test
    fun `allLetters includes center and outer letters`() {
        val expected = setOf('a', 'b', 'c', 'd', 'e', 'f', 'g')
        assertEquals(expected, puzzle.allLetters)
    }

    @Test
    fun `wordCount returns correct number`() {
        assertEquals(7, puzzle.wordCount)
    }

    @Test
    fun `isPangram returns true for pangram`() {
        assertTrue(puzzle.isPangram("cabbage"))
    }

    @Test
    fun `isPangram returns false for non-pangram`() {
        assertFalse(puzzle.isPangram("bade"))
    }

    @Test
    fun `isValidWord returns true for valid word`() {
        assertTrue(puzzle.isValidWord("bade"))
    }

    @Test
    fun `isValidWord returns false for invalid word`() {
        assertFalse(puzzle.isValidWord("invalid"))
    }

    @Test
    fun `calculateWordScore for 4-letter word returns 1`() {
        assertEquals(1, puzzle.calculateWordScore("bade"))
    }

    @Test
    fun `calculateWordScore for 5-letter word returns 5`() {
        assertEquals(5, puzzle.calculateWordScore("badge"))
    }

    @Test
    fun `calculateWordScore for pangram includes bonus`() {
        // cabbage = 7 letters + 7 bonus = 14
        assertEquals(14, puzzle.calculateWordScore("cabbage"))
    }

    @Test
    fun `calculateWordScore for invalid word returns 0`() {
        assertEquals(0, puzzle.calculateWordScore("invalid"))
    }

    @Test
    fun `empty puzzle has zero word count`() {
        val emptyPuzzle = Puzzle.empty()
        assertEquals(0, emptyPuzzle.wordCount)
    }
}
