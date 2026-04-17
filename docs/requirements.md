# Heptad - Detailed Requirements

## Game Rules

### Core Mechanics
1. **Seven Letters (Heptad)**: One center letter (mandatory, displayed as sun/planet) + 6 orbital letters
2. **Word Formation**: Words must:
   - Be 4+ letters long
   - Use the center letter at least once
   - Only use the 7 provided letters (can repeat letters)
3. **Scoring**:
   - 4-letter words: 1 point
   - 5+ letter words: 1 point per letter
   - Pangram (uses all 7 letters): word score + 7 bonus points
4. **Win Condition**: Find all words to achieve "Universal" rank

### Valid Words
- Must exist in SCOWL dictionary (user-selected level: 60, 70, or 80)
- American English spelling only
- No proper nouns, no hyphenated words, no contractions
- Include scientific, technical, and rare words

## Puzzle Generation

### Requirements
1. Generate puzzles using 7 unique letters
2. **S Toggle**: Settings option to include/exclude letter 'S'
   - Default: S allowed
   - When disabled: Never select S as any of the 7 letters
3. **Quality Criteria**:
   - Minimum 20 valid words
   - Maximum ~100 words (avoid overwhelming puzzles)
   - At least 1 pangram (preferably 1-3)
   - Score range: 50-250 points
   - Avoid extremely rare letter combinations (Q without U, all uncommon letters)
4. **Letter Selection**:
   - Each of 7 letters must contribute meaningfully to valid words
   - Test each letter as center, choose best distribution

### Algorithm
```
1. Select 7 random unique letters (respect S toggle)
2. For each letter as potential center:
   a. Find all valid words using that center + other 6
   b. Calculate total points, count words, find pangrams
3. Select combination that meets quality criteria
4. If no valid puzzle after N attempts, retry with new letters
```

## Hint System

### Tier 1: Two-Letter List (Always Available)
**Display Format:**
```
BE- (2/4)      [2 found, 2 remaining]
CA- (0/3)      [0 found, 3 remaining]
✓ DE- (7/7) ✓  [All found - marked complete]
FA- (1/2)      [1 found, 1 remaining]
```

