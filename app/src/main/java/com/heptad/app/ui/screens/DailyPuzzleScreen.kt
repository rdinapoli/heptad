package com.heptad.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import com.heptad.app.ui.viewmodels.DailyPuzzleUiState
import com.heptad.app.ui.viewmodels.DailyPuzzleViewModel
import kotlinx.coroutines.launch
import java.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyPuzzleScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DailyPuzzleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val definitionState by viewModel.definitionState.collectAsState()

    Scaffold(
        topBar = {
            DailyPuzzleTopBar(
                uiState = uiState,
                onBackClick = onNavigateBack,
                onSettingsClick = onNavigateToSettings
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is DailyPuzzleUiState.Loading -> {
                    LoadingContent()
                }
                is DailyPuzzleUiState.Playing -> {
                    DailyPlayingContent(
                        state = state,
                        onLetterClick = viewModel::addLetter,
                        onDeleteClick = viewModel::deleteLetter,
                        onSubmitClick = viewModel::submitWord,
                        onShuffleClick = viewModel::shuffleLetters,
                        onToggleHintTier = viewModel::toggleHintTier,
                        definitionState = definitionState,
                        onFetchDefinition = viewModel::fetchDefinition,
                        onClearDefinition = viewModel::clearDefinition,
                        onDismissCompletionDialog = viewModel::dismissCompletionDialog,
                        onBackToMenu = onNavigateBack
                    )
                }
                is DailyPuzzleUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onBack = onNavigateBack
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DailyPuzzleTopBar(
    uiState: DailyPuzzleUiState,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val puzzleNumber = (uiState as? DailyPuzzleUiState.Playing)?.puzzleNumber ?: 0
    val currentStreak = (uiState as? DailyPuzzleUiState.Playing)?.currentStreak ?: 0

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        title = {
            Text("Daily Puzzle #$puzzleNumber")
        },
        actions = {
            // Streak indicator
            if (currentStreak > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "$currentStreak",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
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
                text = "Loading today's puzzle...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun DailyPlayingContent(
    state: DailyPuzzleUiState.Playing,
    onLetterClick: (Char) -> Unit,
    onDeleteClick: () -> Unit,
    onSubmitClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onToggleHintTier: (HintTier) -> Unit,
    definitionState: DefinitionResult,
    onFetchDefinition: (String) -> Unit,
    onClearDefinition: () -> Unit,
    onDismissCompletionDialog: () -> Unit,
    onBackToMenu: () -> Unit
) {
    val tabs = listOf("Play", "Words (${state.gameState.wordsFoundCount})", "Hints")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    // Show completion dialog
    if (state.showCompletionDialog) {
        DailyCompletionDialog(
            puzzleNumber = state.puzzleNumber,
            rankAchieved = state.gameState.currentRank,
            wordsFound = state.gameState.wordsFoundCount,
            totalWords = state.puzzle.wordCount,
            currentStreak = state.currentStreak,
            timeUntilNext = state.timeUntilNextPuzzle,
            onDismiss = onDismissCompletionDialog,
            onBackToMenu = onBackToMenu
        )
    }

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
                0 -> DailyPlayTab(
                    state = state,
                    onLetterClick = onLetterClick,
                    onDeleteClick = onDeleteClick,
                    onSubmitClick = onSubmitClick,
                    onShuffleClick = onShuffleClick
                )
                1 -> DailyWordsTab(
                    words = state.gameState.foundWords.toList().sorted(),
                    pangrams = state.puzzle.pangrams,
                    totalWords = state.puzzle.wordCount,
                    calculateScore = { state.puzzle.calculateWordScore(it) },
                    definitionState = definitionState,
                    onFetchDefinition = onFetchDefinition,
                    onClearDefinition = onClearDefinition
                )
                2 -> DailyHintsTab(
                    puzzle = state.puzzle,
                    foundWords = state.gameState.foundWords,
                    currentScore = state.gameState.currentScore,
                    hintState = state.gameState.hintState,
                    onToggleHintTier = onToggleHintTier,
                    definitionState = definitionState,
                    onFetchDefinition = onFetchDefinition,
                    onClearDefinition = onClearDefinition
                )
            }
        }
    }
}

@Composable
private fun DailyPlayTab(
    state: DailyPuzzleUiState.Playing,
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

        // Countdown to next puzzle (shown at bottom)
        state.timeUntilNextPuzzle?.let { duration ->
            NextPuzzleCountdown(
                timeUntilNext = duration,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DailyWordsTab(
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
            text = "Found ${words.size} of $totalWords words",
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
private fun DailyHintsTab(
    puzzle: Puzzle,
    foundWords: Set<String>,
    currentScore: Int,
    hintState: HintState,
    onToggleHintTier: (HintTier) -> Unit,
    definitionState: DefinitionResult,
    onFetchDefinition: (String) -> Unit,
    onClearDefinition: () -> Unit
) {
    HintsPanel(
        puzzle = puzzle,
        foundWords = foundWords,
        currentScore = currentScore,
        hintState = hintState,
        onToggleTier = onToggleHintTier,
        definitionState = definitionState,
        onFetchDefinition = onFetchDefinition,
        onClearDefinition = onClearDefinition
    )
}

@Composable
private fun NextPuzzleCountdown(
    timeUntilNext: Duration,
    modifier: Modifier = Modifier
) {
    val hours = timeUntilNext.toHours()
    val minutes = (timeUntilNext.toMinutes() % 60)
    val seconds = (timeUntilNext.seconds % 60)

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Next puzzle in ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DailyCompletionDialog(
    puzzleNumber: Int,
    rankAchieved: com.heptad.app.data.models.Rank,
    wordsFound: Int,
    totalWords: Int,
    currentStreak: Int,
    timeUntilNext: Duration?,
    onDismiss: () -> Unit,
    onBackToMenu: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Puzzle #$puzzleNumber Complete!",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Rank achieved
                Text(
                    text = rankAchieved.displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = rankAchieved.getColor()
                )

                // Words found
                Text(
                    text = "$wordsFound / $totalWords words found",
                    style = MaterialTheme.typography.bodyLarge
                )

                // Streak update
                if (currentStreak > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$currentStreak day streak!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                // Countdown
                timeUntilNext?.let { duration ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Next puzzle in",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val hours = duration.toHours()
                    val minutes = (duration.toMinutes() % 60)
                    Text(
                        text = String.format("%02d:%02d", hours, minutes),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Continue Playing")
            }
        },
        dismissButton = {
            TextButton(onClick = onBackToMenu) {
                Text("Back to Menu")
            }
        }
    )
}

@Composable
private fun ErrorContent(
    message: String,
    onBack: () -> Unit
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
            Button(onClick = onBack) {
                Text("Back to Menu")
            }
        }
    }
}
