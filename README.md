# Heptad

A word puzzle game for Android featuring an orbital visual design and comprehensive dictionary support.

## About

Heptad challenges players to find words using 7 letters, where one center letter must appear in every word. Create 4+ letter words, discover pangrams (words using all 7 letters), and climb the ranks from Beginner to Queen Bee.

## Features

- **Orbital letter display** - Unique planetary visual design with smooth animations
- **Three dictionary levels** - Common (75k), Standard (110k), or Comprehensive (235k words)
- **Progressive hint system** - Unlocks as you progress: two-letter lists, grid view, letter reveals, and definition hints
- **Offline definitions** - Built-in dictionary with 117k word definitions
- **Quality puzzles** - Every puzzle has at least one recognizable pangram
- **Dark mode** - Full light/dark theme support
- **No ads, no tracking** - Personal use, privacy-focused

## Screenshots

<!-- TODO: Add screenshots -->

## Tech Stack

- **Kotlin** - 100% Kotlin codebase
- **Jetpack Compose** - Modern declarative UI
- **MVVM Architecture** - Clean separation of concerns
- **Hilt** - Dependency injection
- **DataStore** - Preferences persistence
- **Coroutines & Flow** - Async operations and reactive state

## Building

Requirements:
- Android Studio Hedgehog or newer
- JDK 17+
- Android SDK 34

```bash
# Clone the repository
git clone https://github.com/yourusername/heptad.git
cd heptad

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

## Project Structure

```
app/src/main/java/com/heptad/app/
├── data/
│   ├── models/          # Data classes (Puzzle, GameState, etc.)
│   └── repository/      # Data access (Dictionary, Definitions, Preferences)
├── domain/              # Business logic (PuzzleGenerator, WordValidator)
├── ui/
│   ├── screens/         # Full screen composables
│   ├── components/      # Reusable UI components
│   └── viewmodel/       # ViewModels
└── di/                  # Hilt modules
```

## Documentation

See the `docs/` folder for detailed specifications:
- [Architecture](docs/architecture.md) - Technical architecture
- [Requirements](docs/requirements.md) - Feature requirements
- [UI Specification](docs/ui-spec.md) - Design system
- [Hints System](docs/hints-system.md) - Hint mechanics

## License

Personal use. All rights reserved.

## Acknowledgments

- Dictionary data from [SCOWL](http://wordlist.aspell.net/)
- Definitions from [WordNet](https://wordnet.princeton.edu/)
- Inspired by NYT Spelling Bee
