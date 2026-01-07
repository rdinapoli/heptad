# Heptad - Design Brief

## App Name: Heptad
**Etymology**: From Greek "heptad" (ἑπτάς) meaning "group of seven"

## Visual Design Concept: Orbital/Planetary

### Core Metaphor
The game uses a celestial/orbital metaphor where seven letters orbit in space:
- **Center Letter**: Represented as a "sun" or central planet - larger, glowing, required in all words
- **Six Orbital Letters**: Represented as "satellites" or orbiting planets - arranged in a circle around the center

### Why This Design?

1. **Unique Identity**: Completely distinct from hexagonal honeycomb layouts used by similar games
2. **Thematic Coherence**: The name "Heptad" (seven) connects naturally to a solar system with 7 celestial bodies
3. **Mathematical Elegance**: 6 letters at 60° intervals creates perfect geometric balance
4. **Motion Potential**: Rotation animation is natural and satisfying (planets orbiting)
5. **Visual Hierarchy**: Center "sun" clearly emphasizes the required letter

### Visual Characteristics

**Center "Sun" Button:**
- Circular shape
- Larger size (100dp)
- Radial gradient (glowing effect)
- Elevated shadow (8dp)
- Primary color scheme
- Typography: Display Small, Bold

**Orbital "Satellite" Buttons:**
- Circular shape  
- Medium size (60dp)
- Solid color with subtle elevation (4dp)
- Secondary color scheme
- Typography: Headline Medium, SemiBold
- Positioned at radius of 150dp from center
- Angular positions: 0°, 60°, 120°, 180°, 240°, 300°

**Orbital Ring (Optional):**
- Faint gray circle showing the orbital path
- Purely decorative, helps establish the celestial theme
- Stroke: 2dp, 20% opacity

### Interaction Model

**Letter Selection:**
- Tap any circle to add that letter to the input
- Haptic feedback on tap
- Ripple effect emanates from tap point

**Rotation Feature:**
- "Rotate" button (with ↻ icon) rotates all orbital letters clockwise by 60°
- Smooth animation over 300ms with easing
- Replaces traditional "shuffle" - more predictable and satisfying
- Optional: Subtle continuous rotation animation (very slow, 1 revolution per minute) for visual interest

**Advantages over Shuffle:**
- Predictable: Users can mentally track where letters will move
- Smooth: Animation shows the motion path
- Thematic: Reinforces the orbital metaphor
- Strategic: Players can position letters where they want them

### Color Palette

**Center Sun:**
- Container: Primary Container (warm glow)
- Border: Primary (stronger emphasis)
- Text: On Primary Container

**Orbital Satellites:**
- Container: Secondary Container (cooler tone)
- Border: Secondary
- Text: On Secondary Container

**Orbital Ring:**
- Stroke: Gray with 20% opacity

### Accessibility Considerations

- All tap targets are circular and ≥ 48dp (satellites are 60dp)
- High contrast between letter and background
- Clear visual distinction between center and orbital letters
- Screen reader: "Center letter [X], required in all words" and "Letter [Y]"
- Support for reduced motion: Disable rotation animations when system preference is set

### Animation Details

**Rotation Animation:**
```kotlin
val rotationDegrees = remember { Animatable(0f) }

fun rotateLetters() {
    scope.launch {
        rotationDegrees.animateTo(
            targetValue = rotationDegrees.value + 60f,
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        )
    }
}
```

**Optional Continuous Rotation:**
```kotlin
LaunchedEffect(Unit) {
    while (true) {
        rotationDegrees.animateTo(
            targetValue = rotationDegrees.value + 360f,
            animationSpec = tween(
                durationMillis = 60000, // 1 minute per revolution
                easing = LinearEasing
            )
        )
    }
}
```

### Typography Scale

- **Center Letter**: displaySmall (36sp)
- **Orbital Letters**: headlineMedium (28sp)
- **Input Display**: headlineMedium (28sp)
- **Score**: titleLarge (22sp)
- **Found Words**: bodyLarge (16sp)

### Spacing & Layout

- Padding around orbital display: 32dp
- Orbital radius from center: 150dp
- Letter button sizes:
  - Center: 100dp diameter
  - Orbital: 60dp diameter
- Minimum spacing between orbital buttons: ~30dp (naturally maintained by 60° spacing)

### Future Enhancement Ideas

1. **Particle Effects**: Stars or sparkles around the orbital ring
2. **Letter Trails**: Subtle motion blur or trail effect during rotation
3. **Constellation Lines**: Optional lines connecting letters that have been used together
4. **Planet Textures**: Subtle surface details on letter buttons
5. **Day/Night Mode**: Visual theme changes with system dark mode (stars vs. sun-lit space)

## Comparison to Original Design

| Aspect | Original (Honeycomb) | New (Orbital) |
|--------|---------------------|---------------|
| Shape | Hexagons | Circles |
| Center Emphasis | Same size as others | Significantly larger |
| Arrangement | Hexagonal packing | Circular orbit |
| Theme | Organic/nature | Cosmic/celestial |
| Rotation | Random shuffle | Predictable rotation |
| Visual Metaphor | Beehive | Solar system |

## Brand Identity

The orbital design positions Heptad as:
- **Modern**: Clean circular geometry
- **Scientific**: Celestial/space theme
- **Elegant**: Mathematical precision
- **Distinctive**: Immediately recognizable as different from competitors
- **Meaningful**: Visual design reinforces the "seven" concept in the name
