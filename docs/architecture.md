# Architecture Documentation

## Overview

This app follows **MVVM (Model-View-ViewModel)** architecture with **Repository pattern** for data access. Built with modern Android development practices using Jetpack Compose and Kotlin.

## Architecture Layers

### 1. Data Layer

**Responsibilities:**
- Dictionary loading and management
- Puzzle generation and storage
- Game state persistence
- Definition lookup

**Components:**

#### Models (`data/models/`)

```kotlin
data class Puzzle(
    val id: String,                    // UUID
    val centerLetter: Char,
    val outerLetters: List<Char>,      // Size: 6
    val validWords: Set<String>,
    val pangrams: Set<String>,
    val maxScore: Int,
    val createdAt: Long,
    val dictionaryLevel: Int,          // 60, 70, or 80
    val includesS: Boolean
)

data class GameState(
    val puzzleId: String,
    val foundWords: Set<String>,
    val currentScore: Int,
    val currentRank: Rank,
    val startedAt: Long,
    val lastPlayedAt: Long,
    val hintsUnlocked: Set<HintTier>,
    val hintsPanelState: HintsPanelState
)

data class HintsPanelState(
    val twoLetterExpanded: Boolean,
    val gridViewExpanded: Boolean,
    val letterRevealsUsed: Int,
    val definitionHintsUsed: Int
)

enum class HintTier {
    TWO_LETTER_LIST,
    GRID_VIEW,
    LETTER_REVEAL,
    DEFINITION_HINT
}

enum class Rank(val threshold: Float, val displayName: String) {
    BEGINNER(0.0f, "Beginner"),
    GOOD_START(0.05f, "Good Start"),
    MOVING_UP(0.10f, "Moving Up"),
    GOOD(0.20f, "Good"),
    SOLID(0.30f, "Solid"),
    NICE(0.40f, "Nice"),
    GREAT(0.50f, "Great"),
    AMAZING(0.60f, "Amazing"),
    GENIUS(0.70f, "Genius"),
    QUEEN_BEE(1.0f, "Queen Bee")
}

data class TwoLetterHint(
    val prefix: String,              // "BE"
    val foundCount: Int,
    val totalCount: Int,
    val words: Set<String>           // All words starting with prefix
)

data class GridCell(
    val letter: Char,
    val length: Int,
    val foundCount: Int,
    val totalCount: Int,
    val words: Set<String>
)

data class Definition(
    val word: String,
    val definition: String,
    val partOfSpeech: String         // "noun", "verb", etc.
)
```

#### Repositories (`data/repository/`)

```kotlin
interface DictionaryRepository {
    suspend fun loadDictionary(level: Int): Set<String>
    suspend fun getDefinition(word: String): Definition?
    suspend fun getAllDefinitions(): Map<String, Definition>
}

interface PuzzleRepository {
    suspend fun generatePuzzle(
        dictionaryLevel: Int, 
        includeS: Boolean
    ): Puzzle
    suspend fun savePuzzle(puzzle: Puzzle)
    suspend fun getCurrentPuzzle(): Puzzle?
    suspend fun savePuzzleHistory(puzzle: Puzzle, gameState: GameState)
}

interface GameStateRepository {
    suspend fun saveGameState(state: GameState)
    suspend fun loadGameState(puzzleId: String): GameState?
    suspend fun clearGameState()
}
```

#### Database (`data/database/`)

```kotlin
@Database(
    entities = [PuzzleEntity::class, GameStateEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun puzzleDao(): PuzzleDao
    abstract fun gameStateDao(): GameStateDao
}

@Entity(tableName = "puzzles")
data class PuzzleEntity(
    @PrimaryKey val id: String,
    val centerLetter: String,
    val outerLetters: String,        // JSON array
    val validWords: String,          // JSON array
    val pangrams: String,            // JSON array
    val maxScore: Int,
    val createdAt: Long,
    val dictionaryLevel: Int,
    val includesS: Boolean
)

@Entity(tableName = "game_states")
data class GameStateEntity(
    @PrimaryKey val puzzleId: String,
    val foundWords: String,          // JSON array
    val currentScore: Int,
    val currentRank: String,
    val startedAt: Long,
    val lastPlayedAt: Long,
    val hintsUnlocked: String,       // JSON array
    val hintsPanelState: String      // JSON object
)

@Dao
interface PuzzleDao {
    @Query("SELECT * FROM puzzles WHERE id = :puzzleId")
    suspend fun getPuzzle(puzzleId: String): PuzzleEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPuzzle(puzzle: PuzzleEntity)
    
    @Query("SELECT * FROM puzzles ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestPuzzle(): PuzzleEntity?
}

@Dao
interface GameStateDao {
    @Query("SELECT * FROM game_states WHERE puzzleId = :puzzleId")
    suspend fun getGameState(puzzleId: String): GameStateEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameState(state: GameStateEntity)
    
    @Query("DELETE FROM game_states WHERE puzzleId = :puzzleId")
    suspend fun deleteGameState(puzzleId: String)
}
```

