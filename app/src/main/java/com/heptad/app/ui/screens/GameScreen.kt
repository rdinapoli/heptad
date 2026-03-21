package com.heptad.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.heptad.app.data.models.HintState
import com.heptad.app.data.models.HintTier
import com.heptad.app.data.models.Puzzle
import com.heptad.app.data.repository.DefinitionResult
import com.heptad.app.ui.components.*
import com.heptad.app.ui.viewmodels.GameUiState
import com.heptad.app.ui.viewmodels.GameViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val definitionState by viewModel.definitionState.collectAsState()
    var showNewPuzzleDialog by remember { mutableStateOf(false) }

    if (showNewPuzzleDialog) {
        AlertDialog(
            onDismissRequest = { showNewPuzzleDialog = false },
            title = { Text("New Puzzle?") },
            text = { Text("Your current progress will be lost.") },
            confirmButton = {
                TextButton(onClick = {
                    showNewPuzzleDialog = false
                    viewModel.generateNewPuzzle()
                }) {
                    Text("New Puzzle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewPuzzleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Random Puzzle") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (uiState is GameUiState.Playing) {
                        IconButton(onClick = { showNewPuzzleDialog = true }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "New Puzzle"
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is GameUiState.Loading -> {
                    LoadingContent()
                }
                is GameUiState.NoPuzzle -> {
                    NoPuzzleContent(
                        onGeneratePuzzle = { viewModel.generateNewPuzzle() }
                    )
                }
                is GameUiState.Generating -> {
                    GeneratingContent()
                }
                is GameUiState.Playing -> {
                    PlayingContent(
                        state = state,
                        onLetterClick = viewModel::addLetter,
                        onDeleteClick = viewModel::deleteLetter,
                        onSubmitClick = viewModel::submitWord,
                        onShuffleClick = viewModel::shuffleLetters,
                        onToggleHintTier = viewModel::toggleHintTier,
                        definitionState = definitionState,
                        onFetchDefinition = viewModel::fetchDefinition,
                        onClearDefinition = viewModel::clearDefinition
                    )
                }
                is GameUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.generateNewPuzzle() }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun NoPuzzleContent(
    onGeneratePuzzle: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Welcome to Heptad!",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Form words using the 7 letters.\nEvery word must include the center letter.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onGeneratePuzzle,
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Start New Puzzle")
            }
        }
    }
}

@Composable
private fun GeneratingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Generating puzzle...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun PlayingContent(
    state: GameUiState.Playing,
    onLetterClick: (Char) -> Unit,
    onDeleteClick: () -> Unit,
    onSubmitClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onToggleHintTier: (HintTier) -> Unit,
    definitionState: DefinitionResult,
    onFetchDefinition: (String) -> Unit,
    onClearDefinition: () -> Unit
) {
    val tabs = listOf("Play", "Hints", "Words (${state.gameState.wordsFoundCount})")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Row
        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(title) }
                )
            }
        }

        // Pager Content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            beyondBoundsPageCount = 1,
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                snapPositionalThreshold = 0.35f
            )
        ) { page ->
            when (page) {
                0 -> PlayTab(
                    state = state,
                    onLetterClick = onLetterClick,
                    onDeleteClick = onDeleteClick,
                    onSubmitClick = onSubmitClick,
                    onShuffleClick = onShuffleClick
                )
                1 -> HintsTab(
                    puzzle = state.puzzle,
                    foundWords = state.gameState.foundWords,
                    hintState = state.gameState.hintState,
                    onToggleHintTier = onToggleHintTier,
                    definitionState = definitionState,
                    onFetchDefinition = onFetchDefinition,
                    onClearDefinition = onClearDefinition
                )
                2 -> WordsTab(
                    words = state.gameState.foundWords.toList().sorted(),
                    pangrams = state.puzzle.pangrams,
                    totalWords = state.puzzle.wordCount,
                    calculateScore = { state.puzzle.calculateWordScore(it) },
                    definitionState = definitionState,
                    onFetchDefinition = onFetchDefinition,
                    onClearDefinition = onClearDefinition
                )
            }
        }
    }
}

@Composable
private fun PlayTab(
    state: GameUiState.Playing,
    onLetterClick: (Char) -> Unit,
    onDeleteClick: () -> Unit,
    onSubmitClick: () -> Unit,
    onShuffleClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Score Display
        ScoreDisplay(
            currentScore = state.gameState.currentScore,
            maxScore = state.puzzle.maxScore,
            currentRank = state.gameState.currentRank,
            wordsFound = state.gameState.wordsFoundCount,
            totalWords = state.puzzle.wordCount
        )

        // Orbital Display
        OrbitalDisplay(
            centerLetter = state.puzzle.centerLetter,
            outerLetters = state.shuffledOuterLetters ?: state.puzzle.outerLetters,
            onLetterClick = onLetterClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        // Input Area
        InputArea(
            currentInput = state.currentInput,
            validationMessage = state.validationMessage,
            onDeleteClick = onDeleteClick,
            onSubmitClick = onSubmitClick,
            onRotateClick = onShuffleClick
        )
    }
}

@Composable
private fun WordsTab(
    words: List<String>,
    pangrams: Set<String>,
    totalWords: Int,
    calculateScore: (String) -> Int,
    definitionState: DefinitionResult,
    onFetchDefinition: (String) -> Unit,
    onClearDefinition: () -> Unit
) {
    var selectedWord by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Found ${ words.size } of $totalWords words",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FoundWordsList(
            words = words,
            pangrams = pangrams,
            onWordClick = { word ->
                selectedWord = word
                onFetchDefinition(word)
            },
            modifier = Modifier.weight(1f)
        )
    }

    // Definition dialog
    selectedWord?.let { word ->
        DefinitionDialog(
            word = word,
            isPangram = word in pangrams,
            score = calculateScore(word),
            definitionResult = definitionState,
            onDismiss = {
                selectedWord = null
                onClearDefinition()
            }
        )
    }
}

@Composable
private fun HintsTab(
    puzzle: Puzzle,
    foundWords: Set<String>,
    hintState: HintState,
    onToggleHintTier: (HintTier) -> Unit,
    definitionState: DefinitionResult,
    onFetchDefinition: (String) -> Unit,
    onClearDefinition: () -> Unit
) {
    HintsPanel(
        puzzle = puzzle,
        foundWords = foundWords,
        hintState = hintState,
        onToggleTier = onToggleHintTier,
        definitionState = definitionState,
        onFetchDefinition = onFetchDefinition,
        onClearDefinition = onClearDefinition
    )
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Oops!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) {
                Text("Try Again")
            }
        }
    }
}
