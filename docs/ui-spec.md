# Heptad - UI/UX Specification

## Design Philosophy

**Goals:**
- Clean, minimal interface focused on gameplay
- Orbital/planetary visual theme emphasizing the "seven" concept
- Material Design 3 principles
- Responsive and intuitive interactions
- Satisfying feedback for achievements
- Accessibility-first approach

**Design Principles:**
- Form follows function
- Visual hierarchy guides attention
- Progressive disclosure of complexity
- Consistent interaction patterns
- Delightful micro-interactions

## Color Scheme

### Light Theme
```kotlin
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),           // Purple
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    
    secondary = Color(0xFF625B71),         // Gray-purple
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    
    tertiary = Color(0xFF7D5260),          // Rose
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    
    // Status colors
    error = Color(0xFFB3261E),
    success = Color(0xFF4CAF50),
    warning = Color(0xFFFF9800)
)
```

### Dark Theme
```kotlin
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    
    error = Color(0xFFF2B8B5),
    success = Color(0xFF81C784),
    warning = Color(0xFFFFB74D)
)
```

## Typography

```kotlin
val Typography = Typography(
    // Display - Large titles
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = 0.sp
    ),
    
    // Headline - Section headers
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    
    // Title - Card headers, important text
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    
    // Body - Main content
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    
    // Label - Buttons, small text
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)
```

## Screen Layouts

### Main Game Screen

```
┌─────────────────────────────────────┐
│  ☰                            ⚙️    │  Top Bar
│                                      │
│  156 / 287 points                    │  Score Display
│  [████████░░░░░░░] Nice!             │  Progress Bar
│  23 / 87 words                       │
│                                      │
│         Orbital Display              │
│              A                       │
│          B       E                   │  Orbital Letters
│             ⊕  ← D (center sun)      │  (tap to add)
│          F       C                   │
│              G                       │
│                                      │
│  [Rotate ↻]                          │  Action Button
│                                      │
│  ┌──────────────────────────┐       │  Input Area
│  │  BEADED_                 │       │
│  └──────────────────────────┘       │
│  [Delete]         [Enter]            │  Input Controls
│                                      │
│  Found Words (23)           [Hints]  │  Lists Header
│  ┌──────────────────────────┐       │
│  │ BEAD (4) ⓘ              │       │  Word List
│  │ BEADED (6) ⓘ            │       │  (scrollable)
│  │ CABBAGE (7) 🌟 ⓘ        │       │  (tap for def)
│  │ DECADE (6) ⓘ             │       │
│  │ ...                      │       │
│  └──────────────────────────┘       │
│                                      │
│  [New Puzzle]                        │  Bottom Actions
└─────────────────────────────────────┘
```

### Component Specifications

#### 1. Top App Bar

```kotlin
@Composable
fun GameTopBar(
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    TopAppBar(
        title = { Text("Heptad") },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, "Menu")
            }
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, "Settings")
            }
        }
    )
}
```

**Behavior:**
- Menu: Opens navigation drawer (future features)
- Settings: Opens settings screen
- Fixed at top, doesn't scroll

#### 2. Score Display

```kotlin
@Composable
fun ScoreDisplay(
    currentScore: Int,
    maxScore: Int,
    currentRank: Rank,
    wordsFound: Int,
    totalWords: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Score text
            Text(
                text = "$currentScore / $maxScore points",
                style = MaterialTheme.typography.titleLarge
            )
            
            // Progress bar
            LinearProgressIndicator(
                progress = currentScore.toFloat() / maxScore,
                modifier = Modifier.fillMaxWidth(),
                color = getRankColor(currentRank)
            )
            
            // Rank and word count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = currentRank.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = getRankColor(currentRank)
                )
                Text(
                    text = "$wordsFound / $totalWords words",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

fun getRankColor(rank: Rank): Color {
    return when (rank) {
        Rank.ADRIFT, Rank.TELLURIC -> Color(0xFF9E9E9E)
        Rank.ORBITAL, Rank.SELENIAN -> Color(0xFF2196F3)
        Rank.COMETARY, Rank.METEORIC -> Color(0xFFFF9800)
        Rank.STELLAR, Rank.NEBULAR -> Color(0xFF9C27B0)
        Rank.GALACTIC -> Color(0xFF5E35B1)
        Rank.UNIVERSAL -> Color(0xFFFFD700)
    }
}
```

