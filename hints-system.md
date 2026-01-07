# Hints System Specification

## Overview

The hint system provides progressive assistance through four tiers that unlock based on player progress. Each tier offers different types of information while maintaining the challenge of word discovery.

## Unlock Thresholds

| Tier | Name | Default Unlock | Description |
|------|------|----------------|-------------|
| 1 | Two-Letter List | Always (0%) | Shows word prefixes with counts |
| 2 | Grid View | 25% words found | Matrix of letters × lengths |
| 3 | Letter Reveal | 40% words found | Partial word reveals |
| 4 | Definition Hint | 60% words found | Definitions of unfound words |

**Calculation:**
```kotlin
fun calculateUnlockProgress(foundWords: Int, totalWords: Int): Float {
    return foundWords.toFloat() / totalWords.toFloat()
}

fun isHintUnlocked(tier: HintTier, progress: Float): Boolean {
    val threshold = when (tier) {
        HintTier.TWO_LETTER_LIST -> 0.0f
        HintTier.GRID_VIEW -> 0.25f
        HintTier.LETTER_REVEAL -> 0.40f
        HintTier.DEFINITION_HINT -> 0.60f
    }
    return progress >= threshold
}
```

## Tier 1: Two-Letter List

### Purpose
Show which two-letter prefixes exist in the puzzle and track progress for each prefix.

### Display Format

**When Expanded:**
```
Two-Letter List

BE- (2/4)      🟡
CA- (0/3)      ⚪
✓ DE- (7/7) ✓  ✅
FA- (1/2)      🟡
GA- (2/2) ✓    ✅
HE- (0/5)      ⚪
```

**Visual States:**
- ⚪ White/Default: 0 words found (0/N)
- 🟡 Yellow/Amber: Some words found (X/N where 0 < X < N)
- ✅ Green/Check: All words found (N/N)
- Completed items get strikethrough or fade

### Data Structure

```kotlin
data class TwoLetterHint(
    val prefix: String,              // "BE"
    val foundCount: Int,             // Number found
    val totalCount: Int,             // Total possible
    val isComplete: Boolean,         // foundCount == totalCount
    val words: Set<String>           // All words with this prefix (for validation)
)

fun generateTwoLetterList(
    puzzle: Puzzle,
    foundWords: Set<String>
): List<TwoLetterHint> {
    return puzzle.validWords
        .groupBy { it.take(2) }
        .map { (prefix, words) ->
            val found = words.count { it in foundWords }
            TwoLetterHint(
                prefix = prefix.uppercase(),
                foundCount = found,
                totalCount = words.size,
                isComplete = found == words.size,
                words = words.toSet()
            )
        }
        .sortedBy { it.prefix }
}
```

### UI Component

```kotlin
@Composable
fun TwoLetterListHint(
    hints: List<TwoLetterHint>,
    expanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Two-Letter List",
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand"
            )
        }
        
        // Content
        AnimatedVisibility(visible = expanded) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(hints) { hint ->
                    TwoLetterItem(hint)
                }
            }
        }
    }
}

@Composable
fun TwoLetterItem(hint: TwoLetterHint) {
    val color = when {
        hint.isComplete -> Color.Green
        hint.foundCount > 0 -> Color.Yellow
        else -> Color.Gray
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${hint.prefix}-",
            style = MaterialTheme.typography.bodyLarge.copy(
                textDecoration = if (hint.isComplete) TextDecoration.LineThrough else null,
                color = color
            )
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "(${hint.foundCount}/${hint.totalCount})",
                style = MaterialTheme.typography.bodyMedium
            )
            if (hint.isComplete) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Complete",
                    tint = Color.Green
                )
            }
        }
    }
}
```

### Update Logic

**Real-time updates when word is found:**
```kotlin
fun updateTwoLetterHints(
    currentHints: List<TwoLetterHint>,
    newlyFoundWord: String
): List<TwoLetterHint> {
    val prefix = newlyFoundWord.take(2).uppercase()
    
    return currentHints.map { hint ->
        if (hint.prefix == prefix && newlyFoundWord in hint.words) {
            hint.copy(
                foundCount = hint.foundCount + 1,
                isComplete = hint.foundCount + 1 == hint.totalCount
            )
        } else {
            hint
        }
    }
}
```

## Tier 2: Grid View

### Purpose
Show distribution of words by starting letter and length, with progress tracking for each cell.

### Display Format

```
Grid View

     B     C     D     E     F     G     H
4   2/2✓  0/1   -    2/3   1/1✓  -    0/1
5   1/1✓  1/2   0/1   -    0/2   0/1   -
6    -    0/1   2/3  1/1✓   -     -    0/2
7   1/1✓   -    0/1   -     -    1/1✓  -
```

**Cell States:**
- `-` = No words exist for this letter+length
- `0/N` = None found (white background)
- `X/N` = Partial (yellow background) where 0 < X < N
- `N/N✓` = Complete (green background)

### Data Structure

