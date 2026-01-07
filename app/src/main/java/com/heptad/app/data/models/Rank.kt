package com.heptad.app.data.models

import androidx.compose.ui.graphics.Color
import com.heptad.app.ui.theme.*

/**
 * Player rank levels based on percentage of max score achieved.
 * Each rank has a threshold (percentage of max score) to reach it.
 */
enum class Rank(
    val threshold: Float,
    val displayName: String
) {
    BEGINNER(0.0f, "Beginner"),
    GOOD_START(0.05f, "Good Start"),
    MOVING_UP(0.10f, "Moving Up"),
    GOOD(0.20f, "Good"),
    SOLID(0.30f, "Solid"),
    NICE(0.40f, "Nice"),
    GREAT(0.50f, "Great"),
    AMAZING(0.60f, "Amazing"),
    GENIUS(0.70f, "Genius"),
    QUEEN_BEE(1.0f, "Queen Bee");

    companion object {
        /**
         * Calculate rank from current score percentage
         */
        fun fromScorePercentage(percentage: Float): Rank {
            return entries.lastOrNull { percentage >= it.threshold } ?: BEGINNER
        }

        /**
         * Calculate rank from score and max score
         */
        fun fromScore(currentScore: Int, maxScore: Int): Rank {
            if (maxScore == 0) return BEGINNER
            val percentage = currentScore.toFloat() / maxScore.toFloat()
            return fromScorePercentage(percentage)
        }

        /**
         * Get the next rank after the given rank
         */
        fun getNextRank(currentRank: Rank): Rank? {
            val currentIndex = entries.indexOf(currentRank)
            return if (currentIndex < entries.size - 1) {
                entries[currentIndex + 1]
            } else {
                null
            }
        }
    }

    /**
     * Get the color associated with this rank
     */
    fun getColor(): Color {
        return when (this) {
            BEGINNER, GOOD_START -> RankBeginner
            MOVING_UP, GOOD -> RankMovingUp
            SOLID, NICE -> RankSolid
            GREAT, AMAZING -> RankGreat
            GENIUS -> RankGenius
            QUEEN_BEE -> RankQueenBee
        }
    }
}