**Layout:**
- Card with elevation
- Score fraction prominently displayed
- Visual progress bar with rank color
- Current rank name and word count

#### 3. Orbital Display

```kotlin
@Composable
fun OrbitalDisplay(
    centerLetter: Char,
    orbitalLetters: List<Char>,
    onLetterClick: (Char) -> Unit,
    rotationDegrees: Float = 0f,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        // Orbital ring/path visualization (optional)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.Gray.copy(alpha = 0.2f),
                radius = 150.dp.toPx(),
                style = Stroke(width = 2.dp.toPx())
            )
        }
        
        // Orbiting letters
        orbitalLetters.forEachIndexed { index, letter ->
            val baseAngle = (index * 60.0) // 360° / 6 letters
            val currentAngle = (baseAngle + rotationDegrees) * PI / 180.0
            val radius = 150.dp
            
            OrbitalLetterButton(
                letter = letter,
                onClick = { onLetterClick(letter) },
                modifier = Modifier
                    .offset(
                        x = (radius.value * cos(currentAngle)).dp,
                        y = (radius.value * sin(currentAngle)).dp
                    )
                    .size(60.dp)
            )
        }
        
        // Center "sun" - larger and more prominent
        CenterSunButton(
            letter = centerLetter,
            onClick = { onLetterClick(centerLetter) },
            modifier = Modifier.size(100.dp)
        )
    }
}

@Composable
fun CenterSunButton(
    letter: Char,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary),
        shadowElevation = 8.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            Text(
                text = letter.uppercase(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun OrbitalLetterButton(
    letter: Char,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
        shadowElevation = 4.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = letter.uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
```

**Interaction:**
- Each circle is tappable
- Center letter is larger and styled as a "sun" (glowing effect, radial gradient)
- Orbital letters are smaller "satellites" in circular arrangement
- Haptic feedback on tap
- Ripple effect on press
- Optional: Subtle continuous rotation animation
- Rotate button rotates all orbital letters clockwise by 60° (smooth animation)

#### 4. Input Area

```kotlin
@Composable
fun InputArea(
    currentInput: String,
    onSubmit: () -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    validationMessage: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Rotate button
        OutlinedButton(
            onClick = { /* rotate */ },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(Icons.Default.Refresh, null)
            Spacer(Modifier.width(8.dp))
            Text("Rotate ↻")
        }
        
        // Input display
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(
                2.dp,
                if (validationMessage != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        ) {
            Column {
                Text(
                    text = currentInput.uppercase().ifEmpty { " " },
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    minLines = 1
                )
                
                // Validation message
                if (validationMessage != null) {
                    Text(
                        text = validationMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            "point" in validationMessage -> MaterialTheme.colorScheme.success
                            "PANGRAM" in validationMessage -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                enabled = currentInput.isNotEmpty()
            ) {
                Icon(Icons.Default.Backspace, "Delete")
                Spacer(Modifier.width(4.dp))
                Text("Delete")
            }
            
            Button(
                onClick = onSubmit,
                modifier = Modifier.weight(1f),
                enabled = currentInput.length >= 4
            ) {
                Icon(Icons.Default.Check, "Submit")
                Spacer(Modifier.width(4.dp))
                Text("Enter")
            }
        }
    }
}
```

**Behavior:**
- Shows current input in large text
- Validation message below input (success/error colors)
- Rotate button rotates orbital letters clockwise
- Enter submits word (disabled if < 4 letters)

#### 5. Found Words List

