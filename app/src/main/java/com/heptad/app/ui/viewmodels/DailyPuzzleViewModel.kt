package com.heptad.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heptad.app.data.models.DailyCompletionStats
import com.heptad.app.data.models.GameState
import com.heptad.app.data.models.HintTier
import com.heptad.app.data.models.Puzzle
import com.heptad.app.data.models.Rank
import com.heptad.app.data.models.StreakData
import com.heptad.app.data.preferences.UserPreferences
import com.heptad.app.data.repository.DailyPuzzleRepository
import com.heptad.app.data.repository.DefinitionRepository
import com.heptad.app.data.repository.DefinitionResult
import com.heptad.app.domain.ValidationResult
import com.heptad.app.domain.WordValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

/**
 * UI State for the Daily Puzzle Screen
 */
sealed class DailyPuzzleUiState {
    object Loading : DailyPuzzleUiState()
    data class Playing(
        val puzzle: Puzzle,
        val gameState: GameState,
        val puzzleNumber: Int,
        val currentStreak: Int,
        val currentInput: String = "",
        val validationMessage: String? = null,
        val shuffledOuterLetters: List<Char>? = null,
        val showCompletionDialog: Boolean = false,
        val timeUntilNextPuzzle: Duration? = null
    ) : DailyPuzzleUiState()
    data class Error(val message: String) : DailyPuzzleUiState()
}

/**
 * ViewModel for the daily puzzle screen.
 */
