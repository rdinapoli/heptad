# Heptad - Android Word Game - Development Guide

## Project Overview

Build a personal-use Android word game called "Heptad" featuring a unique orbital/planetary visual design and enhanced gameplay including:
- Comprehensive American English dictionary with rare/scientific words
- Tiered hint system with progressive unlocking
- Interactive progress tracking on hints
- Word definitions for all found words
- Difficulty customization via dictionary levels

## Technology Stack

**Primary Stack:**
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Architecture:** MVVM (Model-View-ViewModel)
- **Build Tool:** Gradle
- **Minimum SDK:** Android 8.0 (API 26)
- **Target SDK:** Latest stable

**Key Libraries:**
- Jetpack Compose for UI
- Room Database for local storage (puzzle history, user progress)
- Kotlin Coroutines for async operations
- ViewModel and StateFlow for state management
- Gson or Kotlinx Serialization for JSON handling

## Development Priorities

### Phase 1: Core Game Mechanics (MVP)
1. Load and parse SCOWL dictionary (level 70)
2. Implement puzzle generation algorithm
3. Word validation logic
4. Basic Jetpack Compose UI with:
   - Orbital/planetary letter display
   - Input handling
   - Word list display
   - Score tracking
5. Game state persistence

### Phase 2: Hint System
1. Two-letter list generation with progress tracking
2. Grid view with cell completion tracking
3. Letter reveal functionality
4. Definition hint system
5. Milestone-based hint unlocking (25%, 40%, 60%)

### Phase 3: Dictionary Integration
1. Package WordNet or Wiktionary definitions
2. Implement definition lookup for found words
3. Clickable word list with definition display
4. Definition hints for unfound words

### Phase 4: Settings & Polish
1. SCOWL difficulty level selection (60, 70, 80)
2. "Include S" toggle for puzzle generation
3. Dark mode support
4. Statistics tracking
5. Animations and haptic feedback
6. Daily puzzle mode (optional)

## Project Structure

```
app/
├── src/main/
│   ├── java/com/yourname/heptad/
│   │   ├── data/
│   │   │   ├── models/
│   │   │   │   ├── Puzzle.kt
│   │   │   │   ├── GameState.kt
│   │   │   │   └── HintState.kt
│   │   │   ├── repository/
│   │   │   │   ├── DictionaryRepository.kt
│   │   │   │   └── PuzzleRepository.kt
│   │   │   └── database/
│   │   │       ├── AppDatabase.kt
│   │   │       └── PuzzleDao.kt
│   │   ├── domain/
│   │   │   ├── PuzzleGenerator.kt
│   │   │   ├── WordValidator.kt
│   │   │   └── HintGenerator.kt
│   │   ├── ui/
│   │   │   ├── screens/
│   │   │   │   ├── GameScreen.kt
│   │   │   │   ├── HintsScreen.kt
│   │   │   │   └── SettingsScreen.kt
│   │   │   ├── components/
│   │   │   │   ├── OrbitalLetters.kt
│   │   │   │   ├── WordList.kt
│   │   │   │   ├── HintPanel.kt
│   │   │   │   └── ScoreDisplay.kt
│   │   │   └── viewmodels/
│   │   │       └── GameViewModel.kt
│   │   └── MainActivity.kt
│   ├── res/
│   │   ├── raw/
│   │   │   ├── scowl_words.txt
│   │   │   └── definitions.json
│   │   └── values/
│   │       ├── strings.xml
│   │       ├── colors.xml
│   │       └── themes.xml
│   └── AndroidManifest.xml
```

## Key Implementation Notes

### Dictionary Loading
- Load SCOWL word list from `res/raw/` on first launch
- Filter words: 4-15 letters, no hyphens/apostrophes, lowercase
- Store in memory as HashSet for O(1) lookups
- Separate definitions file (JSON format)

### Puzzle Generation
- Select 7 unique letters (exclude S if setting disabled)
- Try each letter as center, calculate valid words
- Accept puzzles with:
  - 20-100 valid words
  - At least 1 pangram
  - Good point range (50-250 points)
- Cache generated puzzles to avoid regeneration

### Scoring Rules
- 4-letter words: 1 point
- 5+ letter words: 1 point per letter
- Pangrams: word score + 7 bonus points
- Ranks: Beginner (0%), Good Start (5%), Moving Up (10%), Good (20%), 
  Solid (30%), Nice (40%), Great (50%), Amazing (60%), Genius (70%), Queen Bee (100%)

### State Management
- Use ViewModel with StateFlow for reactive UI
- Persist game state to Room database
- Save/restore on app lifecycle events

### Hint Unlocking
- Two-letter list: Always available
- Grid view: Unlocks at 25% words found
- Letter reveal: Unlocks at 40% words found
- Definition hint: Unlocks at 60% words found

### Progressive Marking
- Track found/total for each two-letter prefix
- Track found/total for each grid cell (letter × length)
- Update in real-time as words are found
- Visual indicators: ✓ for complete, counts for partial

## Testing Strategy

1. Unit tests for puzzle generation logic
2. Unit tests for word validation
3. UI tests for basic gameplay flow
4. Manual testing for hint system
5. Dictionary completeness validation

## Build Instructions

```bash
# Clone/create project
# Open in Android Studio
# Sync Gradle
./gradlew assembleDebug

# Run on device/emulator
./gradlew installDebug
```

## Configuration Files Needed

See the following companion files:
- `requirements.md` - Detailed feature requirements
- `architecture.md` - Technical architecture details
- `dictionary-setup.md` - Dictionary preparation instructions
- `hints-system.md` - Hint system specifications
- `ui-spec.md` - UI/UX design specifications

## Development Notes

- Prioritize clean, readable code over premature optimization
- Use Kotlin idioms (data classes, sealed classes, extension functions)
- Follow Material Design 3 guidelines for Compose UI
- Add TODO comments for future enhancements
- Keep game logic separate from UI code

## Future Enhancements (Post-MVP)

- Statistics dashboard (words found over time, favorite letters, etc.)
- Puzzle sharing functionality
- Custom puzzle creation
- Achievement system
- Export/import game history
- Accessibility features (screen reader support, larger text)
