package com.badminton.scorecard.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.badminton.scorecard.core.designsystem.theme.BadmintonScorecardTheme
import com.badminton.scorecard.core.designsystem.theme.ShuttlecockGold
import com.badminton.scorecard.core.designsystem.theme.TeamAColor
import com.badminton.scorecard.core.designsystem.theme.TeamBColor
import com.badminton.scorecard.core.designsystem.theme.scoreDisplayLarge

@Composable
fun ScoreButton(
    teamName: String,
    score: Int,
    isServing: Boolean,
    teamColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = teamColor,
        contentColor = Color.White,
        border = if (isServing) BorderStroke(4.dp, ShuttlecockGold) else null,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = teamName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (isServing) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "🏸", style = MaterialTheme.typography.titleLarge)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = score.toString(),
                style = scoreDisplayLarge,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScoreButtonPreview() {
    BadmintonScorecardTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ScoreButton(
                teamName = "Team A",
                score = 20,
                isServing = true,
                teamColor = TeamAColor,
                onClick = {}
            )
            Spacer(modifier = Modifier.height(16.dp))
            ScoreButton(
                teamName = "Team B",
                score = 19,
                isServing = false,
                teamColor = TeamBColor,
                onClick = {}
            )
        }
    }
}
