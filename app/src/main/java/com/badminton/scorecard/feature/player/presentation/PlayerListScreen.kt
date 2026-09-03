package com.badminton.scorecard.feature.player.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.badminton.scorecard.core.database.entity.PlayerEntity
import com.badminton.scorecard.core.database.entity.PlayerStatsCacheEntity
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerListScreen(
    onNavigateToProfile: (Long) -> Unit,
    viewModel: PlayerListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var playerToEdit by remember { mutableStateOf<PlayerEntity?>(null) }
    var playerToDelete by remember { mutableStateOf<PlayerEntity?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("👥", fontSize = 20.sp)
                        Text(
                            text = "Players & Roster",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.onAddPlayerClicked() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Player")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                onSearch = viewModel::onSearchQueryChanged,
                active = false,
                onActiveChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search players...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
            ) {}

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.players.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.searchQuery.isNotEmpty()) "No players found" else "Add your first player to start tracking matches!",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.players, key = { it.player.id }) { playerWithStats ->
                        PlayerCard(
                            playerWithStats = playerWithStats,
                            onClick = { onNavigateToProfile(playerWithStats.player.id) },
                            onEdit = { playerToEdit = playerWithStats.player },
                            onDelete = { playerToDelete = playerWithStats.player }
                        )
                    }
                }
            }
        }

        // Add Dialog
        if (uiState.showAddPlayerDialog) {
            AddPlayerDialog(
                onDismiss = viewModel::onDismissAddDialog,
                onAddPlayer = viewModel::onPlayerAdded
            )
        }

        // Edit Dialog
        playerToEdit?.let { player ->
            EditPlayerDialog(
                initialName = player.name,
                initialNickname = player.nickname,
                onDismiss = { playerToEdit = null },
                onUpdatePlayer = { name, nick ->
                    viewModel.onUpdatePlayer(player, name, nick)
                    playerToEdit = null
                }
            )
        }

        // Delete Dialog
        playerToDelete?.let { player ->
            DeletePlayerDialog(
                playerName = player.name,
                onDismiss = { playerToDelete = null },
                onConfirmDelete = {
                    viewModel.onDeletePlayer(player)
                    playerToDelete = null
                }
            )
        }
    }
}

@Composable
fun PlayerCard(
    playerWithStats: PlayerListViewModel.PlayerWithStats,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val player = playerWithStats.player
    val stats = playerWithStats.stats
    val initials = player.name.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("")
    val wins = stats?.totalWins ?: 0
    val losses = stats?.totalLosses ?: 0
    val matchesPlayed = stats?.totalMatchesPlayed ?: 0
    val winRate = if (matchesPlayed > 0) ((wins.toFloat() / matchesPlayed) * 100).roundToInt() else 0
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top action row with avatar & overflow menu
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Options",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Player") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Player", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = player.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            if (!player.nickname.isNullOrBlank()) {
                Text(
                    text = "\"${player.nickname}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${wins}W-${losses}L",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = "$winRate%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
fun PlayerCardPreview() {
    MaterialTheme {
        PlayerCard(
            playerWithStats = PlayerListViewModel.PlayerWithStats(
                player = PlayerEntity(id = 1, name = "John Doe", nickname = "Johnny"),
                stats = PlayerStatsCacheEntity(playerId = 1, totalMatchesPlayed = 15, totalWins = 12, totalLosses = 3)
            ),
            onClick = {},
            onEdit = {},
            onDelete = {}
        )
    }
}
