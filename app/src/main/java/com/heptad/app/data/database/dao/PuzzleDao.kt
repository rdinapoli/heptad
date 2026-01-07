package com.heptad.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.heptad.app.data.database.entity.PuzzleEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Puzzle operations
 */
@Dao
interface PuzzleDao {

    /**
     * Insert or replace a puzzle
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(puzzle: PuzzleEntity)

    /**
     * Get a puzzle by ID
     */
    @Query("SELECT * FROM puzzles WHERE id = :puzzleId")
    suspend fun getById(puzzleId: String): PuzzleEntity?

    /**
     * Get the current active puzzle
     */
    @Query("SELECT * FROM puzzles WHERE isCurrent = 1 LIMIT 1")
    suspend fun getCurrentPuzzle(): PuzzleEntity?

    /**
     * Observe the current active puzzle
     */
    @Query("SELECT * FROM puzzles WHERE isCurrent = 1 LIMIT 1")
    fun observeCurrentPuzzle(): Flow<PuzzleEntity?>

    /**
     * Set a puzzle as current (and unset all others)
     */
    @Query("UPDATE puzzles SET isCurrent = (id = :puzzleId)")
    suspend fun setCurrentPuzzle(puzzleId: String)

    /**
     * Clear the current puzzle flag from all puzzles
     */
    @Query("UPDATE puzzles SET isCurrent = 0")
    suspend fun clearCurrentPuzzle()

    /**
     * Delete a puzzle by ID
     */
    @Query("DELETE FROM puzzles WHERE id = :puzzleId")
    suspend fun deleteById(puzzleId: String)

    /**
     * Delete all puzzles
     */
    @Query("DELETE FROM puzzles")
    suspend fun deleteAll()

    /**
     * Get count of all puzzles
     */
    @Query("SELECT COUNT(*) FROM puzzles")
    suspend fun getCount(): Int
}
