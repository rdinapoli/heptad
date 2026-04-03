package com.heptad.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heptad.app.data.models.GameState
import com.heptad.app.data.models.HintTier
import com.heptad.app.data.models.Puzzle
import com.heptad.app.data.models.Rank
import com.heptad.app.data.preferences.UserPreferences
import com.heptad.app.data.repository.DefinitionRepository
import com.heptad.app.data.repository.DefinitionResult
import com.heptad.app.data.repository.GameRepository
import com.heptad.app.domain.PuzzleGenerator
import com.heptad.app.domain.ValidationResult
import com.heptad.app.domain.WordValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the Game Screen
 */
sealed class GameUiState {
    object Loading : GameUiState()
    object NoPuzzle : GameUiState()
    object Generating : GameUiState()
    data class Playing(
        val puzzle: Puzzle,
        val gameState: GameState,
        val currentInput: String = "",
        val validationMessage: String? = null,
        val shuffledOuterLetters: List<Char>? = null
    ) : GameUiState()
    data class Error(val message: String) : GameUiState()
}

/**
 * ViewModel for the main game screen
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val puzzleGenerator: PuzzleGenerator,
    private val gameRepository: GameRepository,
    private val userPreferences: UserPreferences,
    private val definitionRepository: DefinitionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Loading)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _definitionState = MutableStateFlow<DefinitionResult>(DefinitionResult.Loading)
    val definitionState: StateFlow<DefinitionResult> = _definitionState.asStateFlow()

    private var currentPuzzle: Puzzle? = null
    private var wordValidator: WordValidator? = null

    init {
        loadGame()
    }

    /**
     * Load existing game from database or show NoPuzzle state
     */
    private fun loadGame() {
        viewModelScope.launch {
            try {
                val savedGame = gameRepository.loadCurrentGame()

                if (savedGame != null) {
                    currentPuzzle = savedGame.puzzle
                    wordValidator = WordValidator(savedGame.puzzle)

                    // Apply all hints setting
                    val prefs = userPreferences.preferencesFlow.first()
                    val updatedHintState = savedGame.gameState.hintState.withUpdatedUnlocks(
                        currentScore = savedGame.gameState.currentScore,
                        maxScore = savedGame.puzzle.maxScore,
                        forceUnlockAll = prefs.allHintsAvailable
                    )
                    val updatedGameState = savedGame.gameState.copy(hintState = updatedHintState)

                    _uiState.value = GameUiState.Playing(
                        puzzle = savedGame.puzzle,
                        gameState = updatedGameState
                    )
                } else {
                    _uiState.value = GameUiState.NoPuzzle
                }
            } catch (e: Exception) {
                _uiState.value = GameUiState.NoPuzzle
            }
        }
    }

    /**
     * Generate a new puzzle using user preferences
     */
    fun generateNewPuzzle() {
        viewModelScope.launch {
            _uiState.value = GameUiState.Generating

            try {
                val prefs = userPreferences.preferencesFlow.first()
                val puzzle = puzzleGenerator.generatePuzzle(prefs.dictionaryLevel, prefs.includeS)

                if (puzzle != null) {
                    currentPuzzle = puzzle
                    wordValidator = WordValidator(puzzle)

                    val baseGameState = GameState.newGame(puzzle.id)
                    // Apply all hints setting
                    val updatedHintState = baseGameState.hintState.withUpdatedUnlocks(
                        currentScore = 0,
                        maxScore = puzzle.maxScore,
                        forceUnlockAll = prefs.allHintsAvailable
                    )
                    val gameState = baseGameState.copy(hintState = updatedHintState)

                    // Save new game to database
                    gameRepository.saveNewGame(puzzle, gameState)

                    _uiState.value = GameUiState.Playing(
                        puzzle = puzzle,
                        gameState = gameState
                    )
                } else {
                    _uiState.value = GameUiState.Error("Failed to generate puzzle. Please try again.")
                }
            } catch (e: Exception) {
                _uiState.value = GameUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    /**
     * Add a letter to the current input
     */
    fun addLetter(letter: Char) {
        val state = (_uiState.value as? GameUiState.Playing) ?: return

        _uiState.value = state.copy(
            currentInput = state.currentInput + letter.lowercase(),
            validationMessage = null
        )
    }

    /**
     * Delete the last letter from the current input
     */
    fun deleteLetter() {
        val state = (_uiState.value as? GameUiState.Playing) ?: return

        if (state.currentInput.isNotEmpty()) {
            _uiState.value = state.copy(
                currentInput = state.currentInput.dropLast(1),
                validationMessage = null
            )
        }
    }

    /**
     * Clear the current input
     */
    fun clearInput() {
        val state = (_uiState.value as? GameUiState.Playing) ?: return

        _uiState.value = state.copy(
            currentInput = "",
            validationMessage = null
        )
    }

    /**
     * Submit the current word for validation
     */
    fun submitWord() {
        val state = (_uiState.value as? GameUiState.Playing) ?: return
        val validator = wordValidator ?: return
        val word = state.currentInput.lowercase().trim()

        if (word.isEmpty()) return

        val result = validator.validate(word, state.gameState.foundWords)

        when (result) {
            is ValidationResult.Valid -> {
                val score = validator.calculateScore(word)
                val newScore = state.gameState.currentScore + score
                val newRank = Rank.fromScore(newScore, state.puzzle.maxScore)

                // Get current preferences for all hints setting
                viewModelScope.launch {
                    val prefs = userPreferences.preferencesFlow.first()
                    val currentState = (_uiState.value as? GameUiState.Playing) ?: return@launch

                    val baseGameState = currentState.gameState.withFoundWord(word, score, newRank)
                    val updatedGameState = baseGameState.copy(
                        hintState = baseGameState.hintState.withUpdatedUnlocks(
                            currentScore = baseGameState.currentScore,
                            maxScore = currentState.puzzle.maxScore,
                            forceUnlockAll = prefs.allHintsAvailable
                        )
                    )

                    val successMessage = validator.getSuccessMessage(word)

                    _uiState.value = currentState.copy(
                        gameState = updatedGameState,
                        currentInput = "",
                        validationMessage = successMessage
                    )

                    // Auto-save game state to database
                    gameRepository.updateGameState(updatedGameState)

                    // Clear the success message after a delay
                    delay(2000)
                    val latestState = _uiState.value as? GameUiState.Playing
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

                // Clear the error message after a delay
                viewModelScope.launch {
                    delay(2000)
                    val currentState = _uiState.value as? GameUiState.Playing
                    if (currentState?.validationMessage == result.toMessage()) {
                        _uiState.value = currentState.copy(validationMessage = null)
                    }
                }
            }
        }
    }

    /**
     * Randomly shuffle the outer letters
     */
    fun shuffleLetters() {
        val state = (_uiState.value as? GameUiState.Playing) ?: return
        val currentOrder = state.shuffledOuterLetters ?: state.puzzle.outerLetters

        _uiState.value = state.copy(
            shuffledOuterLetters = currentOrder.shuffled()
        )
    }

    /**
     * Toggle expansion state of a hint tier
     */
    fun toggleHintTier(tier: HintTier) {
        val state = (_uiState.value as? GameUiState.Playing) ?: return

        val updatedHintState = state.gameState.hintState.toggleTier(tier)
        val updatedGameState = state.gameState.copy(hintState = updatedHintState)

        _uiState.value = state.copy(gameState = updatedGameState)

        // Save to database
        viewModelScope.launch {
            gameRepository.updateGameState(updatedGameState)
        }
    }

    /**
     * Fetch definition for a word
     */
    fun fetchDefinition(word: String) {
        _definitionState.value = DefinitionResult.Loading
        viewModelScope.launch {
            _definitionState.value = definitionRepository.getDefinition(word)
        }
    }

    /**
     * Reset definition state (when dialog is closed)
     */
    fun clearDefinition() {
        _definitionState.value = DefinitionResult.Loading
    }
}
