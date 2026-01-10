package com.heptad.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.heptad.app.data.database.entity.DailyCompletionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for daily puzzle completion operations.
 */
@Dao
interface DailyCompletionDao {

    /**
     * Insert or replace a completion record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(completion: DailyCompletionEntity)

    /**
     * Get completion by puzzle ID.
     */
    @Query("SELECT * FROM daily_completions WHERE puzzleId = :puzzleId")
    suspend fun getByPuzzleId(puzzleId: String): DailyCompletionEntity?

    /**
     * Get completion by date.
     */
    @Query("SELECT * FROM daily_completions WHERE completedDate = :date")
    suspend fun getByDate(date: String): DailyCompletionEntity?

    /**
     * Observe all completions, newest first.
     */
    @Query("SELECT * FROM daily_completions ORDER BY puzzleNumber DESC")
    fun observeAllCompletions(): Flow<List<DailyCompletionEntity>>

    /**
     * Get total count of completions.
     */
    @Query("SELECT COUNT(*) FROM daily_completions")
    suspend fun getCompletionCount(): Int

    /**
     * Get recent completions.
     */
    @Query("SELECT * FROM daily_completions ORDER BY puzzleNumber DESC LIMIT :limit")
    suspend fun getRecentCompletions(limit: Int): List<DailyCompletionEntity>

    /**
     * Get all completed puzzle IDs.
     */
    @Query("SELECT puzzleId FROM daily_completions")
    suspend fun getAllCompletedPuzzleIds(): List<String>

    /**
     * Get the most recent completion.
     */
    @Query("SELECT * FROM daily_completions ORDER BY completedAt DESC LIMIT 1")
    suspend fun getMostRecentCompletion(): DailyCompletionEntity?

    /**
     * Check if a completion exists on or after the given date.
     * Used for streak calculation.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM daily_completions WHERE completedDate >= :date)")
    suspend fun hasCompletionOnOrAfter(date: String): Boolean

    /**
     * Delete all completions (for testing/reset).
     */
    @Query("DELETE FROM daily_completions")
    suspend fun deleteAll()
}
