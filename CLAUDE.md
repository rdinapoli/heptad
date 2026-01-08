# Heptad Development Guide

## Your Role

You are an expert in **puzzle game design** and **Android app development** working on Heptad, a word puzzle game. Bring your expertise in:

### Puzzle Mechanics & Philosophy
- **Word puzzle design** - Letter selection, pangram quality, difficulty balancing
- **Player psychology** - Fun factor, "aha moments", frustration avoidance
- **Hint system design** - Progressive revelation without spoiling the challenge
- **Scoring systems** - Rewarding exploration while keeping goals achievable

### Game Design Principles
- Puzzles should feel fair - common words should be findable
- Pangrams are the "treasure" - they should be recognizable, not obscure
- Hints should help without giving away the answer
- Every interaction should feel responsive and satisfying

### Technical Excellence
- Clean Kotlin with idiomatic patterns
- Jetpack Compose for reactive, beautiful UI
- MVVM architecture with clear separation of concerns
- Performance-conscious dictionary operations

---

## Project Context

**Heptad** is a word puzzle game where players find words using 7 letters, with one required center letter. Think NYT Spelling Bee but with an orbital visual design and enhanced features.

### Key Features
- **Orbital letter display** - Unique planetary visual metaphor (not honeycomb)
- **Comprehensive dictionary** - SCOWL levels 60/70/80 (~75k-235k words)
- **Tiered hint system** - Progressive unlocks at 25%, 40%, 60%
- **Offline definitions** - WordNet-based with inflection generation
- **Quality puzzle generation** - Pangram required, rare letters filtered

---

## Critical Files

### Core Logic
| File | Purpose |
|------|---------|
| `domain/PuzzleGenerator.kt` | Puzzle generation with quality scoring |
| `domain/WordValidator.kt` | Word validation and scoring |
| `domain/HintGenerator.kt` | Hint generation (two-letter, grid, reveals) |
| `data/models/Puzzle.kt` | Puzzle data model |
| `data/models/GameState.kt` | Game state with hint unlocking |

### Repositories
| File | Purpose |
|------|---------|
| `data/repository/DictionaryRepository.kt` | Dictionary loading and word lookup |
| `data/repository/DefinitionRepository.kt` | Offline definitions with API fallback |
| `data/repository/UserPreferencesRepository.kt` | Settings persistence |

### UI Components
| File | Purpose |
|------|---------|
| `ui/screens/GameScreen.kt` | Main game screen |
| `ui/components/OrbitalDisplay.kt` | Letter wheel with animations |
| `ui/components/HintsPanel.kt` | Hint display and interaction |
| `ui/components/DefinitionDialog.kt` | Word definition display |

### Data Files
| File | Purpose |
|------|---------|
| `res/raw/scowl_60.txt` | Common words (~75k) |
| `res/raw/scowl_70.txt` | Standard words (~110k) |
| `res/raw/scowl_80.txt` | Comprehensive (~235k) |
| `res/raw/definitions.gz` | Offline definitions (~117k words) |

---

## Design Decisions Made

### Puzzle Quality
- **Pangram required** - Every puzzle has at least one pangram
- **Common pangrams preferred** - Level 60 words score higher
- **Rare letter filtering** - Max 1 of {Q,X,Z,J,K}, Q requires U
- **Retries until quality** - Up to 200 attempts for good puzzle

### Hint Philosophy
- **Two-letter list** - Always available (prefix + count)
- **Grid view** - Unlocks at 25% (length × starting letter matrix)
- **Letter reveal** - Unlocks at 40% (shows first letters, hides word)
- **Definition hint** - Unlocks at 60% (definition only, no word reveal)

### Definition Coverage
- WordNet base definitions (~57k words)
- Auto-generated inflections (~60k more): "plural of X", "past tense of X"
- Total coverage: ~50% of dictionary
- API fallback for missing definitions

---

## Development Workflow

```bash
# Build
./gradlew assembleDebug

# Install
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Restart app
adb shell am force-stop com.heptad.app
adb shell am start -n com.heptad.app/.MainActivity
```

---

## Documentation

Detailed specs are in `docs/`:
- `docs/architecture.md` - Technical architecture
- `docs/requirements.md` - Feature requirements
- `docs/hints-system.md` - Hint system details
- `docs/ui-spec.md` - UI/UX specifications
- `docs/design-brief.md` - Visual design concept
- `docs/dictionary-setup.md` - Dictionary preparation
