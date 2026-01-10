package com.heptad.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heptad.app.data.models.DailyPuzzleStatus
import com.heptad.app.data.models.Rank
import com.heptad.app.data.repository.DailyPuzzleRepository
import com.heptad.app.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * UI state for the main menu screen.
 */
data class MainMenuUiState(
    val isLoading: Boolean = true,
    val dailyPuzzleStatus: DailyPuzzleStatus = DailyPuzzleStatus.NotStarted,
    val todaysPuzzleNumber: Int = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val hasRandomPuzzleInProgress: Boolean = false,
    val randomPuzzleProgress: RandomPuzzleProgress? = null
)

/**
 * Progress info for an in-progress random puzzle.
 */
data class RandomPuzzleProgress(
    val wordsFound: Int,
    val totalWords: Int,
    val rank: Rank
)

/**
 * ViewModel for the main menu screen.
 */
@HiltViewModel
class MainMenuViewModel @Inject constructor(
    private val dailyPuzzleRepository: DailyPuzzleRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainMenuUiState())
    val uiState: StateFlow<MainMenuUiState> = _uiState.asStateFlow()

    init {
        loadMenuState()
        observeStreakData()
    }

    /**
     * Load the initial menu state.
     */
    private fun loadMenuState() {
        viewModelScope.launch {
            try {
                // Load daily puzzle info
                val dailyStatus = dailyPuzzleRepository.getTodaysStatus()
                val puzzleNumber = dailyPuzzleRepository.getTodaysPuzzleNumber()
                val streakData = dailyPuzzleRepository.getStreakData()

                // Load random puzzle info
                val savedGame = gameRepository.loadCurrentGame()
                val hasRandomInProgress = savedGame != null
                val randomProgress = savedGame?.let {
                    RandomPuzzleProgress(
                        wordsFound = it.gameState.foundWords.size,
                        totalWords = it.puzzle.wordCount,
                        rank = it.gameState.currentRank
                    )
                }

                // Calculate display streak (may be 0 if broken)
                val today = LocalDate.now()
                val displayStreak = streakData.displayStreak(today)

                _uiState.value = MainMenuUiState(
                    isLoading = false,
                    dailyPuzzleStatus = dailyStatus,
                    todaysPuzzleNumber = puzzleNumber,
                    currentStreak = displayStreak,
                    longestStreak = streakData.longestStreak,
                    hasRandomPuzzleInProgress = hasRandomInProgress,
                    randomPuzzleProgress = randomProgress
                )
            } catch (e: Exception) {
                _uiState.value = MainMenuUiState(isLoading = false)
            }
        }
    }

    /**
     * Observe streak data changes.
     */
    private fun observeStreakData() {
        viewModelScope.launch {
            dailyPuzzleRepository.observeStreakData().collect { streakData ->
                val today = LocalDate.now()
                _uiState.value = _uiState.value.copy(
                    currentStreak = streakData.displayStreak(today),
                    longestStreak = streakData.longestStreak
                )
            }
        }
    }

    /**
     * Refresh the menu state (call when returning from other screens).
     */
    fun refresh() {
        loadMenuState()
    }
}
