package com.heptad.app.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.heptad.app.data.models.DailyCompletionStats
import com.heptad.app.data.models.Rank

/**
 * Room entity for tracking daily puzzle completions.
 * Stores summary stats rather than full game state.
 */
@Entity(
    tableName = "daily_completions",
    indices = [
        Index("completedDate"),
        Index("puzzleNumber")
    ]
)
data class DailyCompletionEntity(
    @PrimaryKey
    val puzzleId: String,           // "daily-147"
    val puzzleNumber: Int,          // 147
    val completedDate: String,      // ISO date: "2026-01-15"
    val completedAt: Long,          // Timestamp of completion
    val wordsFound: Int,
    val totalWords: Int,
    val scoreAchieved: Int,
    val maxScore: Int,
    val rankAchieved: String,       // Rank name: "GENIUS"
    val pangramsFound: Int,
    val totalPangrams: Int
) {
    /**
     * Convert entity to completion stats.
     */
    fun toStats(): DailyCompletionStats {
        return DailyCompletionStats(
            wordsFound = wordsFound,
            totalWords = totalWords,
            scoreAchieved = scoreAchieved,
            maxScore = maxScore,
            rankAchieved = runCatching { Rank.valueOf(rankAchieved) }.getOrDefault(Rank.ADRIFT),
            pangramsFound = pangramsFound,
            totalPangrams = totalPangrams
        )
    }

    companion object {
        /**
         * Create entity from completion stats.
         */
        fun fromStats(
            puzzleId: String,
            puzzleNumber: Int,
            completedDate: String,
            stats: DailyCompletionStats
        ): DailyCompletionEntity {
            return DailyCompletionEntity(
                puzzleId = puzzleId,
                puzzleNumber = puzzleNumber,
                completedDate = completedDate,
                completedAt = System.currentTimeMillis(),
                wordsFound = stats.wordsFound,
                totalWords = stats.totalWords,
                scoreAchieved = stats.scoreAchieved,
                maxScore = stats.maxScore,
                rankAchieved = stats.rankAchieved.name,
                pangramsFound = stats.pangramsFound,
                totalPangrams = stats.totalPangrams
            )
        }
    }
}
