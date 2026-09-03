package com.badminton.scorecard.feature.live_scoreboard.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.badminton.scorecard.core.rules.CourtSide
import com.badminton.scorecard.core.rules.MatchType
import com.badminton.scorecard.core.rules.PlayerInfo
import com.badminton.scorecard.core.rules.TeamSide
import java.util.Locale

@Composable
fun LiveMatchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSummary: (Long) -> Unit,
    viewModel: LiveMatchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val events = viewModel.events.collectAsState(initial = null)
    var showMenuSheet by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(events.value) {
        when (val event = events.value) {
            is LiveMatchEvent.NavigateToSummary -> onNavigateToSummary(event.matchId)
            LiveMatchEvent.NavigateBack -> onNavigateBack()
            null -> {}
        }
    }

    if (uiState.showEndMatchDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onEndMatchDismissed,
            title = { Text("End Match?") },
            text = { Text("Are you sure? This will abandon the match.") },
            confirmButton = {
                TextButton(onClick = viewModel::onEndMatchConfirmed) {
                    Text("End Match", color = Color(0xFFE53935))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onEndMatchDismissed) {
                    Text("Cancel")
                }
            }
        )
    }

    // Player Point Attribution Dialog
    if (uiState.showScoringPlayerDialog && uiState.pendingScoringTeam != null) {
        val scoringTeamNames = if (uiState.pendingScoringTeam == TeamSide.TEAM_A) uiState.teamAPlayerNames else uiState.teamBPlayerNames
        val scoringTeamState = uiState.gameState?.getTeam(uiState.pendingScoringTeam!!)
        
        AlertDialog(
            onDismissRequest = viewModel::onScoringPlayerDialogDismissed,
            title = { 
                Text(
                    "Who made the winning shot?",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                ) 
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    scoringTeamState?.let { team ->
                        Button(
                            onClick = { viewModel.onScoringPlayerSelected(team.player1) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                        ) {
                            Text(team.player1.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        team.player2?.let { p2 ->
                            Button(
                                onClick = { viewModel.onScoringPlayerSelected(p2) },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                            ) {
                                Text(p2.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = viewModel::onScoringPlayerDialogDismissed) {
                    Text("Cancel")
                }
            }
        )
    }

    // Match Menu Dialog
    if (showMenuSheet) {
        AlertDialog(
            onDismissRequest = { showMenuSheet = false },
            title = { Text("Match Controls 🏸") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            viewModel.onPauseToggle()
                            showMenuSheet = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (uiState.isPaused) "▶ Resume Match" else "⏸ Pause Match")
                    }

                    if (uiState.isMatchComplete) {
                        Button(
                            onClick = {
                                showMenuSheet = false
                                onNavigateToSummary(uiState.matchId)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("📊 View Match Summary")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            showMenuSheet = false
                            viewModel.onEndMatchRequested()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935))
                    ) {
                        Text("🏁 Abandon Match")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMenuSheet = false }) {
                    Text("Close")
                }
            }
        )
    }

    val state = uiState.gameState ?: return

    // Top team is Team A unless sides are flipped
    val topTeamSide = if (!state.isSidesSwapped) TeamSide.TEAM_A else TeamSide.TEAM_B
    val bottomTeamSide = if (!state.isSidesSwapped) TeamSide.TEAM_B else TeamSide.TEAM_A

    val topTeamNames = if (!state.isSidesSwapped) uiState.teamAPlayerNames else uiState.teamBPlayerNames
    val bottomTeamNames = if (!state.isSidesSwapped) uiState.teamBPlayerNames else uiState.teamAPlayerNames

    val topScore = if (!state.isSidesSwapped) state.teamA.score else state.teamB.score
    val bottomScore = if (!state.isSidesSwapped) state.teamB.score else state.teamA.score

    val isTopServing = state.servingTeam == topTeamSide
    val isBottomServing = state.servingTeam == bottomTeamSide

    // Court Colors adapting to current Theme (Dark/Light)
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val darkBackground = if (isDark) Color(0xFF191B1F) else MaterialTheme.colorScheme.background
    val cardBackground = if (isDark) Color(0xFF23262D) else Color(0xFFF2F7F4)
    val courtColor = if (isDark) Color(0xFF262A32) else Color(0xFF135A31)
    val courtLineColor = if (isDark) Color(0xFF4A5160) else Color(0xFFFFFFFF)
    val cyanAccent = Color(0xFF00E5FF)
    val teamAGradient = listOf(Color(0xFF3F51B5), Color(0xFF283593))
    val teamBGradient = listOf(Color(0xFF00897B), Color(0xFF00695C))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Match Timer Pill (Silver/Platinum pill in Light Mode)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, if (isDark) Color(0xFF555B68) else Color(0xFFB0BEC5), RoundedCornerShape(12.dp))
                    .background(if (isDark) Color(0xFF23262D) else Color(0xFFECEFF1))
                    .padding(horizontal = 14.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "⏱", fontSize = 12.sp)
                    val hrs = uiState.elapsedSeconds / 3600
                    val mins = (uiState.elapsedSeconds % 3600) / 60
                    val secs = uiState.elapsedSeconds % 60
                    Text(
                        text = String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs),
                        color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF1B5E20),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 2. Scoreboard Banner (Dual Row with Sleek Silver border in Light Mode)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF383D48) else Color(0xFFB0BEC5)),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 4.dp else 2.dp)
            ) {
                Column {
                    // Top Team Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Gradient Accent Bar
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .fillMaxHeight()
                                .background(Brush.verticalGradient(if (!state.isSidesSwapped) teamAGradient else teamBGradient))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = topTeamNames.joinToString(" / ").ifEmpty { if (topTeamSide == TeamSide.TEAM_A) "Team A" else "Team B" },
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (isTopServing) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(cyanAccent)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Text(
                            text = topScore.toString(),
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(color = if (isDark) Color(0xFF2E323B) else Color(0xFFE0E0E0), thickness = 1.dp)

                    // Bottom Team Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Gradient Accent Bar
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .fillMaxHeight()
                                .background(Brush.verticalGradient(if (!state.isSidesSwapped) teamBGradient else teamAGradient))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = bottomTeamNames.joinToString(" / ").ifEmpty { if (bottomTeamSide == TeamSide.TEAM_A) "Team A" else "Team B" },
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (isBottomServing) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(cyanAccent)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Text(
                            text = bottomScore.toString(),
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 3. Umpire Call Speech Bubble
            AnimatedVisibility(visible = uiState.isAnnouncerVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF00BFA5))
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = state.umpireCall,
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 4. Top Team Score Button (+1)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clickable { viewModel.onTeamScored(topTeamSide) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) cardBackground else (if (topTeamSide == TeamSide.TEAM_A) Color(0xFF303F9F) else Color(0xFF00796B))
                ),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF383D48) else Color(0x33000000)),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "+1  ${topTeamNames.joinToString(" and ").ifEmpty { "Top Team" }}",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 5. Central Badminton Court Visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(courtColor)
                    .border(2.dp, courtLineColor, RoundedCornerShape(12.dp))
            ) {
                // Outer Doubles Boundary Lines & Inner Lines
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .border(2.dp, courtLineColor)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (state.isServiceRotationEnabled) {
                            // Top Half (Top Team - Facing DOWN towards the net)
                            val topTeamState = state.getTeam(topTeamSide)
                            val isSingles = state.matchType == MatchType.SINGLES
                            
                            val topPlayerRight = if (isSingles) {
                                if (state.serverCourt == CourtSide.RIGHT) topTeamState.player1 else null
                            } else {
                                topTeamState.playerAt(CourtSide.RIGHT)
                            }
                            val topPlayerLeft = if (isSingles) {
                                if (state.serverCourt == CourtSide.LEFT) topTeamState.player1 else null
                            } else {
                                topTeamState.playerAt(CourtSide.LEFT)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    CourtBox(
                                        player = topPlayerRight,
                                        isServer = isTopServing && topPlayerRight != null && (state.serverPlayer.id == topPlayerRight.id),
                                        onSelectServer = { topPlayerRight?.let { viewModel.onSetServer(it) } },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .border(1.dp, courtLineColor)
                                    )
                                    CourtBox(
                                        player = topPlayerLeft,
                                        isServer = isTopServing && topPlayerLeft != null && (state.serverPlayer.id == topPlayerLeft.id),
                                        onSelectServer = { topPlayerLeft?.let { viewModel.onSetServer(it) } },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .border(1.dp, courtLineColor)
                                    )
                                }

                                if (state.matchType == MatchType.DOUBLES) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .offset(y = 12.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isDark) Color(0xFF333842) else Color(0xFFE1F5FE))
                                            .border(1.dp, if (isDark) Color(0xFF555B68) else Color(0xFF81D4FA), RoundedCornerShape(8.dp))
                                            .clickable { viewModel.onSwapPositions(topTeamSide) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "⇄",
                                            color = if (isDark) Color.White else Color(0xFF0277BD),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // The Net
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .background(Color.White)
                            )

                            // Bottom Half (Bottom Team - Facing UP towards the net)
                            val bottomTeamState = state.getTeam(bottomTeamSide)
                            val bottomPlayerLeft = if (isSingles) {
                                if (state.serverCourt == CourtSide.LEFT) bottomTeamState.player1 else null
                            } else {
                                bottomTeamState.playerAt(CourtSide.LEFT)
                            }
                            val bottomPlayerRight = if (isSingles) {
                                if (state.serverCourt == CourtSide.RIGHT) bottomTeamState.player1 else null
                            } else {
                                bottomTeamState.playerAt(CourtSide.RIGHT)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    CourtBox(
                                        player = bottomPlayerLeft,
                                        isServer = isBottomServing && bottomPlayerLeft != null && (state.serverPlayer.id == bottomPlayerLeft.id),
                                        onSelectServer = { bottomPlayerLeft?.let { viewModel.onSetServer(it) } },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .border(1.dp, courtLineColor)
                                    )
                                    CourtBox(
                                        player = bottomPlayerRight,
                                        isServer = isBottomServing && bottomPlayerRight != null && (state.serverPlayer.id == bottomPlayerRight.id),
                                        onSelectServer = { bottomPlayerRight?.let { viewModel.onSetServer(it) } },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .border(1.dp, courtLineColor)
                                    )
                                }

                                if (state.matchType == MatchType.DOUBLES) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .offset(y = (-12).dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isDark) Color(0xFF333842) else Color(0xFFE1F5FE))
                                            .border(1.dp, if (isDark) Color(0xFF555B68) else Color(0xFF81D4FA), RoundedCornerShape(8.dp))
                                            .clickable { viewModel.onSwapPositions(bottomTeamSide) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "⇄",
                                            color = if (isDark) Color.White else Color(0xFF0277BD),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            // No-rotation simplified court: single rectangle with player names vertically
                            val topTeamState = state.getTeam(topTeamSide)
                            val bottomTeamState = state.getTeam(bottomTeamSide)

                            // Top Team Rectangle
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(if (isTopServing) Color(0x3300E5FF) else Color.Transparent)
                                    .border(if (isTopServing) BorderStroke(2.5.dp, cyanAccent) else BorderStroke(1.dp, courtLineColor))
                                    .clickable { if (!isTopServing) viewModel.onToggleServingSide() }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (isTopServing) cyanAccent else Color.White.copy(alpha = 0.85f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = topTeamState.player1.name,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = if (isTopServing) FontWeight.ExtraBold else FontWeight.Bold
                                        )
                                    }

                                    if (state.matchType == MatchType.DOUBLES && topTeamState.player2 != null) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = if (isTopServing) cyanAccent else Color.White.copy(alpha = 0.85f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = topTeamState.player2!!.name,
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = if (isTopServing) FontWeight.ExtraBold else FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                if (isTopServing) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(4.dp)
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(cyanAccent)
                                    )
                                }
                            }

                            // The Net
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .background(Color.White)
                            )

                            // Bottom Team Rectangle
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(if (isBottomServing) Color(0x3300E5FF) else Color.Transparent)
                                    .border(if (isBottomServing) BorderStroke(2.5.dp, cyanAccent) else BorderStroke(1.dp, courtLineColor))
                                    .clickable { if (!isBottomServing) viewModel.onToggleServingSide() }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (isBottomServing) cyanAccent else Color.White.copy(alpha = 0.85f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = bottomTeamState.player1.name,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = if (isBottomServing) FontWeight.ExtraBold else FontWeight.Bold
                                        )
                                    }

                                    if (state.matchType == MatchType.DOUBLES && bottomTeamState.player2 != null) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = if (isBottomServing) cyanAccent else Color.White.copy(alpha = 0.85f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = bottomTeamState.player2!!.name,
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = if (isBottomServing) FontWeight.ExtraBold else FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                if (isBottomServing) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(4.dp)
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(cyanAccent)
                                    )
                                }
                            }
                        }
                    }

                    // Floating Net Controls:
                    // 1. Center Serve Switch Button (⇅) (Soft Gold in Light Mode)
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDark) Color(0xFF333842) else Color(0xFFFFF9C4))
                            .border(1.dp, if (isDark) Color(0xFF555B68) else Color(0xFFFFD54F), RoundedCornerShape(8.dp))
                            .clickable { viewModel.onToggleServingSide() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⇅",
                            color = if (isDark) Color.White else Color(0xFFE65100),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 2. Right Side Flip Court Button (🔄) (Silver in Light Mode)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .offset(x = (-8).dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDark) Color(0xFF333842) else Color(0xFFECEFF1))
                            .border(1.dp, if (isDark) Color(0xFF555B68) else Color(0xFFB0BEC5), RoundedCornerShape(8.dp))
                            .clickable { viewModel.onToggleCourtSides() }
                            .padding(8.dp)
                    ) {
                        Text(text = "🔄", color = if (isDark) cyanAccent else Color(0xFF0277BD), fontSize = 16.sp)
                    }
                }
            }

            // 6. Bottom Team Score Button (+1)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clickable { viewModel.onTeamScored(bottomTeamSide) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) cardBackground else (if (bottomTeamSide == TeamSide.TEAM_A) Color(0xFF303F9F) else Color(0xFF00796B))
                ),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF383D48) else Color(0x33000000)),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "+1  ${bottomTeamNames.joinToString(" and ").ifEmpty { "Bottom Team" }}",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Victory Card / Match Complete Banner
            if (uiState.isMatchComplete || uiState.isSkunkVictory) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isSkunkVictory) Color(0xFFC62828) else Color(0xFF1B5E20)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (uiState.isSkunkVictory) "⚡ 7-0 SKUNK VICTORY!" else "🏆 MATCH WON!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Tap to save and view summary",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        try {
                                            val bitmap = com.badminton.scorecard.core.sharing.ScorecardImageGenerator.generateMatchSummaryCard(
                                                context = context,
                                                winnerTeam = if (topScore > bottomScore) "TEAM_A" else "TEAM_B",
                                                teamANames = topTeamNames,
                                                teamBNames = bottomTeamNames,
                                                setScores = listOf(com.badminton.scorecard.core.rules.SetScore(1, topScore, bottomScore)),
                                                matchType = uiState.matchType,
                                                durationSeconds = uiState.elapsedSeconds
                                            )
                                            val uri = com.badminton.scorecard.core.sharing.ShareUtils.saveBitmapAndGetUri(context, bitmap, "match_${uiState.matchId}_result.png")
                                            com.badminton.scorecard.core.sharing.ShareUtils.shareImage(context, uri, "🏸 Badminton Match Result")
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            onNavigateToSummary(uiState.matchId)
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                            ) {
                                Text("Share 📤", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { onNavigateToSummary(uiState.matchId) },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color.White else Color(0xFFFFD54F))
                            ) {
                                Text("Summary 📊", color = if (isDark) Color.Black else Color(0xFF3E2723), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 7. Bottom Dock Controls (Sleek Silver/Platinum in Light Mode)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) cardBackground else Color(0xFFECEFF1)),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF383D48) else Color(0xFFB0BEC5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Match Rules / Sets
                    IconButton(onClick = { showMenuSheet = true }) {
                        Text(text = "🏸", fontSize = 18.sp)
                    }

                    // Toggle Speech Bubble
                    IconButton(onClick = viewModel::onToggleAnnouncer) {
                        Text(
                            text = "💬",
                            fontSize = 16.sp
                        )
                    }

                    // Flip Ends (Sides)
                    IconButton(onClick = viewModel::onToggleCourtSides) {
                        Text(text = "🔁", fontSize = 16.sp)
                    }

                    // Undo
                    IconButton(
                        onClick = viewModel::onUndo,
                        enabled = uiState.canUndo && !uiState.isMatchComplete
                    ) {
                        Text(
                            text = "↩",
                            color = if (uiState.canUndo && !uiState.isMatchComplete) {
                                if (isDark) Color.White else Color(0xFF1E6F3E)
                            } else Color(0xFFB0BEC5),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Redo
                    IconButton(
                        onClick = viewModel::onRedo,
                        enabled = uiState.canRedo && !uiState.isMatchComplete
                    ) {
                        Text(
                            text = "↪",
                            color = if (uiState.canRedo && !uiState.isMatchComplete) {
                                if (isDark) Color.White else Color(0xFF1E6F3E)
                            } else Color(0xFFB0BEC5),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Menu
                    IconButton(onClick = { showMenuSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = if (isDark) Color.White else Color(0xFF263238)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourtBox(
    player: PlayerInfo?,
    isServer: Boolean,
    onSelectServer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cyanAccent = Color(0xFF00E5FF)

    Box(
        modifier = modifier
            .background(if (isServer) Color(0x4400E5FF) else Color.Transparent)
            .border(if (isServer) BorderStroke(2.5.dp, cyanAccent) else BorderStroke(0.dp, Color.Transparent))
            .then(
                if (player != null) Modifier.clickable(onClick = onSelectServer) else Modifier
            )
            .padding(8.dp)
    ) {
        if (player != null) {
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isServer) cyanAccent else Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = player.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = if (isServer) FontWeight.ExtraBold else FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isServer) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(cyanAccent)
                )
            }
        }
    }
}

