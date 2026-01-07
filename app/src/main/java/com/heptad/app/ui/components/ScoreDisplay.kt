package com.heptad.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heptad.app.data.models.Rank

/**
 * Displays the current score, progress bar, rank, and word count
 */
@Composable
fun ScoreDisplay(
    currentScore: Int,
    maxScore: Int,
    currentRank: Rank,
    wordsFound: Int,
    totalWords: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (maxScore > 0) currentScore.toFloat() / maxScore else 0f
    val rankColor = currentRank.getColor()

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                color = rankColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Rank and word count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentRank.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = rankColor
                )
                Text(
                    text = "$wordsFound / $totalWords words",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