@HiltViewModel
class DailyPuzzleViewModel @Inject constructor(
    private val dailyPuzzleRepository: DailyPuzzleRepository,
    private val definitionRepository: DefinitionRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<DailyPuzzleUiState>(DailyPuzzleUiState.Loading)
    val uiState: StateFlow<DailyPuzzleUiState> = _uiState.asStateFlow()

    private val _definitionState = MutableStateFlow<DefinitionResult>(DefinitionResult.Loading)
    val definitionState: StateFlow<DefinitionResult> = _definitionState.asStateFlow()

    private var currentPuzzle: Puzzle? = null
    private var wordValidator: WordValidator? = null
    private var puzzleNumber: Int = 0
    private var countdownJob: Job? = null
    private var isCompleted: Boolean = false

    init {
        loadTodaysPuzzle()
    }

    /**
     * Load today's daily puzzle.
     */
    private fun loadTodaysPuzzle() {
        viewModelScope.launch {
            try {
                val puzzle = dailyPuzzleRepository.getTodaysPuzzle()
                puzzleNumber = dailyPuzzleRepository.getTodaysPuzzleNumber()
                val streakData = dailyPuzzleRepository.getStreakData()
                val today = LocalDate.now()

                currentPuzzle = puzzle
                wordValidator = WordValidator(puzzle)

                // Check if already completed
                isCompleted = dailyPuzzleRepository.isTodayCompleted()

                // Create or load game state
                val prefs = userPreferences.preferencesFlow.first()
                val gameState = GameState.newGame(puzzle.id).let { base ->
                    base.copy(
                        hintState = base.hintState.withUpdatedUnlocks(
                            currentScore = 0,
                            maxScore = puzzle.maxScore,
                            forceUnlockAll = prefs.allHintsAvailable
                        )
                    )
                }

                _uiState.value = DailyPuzzleUiState.Playing(
                    puzzle = puzzle,
                    gameState = gameState,
                    puzzleNumber = puzzleNumber,
                    currentStreak = streakData.displayStreak(today),
                    showCompletionDialog = isCompleted
                )

                // Start countdown timer
                startCountdownTimer()
            } catch (e: Exception) {
                _uiState.value = DailyPuzzleUiState.Error(e.message ?: "Failed to load daily puzzle")
            }
        }
    }

    /**
     * Start countdown timer to next puzzle.
     */
    private fun startCountdownTimer() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (isActive) {
                val now = LocalDateTime.now()
                val midnight = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT)
                val timeUntil = Duration.between(now, midnight)

                val state = _uiState.value as? DailyPuzzleUiState.Playing
                if (state != null) {
                    _uiState.value = state.copy(timeUntilNextPuzzle = timeUntil)
                }

                delay(1000)
            }
        }
    }

    /**
     * Add a letter to the current input.
     */
    fun addLetter(letter: Char) {
        val state = (_uiState.value as? DailyPuzzleUiState.Playing) ?: return

        _uiState.value = state.copy(
            currentInput = state.currentInput + letter.lowercase(),
            validationMessage = null
        )
    }

    /**
     * Delete the last letter from the current input.
     */
    fun deleteLetter() {
        val state = (_uiState.value as? DailyPuzzleUiState.Playing) ?: return

        if (state.currentInput.isNotEmpty()) {
            _uiState.value = state.copy(
                currentInput = state.currentInput.dropLast(1),
                validationMessage = null
            )
        }
    }

    /**
     * Clear the current input.
     */
    fun clearInput() {
        val state = (_uiState.value as? DailyPuzzleUiState.Playing) ?: return

        _uiState.value = state.copy(
            currentInput = "",
            validationMessage = null
        )
    }

    /**
     * Submit the current word for validation.
     */
    fun submitWord() {
        val state = (_uiState.value as? DailyPuzzleUiState.Playing) ?: return
        val validator = wordValidator ?: return
        val word = state.currentInput.lowercase().trim()

        if (word.isEmpty()) return

        val result = validator.validate(word, state.gameState.foundWords)

        when (result) {
            is ValidationResult.Valid -> {
                val score = validator.calculateScore(word)
                val newScore = state.gameState.currentScore + score
                val newRank = Rank.fromScore(newScore, state.puzzle.maxScore)

                viewModelScope.launch {
                    val prefs = userPreferences.preferencesFlow.first()
                    val currentState = (_uiState.value as? DailyPuzzleUiState.Playing) ?: return@launch

                    val baseGameState = currentState.gameState.withFoundWord(word, score, newRank)
                    val updatedGameState = baseGameState.copy(
                        hintState = baseGameState.hintState.withUpdatedUnlocks(
                            currentScore = baseGameState.currentScore,
                            maxScore = currentState.puzzle.maxScore,
                            forceUnlockAll = prefs.allHintsAvailable
                        )
                    )

                    val successMessage = validator.getSuccessMessage(word)

                    // Check if this is the first word found (marks completion)
                    val shouldMarkComplete = !isCompleted && updatedGameState.foundWords.isNotEmpty()

                    _uiState.value = currentState.copy(
                        gameState = updatedGameState,
                        currentInput = "",
                        validationMessage = successMessage,
                        showCompletionDialog = shouldMarkComplete
                    )

                    // Mark puzzle as completed on first word
                    if (shouldMarkComplete) {
                        isCompleted = true
                        markPuzzleCompleted(updatedGameState, currentState.puzzle)
                    }

                    // Clear the success message after a delay
                    delay(2000)
                    val latestState = _uiState.value as? DailyPuzzleUiState.Playing
                    if (latestState?.validationMessage == successMessage) {
                        _uiState.value = latestState.copy(validationMessage = null)
                    }
                }
            }
            else -> {
                _uiState.value = state.copy(
                    currentInput = "",
                    validationMessage = result.toMessage()
                )

                viewModelScope.launch {
                    delay(2000)
                    val currentState = _uiState.value as? DailyPuzzleUiState.Playing
                    if (currentState?.validationMessage == result.toMessage()) {
                        _uiState.value = currentState.copy(validationMessage = null)
                    }
                }
            }
        }
    }

    /**
     * Mark the puzzle as completed and update streak.
     */
    private suspend fun markPuzzleCompleted(gameState: GameState, puzzle: Puzzle) {
        val stats = DailyCompletionStats(
            wordsFound = gameState.foundWords.size,
            totalWords = puzzle.wordCount,
            scoreAchieved = gameState.currentScore,
            maxScore = puzzle.maxScore,
            rankAchieved = gameState.currentRank,
            pangramsFound = gameState.foundWords.count { puzzle.isPangram(it) },
            totalPangrams = puzzle.pangrams.size
        )

        dailyPuzzleRepository.markCompleted(
            puzzleId = puzzle.id,
            puzzleNumber = puzzleNumber,
            stats = stats
        )

        // Update streak display
        val streakData = dailyPuzzleRepository.getStreakData()
        val today = LocalDate.now()
        val state = _uiState.value as? DailyPuzzleUiState.Playing
        if (state != null) {
            _uiState.value = state.copy(currentStreak = streakData.displayStreak(today))
        }
    }

    /**
     * Randomly shuffle the outer letters.
     */
    fun shuffleLetters() {
        val state = (_uiState.value as? DailyPuzzleUiState.Playing) ?: return
        val currentOrder = state.shuffledOuterLetters ?: state.puzzle.outerLetters

        _uiState.value = state.copy(
            shuffledOuterLetters = currentOrder.shuffled()
        )
    }

    /**
     * Toggle expansion state of a hint tier.
     */
    fun toggleHintTier(tier: HintTier) {
        val state = (_uiState.value as? DailyPuzzleUiState.Playing) ?: return

        val updatedHintState = state.gameState.hintState.toggleTier(tier)
        val updatedGameState = state.gameState.copy(hintState = updatedHintState)

        _uiState.value = state.copy(gameState = updatedGameState)
    }

    /**
     * Dismiss the completion dialog.
     */
    fun dismissCompletionDialog() {
        val state = (_uiState.value as? DailyPuzzleUiState.Playing) ?: return
        _uiState.value = state.copy(showCompletionDialog = false)
    }

    /**
     * Fetch definition for a word.
     */
    fun fetchDefinition(word: String) {
        _definitionState.value = DefinitionResult.Loading
        viewModelScope.launch {
            _definitionState.value = definitionRepository.getDefinition(word)
        }
    }

    /**
     * Reset definition state (when dialog is closed).
     */
    fun clearDefinition() {
        _definitionState.value = DefinitionResult.Loading
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
