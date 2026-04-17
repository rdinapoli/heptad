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
    ADRIFT(0.0f, "Adrift"),
    TELLURIC(0.05f, "Telluric"),
    ORBITAL(0.10f, "Orbital"),
    SELENIAN(0.20f, "Selenian"),
    COMETARY(0.30f, "Cometary"),
    METEORIC(0.40f, "Meteoric"),
    STELLAR(0.55f, "Stellar"),
    NEBULAR(0.70f, "Nebular"),
    GALACTIC(0.85f, "Galactic"),
    UNIVERSAL(1.0f, "Universal");

    companion object {
        /**
         * Calculate rank from current score percentage
         */
        fun fromScorePercentage(percentage: Float): Rank {
            return entries.lastOrNull { percentage >= it.threshold } ?: ADRIFT
        }

        /**
         * Calculate rank from score and max score
         */
        fun fromScore(currentScore: Int, maxScore: Int): Rank {
            if (maxScore == 0) return ADRIFT
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
            ADRIFT, TELLURIC -> RankAdrift
            ORBITAL, SELENIAN -> RankOrbital
            COMETARY, METEORIC -> RankMeteoric
            STELLAR, NEBULAR -> RankStellar
            GALACTIC -> RankGalactic
            UNIVERSAL -> RankUniversal
        }
    }
}