### 2. Domain Layer

**Responsibilities:**
- Business logic
- Puzzle generation algorithm
- Word validation
- Hint generation
- Scoring calculations

**Components:**

#### PuzzleGenerator (`domain/PuzzleGenerator.kt`)

```kotlin
class PuzzleGenerator(
    private val dictionaryRepository: DictionaryRepository
) {
    suspend fun generatePuzzle(
        level: Int,
        includeS: Boolean,
        maxAttempts: Int = 100
    ): Puzzle? {
        val dictionary = dictionaryRepository.loadDictionary(level)
        
        repeat(maxAttempts) {
            val letters = selectLetters(includeS)
            val bestCenter = findBestCenter(letters, dictionary)
            
            if (bestCenter != null) {
                val (center, validWords) = bestCenter
                val pangrams = findPangrams(validWords, letters.toSet())
                
                if (meetsQualityCriteria(validWords, pangrams)) {
                    return createPuzzle(center, letters, validWords, pangrams, level, includeS)
                }
            }
        }
        
        return null  // Failed to generate valid puzzle
    }
    
    private fun selectLetters(includeS: Boolean): List<Char> {
        val alphabet = if (includeS) {
            ('a'..'z').toList()
        } else {
            ('a'..'z').filter { it != 's' }
        }
        
        return alphabet.shuffled().take(7)
    }
    
    private fun findBestCenter(
        letters: List<Char>,
        dictionary: Set<String>
    ): Pair<Char, Set<String>>? {
        // Try each letter as center, return the one with most valid words
        return letters.map { center ->
            val otherLetters = letters.filter { it != center }.toSet()
            val validWords = findValidWords(center, otherLetters, dictionary)
            center to validWords
        }.maxByOrNull { it.second.size }
    }
    
    private fun findValidWords(
        center: Char,
        otherLetters: Set<Char>,
        dictionary: Set<String>
    ): Set<String> {
        val allowedLetters = otherLetters + center
        
        return dictionary.filter { word ->
            word.length >= 4 &&
            center in word &&
            word.all { it in allowedLetters }
        }.toSet()
    }
    
    private fun findPangrams(
        validWords: Set<String>,
        allLetters: Set<Char>
    ): Set<String> {
        return validWords.filter { word ->
            allLetters.all { it in word }
        }.toSet()
    }
    
    private fun meetsQualityCriteria(
        validWords: Set<String>,
        pangrams: Set<String>
    ): Boolean {
        val wordCount = validWords.size
        val score = calculateMaxScore(validWords, pangrams)
        
        return wordCount >= 20 &&
               wordCount <= 100 &&
               pangrams.isNotEmpty() &&
               score in 50..250
    }
    
    private fun calculateMaxScore(
        validWords: Set<String>,
        pangrams: Set<String>
    ): Int {
        return validWords.sumOf { word ->
            val baseScore = if (word.length == 4) 1 else word.length
            val pangramBonus = if (word in pangrams) 7 else 0
            baseScore + pangramBonus
        }
    }
}
```

#### WordValidator (`domain/WordValidator.kt`)

```kotlin
class WordValidator(private val puzzle: Puzzle) {
    
    sealed class ValidationResult {
        object Valid : ValidationResult()
        object TooShort : ValidationResult()
        object MissingCenter : ValidationResult()
        object InvalidLetters : ValidationResult()
        object NotInWordList : ValidationResult()
        object AlreadyFound : ValidationResult()
    }
    
    fun validate(
        word: String,
        foundWords: Set<String>
    ): ValidationResult {
        val normalizedWord = word.lowercase().trim()
        
        return when {
            normalizedWord.length < 4 -> 
                ValidationResult.TooShort
            puzzle.centerLetter !in normalizedWord -> 
                ValidationResult.MissingCenter
            normalizedWord.any { it !in getAllLetters() } -> 
                ValidationResult.InvalidLetters
            normalizedWord !in puzzle.validWords -> 
                ValidationResult.NotInWordList
            normalizedWord in foundWords -> 
                ValidationResult.AlreadyFound
            else -> 
                ValidationResult.Valid
        }
    }
    
    fun calculateScore(word: String): Int {
        val baseScore = if (word.length == 4) 1 else word.length
        val pangramBonus = if (word in puzzle.pangrams) 7 else 0
        return baseScore + pangramBonus
    }
    
    private fun getAllLetters(): Set<Char> {
        return (puzzle.outerLetters + puzzle.centerLetter).toSet()
    }
}
```

#### HintGenerator (`domain/HintGenerator.kt`)