**Requirements:**
- Show all possible two-letter prefixes that have valid words
- Display as: `PREFIX- (found/total)`
- Mark complete prefixes with ✓ and visual styling (strikethrough/gray)
- Sort alphabetically
- Update in real-time as words are found
- Only show prefixes that exist in the puzzle (don't show empty combinations)

### Tier 2: Grid View (Unlocks at 25% words found)
**Display Format:**
```
Len | A     | B     | C     | D     | E     | F     | G  
----|-------|-------|-------|-------|-------|-------|-------
4   | 2/2 ✓ | 0/1   | -     | 2/3   | 1/1 ✓ | -     | 0/1
5   | 1/1 ✓ | 1/2   | 0/1   | -     | 0/2   | 0/1   | -
6   | -     | 0/1   | 2/3   | 1/1 ✓ | -     | -     | 0/2
7   | 1/1 ✓ | -     | 0/1   | -     | -     | 1/1 ✓ | -
```

**Requirements:**
- Rows: Word lengths (4 to max length in puzzle)
- Columns: All 7 letters (center + outer)
- Cells: `found/total` for that length + starting letter
- `-` for impossible combinations
- ✓ for completed cells
- Color coding:
  - Green/checkmark: All found (2/2)
  - Yellow/amber: Partial (1/3)
  - White/default: None found (0/2)
  - Gray: No words exist (-)
- Update in real-time

### Tier 3: Letter Reveal (Unlocks at 40% words found)
**Functionality:**
- Display a random unfound word with some letters revealed
- Format: `_ A _ _ E L` or `C _ N _ _ E`
- Each click reveals 2-3 letters from a random unfound word
- Cycle through different words on repeated clicks
- No limit on number of reveals
- Don't reveal pangrams first (save those for last if possible)

**Requirements:**
- Random selection of unfound words
- Random letter positions revealed (but keep it readable)
- Clear visual display with blanks vs. revealed letters
- Button to get next reveal

### Tier 4: Definition Hint (Unlocks at 60% words found)
**Functionality:**
- Show definition of a random unfound word
- Don't show the word itself
- Format:
  ```
  Definition Hint:
  "A small, round object used for decoration or jewelry"
  
  [Next Definition]
  ```

**Requirements:**
- Pull from definitions database
- Random selection of unfound words
- Prioritize interesting/rare words
- Clear, readable definition format
- Button to cycle to next definition

## Definition System

### Found Words - Clickable Definitions
**Display:**
```
Your Words (23/87) - 156 points

BEAD (4)           ⓘ [tap for definition]
BEADED (6)         ⓘ
CABBAGE (7) 🌟     ⓘ [pangram indicator]
DECADE (6)         ⓘ
```

**Requirements:**
- Each found word is clickable/tappable
- Tap shows definition overlay or inline expansion
- Definition format:
  ```
  BEAD (noun)
  A small, round object with a hole through it, 
  used for making jewelry or decoration
  ```
- Include word class (noun, verb, adjective, etc.)
- Smooth animation for showing/hiding
- Close button or tap-outside-to-dismiss

### Definition Database
- JSON format: `{"word": "bead", "definition": "...", "part_of_speech": "noun"}`
- Package with app for offline access
- Source: WordNet or Wiktionary
- Fallback: "Definition not available" if missing

## Settings

### Dictionary Level
- **Options**: SCOWL Level 60, 70, or 80
  - Level 60: ~127,000 words (common + some rare)
  - Level 70: ~157,000 words (includes more scientific/technical)
  - Level 80: ~216,000 words (very comprehensive)
- **Default**: Level 70
- **Effect**: Changes which words are valid for puzzle generation
- **Note**: Changing level requires generating new puzzle

### Include Letter S
- **Options**: On / Off
- **Default**: On
- **Effect**: When off, letter S never appears in generated puzzles
- **Rationale**: S makes puzzles easier (plurals)

### Theme
- Light mode
- Dark mode
- System default (recommended)

### Hint Unlock Thresholds (Optional Advanced Setting)
- Allow customization of unlock percentages
- Defaults: 25%, 40%, 60%
- Range: 10% - 80% for each tier

## User Interface

### Main Game Screen

**Components:**
1. **Header**
   - Score display: "156 / 287 points"
   - Progress bar to next rank
   - Current rank: "Meteoric" or "Nebular" etc.
   - Words found: "23 / 87"

2. **Orbital Display**
   - Center "sun/planet": Mandatory letter (visually distinct - larger, glowing, different color)
   - 6 orbiting "satellites": Optional letters arranged in a circle
   - All letters tappable to add letter to input
   - Rotation button: Rotates orbital letters clockwise (smooth animation)
   - Optional: Subtle continuous rotation animation for visual interest

3. **Input Area**
   - Current word being typed
   - Enter button (or tap/swipe gesture)
   - Delete/backspace button
   - Clear button

4. **Found Words List**
   - Scrollable list of found words
   - Sorted options: alphabetical, by length, by score, by recency
   - Each word shows:
     - The word
     - Points earned
     - Pangram indicator (🌟 or special styling)
     - Tap for definition

5. **Action Buttons**
   - Rotate (rotate orbital letters clockwise)
   - Hints (opens hint panel)
   - New Puzzle (with confirmation dialog)
   - Settings

### Hint Panel

**Layout:**
```
┌─────────────────────────────────────┐
│ HINTS                               │
│ Progress: 23/87 words (26%)         │
├─────────────────────────────────────┤
│ ✓ Two-Letter List                  │
│   [Tap to expand/collapse]         │
│   BE- (2/4)                        │
│   CA- (0/3)                        │
│   ...                              │
├─────────────────────────────────────┤
│ 🔒 Grid View                        │
│    Unlocks at 22/87 words (25%)    │
├─────────────────────────────────────┤
│ 🔒 Letter Reveal                    │
│    Unlocks at 35/87 words (40%)    │
├─────────────────────────────────────┤
│ 🔒 Definition Hint                  │
│    Unlocks at 53/87 words (60%)    │
└─────────────────────────────────────┘
```

**Behavior:**
- Expand/collapse sections
- Show unlock progress for locked tiers
- Visual feedback when tier unlocks
- Persistent state (remembers which sections are expanded)

### Settings Screen

**Options:**
- Dictionary Level: Dropdown (60, 70, 80)
- Include Letter S: Toggle
- Theme: Radio buttons (Light, Dark, System)
- Hint Thresholds: Advanced section (optional)
- About: App version, credits, dictionary source

### Feedback & Animations

**Visual Feedback:**
- Word accepted: Green flash, add to list with animation
- Word rejected: Red shake, error message
  - "Too short" (< 4 letters)
  - "Missing center letter"
  - "Not in word list"
  - "Already found"
- Pangram found: Special celebration animation
- Rank up: Badge animation, haptic feedback
- Hint unlocked: Notification badge, unlock animation

**Haptic Feedback:**
- Letter tap: Light haptic
- Word accepted: Medium haptic
- Word rejected: Error haptic
- Pangram found: Success pattern
- Rank up: Success pattern

## Data Persistence

### Save Game State
- Current puzzle configuration
- Found words
- Current score
- Hint states (which hints used, which sections expanded)
- Time spent on puzzle
- Auto-save on every word found

### Statistics (Future)
- Total words found
- Total points earned
- Puzzles completed
- Average words per puzzle
- Favorite letters
- Longest word found
- Most pangrams in one puzzle

## Edge Cases & Error Handling

1. **Invalid Dictionary File**: Show error, prevent app crash
2. **No Valid Puzzle After N Attempts**: Inform user, offer to retry
3. **Definition Not Found**: Show "Definition unavailable"
4. **Memory Issues**: Lazy load definitions, paginate word lists if needed
5. **Empty Found Words**: Encourage user, show tutorial
6. **All Words Found**: Celebration screen, offer new puzzle

## Accessibility

- Content descriptions for screen readers
- Sufficient color contrast (WCAG AA)
- Tappable targets ≥ 48dp
- Support for large text sizes
- No audio-only feedback (visual alternatives)

## Performance Requirements

- Dictionary load time: < 2 seconds on first launch
- Puzzle generation: < 3 seconds
- Word validation: < 100ms
- UI responsiveness: 60 FPS
- App size: < 20 MB

## Out of Scope (For Now)

- Multiplayer features
- Cloud sync
- Social sharing
- In-app purchases
- Advertisements
- Multiple language support
- Custom dictionaries (user upload)
