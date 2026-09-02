package com.badminton.scorecard.feature.live_scoreboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.badminton.scorecard.core.designsystem.theme.CourtGreen
import com.badminton.scorecard.core.rules.CourtSide
import com.badminton.scorecard.core.rules.MatchType

@Composable
fun CourtDisplay(
    teamANames: List<String>,
    teamBNames: List<String>,
    serverName: String,
    serverCourt: CourtSide,
    matchType: MatchType,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(CourtGreen)
            .border(2.dp, Color.White),
        verticalArrangement = Arrangement.Center
    ) {
        Row(modifier = Modifier.weight(1f)) {
            // Team A side
            CourtSideDisplay(
                names = teamANames,
                matchType = matchType,
                isServingSide = teamANames.contains(serverName),
                serverName = serverName,
                serverCourt = serverCourt,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(1.dp, Color.White)
            )

            // Net
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(Color.White)
            )

            // Team B side
            CourtSideDisplay(
                names = teamBNames,
                matchType = matchType,
                isServingSide = teamBNames.contains(serverName),
                serverName = serverName,
                serverCourt = serverCourt,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(1.dp, Color.White)
            )
        }
    }
}

@Composable
private fun CourtSideDisplay(
    names: List<String>,
    matchType: MatchType,
    isServingSide: Boolean,
    serverName: String,
    serverCourt: CourtSide,
    modifier: Modifier
) {
    if (matchType == MatchType.SINGLES) {
        val player = names.firstOrNull() ?: ""
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            PlayerCourtIndicator(
                name = player,
                isServer = isServingSide && serverName == player
            )
        }
    } else {
        val player1 = names.getOrNull(0) ?: ""
        val player2 = names.getOrNull(1) ?: ""

        // Right court is top, Left court is bottom in this vertical layout visualization
        Column(modifier = modifier) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(1.dp, Color.White),
                contentAlignment = Alignment.Center
            ) {
                PlayerCourtIndicator(
                    name = if (isServingSide && serverCourt == CourtSide.RIGHT && serverName == player2) player2 else player1, // Simplified positional logic
                    isServer = isServingSide && serverCourt == CourtSide.RIGHT
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(1.dp, Color.White),
                contentAlignment = Alignment.Center
            ) {
                PlayerCourtIndicator(
                    name = if (isServingSide && serverCourt == CourtSide.LEFT && serverName == player1) player1 else player2, // Simplified positional logic
                    isServer = isServingSide && serverCourt == CourtSide.LEFT
                )
            }
        }
    }
}

@Composable
private fun PlayerCourtIndicator(
    name: String,
    isServer: Boolean
) {
    val text = if (isServer) "🏸 $name" else name
    Text(
        text = text,
        color = Color.White,
        fontWeight = if (isServer) FontWeight.Bold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium
    )
}
