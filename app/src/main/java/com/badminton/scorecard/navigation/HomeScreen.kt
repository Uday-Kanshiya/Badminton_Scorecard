package com.badminton.scorecard.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.badminton.scorecard.core.database.dao.MatchDao
import com.badminton.scorecard.core.database.dao.PlayerDao
import com.badminton.scorecard.core.designsystem.components.MatchResultCard
import com.badminton.scorecard.core.designsystem.theme.CourtGreen
import com.badminton.scorecard.core.designsystem.theme.CourtGreenDark
import com.badminton.scorecard.core.designsystem.theme.ShuttlecockGold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class HomeRecentMatchItem(
    val matchId: Long,
    val matchType: String,
    val teamANames: List<String>,
    val teamBNames: List<String>,
    val teamAScore: Int,
    val teamBScore: Int,
    val winnerTeam: String?,
    val date: String
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val matchDao: MatchDao,
    private val playerDao: PlayerDao
) : ViewModel() {
    private val _recentMatches = MutableStateFlow<List<HomeRecentMatchItem>>(emptyList())
    val recentMatches: StateFlow<List<HomeRecentMatchItem>> = _recentMatches.asStateFlow()

    init {
        loadRecentMatches()
    }

    fun loadRecentMatches() {
        viewModelScope.launch {
            matchDao.getAllCompletedMatches().collect { matches ->
                val topMatches = matches.take(3)
                val allPlayers = playerDao.getAllPlayers().first().associateBy { it.id }

                val items = topMatches.map { match ->
                    val crossRefs = matchDao.getMatchPlayersForMatch(match.id).first()
                    val teamANames = crossRefs.filter { it.team == "TEAM_A" }.mapNotNull { allPlayers[it.playerId]?.name }
                    val teamBNames = crossRefs.filter { it.team == "TEAM_B" }.mapNotNull { allPlayers[it.playerId]?.name }

                    val sets = matchDao.getSetsForMatch(match.id).first()
                    val setA = sets.lastOrNull()?.teamAScore ?: 0
                    val setB = sets.lastOrNull()?.teamBScore ?: 0

                    val dateFormatter = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
                    val dateStr = dateFormatter.format(Date(match.startedAt))

                    HomeRecentMatchItem(
                        matchId = match.id,
                        matchType = match.matchType,
                        teamANames = teamANames,
                        teamBNames = teamBNames,
                        teamAScore = setA,
                        teamBScore = setB,
                        winnerTeam = match.winnerTeam,
                        date = dateStr
                    )
                }
                _recentMatches.value = items
            }
        }
    }
}

@Composable
fun HomeScreen(
    onNavigateToNewMatch: () -> Unit,
    onNavigateToPlayers: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToMatchDetail: (Long) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val recentMatches by viewModel.recentMatches.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App Header Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(CourtGreenDark, CourtGreen)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "🏸", fontSize = 36.sp)
                            Column {
                                Text(
                                    text = "Badminton Scorecard",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Live scoring • Serve rotation • Analytics",
                                    fontSize = 12.sp,
                                    color = ShuttlecockGold,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                androidx.compose.material.icons.Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        // 2x2 Feature Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeCard(
                        title = "New Match",
                        subtitle = "Start scoring",
                        icon = Icons.Default.Add,
                        gradientColors = listOf(Color(0xFF2E7D32), Color(0xFF43A047)),
                        onClick = onNavigateToNewMatch,
                        modifier = Modifier.weight(1f)
                    )
                    HomeCard(
                        title = "Players",
                        subtitle = "Roster & profiles",
                        icon = Icons.Default.Group,
                        gradientColors = listOf(Color(0xFF0277BD), Color(0xFF0288D1)),
                        onClick = onNavigateToPlayers,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeCard(
                        title = "Match History",
                        subtitle = "Review past games",
                        icon = Icons.AutoMirrored.Default.List,
                        gradientColors = listOf(Color(0xFFF57F17), Color(0xFFFFA000)),
                        onClick = onNavigateToHistory,
                        modifier = Modifier.weight(1f)
                    )
                    HomeCard(
                        title = "Analytics",
                        subtitle = "Stats & charts",
                        icon = Icons.Default.Analytics,
                        gradientColors = listOf(Color(0xFF455A64), Color(0xFF78909C)),
                        onClick = onNavigateToStats,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Recent Matches Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Matches",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (recentMatches.isNotEmpty()) {
                    TextButton(onClick = onNavigateToHistory) {
                        Text("View all", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        if (recentMatches.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🏸", fontSize = 32.sp)
                        Text(
                            text = "No matches played yet",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tap 'New Match' above to start your first game!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(recentMatches) { match ->
                MatchResultCard(
                    matchType = match.matchType,
                    teamANames = match.teamANames,
                    teamBNames = match.teamBNames,
                    teamAScore = match.teamAScore,
                    teamBScore = match.teamBScore,
                    winnerTeam = match.winnerTeam,
                    date = match.date,
                    onClick = { onNavigateToMatchDetail(match.matchId) }
                )
            }
        }
    }
}

@Composable
fun HomeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors))
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
