package com.heptad.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.heptad.app.ui.theme.SuccessLight
import kotlinx.coroutines.launch

/**
 * Input area showing current word and control buttons with animations
 */
@Composable
fun InputArea(
    currentInput: String,
    validationMessage: String?,
    onDeleteClick: () -> Unit,
    onSubmitClick: () -> Unit,
    onRotateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isError = validationMessage != null && !validationMessage.contains("point", ignoreCase = true)
    val isSuccess = validationMessage?.contains("point", ignoreCase = true) == true

    // Scale animation for success
    val successScale = remember { Animatable(1f) }

    val coroutineScope = rememberCoroutineScope()

    // Trigger animations when validation message changes
    LaunchedEffect(validationMessage) {
        if (isSuccess && validationMessage != null) {
            // Bounce animation
            coroutineScope.launch {
                successScale.animateTo(
                    targetValue = 1.1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                )
                successScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
    }

    // Animated border color
    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.error
            isSuccess -> SuccessLight
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(durationMillis = 200),
        label = "borderColor"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Rotate button
        OutlinedButton(
            onClick = onRotateClick,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Shuffle")
        }

        // Input display with animations
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .scale(successScale.value),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(2.dp, borderColor)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentInput.uppercase().ifEmpty { " " },
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    minLines = 1
                )

                // Validation message
                if (validationMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = validationMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            validationMessage.contains("PANGRAM", ignoreCase = true) ->
                                MaterialTheme.colorScheme.tertiary
                            isSuccess -> SuccessLight
                            else -> MaterialTheme.colorScheme.error
                        },
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
                onClick = onDeleteClick,
                modifier = Modifier.weight(1f),
                enabled = currentInput.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Delete",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Delete")
            }

            Button(
                onClick = onSubmitClick,
                modifier = Modifier.weight(1f),
                enabled = currentInput.length >= 4
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Submit",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Enter")
            }
        }
    }
}