```kotlin
@Composable
fun FoundWordsList(
    words: List<String>,
    pangrams: Set<String>,
    onWordClick: (String) -> Unit,
    onSortChange: (SortOption) -> Unit,
    currentSort: SortOption,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Header with sort options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Found Words (${words.size})",
                style = MaterialTheme.typography.titleMedium
            )
            
            // Sort dropdown
            SortDropdown(
                currentSort = currentSort,
                onSortChange = onSortChange
            )
        }
        
        Divider()
        
        // Word list
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(words) { word ->
                FoundWordItem(
                    word = word,
                    isPangram = word in pangrams,
                    onClick = { onWordClick(word) }
                )
            }
        }
    }
}

@Composable
fun FoundWordItem(
    word: String,
    isPangram: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = word.uppercase(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isPangram) FontWeight.Bold else FontWeight.Normal
                )
                
                if (isPangram) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Pangram",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Text(
                    text = "(${word.length})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Definition",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

enum class SortOption {
    ALPHABETICAL,
    LENGTH,
    SCORE,
    RECENT
}
```

**Features:**
- Scrollable list of found words
- Sort options: alphabetical, by length, by score, by recency
- Pangrams highlighted with star icon and bold text
- Each word shows length
- Info icon indicates tappable for definition
- Smooth animations for additions

#### 6. Definition Dialog

```kotlin
@Composable
fun DefinitionDialog(
    word: String,
    definition: Definition?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = word.uppercase(),
                    style = MaterialTheme.typography.titleLarge
                )
                if (definition != null) {
                    Text(
                        text = definition.partOfSpeech,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            Text(
                text = definition?.definition ?: "Definition not available",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
```

### Hints Screen

```
┌─────────────────────────────────────┐
│  ← Hints                      ✕     │
│                                      │
│  Progress: 23/87 words (26%)         │
│                                      │
│  ✓ Two-Letter List           ▼      │
│     BE- (2/4)                       │
│     CA- (0/3)                       │
│     ✓ DE- (7/7) ✓                   │
│     ...                              │
│                                      │
│  🔒 Grid View                        │
│     Unlocks at 22/87 (25%)          │
│                                      │
│  🔒 Letter Reveal                    │
│     Unlocks at 35/87 (40%)          │
│                                      │
│  🔒 Definition Hint                  │
│     Unlocks at 53/87 (60%)          │
└─────────────────────────────────────┘
```

**Implementation:** See `hints-system.md` for detailed specifications

### Settings Screen

```
┌─────────────────────────────────────┐
│  ← Settings                          │
│                                      │
│  DICTIONARY                          │
│  ┌─────────────────────────────┐   │
│  │ Difficulty Level            │   │
│  │ ○ Level 60 (~127k words)   │   │
│  │ ● Level 70 (~157k words)   │   │
│  │ ○ Level 80 (~216k words)   │   │
│  └─────────────────────────────┘   │
│                                      │
│  PUZZLE GENERATION                   │
│  ┌─────────────────────────────┐   │
│  │ Include letter 'S'    [ON]  │   │
│  └─────────────────────────────┘   │
│                                      │
│  APPEARANCE                          │
│  ┌─────────────────────────────┐   │
│  │ Theme                       │   │
│  │ ○ Light                     │   │
│  │ ○ Dark                      │   │
│  │ ● System Default            │   │
│  └─────────────────────────────┘   │
│                                      │
│  ADVANCED                            │
│  ┌─────────────────────────────┐   │
│  │ Hint Unlock Thresholds      │   │
│  │ Grid View:     25%  ▢▢▢▢▢  │   │
│  │ Letter Reveal: 40%  ▢▢▢▢▢  │   │
│  │ Definition:    60%  ▢▢▢▢▢  │   │
│  └─────────────────────────────┘   │
│                                      │
│  ABOUT                               │
│  Version 1.0.0                       │
│  Dictionary: SCOWL                   │
│                                      │
└─────────────────────────────────────┘
```

## Animations & Feedback

### Word Submission

**Success (Valid Word):**
```kotlin
val scale = remember { Animatable(1f) }
val alpha = remember { Animatable(1f) }

LaunchedEffect(word) {
    launch {
        scale.animateTo(
            targetValue = 1.2f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
        scale.animateTo(1f)
    }
    launch {
        delay(500)
        alpha.animateTo(0f, animationSpec = tween(300))
    }
}
```
- Scale animation (bounce)
- Green flash
- Haptic success feedback
- Word added to list with slide-in

