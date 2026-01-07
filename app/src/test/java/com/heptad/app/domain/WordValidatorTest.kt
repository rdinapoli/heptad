package com.heptad.app.domain

import com.heptad.app.data.models.Puzzle
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class WordValidatorTest {

    private lateinit var puzzle: Puzzle
    private lateinit var validator: WordValidator

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
        validator = WordValidator(puzzle)
    }

    @Test
    fun `valid word returns Valid`() {
        val result = validator.validate("bade", emptySet())
        assertEquals(ValidationResult.Valid, result)
    }

    @Test
    fun `word without center letter returns MissingCenter`() {
        val result = validator.validate("beef", emptySet())
        assertEquals(ValidationResult.MissingCenter, result)
    }

    @Test
    fun `word too short returns TooShort`() {
        val result = validator.validate("bad", emptySet())
        assertEquals(ValidationResult.TooShort, result)
    }

    @Test
    fun `word with invalid letters returns InvalidLetters`() {
        val result = validator.validate("azure", emptySet())
        assertEquals(ValidationResult.InvalidLetters, result)
    }

    @Test
    fun `word not in dictionary returns NotInWordList`() {
        val result = validator.validate("abcd", emptySet())
        assertEquals(ValidationResult.NotInWordList, result)
    }

    @Test
    fun `already found word returns AlreadyFound`() {
        val result = validator.validate("bade", setOf("bade"))
        assertEquals(ValidationResult.AlreadyFound, result)
    }

    @Test
    fun `4-letter word scores 1 point`() {
        val score = validator.calculateScore("bade")
        assertEquals(1, score)
    }

    @Test
    fun `5-letter word scores 5 points`() {
        val score = validator.calculateScore("badge")
        assertEquals(5, score)
    }

    @Test
    fun `pangram scores word length plus 7 bonus`() {
        val score = validator.calculateScore("cabbage")
        // 7 letters + 7 bonus = 14
        assertEquals(14, score)
    }

    @Test
    fun `invalid word scores 0`() {
        val score = validator.calculateScore("invalid")
        assertEquals(0, score)
    }

    @Test
    fun `isPangram returns true for pangram`() {
        assertTrue(validator.isPangram("cabbage"))
    }

    @Test
    fun `isPangram returns false for non-pangram`() {
        assertFalse(validator.isPangram("bade"))
    }

    @Test
    fun `validation is case insensitive`() {
        val result = validator.validate("BADE", emptySet())
        assertEquals(ValidationResult.Valid, result)
    }

    @Test
    fun `validation trims whitespace`() {
        val result = validator.validate("  bade  ", emptySet())
        assertEquals(ValidationResult.Valid, result)
    }
}
