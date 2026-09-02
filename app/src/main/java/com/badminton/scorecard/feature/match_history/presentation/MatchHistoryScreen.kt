package com.badminton.scorecard.feature.match_history.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.badminton.scorecard.core.rules.MatchType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MatchHistoryScreen(
    onNavigateToMatchDetail: (Long) -> Unit,
    viewModel: MatchHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDelete() },
            title = { Text("Delete Match") },
            text = { Text("Delete this match? Player stats will not be affected.") },
            confirmButton = {
                TextButton(onClick = { viewModel.onConfirmDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissDelete() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 1.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏸", fontSize = 24.sp)
                        }
                        Column {
                            Text(
                                text = "Match History",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Past games, set scores & match records",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filterType == MatchHistoryViewModel.MatchTypeFilter.ALL,
                    onClick = { viewModel.onFilterChanged(MatchHistoryViewModel.MatchTypeFilter.ALL) },
                    label = { Text("All Matches") }
                )
                FilterChip(
                    selected = uiState.filterType == MatchHistoryViewModel.MatchTypeFilter.SINGLES,
                    onClick = { viewModel.onFilterChanged(MatchHistoryViewModel.MatchTypeFilter.SINGLES) },
                    label = { Text("Singles 👤") }
                )
                FilterChip(
                    selected = uiState.filterType == MatchHistoryViewModel.MatchTypeFilter.DOUBLES,
                    onClick = { viewModel.onFilterChanged(MatchHistoryViewModel.MatchTypeFilter.DOUBLES) },
                    label = { Text("Doubles 👥") }
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredMatches.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🏸", fontSize = 40.sp)
                        Text("No completed matches found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Play a match from 'New Match' to record your first game!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    uiState.groupedMatches.forEach { (dateHeader, matches) ->
                        stickyHeader {
                            Surface(
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = dateHeader,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
                                )
                            }
                        }
                        
                        items(matches, key = { it.matchId }) { match ->
                            val isTeamAWinner = match.winnerTeam.equals("TEAM_A", ignoreCase = true) || match.winnerTeam.equals("TeamA", ignoreCase = true)
                            val winnerNames = if (isTeamAWinner) match.teamANames.joinToString(" & ") else match.teamBNames.joinToString(" & ")

                            SwipeToDismissBox(
                                state = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { state ->
                                        if (state == SwipeToDismissBoxValue.EndToStart) {
                                            viewModel.onDeleteMatch(match.matchId)
                                            true
                                        } else {
                                            false
                                        }
                                    }
                                ),
                                backgroundContent = {
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.errorContainer)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToMatchDetail(match.matchId) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "🏸 ${match.matchType.uppercase()}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = match.timeFormatted,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = match.teamANames.joinToString(" & ").ifBlank { "Team A" },
                                                fontWeight = if (isTeamAWinner) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = match.setScores.joinToString("   ") { "${it.first} - ${it.second}" },
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium,
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            )
                                            Text(
                                                text = match.teamBNames.joinToString(" & ").ifBlank { "Team B" },
                                                fontWeight = if (!isTeamAWinner) FontWeight.Bold else FontWeight.Normal,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = if (winnerNames.isNotBlank()) "🏆 $winnerNames Won" else "Match Finished",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