```kotlin
data class GridCell(
    val letter: Char,
    val length: Int,
    val foundCount: Int,
    val totalCount: Int,
    val isComplete: Boolean,
    val words: Set<String>
)

data class GridHint(
    val lengths: List<Int>,          // e.g., [4, 5, 6, 7, 8]
    val letters: List<Char>,         // e.g., [B, C, D, E, F, G, H]
    val grid: List<List<GridCell?>>  // Rows = lengths, Cols = letters
)

fun generateGridView(
    puzzle: Puzzle,
    foundWords: Set<String>
): GridHint {
    val allWords = puzzle.validWords
    val allLetters = (puzzle.outerLetters + puzzle.centerLetter).sorted()
    val lengths = allWords.map { it.length }.distinct().sorted()
    
    val grid = lengths.map { length ->
        allLetters.map { letter ->
            val cellWords = allWords.filter { 
                it.length == length && it.first() == letter
            }
            
            if (cellWords.isEmpty()) {
                null  // No words for this combination
            } else {
                val found = cellWords.count { it in foundWords }
                GridCell(
                    letter = letter,
                    length = length,
                    foundCount = found,
                    totalCount = cellWords.size,
                    isComplete = found == cellWords.size,
                    words = cellWords.toSet()
                )
            }
        }
    }
    
    return GridHint(
        lengths = lengths,
        letters = allLetters,
        grid = grid
    )
}
```

### UI Component

```kotlin
@Composable
fun GridViewHint(
    gridHint: GridHint,
    expanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Grid View", style = MaterialTheme.typography.titleMedium)
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null
            )
        }
        
        // Grid
        AnimatedVisibility(visible = expanded) {
            ScrollableGrid(gridHint)
        }
    }
}

@Composable
fun ScrollableGrid(gridHint: GridHint) {
    Column(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header row with letters
        Row {
            Box(modifier = Modifier.width(40.dp)) // Empty corner
            gridHint.letters.forEach { letter ->
                Box(
                    modifier = Modifier.width(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Divider()
        
        // Data rows
        gridHint.grid.forEachIndexed { rowIndex, row ->
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                // Length label
                Box(
                    modifier = Modifier.width(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = gridHint.lengths[rowIndex].toString(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Cells
                row.forEach { cell ->
                    GridCellView(cell)
                }
            }
        }
    }
}

@Composable
fun GridCellView(cell: GridCell?) {
    val backgroundColor = when {
        cell == null -> Color.LightGray
        cell.isComplete -> Color.Green.copy(alpha = 0.3f)
        cell.foundCount > 0 -> Color.Yellow.copy(alpha = 0.3f)
        else -> Color.White
    }
    
    val text = when {
        cell == null -> "-"
        cell.isComplete -> "${cell.totalCount}/${cell.totalCount}✓"
        else -> "${cell.foundCount}/${cell.totalCount}"
    }
    
    Box(
        modifier = Modifier
            .width(50.dp)
            .height(40.dp)
            .background(backgroundColor)
            .border(0.5.dp, Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp
        )
    }
}
```

## Tier 3: Letter Reveal

### Purpose
Show partial letters from a random unfound word to help players identify it.

### Display Format

```
Letter Reveal

_ A _ _ E L

[Show Another]
```

After clicking "Show Another":
```
C _ N _ _ E
```

### Data Structure

```kotlin
data class LetterReveal(
    val originalWord: String,
    val revealedPattern: String,     // "_ A _ _ E L"
    val revealCount: Int             // How many letters shown
)

fun generateLetterReveal(
    puzzle: Puzzle,
    foundWords: Set<String>,
    previousReveals: Set<String> = emptySet()
): LetterReveal? {
    // Get unfound words, excluding previously revealed
    val unfoundWords = (puzzle.validWords - foundWords - previousReveals).toList()
    if (unfoundWords.isEmpty()) return null
    
    // Prefer non-pangrams first (save pangrams for later)
    val word = unfoundWords
        .filterNot { it in puzzle.pangrams }
        .randomOrNull() 
        ?: unfoundWords.random()
    
    // Reveal 40-60% of letters
    val revealCount = (word.length * 0.5).toInt().coerceAtLeast(2)
    val revealPositions = word.indices.shuffled().take(revealCount).toSet()
    
    val pattern = word.mapIndexed { index, char ->
        if (index in revealPositions) char.uppercase() else '_'
    }.joinToString(" ")
    
    return LetterReveal(
        originalWord = word,
        revealedPattern = pattern,
        revealCount = revealCount
    )
}
```

### UI Component

```kotlin
@Composable
fun LetterRevealHint(
    currentReveal: LetterReveal?,
    onRequestNew: () -> Unit,
    onClearReveals: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Letter Reveal",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (currentReveal != null) {
            // Display revealed pattern
            Text(
                text = currentReveal.revealedPattern,
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRequestNew,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Show Another")
                }
                
                OutlinedButton(
                    onClick = onClearReveals,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear All")
                }
            }
        } else {
            Text(
                text = "No more words to reveal!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}
```

### State Management

