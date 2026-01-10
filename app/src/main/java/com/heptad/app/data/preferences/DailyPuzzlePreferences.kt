package com.heptad.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.heptad.app.data.models.StreakData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dailyDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "daily_puzzle_preferences"
)

/**
 * DataStore-backed preferences for daily puzzle streak tracking.
 */
@Singleton
class DailyPuzzlePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val LONGEST_STREAK = intPreferencesKey("longest_streak")
        val LAST_COMPLETED_DATE = stringPreferencesKey("last_completed_date")
        val TOTAL_DAILIES_COMPLETED = intPreferencesKey("total_dailies_completed")
    }

    /**
     * Observe streak data as a Flow.
     */
    val streakDataFlow: Flow<StreakData> = context.dailyDataStore.data.map { prefs ->
        StreakData(
            currentStreak = prefs[Keys.CURRENT_STREAK] ?: 0,
            longestStreak = prefs[Keys.LONGEST_STREAK] ?: 0,
            lastCompletedDate = prefs[Keys.LAST_COMPLETED_DATE]?.let {
                LocalDate.parse(it)
            },
            totalDailiesCompleted = prefs[Keys.TOTAL_DAILIES_COMPLETED] ?: 0
        )
    }

    /**
     * Get current streak data (non-flow).
     */
    suspend fun getStreakData(): StreakData {
        return streakDataFlow.first()
    }

    /**
     * Record a daily puzzle completion and update streak.
     *
     * @param completedDate The date of the puzzle that was completed
     * @param today The current date (for streak calculation)
     */
    suspend fun recordCompletion(completedDate: LocalDate, today: LocalDate) {
        context.dailyDataStore.edit { prefs ->
            val lastCompleted = prefs[Keys.LAST_COMPLETED_DATE]?.let {
                LocalDate.parse(it)
            }
            val currentStreak = prefs[Keys.CURRENT_STREAK] ?: 0
            val longestStreak = prefs[Keys.LONGEST_STREAK] ?: 0
            val totalCompleted = prefs[Keys.TOTAL_DAILIES_COMPLETED] ?: 0

            // Calculate new streak
            val newStreak = when {
                // First completion ever
                lastCompleted == null -> 1

                // Completed same day again (no change to streak)
                completedDate == lastCompleted -> currentStreak

                // Consecutive day - extend streak
                ChronoUnit.DAYS.between(lastCompleted, completedDate) == 1L -> {
                    currentStreak + 1
                }

                // Gap in completion - streak resets
                else -> 1
            }

            // Only increment total if this is a new completion date
            val newTotal = if (completedDate != lastCompleted) {
                totalCompleted + 1
            } else {
                totalCompleted
            }

            prefs[Keys.CURRENT_STREAK] = newStreak
            prefs[Keys.LONGEST_STREAK] = maxOf(longestStreak, newStreak)
            prefs[Keys.LAST_COMPLETED_DATE] = completedDate.toString()
            prefs[Keys.TOTAL_DAILIES_COMPLETED] = newTotal
        }
    }

    /**
     * Check if the streak is broken (more than 1 day since last completion).
     */
    suspend fun isStreakBroken(today: LocalDate): Boolean {
        val data = getStreakData()
        val lastCompleted = data.lastCompletedDate ?: return true
        val daysSinceLast = ChronoUnit.DAYS.between(lastCompleted, today).toInt()
        return daysSinceLast > 1
    }

    /**
     * Reset streak data (for testing).
     */
    suspend fun resetStreak() {
        context.dailyDataStore.edit { prefs ->
            prefs.remove(Keys.CURRENT_STREAK)
            prefs.remove(Keys.LONGEST_STREAK)
            prefs.remove(Keys.LAST_COMPLETED_DATE)
            prefs.remove(Keys.TOTAL_DAILIES_COMPLETED)
        }
    }
}
