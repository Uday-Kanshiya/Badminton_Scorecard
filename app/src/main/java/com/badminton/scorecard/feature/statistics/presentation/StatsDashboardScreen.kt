package com.badminton.scorecard.feature.statistics.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.luminance
import com.badminton.scorecard.core.designsystem.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.badminton.scorecard.core.designsystem.components.StatCard
import com.badminton.scorecard.core.designsystem.components.TimePeriodSelector
import com.badminton.scorecard.feature.statistics.data.LeaderboardEntry
import com.badminton.scorecard.feature.statistics.data.MatchCountByDate
import com.badminton.scorecard.feature.statistics.data.PartnershipWinRate

@Composable
fun StatsDashboardScreen(
    onNavigateToPartnerships: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            TimePeriodSelector(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodSelected = viewModel::onPeriodSelected
            )
        }

        uiState.overallStats?.let { stats ->
            item {
                SectionHeader("Overview")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        label = "Matches",
                        value = stats.totalMatches.toString(),
                        containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightSkyContainer,
                        borderColor = if (isDark) MaterialTheme.colorScheme.outlineVariant else LightSkyBorder,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Players",
                        value = stats.totalPlayers.toString(),
                        containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightMintContainer,
                        borderColor = if (isDark) MaterialTheme.colorScheme.outlineVariant else LightMintBorder,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "MVP",
                        value = stats.mostActivePlayer?.name ?: "-",
                        containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightGoldContainer,
                        borderColor = if (isDark) MaterialTheme.colorScheme.outlineVariant else LightGoldBorder,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (uiState.matchesOverTime.isNotEmpty()) {
            item {
                SectionHeader("Matches Over Time")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightSilverContainer
                    ),
                    border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else LightSilverBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    MatchesOverTimeChart(
                        data = uiState.matchesOverTime,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(16.dp)
                    )
                }
            }
        }

        uiState.matchTypeDistribution?.let { dist ->
            if (dist.singlesCount > 0 || dist.doublesCount > 0) {
                item {
                    SectionHeader("Match Type Split")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightMintContainer
                        ),
                        border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else LightMintBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        MatchTypePieChart(
                            singlesCount = dist.singlesCount,
                            doublesCount = dist.doublesCount,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(16.dp)
                        )
                    }
                }
            }
        }

        if (uiState.leaderboard.isNotEmpty()) {
            item {
                SectionHeader("Leaderboard")
                LeaderboardTable(entries = uiState.leaderboard)
            }
        }

        if (uiState.partnerships.isNotEmpty()) {
            item {
                SectionHeader("Top Partnerships")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.partnerships.take(5).forEach { partnership ->
                        PartnershipSimpleCard(partnership)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onNavigateToPartnerships,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View All Partnerships")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun MatchesOverTimeChart(
    data: List<MatchCountByDate>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = "No match activity in this period",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxCount = (data.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)

    Row(
        modifier = modifier.padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { entry ->
            val fillFraction = (entry.count.toFloat() / maxCount).coerceIn(0.15f, 1f)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp)
            ) {
                // Count badge
                Text(
                    text = "${entry.count}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                // Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .fillMaxHeight(fillFraction * 0.72f)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    CourtGreenLight,
                                    MaterialTheme.colorScheme.primary
                                )
                            )
                        )
                )
                // Baseline
                HorizontalDivider(
                    thickness = 1.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                // Date label
                Text(
                    text = entry.dateLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MatchTypePieChart(
    singlesCount: Int,
    doublesCount: Int,
    modifier: Modifier = Modifier
) {
    val total = singlesCount + doublesCount
    val singlesAngle = if (total > 0) (singlesCount.toFloat() / total) * 360f else 0f
    
    val singlesColor = CourtGreen
    val doublesColor = MaterialTheme.colorScheme.secondary

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Canvas(modifier = Modifier.size(150.dp)) {
            val strokeWidth = 30.dp.toPx()
            
            if (singlesAngle > 0) {
                drawArc(
                    color = singlesColor,
                    startAngle = -90f,
                    sweepAngle = singlesAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
            }
            
            if (singlesAngle < 360f) {
                drawArc(
                    color = doublesColor,
                    startAngle = -90f + singlesAngle,
                    sweepAngle = 360f - singlesAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
            }
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem(color = singlesColor, label = "Singles ($singlesCount)")
            LegendItem(color = doublesColor, label = "Doubles ($doublesCount)")
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun LeaderboardTable(entries: List<LeaderboardEntry>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("#", modifier = Modifier.weight(0.5f), fontWeight = FontWeight.Bold)
                Text("Player", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                Text("P", modifier = Modifier.weight(0.5f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("W", modifier = Modifier.weight(0.5f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("L", modifier = Modifier.weight(0.5f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("%", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
            }
            
            entries.forEachIndexed { index, entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (index % 2 == 0) MaterialTheme.colorScheme.surface 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val badge = when (entry.rank) {
                        1 -> "🥇"
                        2 -> "🥈"
                        3 -> "🥉"
                        else -> "${entry.rank}."
                    }
                    Text(badge, modifier = Modifier.weight(0.5f))
                    Text(entry.player.name, modifier = Modifier.weight(2f))
                    Text(entry.stats.totalMatchesPlayed.toString(), modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                    Text(entry.stats.totalWins.toString(), modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                    Text(entry.stats.totalLosses.toString(), modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                    Text("${entry.winPercentage.toInt()}%", modifier = Modifier.weight(1f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PartnershipSimpleCard(partnership: PartnershipWinRate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${partnership.player1Name} & ${partnership.player2Name}",
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${partnership.matchesPlayed} matches",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${partnership.winPercentage.toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = CourtGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