```kotlin
// In ViewModel
private val _letterReveals = MutableStateFlow<List<LetterReveal>>(emptyList())
val letterReveals: StateFlow<List<LetterReveal>> = _letterReveals.asStateFlow()

private val revealedWords = mutableSetOf<String>()

fun requestNewLetterReveal() {
    val state = (_uiState.value as? GameUiState.Playing) ?: return
    val generator = hintGenerator ?: return
    
    val reveal = generator.generateLetterReveal(
        state.puzzle,
        state.gameState.foundWords,
        revealedWords
    )
    
    if (reveal != null) {
        revealedWords.add(reveal.originalWord)
        _letterReveals.value = _letterReveals.value + reveal
    }
}

fun clearLetterReveals() {
    revealedWords.clear()
    _letterReveals.value = emptyList()
}
```

## Tier 4: Definition Hint

### Purpose
Show the definition of a random unfound word without revealing the word itself.

### Display Format

```
Definition Hint

[Part of Speech: noun]

A small, round object with a hole through it, 
typically used in jewelry or decoration.

[Show Another Definition]
```

### Data Structure

```kotlin
data class DefinitionHint(
    val word: String,               // Hidden from user
    val definition: String,
    val partOfSpeech: String
)

fun generateDefinitionHint(
    puzzle: Puzzle,
    foundWords: Set<String>,
    definitions: Map<String, Definition>,
    previousHints: Set<String> = emptySet()
): DefinitionHint? {
    val unfoundWords = (puzzle.validWords - foundWords - previousHints).toList()
    if (unfoundWords.isEmpty()) return null
    
    // Try to find a word with a definition
    val candidates = unfoundWords.shuffled()
    
    for (word in candidates) {
        val def = definitions[word]
        if (def != null) {
            return DefinitionHint(
                word = word,
                definition = def.definition,
                partOfSpeech = def.partOfSpeech
            )
        }
    }
    
    return null  // No definitions available
}
```

### UI Component

```kotlin
@Composable
fun DefinitionHintView(
    currentHint: DefinitionHint?,
    onRequestNew: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Definition Hint",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (currentHint != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Part of speech badge
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = currentHint.partOfSpeech,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    
                    // Definition text
                    Text(
                        text = currentHint.definition,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            Button(
                onClick = onRequestNew,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Show Another Definition")
            }
        } else {
            Text(
                text = "No definitions available",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}
```

## Hint Panel State Management

### State Model

```kotlin
data class HintPanelState(
    val twoLetterExpanded: Boolean = false,
    val gridViewExpanded: Boolean = false,
    val letterRevealExpanded: Boolean = false,
    val definitionHintExpanded: Boolean = false,
    val letterReveals: List<LetterReveal> = emptyList(),
    val definitionHints: List<DefinitionHint> = emptyList()
)

sealed class HintPanelEvent {
    data class ToggleSection(val tier: HintTier) : HintPanelEvent()
    object RequestLetterReveal : HintPanelEvent()
    object ClearLetterReveals : HintPanelEvent()
    object RequestDefinitionHint : HintPanelEvent()
}
```

### ViewModel Integration

```kotlin
private val _hintPanelState = MutableStateFlow(HintPanelState())
val hintPanelState: StateFlow<HintPanelState> = _hintPanelState.asStateFlow()

fun handleHintEvent(event: HintPanelEvent) {
    when (event) {
        is HintPanelEvent.ToggleSection -> toggleHintSection(event.tier)
        is HintPanelEvent.RequestLetterReveal -> requestNewLetterReveal()
        is HintPanelEvent.ClearLetterReveals -> clearLetterReveals()
        is HintPanelEvent.RequestDefinitionHint -> requestNewDefinitionHint()
    }
}

private fun toggleHintSection(tier: HintTier) {
    _hintPanelState.update { state ->
        when (tier) {
            HintTier.TWO_LETTER_LIST -> state.copy(twoLetterExpanded = !state.twoLetterExpanded)
            HintTier.GRID_VIEW -> state.copy(gridViewExpanded = !state.gridViewExpanded)
            HintTier.LETTER_REVEAL -> state.copy(letterRevealExpanded = !state.letterRevealExpanded)
            HintTier.DEFINITION_HINT -> state.copy(definitionHintExpanded = !state.definitionHintExpanded)
        }
    }
}
```

## Persistence

Save hint panel state to maintain user preferences across sessions:

```kotlin
// In Room database
@Entity(tableName = "hint_states")
data class HintStateEntity(
    @PrimaryKey val puzzleId: String,
    val twoLetterExpanded: Boolean,
    val gridViewExpanded: Boolean,
    val letterRevealExpanded: Boolean,
    val definitionHintExpanded: Boolean,
    val revealedWords: String,      // JSON array
    val shownDefinitions: String     // JSON array
)
```

## Performance Optimizations

1. **Lazy generation**: Only generate hints when tier is unlocked
2. **Caching**: Cache grid and two-letter list, update incrementally
3. **Pagination**: For very large puzzles, paginate grid view
4. **Debouncing**: Debounce rapid hint requests

## Accessibility

- Screen reader announcements for unlocked hints
- Sufficient color contrast for hint states
- Keyboard navigation support
- Haptic feedback when hints unlock
