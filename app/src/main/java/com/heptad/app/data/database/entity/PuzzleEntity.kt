package com.heptad.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.heptad.app.data.models.Puzzle

/**
 * Room entity for storing puzzles
 */
@Entity(tableName = "puzzles")
data class PuzzleEntity(
    @PrimaryKey
    val id: String,
    val centerLetter: Char,
    val outerLetters: String, // Stored as comma-separated: "a,b,c,d,e,f"
    val validWords: String,   // Stored as comma-separated words
    val pangrams: String,     // Stored as comma-separated words
    val maxScore: Int,
    val createdAt: Long,
    val dictionaryLevel: Int,
    val includesS: Boolean,
    val isCurrent: Boolean = false // Track which puzzle is currently active
) {
    /**
     * Convert entity to domain model
     */
    fun toPuzzle(): Puzzle {
        return Puzzle(
            id = id,
            centerLetter = centerLetter,
            outerLetters = outerLetters.split(",").map { it.first() },
            validWords = validWords.split(",").filter { it.isNotBlank() }.toSet(),
            pangrams = pangrams.split(",").filter { it.isNotBlank() }.toSet(),
            maxScore = maxScore,
            createdAt = createdAt,
            dictionaryLevel = dictionaryLevel,
            includesS = includesS
        )
    }

    companion object {
        /**
         * Convert domain model to entity
         */
        fun fromPuzzle(puzzle: Puzzle, isCurrent: Boolean = false): PuzzleEntity {
            return PuzzleEntity(
                id = puzzle.id,
                centerLetter = puzzle.centerLetter,
                outerLetters = puzzle.outerLetters.joinToString(","),
                validWords = puzzle.validWords.joinToString(","),
                pangrams = puzzle.pangrams.joinToString(","),
                maxScore = puzzle.maxScore,
                createdAt = puzzle.createdAt,
                dictionaryLevel = puzzle.dictionaryLevel,
                includesS = puzzle.includesS,
                isCurrent = isCurrent
            )
        }
    }
}
