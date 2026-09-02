package com.badminton.scorecard.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.badminton.scorecard.core.designsystem.theme.BadmintonScorecardTheme
import com.badminton.scorecard.core.designsystem.theme.WinGreen

@Composable
fun MatchResultCard(
    matchType: String,
    teamANames: List<String>,
    teamBNames: List<String>,
    teamAScore: Int,
    teamBScore: Int,
    winnerTeam: String?, // "TeamA" or "TeamB" or null for draw/ongoing
    date: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTeamAWinner = winnerTeam.equals("TEAM_A", ignoreCase = true) || winnerTeam.equals("TeamA", ignoreCase = true)
    val isTeamBWinner = winnerTeam.equals("TEAM_B", ignoreCase = true) || winnerTeam.equals("TeamB", ignoreCase = true)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = matchType.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team A
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    teamANames.forEach { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isTeamAWinner) FontWeight.Bold else FontWeight.Normal,
                            color = if (isTeamAWinner) WinGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Scores
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = teamAScore.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isTeamAWinner) WinGreen else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "-",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = teamBScore.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isTeamBWinner) WinGreen else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Team B
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    teamBNames.forEach { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isTeamBWinner) FontWeight.Bold else FontWeight.Normal,
                            color = if (isTeamBWinner) WinGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MatchResultCardPreview() {
    BadmintonScorecardTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            MatchResultCard(
                matchType = "Singles",
                teamANames = listOf("Uday Kiran"),
                teamBNames = listOf("John Doe"),
                teamAScore = 21,
                teamBScore = 18,
                winnerTeam = "TeamA",
                date = "Oct 24, 2023",
                onClick = {}
            )
            Spacer(modifier = Modifier.padding(8.dp))
            MatchResultCard(
                matchType = "Doubles",
                teamANames = listOf("Alice", "Bob"),
                teamBNames = listOf("Charlie", "David"),
                teamAScore = 19,
                teamBScore = 21,
                winnerTeam = "TeamB",
                date = "Oct 25, 2023",
                onClick = {}
            )
        }
    }
}
