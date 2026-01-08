# Daily Puzzle Mode - Planning Document

## Overview

Add a daily puzzle feature where all players solve the same puzzle each day, creating shared experience and habit-forming gameplay.

---

## Core Design Decisions

### Puzzle Selection Strategy

| Option | Pros | Cons |
|--------|------|------|
| **Pre-generated seed list** | Quality control, curated experience | Requires maintenance, finite supply |
| **Deterministic generation** | Infinite puzzles, no maintenance | Can't guarantee quality |
| **Server-based** | Enables leaderboards, community stats | Infrastructure complexity, requires internet |

**Recommendation**: Pre-generate 100+ curated puzzles, fall back to deterministic generation using date as seed.

**Questions to resolve**:
- [ ] How many puzzles to pre-generate for launch?
- [ ] What quality threshold for daily puzzles vs random?
- [ ] Should daily puzzles be harder than random mode?

### Puzzle Quality Criteria for Dailies

Consider stricter requirements than random mode:
- [ ] Minimum 2 pangrams?
- [ ] Higher minimum word count (e.g., 30+ valid words)?
- [ ] Prefer interesting center letters over vowels?
- [ ] Seasonal/thematic puzzles for holidays?

### Timezone Handling

| Approach | Used By | Trade-offs |
|----------|---------|------------|
| Midnight local time | Simpler UX | Timezone travel breaks experience |
| Midnight EST/UTC | NYT Games | Consistent but arbitrary for non-US |
| Rolling 24h window | - | Complex, confusing |

**Questions to resolve**:
- [ ] Which timezone approach?
- [ ] What happens when user crosses timezones mid-streak?
- [ ] Allow playing "yesterday's" puzzle if missed?

---

## Streak System

### Basic Mechanics
- [ ] Define what counts as "completing" daily (any words? reach a rank? find pangram?)
- [ ] Streak display location (main menu? game screen? both?)
- [ ] Streak milestone celebrations (7 days, 30 days, 100 days, 365 days)

### Streak Protection
Options to prevent rage-quit from broken streaks:
- [ ] Weekly "freeze" - one free pass per week
- [ ] Earned freezes through achievements
- [ ] Grace period (complete yesterday's puzzle today)
- [ ] No protection (purist approach)

**Questions to resolve**:
- [ ] How forgiving should streak system be?
- [ ] Should freeze be automatic or manually activated?

---

## Navigation & UX

### Menu Structure
```
Main Menu:
├── Daily Puzzle
│   ├── Today's Puzzle (with streak badge)
│   ├── Yesterday (if missed, grace period)
│   └── Archive (past dailies)
├── Random Puzzle (existing)
└── Settings
```

### Daily Puzzle Screen Additions
- [ ] "Puzzle #147" identifier
- [ ] Estimated difficulty indicator before starting?
- [ ] Time until next puzzle countdown
- [ ] Streak display

### Completion Screen
- [ ] Clear "You finished today!" celebration
- [ ] Stats summary (words found, rank achieved, hints used)
- [ ] Share button
- [ ] "Come back tomorrow" messaging
- [ ] Streak update animation

---

## Sharing Feature

### Share Format Design
```
Heptad #147 🌟
⭐ 42 words · 🎯 Genius
🔷 Found both pangrams!

heptad.app/daily
```

**Critical**: No spoilers - don't reveal letters or words.

**Questions to resolve**:
- [ ] What stats to include in share?
- [ ] Emoji style (minimal vs expressive)?
- [ ] Include app link?
- [ ] Platform-specific formatting (Twitter vs text)?

---

## Archive System

### Access Model
- [ ] Free access to all past puzzles?
- [ ] Limited access (last 7 days free)?
- [ ] Unlock through play (complete today's to unlock archive)?

### Archive Stats
- [ ] Track separately from daily stats?
- [ ] Show completion percentage of archive?
- [ ] Allow replaying completed archive puzzles?

---

## Data & Persistence

### Local Storage Needed
```kotlin
data class DailyPuzzleData(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastCompletedDate: LocalDate?,
    val freezesAvailable: Int,
    val completedPuzzleIds: Set<Int>,
    val dailyStats: Map<Int, DailyPuzzleStats>
)

data class DailyPuzzleStats(
    val puzzleId: Int,
    val completedAt: Instant,
    val wordsFound: Int,
    val rankAchieved: String,
    val hintsUsed: Int,
    val foundPangrams: Boolean
)
```

### Questions to resolve
- [ ] Use existing UserPreferencesRepository or new DailyPuzzleRepository?
- [ ] DataStore vs Room for daily puzzle data?
- [ ] Backup/restore considerations?

---

## Future Enhancements (Post-Launch)

### Community Features (requires backend)
- [ ] "X% of players reached Genius today"
- [ ] Global/friend leaderboards
- [ ] Achievement unlocks

### Additional Features
- [ ] Android widget showing streak/daily status
- [ ] Notification reminder (opt-in)
- [ ] Cross-device sync
- [ ] "Hint penalty" tracking for competitive players
- [ ] Weekly challenge (complete all 7 for bonus)
- [ ] Difficulty ratings based on community performance

---

## Implementation Phases

### Phase 1: MVP
- [ ] Daily puzzle selection (pre-generated list)
- [ ] Basic streak tracking
- [ ] Completion celebration
- [ ] Simple share format

### Phase 2: Polish
- [ ] Archive access
- [ ] Streak freezes
- [ ] Enhanced sharing
- [ ] Streak milestones

### Phase 3: Community (optional)
- [ ] Backend integration
- [ ] Community stats
- [ ] Leaderboards

---

## Open Questions Summary

1. **Quality**: What makes a puzzle "daily-worthy" vs random?
2. **Timezone**: Local midnight or fixed timezone?
3. **Completion**: What defines "done" for the day?
4. **Streaks**: How forgiving? Freezes or no?
5. **Archive**: Free or gated? Stats separate?
6. **Sharing**: What info to include without spoilers?
7. **Storage**: Extend existing repos or create new?

---

## References

- NYT Spelling Bee daily model
- Wordle sharing format innovation
- Duolingo streak/freeze psychology
