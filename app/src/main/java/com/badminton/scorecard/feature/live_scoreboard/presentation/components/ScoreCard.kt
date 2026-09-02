package com.badminton.scorecard.feature.live_scoreboard.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.badminton.scorecard.core.designsystem.components.ScoreButton

@Composable
fun ScoreCard(
    teamName: String,
    playerNames: List<String>,
    score: Int,
    isServing: Boolean,
    teamColor: Color,
    onScoreClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = teamName,
            style = MaterialTheme.typography.titleMedium,
            color = teamColor
        )
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.height(48.dp)
        ) {
            playerNames.forEach { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ScoreButton(
            teamName = teamName,
            score = score,
            isServing = isServing,
            teamColor = teamColor,
            onClick = onScoreClick
        )
    }
}