```kotlin
class HintGenerator(private val puzzle: Puzzle) {
    
    fun generateTwoLetterList(foundWords: Set<String>): List<TwoLetterHint> {
        val allWords = puzzle.validWords
        
        return allWords
            .groupBy { it.take(2) }
            .map { (prefix, words) ->
                val foundCount = words.count { it in foundWords }
                TwoLetterHint(
                    prefix = prefix,
                    foundCount = foundCount,
                    totalCount = words.size,
                    words = words.toSet()
                )
            }
            .sortedBy { it.prefix }
    }
    
    fun generateGridView(foundWords: Set<String>): List<List<GridCell?>> {
        val allWords = puzzle.validWords
        val allLetters = (puzzle.outerLetters + puzzle.centerLetter).sorted()
        val lengths = allWords.map { it.length }.distinct().sorted()
        
        return lengths.map { length ->
            allLetters.map { letter ->
                val cellWords = allWords.filter { 
                    it.length == length && it.startsWith(letter)
                }
                
                if (cellWords.isEmpty()) {
                    null
                } else {
                    val foundCount = cellWords.count { it in foundWords }
                    GridCell(
                        letter = letter,
                        length = length,
                        foundCount = foundCount,
                        totalCount = cellWords.size,
                        words = cellWords.toSet()
                    )
                }
            }
        }
    }
    
    fun generateLetterReveal(foundWords: Set<String>): String? {
        val unfoundWords = puzzle.validWords - foundWords
        if (unfoundWords.isEmpty()) return null
        
        val word = unfoundWords.random()
        val revealCount = (word.length * 0.5).toInt().coerceAtLeast(2)
        val revealPositions = word.indices.shuffled().take(revealCount)
        
        return word.mapIndexed { index, char ->
            if (index in revealPositions) char else '_'
        }.joinToString(" ")
    }
    
    fun getDefinitionHint(
        foundWords: Set<String>,
        definitions: Map<String, Definition>
    ): Definition? {
        val unfoundWords = puzzle.validWords - foundWords
        if (unfoundWords.isEmpty()) return null
        
        // Try to find a word with a definition
        return unfoundWords.shuffled().firstNotNullOfOrNull { word ->
            definitions[word]
        }
    }
    
    fun calculateUnlockThreshold(
        tier: HintTier,
        customThresholds: Map<HintTier, Float>? = null
    ): Int {
        val defaultThresholds = mapOf(
            HintTier.TWO_LETTER_LIST to 0.0f,
            HintTier.GRID_VIEW to 0.25f,
            HintTier.LETTER_REVEAL to 0.40f,
            HintTier.DEFINITION_HINT to 0.60f
        )
        
        val thresholds = customThresholds ?: defaultThresholds
        val percentage = thresholds[tier] ?: 0.0f
        
        return (puzzle.validWords.size * percentage).toInt()
    }
}
```

### 3. Presentation Layer (UI)

**Responsibilities:**
- User interface rendering
- User interaction handling
- State observation and updates

**Architecture Pattern:** MVVM with Jetpack Compose

#### ViewModel (`ui/viewmodels/GameViewModel.kt`)

