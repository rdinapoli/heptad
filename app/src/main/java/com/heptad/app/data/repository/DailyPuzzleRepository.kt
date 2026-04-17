package com.heptad.app.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.heptad.app.R
import com.heptad.app.data.database.dao.DailyCompletionDao
import com.heptad.app.data.database.entity.DailyCompletionEntity
import com.heptad.app.data.models.DailyCompletionStats
import com.heptad.app.data.models.DailyPuzzleStatus
import com.heptad.app.data.models.DailyPuzzlesAsset
import com.heptad.app.data.models.Puzzle
import com.heptad.app.data.models.Rank
import com.heptad.app.data.models.StreakData
import com.heptad.app.data.preferences.DailyProgressSnapshot
import com.heptad.app.data.preferences.DailyPuzzlePreferences
import com.heptad.app.domain.PuzzleGenerationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Repository for managing daily puzzle mode.
 *
 * Responsibilities:
 * - Loading curated puzzle data from JSON asset
 * - Mapping dates to puzzle IDs
 * - Tracking daily puzzle completion status
 * - Managing streak data
 * - Generating fallback puzzles when curated list exhausted
 */
@Singleton
class DailyPuzzleRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dailyCompletionDao: DailyCompletionDao,
    private val dailyPreferences: DailyPuzzlePreferences,
    private val dictionaryRepository: DictionaryRepository
) {
    companion object {
        private const val TAG = "DAILY_PUZZLE"
        private const val LOW_PUZZLE_WARNING_THRESHOLD = 30

        // Fixed settings for daily puzzles
        private const val DAILY_DICTIONARY_LEVEL = 70
        private const val DAILY_INCLUDE_S = true

        // Seed salt for fallback generation
        private const val SEED_SALT = 0x48455054_41445F56L  // "HEPTAD_V"
    }

    private var puzzlesAsset: DailyPuzzlesAsset? = null
    private var assetLoaded = false

    /**
     * Get today's daily puzzle.
     */
    suspend fun getTodaysPuzzle(): Puzzle = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        getPuzzleForDate(today)
    }

    /**
     * Get the puzzle for a specific date.
     */
    suspend fun getPuzzleForDate(date: LocalDate): Puzzle = withContext(Dispatchers.IO) {
        ensureAssetLoaded()

        val asset = puzzlesAsset
        if (asset != null) {
            val startDate = LocalDate.parse(asset.startDate)
            val puzzleIndex = ChronoUnit.DAYS.between(startDate, date).toInt()

            if (puzzleIndex >= 0 && puzzleIndex < asset.puzzles.size) {
                // Use curated puzzle
                val entry = asset.puzzles[puzzleIndex]

                // Log warning if running low
                val remaining = asset.puzzles.size - puzzleIndex - 1
                if (remaining in 1 until LOW_PUZZLE_WARNING_THRESHOLD) {
                    Log.w(TAG, "Only $remaining curated puzzles remaining before fallback!")
                }

                return@withContext entry.toPuzzle()
            }

            // Log when using fallback
            Log.i(TAG, "Using deterministic fallback for date $date (curated list exhausted)")
        }

        // Fallback to deterministic generation
        generateFallbackPuzzle(date)
    }

    /**
     * Get puzzle number for a given date.
     * Puzzle #1 corresponds to the startDate in the manifest.
     */
    suspend fun getPuzzleNumber(date: LocalDate): Int = withContext(Dispatchers.IO) {
        ensureAssetLoaded()
        val asset = puzzlesAsset ?: return@withContext 1
        val startDate = LocalDate.parse(asset.startDate)
        ChronoUnit.DAYS.between(startDate, date).toInt() + 1
    }

    /**
     * Get today's puzzle number.
     */
    suspend fun getTodaysPuzzleNumber(): Int {
        return getPuzzleNumber(LocalDate.now())
    }

    /**
     * Get the status of today's daily puzzle.
     */
    suspend fun getTodaysStatus(): DailyPuzzleStatus = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        val puzzleNumber = getPuzzleNumber(today)
        val puzzleId = "daily-$puzzleNumber"

        val completion = dailyCompletionDao.getByPuzzleId(puzzleId)

        when {
            completion != null -> DailyPuzzleStatus.Completed(
                wordsFound = completion.wordsFound,
                totalWords = completion.totalWords,
                rank = runCatching { Rank.valueOf(completion.rankAchieved) }.getOrDefault(Rank.ADRIFT)
            )
            else -> DailyPuzzleStatus.NotStarted
        }
    }

    /**
     * Get the status for a specific puzzle ID.
     */
    suspend fun getStatusForPuzzle(puzzleId: String): DailyPuzzleStatus = withContext(Dispatchers.IO) {
        val completion = dailyCompletionDao.getByPuzzleId(puzzleId)

        when {
            completion != null -> DailyPuzzleStatus.Completed(
                wordsFound = completion.wordsFound,
                totalWords = completion.totalWords,
                rank = runCatching { Rank.valueOf(completion.rankAchieved) }.getOrDefault(Rank.ADRIFT)
            )
            else -> DailyPuzzleStatus.NotStarted
        }
    }

    /**
     * Mark a daily puzzle as completed.
     */
    suspend fun markCompleted(
        puzzleId: String,
        puzzleNumber: Int,
        stats: DailyCompletionStats
    ) = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        val puzzleDate = getPuzzleDateFromNumber(puzzleNumber)

        // Insert completion record
        dailyCompletionDao.insert(
            DailyCompletionEntity.fromStats(
                puzzleId = puzzleId,
                puzzleNumber = puzzleNumber,
                completedDate = puzzleDate.toString(),
                stats = stats
            )
        )

        // Update streak
        dailyPreferences.recordCompletion(puzzleDate, today)
    }

    /**
     * Check if today's puzzle has been completed.
     */
    suspend fun isTodayCompleted(): Boolean = withContext(Dispatchers.IO) {
        val puzzleNumber = getTodaysPuzzleNumber()
        val puzzleId = "daily-$puzzleNumber"
        dailyCompletionDao.getByPuzzleId(puzzleId) != null
    }

    /**
     * Check if a specific puzzle has been completed.
     */
    suspend fun isCompleted(puzzleId: String): Boolean = withContext(Dispatchers.IO) {
        dailyCompletionDao.getByPuzzleId(puzzleId) != null
    }

    /**
     * Observe streak data as a Flow.
     */
    fun observeStreakData(): Flow<StreakData> {
        return dailyPreferences.streakDataFlow
    }

    /**
     * Get current streak data.
     */
    suspend fun getStreakData(): StreakData {
        return dailyPreferences.getStreakData()
    }

    /**
     * Persist in-progress state for today's puzzle.
     */
    suspend fun saveProgress(puzzleId: String, foundWords: Set<String>, score: Int, rank: Rank) {
        dailyPreferences.saveProgress(puzzleId, foundWords, score, rank)
    }

    /**
     * Load any saved in-progress state for the given puzzle id.
     */
    suspend fun loadProgress(puzzleId: String): DailyProgressSnapshot? {
        return dailyPreferences.loadProgress(puzzleId)
    }

    /**
     * Get the number of remaining curated puzzles.
     */
    suspend fun getRemainingCuratedPuzzleCount(): Int = withContext(Dispatchers.IO) {
        ensureAssetLoaded()
        val asset = puzzlesAsset ?: return@withContext 0
        val today = LocalDate.now()
        val startDate = LocalDate.parse(asset.startDate)
        val currentPuzzleIndex = ChronoUnit.DAYS.between(startDate, today).toInt()
        maxOf(0, asset.puzzles.size - currentPuzzleIndex - 1)
    }

    /**
     * Check if we're currently using fallback generation.
     */
    suspend fun isUsingFallbackGeneration(): Boolean = withContext(Dispatchers.IO) {
        ensureAssetLoaded()
        val asset = puzzlesAsset ?: return@withContext true
        val today = LocalDate.now()
        val startDate = LocalDate.parse(asset.startDate)
        val currentPuzzleIndex = ChronoUnit.DAYS.between(startDate, today).toInt()
        currentPuzzleIndex >= asset.puzzles.size
    }

    /**
     * Get the date for a puzzle number.
     */
    private suspend fun getPuzzleDateFromNumber(puzzleNumber: Int): LocalDate {
        ensureAssetLoaded()
        val asset = puzzlesAsset
        return if (asset != null) {
            val startDate = LocalDate.parse(asset.startDate)
            startDate.plusDays((puzzleNumber - 1).toLong())
        } else {
            // Fallback: assume puzzles started 2026-01-15
            LocalDate.of(2026, 1, 15).plusDays((puzzleNumber - 1).toLong())
        }
    }

    /**
     * Load the daily puzzles asset from resources.
     */
    private suspend fun ensureAssetLoaded() {
        if (assetLoaded) return

        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.resources.openRawResource(R.raw.daily_puzzles)
                val json = inputStream.bufferedReader().use { it.readText() }
                puzzlesAsset = Gson().fromJson(json, DailyPuzzlesAsset::class.java)
                Log.d(TAG, "Loaded ${puzzlesAsset?.puzzles?.size ?: 0} curated daily puzzles")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load daily puzzles asset", e)
                // Will use fallback generation
            }
            assetLoaded = true
        }
    }

    /**
     * Generate a fallback puzzle deterministically from the date.
     * Three-tier strategy:
     * 1. Strict: 200 attempts, require 20+ words and a pangram
     * 2. Relaxed: 100 attempts, require 10+ words, no pangram required
     * 3. Emergency: vowel-heavy letter sets guaranteed to produce valid puzzles
     */
    private suspend fun generateFallbackPuzzle(date: LocalDate): Puzzle {
        val epochDay = date.toEpochDay()
        val seed = (epochDay * 2654435761L) xor SEED_SALT
        val puzzleNumber = getPuzzleNumber(date)

        val dictionary = dictionaryRepository.loadDictionary(DAILY_DICTIONARY_LEVEL)

        // Tier 1: Strict quality (20+ words, pangram required)
        repeat(200) { attempt ->
            val attemptRandom = Random(seed + attempt)
            val letters = PuzzleGenerationHelper.selectLetters(DAILY_INCLUDE_S, attemptRandom)
            val result = PuzzleGenerationHelper.findBestCenter(letters, dictionary)
                ?: return@repeat

            val (centerLetter, validWords) = result
            val pangrams = PuzzleGenerationHelper.findPangrams(validWords, letters.toSet())

            if (validWords.size >= 20 && pangrams.isNotEmpty()) {
                val outerLetters = letters.filter { it != centerLetter }
                val maxScore = PuzzleGenerationHelper.calculateMaxScore(validWords, pangrams)
                return Puzzle(
                    id = "daily-$puzzleNumber",
                    centerLetter = centerLetter,
                    outerLetters = outerLetters,
                    validWords = validWords,
                    pangrams = pangrams,
                    maxScore = maxScore,
                    dictionaryLevel = DAILY_DICTIONARY_LEVEL,
                    includesS = DAILY_INCLUDE_S
                )
            }
        }

        // Tier 2: Relaxed quality (10+ words, no pangram required)
        Log.w(TAG, "Strict quality check failed after 200 attempts, trying relaxed criteria")
        repeat(100) { attempt ->
            val attemptRandom = Random(seed + 1000 + attempt)
            val letters = PuzzleGenerationHelper.selectLetters(DAILY_INCLUDE_S, attemptRandom)
            val result = PuzzleGenerationHelper.findBestCenter(letters, dictionary)
                ?: return@repeat

            val (centerLetter, validWords) = result
            val pangrams = PuzzleGenerationHelper.findPangrams(validWords, letters.toSet())

            if (validWords.size >= 10) {
                val outerLetters = letters.filter { it != centerLetter }
                val maxScore = PuzzleGenerationHelper.calculateMaxScore(validWords, pangrams)
                Log.w(TAG, "Using relaxed-criteria puzzle: ${validWords.size} words, ${pangrams.size} pangrams")
                return Puzzle(
                    id = "daily-$puzzleNumber",
                    centerLetter = centerLetter,
                    outerLetters = outerLetters,
                    validWords = validWords,
                    pangrams = pangrams,
                    maxScore = maxScore,
                    dictionaryLevel = DAILY_DICTIONARY_LEVEL,
                    includesS = DAILY_INCLUDE_S
                )
            }
        }

        // Tier 3: Emergency vowel-heavy letter sets (guaranteed to produce words)
        Log.e(TAG, "All generation attempts failed, using emergency letter set")
        val emergencyLetterSets = listOf(
            listOf('a', 'e', 'i', 'n', 'r', 's', 't'),
            listOf('a', 'e', 'o', 'l', 'n', 'r', 's'),
            listOf('a', 'e', 'i', 'l', 'n', 's', 't'),
            listOf('a', 'e', 'o', 'r', 's', 't', 'd'),
            listOf('a', 'e', 'i', 'g', 'n', 'r', 't'),
        )
        val idx = ((epochDay % emergencyLetterSets.size).toInt())
            .let { if (it < 0) it + emergencyLetterSets.size else it }
        val letters = emergencyLetterSets[idx]
        val (centerLetter, validWords) = PuzzleGenerationHelper.findBestCenter(letters, dictionary)!!
        val outerLetters = letters.filter { it != centerLetter }
        val pangrams = PuzzleGenerationHelper.findPangrams(validWords, letters.toSet())
        val maxScore = PuzzleGenerationHelper.calculateMaxScore(validWords, pangrams)
        return Puzzle(
            id = "daily-$puzzleNumber",
            centerLetter = centerLetter,
            outerLetters = outerLetters,
            validWords = validWords,
            pangrams = pangrams,
            maxScore = maxScore,
            dictionaryLevel = DAILY_DICTIONARY_LEVEL,
            includesS = DAILY_INCLUDE_S
        )
    }
}
