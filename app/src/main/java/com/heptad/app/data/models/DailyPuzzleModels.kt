package com.heptad.app.data.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

/**
 * Root container for the daily puzzles JSON asset.
 */
data class DailyPuzzlesAsset(
    val version: Int,
    @SerializedName("start_date")
    val startDate: String,
    val puzzles: List<DailyPuzzleEntry>
)

/**
 * Entry for a single daily puzzle in the asset file.
 */
data class DailyPuzzleEntry(
    @SerializedName("puzzle_number")
    val puzzleNumber: Int,
    @SerializedName("center_letter")
    val centerLetter: String,
    @SerializedName("outer_letters")
    val outerLetters: List<String>,
    @SerializedName("valid_words")
    val validWords: List<String>,
    val pangrams: List<String>,
    @SerializedName("max_score")
    val maxScore: Int
) {
    /**
     * Convert to a Puzzle domain object.
     */
    fun toPuzzle(): Puzzle {
        return Puzzle(
            id = "daily-$puzzleNumber",
            centerLetter = centerLetter.first().lowercaseChar(),
            outerLetters = outerLetters.map { it.first().lowercaseChar() },
            validWords = validWords.map { it.lowercase() }.toSet(),
            pangrams = pangrams.map { it.lowercase() }.toSet(),
            maxScore = maxScore,
            dictionaryLevel = 70,  // Daily puzzles always use Level 70
            includesS = true       // Daily puzzles always include S
        )
    }
}

/**
 * Status of the daily puzzle for display purposes.
 */
sealed class DailyPuzzleStatus {
    /**
     * Puzzle has not been started yet.
     */
    object NotStarted : DailyPuzzleStatus()

    /**
     * Puzzle is in progress.
     */
    data class InProgress(
        val wordsFound: Int,
        val totalWords: Int,
        val rank: Rank
    ) : DailyPuzzleStatus()

    /**
     * Puzzle has been completed (at least one word found).
     */
    data class Completed(
        val wordsFound: Int,
        val totalWords: Int,
        val rank: Rank
    ) : DailyPuzzleStatus()
}

/**
 * Streak data for the daily puzzle feature.
 */
data class StreakData(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedDate: LocalDate? = null,
    val totalDailiesCompleted: Int = 0
) {
    /**
     * Check if streak is still active (completed yesterday or today).
     */
    fun isStreakActive(today: LocalDate): Boolean {
        val lastCompleted = lastCompletedDate ?: return false
        val daysSinceLast = java.time.temporal.ChronoUnit.DAYS.between(lastCompleted, today).toInt()
        return daysSinceLast <= 1
    }

    /**
     * Get display-friendly streak, accounting for broken streaks.
     */
    fun displayStreak(today: LocalDate): Int {
        return if (isStreakActive(today)) currentStreak else 0
    }
}

/**
 * Stats recorded when completing a daily puzzle.
 */
data class DailyCompletionStats(
    val wordsFound: Int,
    val totalWords: Int,
    val scoreAchieved: Int,
    val maxScore: Int,
    val rankAchieved: Rank,
    val pangramsFound: Int,
    val totalPangrams: Int
) {
    val completionPercentage: Float
        get() = if (totalWords > 0) wordsFound.toFloat() / totalWords else 0f

    val foundAllPangrams: Boolean
        get() = pangramsFound >= totalPangrams

    val isNebularOrBetter: Boolean
        get() = rankAchieved.ordinal >= Rank.NEBULAR.ordinal
}
