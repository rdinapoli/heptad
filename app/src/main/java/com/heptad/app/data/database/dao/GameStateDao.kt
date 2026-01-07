package com.heptad.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.heptad.app.data.database.entity.GameStateEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for GameState operations
 */
@Dao
interface GameStateDao {

    /**
     * Insert or replace a game state
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gameState: GameStateEntity)

    /**
     * Update an existing game state
     */
    @Update
    suspend fun update(gameState: GameStateEntity)

    /**
     * Get game state for a puzzle
     */
    @Query("SELECT * FROM game_states WHERE puzzleId = :puzzleId")
    suspend fun getByPuzzleId(puzzleId: String): GameStateEntity?

    /**
     * Observe game state for a puzzle
     */
    @Query("SELECT * FROM game_states WHERE puzzleId = :puzzleId")
    fun observeByPuzzleId(puzzleId: String): Flow<GameStateEntity?>

    /**
     * Delete game state for a puzzle
     */
    @Query("DELETE FROM game_states WHERE puzzleId = :puzzleId")
    suspend fun deleteByPuzzleId(puzzleId: String)

    /**
     * Delete all game states
     */
    @Query("DELETE FROM game_states")
    suspend fun deleteAll()

    /**
     * Get the most recently played game state
     */
    @Query("SELECT * FROM game_states ORDER BY lastPlayedAt DESC LIMIT 1")
    suspend fun getMostRecent(): GameStateEntity?
}