**Pangram:**
```kotlin
val rotation = remember { Animatable(0f) }
val scale = remember { Animatable(1f) }

LaunchedEffect(Unit) {
    launch {
        rotation.animateTo(360f, animationSpec = tween(600))
    }
    launch {
        repeat(3) {
            scale.animateTo(1.3f, animationSpec = tween(100))
            scale.animateTo(1f, animationSpec = tween(100))
        }
    }
}
```
- Sparkle effect
- Rotation animation
- Stronger haptic
- Confetti particles (optional)

**Error (Invalid Word):**
```kotlin
val offsetX = remember { Animatable(0f) }

LaunchedEffect(error) {
    repeat(3) {
        offsetX.animateTo(10f, animationSpec = tween(50))
        offsetX.animateTo(-10f, animationSpec = tween(50))
    }
    offsetX.animateTo(0f)
}
```
- Shake animation
- Red flash
- Error haptic
- Error message display

### Rank Up

```kotlin
@Composable
fun RankUpAnimation(
    newRank: Rank,
    onComplete: () -> Unit
) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    
    LaunchedEffect(newRank) {
        launch {
            alpha.animateTo(1f, animationSpec = tween(300))
            delay(2000)
            alpha.animateTo(0f, animationSpec = tween(300))
            onComplete()
        }
        launch {
            scale.animateTo(1.2f, animationSpec = spring())
            scale.animateTo(1f, animationSpec = spring())
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha.value)
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.scale(scale.value)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = getRankColor(newRank)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Rank Up!",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = newRank.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = getRankColor(newRank)
                )
            }
        }
    }
}
```

### Hint Unlock

```kotlin
val shake = remember { Animatable(0f) }

LaunchedEffect(unlocked) {
    repeat(5) {
        shake.animateTo(5f, animationSpec = tween(50))
        shake.animateTo(-5f, animationSpec = tween(50))
    }
    shake.animateTo(0f)
}

// Badge notification
Box {
    HintIcon()
    if (newlyUnlocked) {
        Badge(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = shake.value.dp)
        ) {
            Text("NEW")
        }
    }
}
```

## Accessibility

### Screen Reader Support

```kotlin
@Composable
fun AccessibleHexagon(
    letter: Char,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.semantics {
            contentDescription = if (isPrimary) {
                "Center letter $letter, required in all words"
            } else {
                "Letter $letter"
            }
            role = Role.Button
        }
    ) {
        // Hexagon content
    }
}
```

### Focus Order

- Top to bottom, left to right
- Skip decorative elements
- Logical tab order
- Focus indicators visible

### Color Contrast

- WCAG AA minimum (4.5:1 for normal text)
- WCAG AAA preferred (7:1 for normal text)
- Test with color blindness simulators

### Touch Targets

- Minimum 48×48 dp
- Adequate spacing between targets
- Visual feedback on press

### Dynamic Type

```kotlin
@Composable
fun ScalableText(text: String) {
    val fontScale = LocalConfiguration.current.fontScale
    
    Text(
        text = text,
        fontSize = (16.sp * fontScale).coerceIn(12.sp, 24.sp)
    )
}
```

## Responsive Design

### Phone (Portrait)
- Single column layout
- Full-width components
- Scrollable content

### Phone (Landscape)
- Two-column when space permits
- Orbital display scaled appropriately
- Bottom sheet for hints

### Tablet
- Two-pane layout
- Hints panel alongside game
- Larger orbital display with more spacing

### Foldable
- Adaptive layout for different configurations
- Utilize extra screen space

## Loading States

```kotlin
@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text("Generating puzzle...")
        }
    }
}
```

## Error States

```kotlin
@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
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
```

## Testing Checklist

- [ ] All tappable areas ≥ 48dp
- [ ] Color contrast meets WCAG AA
- [ ] Screen reader descriptions accurate
- [ ] Animations respect reduced motion preference
- [ ] Keyboard navigation works
- [ ] Dark theme properly implemented
- [ ] Text scales with system settings
- [ ] Focus indicators visible
- [ ] Error messages clear and helpful
- [ ] Loading states don't block interaction
