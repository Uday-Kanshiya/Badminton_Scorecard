package com.badminton.scorecard.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.badminton.scorecard.core.database.dao.MatchDao
import com.badminton.scorecard.core.database.dao.PlayerDao
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
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Court photo in the background - dark or light theme
        val bgDrawable = if (isDark) com.badminton.scorecard.R.drawable.court_bg_dark else com.badminton.scorecard.R.drawable.court_bg_light
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = bgDrawable),
            contentDescription = null,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay for readability and contrast across both court themes
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(
                                Color.Black.copy(alpha = 0.45f),
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.8f)
                            )
                        } else {
                            listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.7f)
                            )
                        }
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2A2A3E).copy(alpha = 0.85f))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("\uD83C\uDFF8", fontSize = 28.sp)  // Shuttlecock
                    Column {
                        Text(
                            text = "badminton",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 24.sp
                        )
                        Text(
                            text = "SCORER",
                            color = Color(0xFF6C63FF),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // New Match Button
            Button(
                onClick = onNavigateToNewMatch,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6C63FF)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = "New Match",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Matches Section
            if (recentMatches.isNotEmpty()) {
                Text(
                    text = "Recent Matches",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                recentMatches.forEach { match ->
                    val isTeamAWinner = match.winnerTeam.equals("TEAM_A", ignoreCase = true)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onNavigateToMatchDetail(match.matchId) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.45f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = match.teamANames.joinToString(" & ").ifBlank { "Team A" },
                                    color = if (isTeamAWinner) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                    fontWeight = if (isTeamAWinner) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                                Text(
                                    text = match.teamBNames.joinToString(" & ").ifBlank { "Team B" },
                                    color = if (!isTeamAWinner) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                    fontWeight = if (!isTeamAWinner) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "${match.teamAScore} - ${match.teamBScore}",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.4f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("\uD83C\uDFF8", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No matches yet",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
