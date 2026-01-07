package com.heptad.app.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.heptad.app.data.models.GameState
import com.heptad.app.data.models.HintState
import com.heptad.app.data.models.HintTier
import com.heptad.app.data.models.Rank

/**
 * Room entity for storing game state
 */
@Entity(
    tableName = "game_states",
    foreignKeys = [
        ForeignKey(
            entity = PuzzleEntity::class,
            parentColumns = ["id"],
            childColumns = ["puzzleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("puzzleId")]
)
data class GameStateEntity(
    @PrimaryKey
    val puzzleId: String,
    val foundWords: String,     // Comma-separated words
    val currentScore: Int,
    val currentRank: String,    // Rank name
    val startedAt: Long,
    val lastPlayedAt: Long,
    // Hint state fields (flattened)
    val unlockedTiers: String,  // Comma-separated tier names
    val twoLetterExpanded: Boolean,
    val gridViewExpanded: Boolean,
    val letterRevealExpanded: Boolean,
    val definitionHintExpanded: Boolean,
    val letterRevealsUsed: Int,
    val definitionHintsUsed: Int,
    val revealedWords: String,       // Comma-separated
    val shownDefinitionWords: String // Comma-separated
) {
    /**
     * Convert entity to domain model
     */
    fun toGameState(): GameState {
        return GameState(
            puzzleId = puzzleId,
            foundWords = foundWords.split(",").filter { it.isNotBlank() }.toSet(),
            currentScore = currentScore,
            currentRank = Rank.valueOf(currentRank),
            startedAt = startedAt,
            lastPlayedAt = lastPlayedAt,
            hintState = HintState(
                unlockedTiers = unlockedTiers.split(",")
                    .filter { it.isNotBlank() }
                    .map { HintTier.valueOf(it) }
                    .toSet(),
                twoLetterExpanded = twoLetterExpanded,
                gridViewExpanded = gridViewExpanded,
                letterRevealExpanded = letterRevealExpanded,
                definitionHintExpanded = definitionHintExpanded,
                letterRevealsUsed = letterRevealsUsed,
                definitionHintsUsed = definitionHintsUsed,
                revealedWords = revealedWords.split(",").filter { it.isNotBlank() }.toSet(),
                shownDefinitionWords = shownDefinitionWords.split(",").filter { it.isNotBlank() }.toSet()
            )
        )
    }

    companion object {
        /**
         * Convert domain model to entity
         */
        fun fromGameState(gameState: GameState): GameStateEntity {
            return GameStateEntity(
                puzzleId = gameState.puzzleId,
                foundWords = gameState.foundWords.joinToString(","),
                currentScore = gameState.currentScore,
                currentRank = gameState.currentRank.name,
                startedAt = gameState.startedAt,
                lastPlayedAt = gameState.lastPlayedAt,
                unlockedTiers = gameState.hintState.unlockedTiers.joinToString(",") { it.name },
                twoLetterExpanded = gameState.hintState.twoLetterExpanded,
                gridViewExpanded = gameState.hintState.gridViewExpanded,
                letterRevealExpanded = gameState.hintState.letterRevealExpanded,
                definitionHintExpanded = gameState.hintState.definitionHintExpanded,
                letterRevealsUsed = gameState.hintState.letterRevealsUsed,
                definitionHintsUsed = gameState.hintState.definitionHintsUsed,
                revealedWords = gameState.hintState.revealedWords.joinToString(","),
                shownDefinitionWords = gameState.hintState.shownDefinitionWords.joinToString(",")
            )
        }
    }
}
