package com.heptad.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heptad.app.data.models.HintState
import com.heptad.app.data.models.HintTier
import com.heptad.app.data.models.Puzzle
import com.heptad.app.data.repository.DefinitionResult
import com.heptad.app.domain.GridCell
import com.heptad.app.domain.HintGenerator
import com.heptad.app.domain.LetterRevealHint
import com.heptad.app.domain.TwoLetterHint

@Composable
fun HintsPanel(
    puzzle: Puzzle,
    foundWords: Set<String>,
    hintState: HintState,
    onToggleTier: (HintTier) -> Unit,
    definitionState: DefinitionResult,
    onFetchDefinition: (String) -> Unit,
    onClearDefinition: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hintGenerator = remember(puzzle) { HintGenerator(puzzle) }
    val progress = hintGenerator.getProgressPercentage(foundWords)
    val isDefinitionHintUnlocked = hintState.isTierUnlocked(HintTier.DEFINITION_HINT)

    // Track selected word for definition dialog
    var selectedHintWord by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Progress indicator
        ProgressHeader(
            wordsFound = foundWords.size,
            totalWords = puzzle.wordCount,
            progress = progress
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tier 1: Two-Letter List (Always available)
        HintTierCard(
            tier = HintTier.TWO_LETTER_LIST,
            isUnlocked = true,
            isExpanded = hintState.twoLetterExpanded,
            onToggle = { onToggleTier(HintTier.TWO_LETTER_LIST) },
            progress = progress
        ) {
            TwoLetterHintContent(
                hints = hintGenerator.generateTwoLetterHints(foundWords)
            )
        }

        // Tier 2: Grid View (25%)
        HintTierCard(
            tier = HintTier.GRID_VIEW,
            isUnlocked = hintState.isTierUnlocked(HintTier.GRID_VIEW),
            isExpanded = hintState.gridViewExpanded,
            onToggle = { onToggleTier(HintTier.GRID_VIEW) },
            progress = progress
        ) {
            GridViewHintContent(
                cells = hintGenerator.generateGridHints(foundWords),
                lengths = hintGenerator.getWordLengths(),
                letters = hintGenerator.getStartingLetters()
            )
        }

        // Tier 3: Letter Reveal (40%)
        HintTierCard(
            tier = HintTier.LETTER_REVEAL,
            isUnlocked = hintState.isTierUnlocked(HintTier.LETTER_REVEAL),
            isExpanded = hintState.letterRevealExpanded,
            onToggle = { onToggleTier(HintTier.LETTER_REVEAL) },
            progress = progress
        ) {
            LetterRevealHintContent(
                hints = hintGenerator.generateLetterRevealHints(foundWords, revealCount = 2),
                isClickable = isDefinitionHintUnlocked,
                onHintClick = { word ->
                    selectedHintWord = word
                    onFetchDefinition(word)
                }
            )
        }

        // Tier 4: Definition Hint (60%)
        HintTierCard(
            tier = HintTier.DEFINITION_HINT,
            isUnlocked = hintState.isTierUnlocked(HintTier.DEFINITION_HINT),
            isExpanded = hintState.definitionHintExpanded,
            onToggle = { onToggleTier(HintTier.DEFINITION_HINT) },
            progress = progress
        ) {
            DefinitionHintContent(
                remainingCount = hintGenerator.getRemainingWordCount(foundWords),
                isUnlocked = isDefinitionHintUnlocked
            )
        }
    }

    // Definition dialog for clicked hint words
    selectedHintWord?.let { word ->
        DefinitionDialog(
            word = word,
            isPangram = word in puzzle.pangrams,
            score = puzzle.calculateWordScore(word),
            definitionResult = definitionState,
            onDismiss = {
                selectedHintWord = null
                onClearDefinition()
            }
        )
    }
}

@Composable
private fun ProgressHeader(
    wordsFound: Int,
    totalWords: Int,
    progress: Float
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progress",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "$wordsFound / $totalWords words (${(progress * 100).toInt()}%)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun HintTierCard(
    tier: HintTier,
    isUnlocked: Boolean,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    progress: Float,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isUnlocked) { onToggle() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isUnlocked) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = tier.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isUnlocked)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!isUnlocked) {
                            Text(
                                text = "Unlocks at ${tier.getUnlockPercentage()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (isUnlocked) {
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }

            // Content
            AnimatedVisibility(
                visible = isUnlocked && isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TwoLetterHintContent(hints: List<TwoLetterHint>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        hints.forEach { hint ->
            TwoLetterChip(hint)
        }
    }
}

@Composable
private fun TwoLetterChip(hint: TwoLetterHint) {
    val backgroundColor = when {
        hint.isComplete -> MaterialTheme.colorScheme.primaryContainer
        hint.foundCount > 0 -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = hint.prefix,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (hint.isComplete) "✓" else "${hint.foundCount}/${hint.totalCount}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GridViewHintContent(
    cells: List<GridCell>,
    lengths: List<Int>,
    letters: List<Char>
) {
    val cellMap = cells.associateBy { "${it.letter}-${it.length}" }

    Column(
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        // Header row with lengths
        Row {
            // Empty corner cell
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .padding(2.dp)
            )
            lengths.forEach { length ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = length.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Data rows
        letters.forEach { letter ->
            Row {
                // Row header (letter)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Data cells
                lengths.forEach { length ->
                    val cell = cellMap["$letter-$length"]
                    GridCellView(cell)
                }
            }
        }
    }
}

@Composable
private fun GridCellView(cell: GridCell?) {
    val backgroundColor = when {
        cell == null -> MaterialTheme.colorScheme.surface
        cell.isComplete -> MaterialTheme.colorScheme.primaryContainer
        cell.foundCount > 0 -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .then(
                if (cell != null)
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (cell != null) {
            Text(
                text = if (cell.isComplete) "✓" else cell.remainingCount.toString(),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LetterRevealHintContent(
    hints: List<LetterRevealHint>,
    isClickable: Boolean = false,
    onHintClick: (String) -> Unit = {}
) {
    if (hints.isEmpty()) {
        Text(
            text = "All words found!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "${hints.size} words remaining",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isClickable) {
            Text(
                text = "Tap a word to see its definition",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Group by prefix for cleaner display
        hints.groupBy { it.partialWord.take(2) }.forEach { (prefix, groupedHints) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$prefix:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(36.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    groupedHints.forEach { hint ->
                        val displayText = if (hint.isPangram) "[${hint.partialWord}]" else hint.partialWord
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = if (isClickable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = if (isClickable) {
                                Modifier.clickable { onHintClick(hint.fullWord) }
                            } else {
                                Modifier
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "[brackets] = pangram",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DefinitionHintContent(
    remainingCount: Int,
    isUnlocked: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (remainingCount == 0) {
            Text(
                text = "All words found!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        } else if (isUnlocked) {
            Text(
                text = "You can now tap on words in the Letter Reveal hint above to see their definitions!",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "$remainingCount words remaining to find.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "Unlock this tier to tap on unfound words and see their definitions.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "$remainingCount words remaining to find.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