```kotlin
class GameViewModel(
    private val puzzleRepository: PuzzleRepository,
    private val gameStateRepository: GameStateRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Loading)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    
    private var currentPuzzle: Puzzle? = null
    private var wordValidator: WordValidator? = null
    private var hintGenerator: HintGenerator? = null
    
    init {
        loadGame()
    }
    
    fun loadGame() {
        viewModelScope.launch {
            try {
                val puzzle = puzzleRepository.getCurrentPuzzle()
                val gameState = puzzle?.let { 
                    gameStateRepository.loadGameState(it.id) 
                }
                
                if (puzzle != null && gameState != null) {
                    initializeGame(puzzle, gameState)
                } else {
                    _uiState.value = GameUiState.NoPuzzle
                }
            } catch (e: Exception) {
                _uiState.value = GameUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun generateNewPuzzle(level: Int, includeS: Boolean) {
        viewModelScope.launch {
            _uiState.value = GameUiState.Generating
            
            val puzzle = puzzleRepository.generatePuzzle(level, includeS)
            if (puzzle != null) {
                puzzleRepository.savePuzzle(puzzle)
                val gameState = GameState(
                    puzzleId = puzzle.id,
                    foundWords = emptySet(),
                    currentScore = 0,
                    currentRank = Rank.BEGINNER,
                    startedAt = System.currentTimeMillis(),
                    lastPlayedAt = System.currentTimeMillis(),
                    hintsUnlocked = setOf(HintTier.TWO_LETTER_LIST),
                    hintsPanelState = HintsPanelState(
                        twoLetterExpanded = false,
                        gridViewExpanded = false,
                        letterRevealsUsed = 0,
                        definitionHintsUsed = 0
                    )
                )
                gameStateRepository.saveGameState(gameState)
                initializeGame(puzzle, gameState)
            } else {
                _uiState.value = GameUiState.Error("Failed to generate puzzle")
            }
        }
    }
    
    private fun initializeGame(puzzle: Puzzle, gameState: GameState) {
        currentPuzzle = puzzle
        wordValidator = WordValidator(puzzle)
        hintGenerator = HintGenerator(puzzle)
        
        _uiState.value = GameUiState.Playing(
            puzzle = puzzle,
            gameState = gameState,
            currentInput = "",
            validationMessage = null
        )
    }
    
    fun submitWord(word: String) {
        val state = (_uiState.value as? GameUiState.Playing) ?: return
        val validator = wordValidator ?: return
        
        val result = validator.validate(word, state.gameState.foundWords)
        
        when (result) {
            is WordValidator.ValidationResult.Valid -> {
                val score = validator.calculateScore(word)
                val newFoundWords = state.gameState.foundWords + word
                val newScore = state.gameState.currentScore + score
                val newRank = calculateRank(newScore, state.puzzle.maxScore)
                
                val updatedGameState = state.gameState.copy(
                    foundWords = newFoundWords,
                    currentScore = newScore,
                    currentRank = newRank,
                    lastPlayedAt = System.currentTimeMillis(),
                    hintsUnlocked = calculateUnlockedHints(
                        newFoundWords.size,
                        state.puzzle.validWords.size
                    )
                )
                
                viewModelScope.launch {
                    gameStateRepository.saveGameState(updatedGameState)
                }
                
                _uiState.value = state.copy(
                    gameState = updatedGameState,
                    currentInput = "",
                    validationMessage = if (word in state.puzzle.pangrams) {
                        "PANGRAM! +${score} points"
                    } else {
                        "+${score} points"
                    }
                )
            }
            else -> {
                _uiState.value = state.copy(
                    validationMessage = result.toMessage()
                )
            }
        }
    }
    
    fun updateInput(input: String) {
        val state = (_uiState.value as? GameUiState.Playing) ?: return
        _uiState.value = state.copy(
            currentInput = input.lowercase(),
            validationMessage = null
        )
    }
    
    fun shuffleLetters() {
        val state = (_uiState.value as? GameUiState.Playing) ?: return
        val shuffled = state.puzzle.outerLetters.shuffled()
        _uiState.value = state.copy(
            puzzle = state.puzzle.copy(outerLetters = shuffled)
        )
    }
    
    fun getDefinition(word: String): Definition? {
        // Implementation to fetch definition
        return null
    }
    
    // Additional methods for hints, settings, etc.
}

sealed class GameUiState {
    object Loading : GameUiState()
    object NoPuzzle : GameUiState()
    object Generating : GameUiState()
    data class Playing(
        val puzzle: Puzzle,
        val gameState: GameState,
        val currentInput: String,
        val validationMessage: String?
    ) : GameUiState()
    data class Error(val message: String) : GameUiState()
}
```

#### Compose Screens (`ui/screens/`)

**GameScreen.kt** - Main gameplay screen
**HintsScreen.kt** - Expandable hint panel
**SettingsScreen.kt** - App settings

#### Compose Components (`ui/components/`)

**OrbitalLetters.kt** - Orbital/planetary letter display
**WordList.kt** - Found words list
**ScoreDisplay.kt** - Score and progress
**HintPanel.kt** - Individual hint displays

## Data Flow

```
User Action
    ↓
Compose UI
    ↓
ViewModel (StateFlow)
    ↓
Repository
    ↓
Database / File System
    ↓
Repository
    ↓
ViewModel (StateFlow update)
    ↓
Compose UI (Recompose)
```

## Dependency Injection

Use **Hilt** for dependency injection:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "heptad-db"
        ).build()
    }
    
    @Provides
    fun providePuzzleDao(db: AppDatabase) = db.puzzleDao()
    
    @Provides
    fun provideGameStateDao(db: AppDatabase) = db.gameStateDao()
    
    // Additional providers...
}
```

## Performance Optimizations

1. **Dictionary Loading**: Load once on app start, cache in memory
2. **Puzzle Generation**: Offload to background coroutine
3. **Word Validation**: Use HashSet for O(1) lookups
4. **UI Rendering**: Lazy composables for long lists
5. **State Updates**: Use `derivedStateOf` for computed values
6. **Database**: Index frequently queried columns

## Testing Strategy

- **Unit Tests**: Domain layer (PuzzleGenerator, WordValidator, HintGenerator)
- **Integration Tests**: Repository layer
- **UI Tests**: Compose UI tests for critical flows
- **Manual Testing**: Full gameplay scenarios

## Build Configuration

```gradle
// app/build.gradle.kts
plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.yourname.heptad"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.yourname.heptad"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```
