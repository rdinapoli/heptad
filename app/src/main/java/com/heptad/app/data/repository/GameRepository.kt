package com.heptad.app.data.repository

import com.heptad.app.data.database.dao.GameStateDao
import com.heptad.app.data.database.dao.PuzzleDao
import com.heptad.app.data.database.entity.GameStateEntity
import com.heptad.app.data.database.entity.PuzzleEntity
import com.heptad.app.data.models.GameState
import com.heptad.app.data.models.Puzzle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing game data (puzzles and game states)
 */
@Singleton
class GameRepository @Inject constructor(
    private val puzzleDao: PuzzleDao,
    private val gameStateDao: GameStateDao
) {
    /**
     * Data class combining puzzle and its game state
     */
    data class SavedGame(
        val puzzle: Puzzle,
        val gameState: GameState
    )

    /**
     * Load the current game (puzzle + state) if one exists
     */
    suspend fun loadCurrentGame(): SavedGame? = withContext(Dispatchers.IO) {
        val puzzleEntity = puzzleDao.getCurrentPuzzle() ?: return@withContext null
        val gameStateEntity = gameStateDao.getByPuzzleId(puzzleEntity.id) ?: return@withContext null

        SavedGame(
            puzzle = puzzleEntity.toPuzzle(),
            gameState = gameStateEntity.toGameState()
        )
    }

    /**
     * Save a new puzzle as the current game
     */
    suspend fun saveNewGame(puzzle: Puzzle, gameState: GameState) = withContext(Dispatchers.IO) {
        // Clear current puzzle flag from all puzzles
        puzzleDao.clearCurrentPuzzle()

        // Save the new puzzle as current
        puzzleDao.insert(PuzzleEntity.fromPuzzle(puzzle, isCurrent = true))

        // Save the initial game state
        gameStateDao.insert(GameStateEntity.fromGameState(gameState))
    }

    /**
     * Update the game state for the current puzzle
     */
    suspend fun updateGameState(gameState: GameState) = withContext(Dispatchers.IO) {
        gameStateDao.insert(GameStateEntity.fromGameState(gameState))
    }

    /**
     * Check if there's a saved game
     */
    suspend fun hasSavedGame(): Boolean = withContext(Dispatchers.IO) {
        puzzleDao.getCurrentPuzzle() != null
    }

    /**
     * Delete the current game
     */
    suspend fun deleteCurrentGame() = withContext(Dispatchers.IO) {
        val currentPuzzle = puzzleDao.getCurrentPuzzle()
        if (currentPuzzle != null) {
            // GameState will be deleted by cascade
            puzzleDao.deleteById(currentPuzzle.id)
        }
    }

    /**
     * Observe the current game state
     */
    fun observeCurrentGame(): Flow<SavedGame?> {
        return puzzleDao.observeCurrentPuzzle().map { puzzleEntity ->
            if (puzzleEntity == null) return@map null

            val gameStateEntity = gameStateDao.getByPuzzleId(puzzleEntity.id)
                ?: return@map null

            SavedGame(
                puzzle = puzzleEntity.toPuzzle(),
                gameState = gameStateEntity.toGameState()
            )
        }
    }

    /**
     * Delete all saved games (useful for testing/reset)
     */
    suspend fun deleteAllGames() = withContext(Dispatchers.IO) {
        gameStateDao.deleteAll()
        puzzleDao.deleteAll()
    }
}
