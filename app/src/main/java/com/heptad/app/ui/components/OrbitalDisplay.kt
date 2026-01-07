package com.heptad.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The main orbital letter display - shows center "sun" letter
 * surrounded by 6 orbiting "satellite" letters
 */
@Composable
fun OrbitalDisplay(
    centerLetter: Char,
    outerLetters: List<Char>,
    onLetterClick: (Char) -> Unit,
    rotationDegrees: Float = 0f,
    modifier: Modifier = Modifier
) {
    val animatedRotation by animateFloatAsState(
        targetValue = rotationDegrees,
        animationSpec = tween(durationMillis = 300),
        label = "rotation"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        val containerSize = minOf(maxWidth, maxHeight)
        val orbitalRadius = containerSize * 0.32f
        val centerSize = 72.dp
        val orbitalSize = 52.dp

        // Orbital ring (decorative)
        OrbitalRing(radius = orbitalRadius)

        // Orbiting letters
        outerLetters.forEachIndexed { index, letter ->
            val baseAngle = (index * 60.0) - 90.0 // Start from top
            val currentAngle = (baseAngle + animatedRotation) * PI / 180.0

            val xOffset = (orbitalRadius.value * cos(currentAngle)).dp
            val yOffset = (orbitalRadius.value * sin(currentAngle)).dp

            Box(
                modifier = Modifier.offset(x = xOffset, y = yOffset)
            ) {
                OrbitalLetterButton(
                    letter = letter,
                    onClick = { onLetterClick(letter) },
                    size = orbitalSize
                )
            }
        }

        // Center "sun" letter
        CenterSunButton(
            letter = centerLetter,
            onClick = { onLetterClick(centerLetter) },
            size = centerSize
        )
    }
}

/**
 * Decorative orbital ring showing the path
 */
@Composable
private fun OrbitalRing(radius: Dp) {
    val density = LocalDensity.current
    val radiusPx = with(density) { radius.toPx() }
    val color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = color,
            radius = radiusPx,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

/**
 * Center "sun" button - larger and more prominent with tap animation
 */
@Composable
fun CenterSunButton(
    letter: Char,
    onClick: () -> Unit,
    size: Dp,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "centerScale"
    )

    Surface(
        modifier = modifier
            .size(size)
            .scale(scale)
            .shadow(8.dp, CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            }
            .semantics {
                contentDescription = "Center letter ${letter.uppercase()}, required in all words"
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
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
                text = letter.uppercase().toString(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Orbital "satellite" letter button with tap animation
 */
@Composable
fun OrbitalLetterButton(
    letter: Char,
    onClick: () -> Unit,
    size: Dp,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "orbitalScale"
    )

    Surface(
        modifier = modifier
            .size(size)
            .scale(scale)
            .shadow(4.dp, CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            }
            .semantics {
                contentDescription = "Letter ${letter.uppercase()}"
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = letter.uppercase().toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
