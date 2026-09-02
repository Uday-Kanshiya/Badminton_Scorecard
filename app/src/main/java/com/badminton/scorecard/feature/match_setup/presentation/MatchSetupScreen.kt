package com.badminton.scorecard.feature.match_setup.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.graphics.luminance
import com.badminton.scorecard.core.database.entity.PlayerEntity
import com.badminton.scorecard.core.designsystem.theme.*
import com.badminton.scorecard.core.rules.MatchType
import com.badminton.scorecard.core.rules.TeamSide

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchSetupScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLiveMatch: (Long) -> Unit,
    viewModel: MatchSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    LaunchedEffect(uiState.createdMatchId) {
        uiState.createdMatchId?.let {
            onNavigateToLiveMatch(it)
        }
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
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏸", fontSize = 22.sp)
                        }
                        Column {
                            Text(
                                text = "New Match Setup",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Configure format, rules & lineup",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Match Type Card (Sleek Silver in Light Mode)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightSilverContainer
                ),
                border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else LightSilverBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Match Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.matchType == MatchType.SINGLES,
                            onClick = { viewModel.onMatchTypeChanged(MatchType.SINGLES) },
                            label = { Text("Singles 👤") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = uiState.matchType == MatchType.DOUBLES,
                            onClick = { viewModel.onMatchTypeChanged(MatchType.DOUBLES) },
                            label = { Text("Doubles 👥") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            val selectedIds = setOfNotNull(
                uiState.teamAPlayer1?.id,
                uiState.teamAPlayer2?.id,
                uiState.teamBPlayer1?.id,
                uiState.teamBPlayer2?.id
            )

            // Team A Card (Sky Blue Theme in Light Mode)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightSkyContainer
                ),
                border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else LightSkyBorder)
            ) {
                Box(modifier = Modifier.padding(14.dp)) {
                    TeamSection(
                        teamName = "Team A",
                        teamColor = TeamAColor,
                        matchType = uiState.matchType,
                        player1 = uiState.teamAPlayer1,
                        player2 = uiState.teamAPlayer2,
                        player1AvailablePlayers = uiState.availablePlayers.filter { it.id !in (selectedIds - setOfNotNull(uiState.teamAPlayer1?.id)) },
                        player2AvailablePlayers = uiState.availablePlayers.filter { it.id !in (selectedIds - setOfNotNull(uiState.teamAPlayer2?.id)) },
                        onPlayer1Selected = { viewModel.onPlayerSelected(TeamSide.TEAM_A, 1, it) },
                        onPlayer2Selected = { viewModel.onPlayerSelected(TeamSide.TEAM_A, 2, it) }
                    )
                }
            }

            // Team B Card (Mint Green Theme in Light Mode)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightMintContainer
                ),
                border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else LightMintBorder)
            ) {
                Box(modifier = Modifier.padding(14.dp)) {
                    TeamSection(
                        teamName = "Team B",
                        teamColor = TeamBColor,
                        matchType = uiState.matchType,
                        player1 = uiState.teamBPlayer1,
                        player2 = uiState.teamBPlayer2,
                        player1AvailablePlayers = uiState.availablePlayers.filter { it.id !in (selectedIds - setOfNotNull(uiState.teamBPlayer1?.id)) },
                        player2AvailablePlayers = uiState.availablePlayers.filter { it.id !in (selectedIds - setOfNotNull(uiState.teamBPlayer2?.id)) },
                        onPlayer1Selected = { viewModel.onPlayerSelected(TeamSide.TEAM_B, 1, it) },
                        onPlayer2Selected = { viewModel.onPlayerSelected(TeamSide.TEAM_B, 2, it) }
                    )
                }
            }

            // Rules Card (Warm Golden Yellow Theme in Light Mode)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightGoldContainer
                ),
                border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else LightGoldBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Match Rules", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    // Points per set
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Points per set", style = MaterialTheme.typography.bodyMedium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(11, 15, 21).forEach { points ->
                                FilterChip(
                                    selected = uiState.targetPoints == points && uiState.customPointsText.isEmpty(),
                                    onClick = { viewModel.onTargetPointsSelected(points) },
                                    label = { Text(points.toString()) }
                                )
                            }
                            OutlinedTextField(
                                value = uiState.customPointsText,
                                onValueChange = { viewModel.onCustomPointsChanged(it) },
                                placeholder = { Text("Custom") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(100.dp),
                                singleLine = true
                            )
                        }
                    }

                    // Number of sets
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Number of sets", style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1, 3, 5).forEach { sets ->
                                FilterChip(
                                    selected = uiState.bestOfSets == sets,
                                    onClick = { viewModel.onBestOfSetsSelected(sets) },
                                    label = { Text(sets.toString()) }
                                )
                            }
                        }
                    }

                    // First serve
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("First Serve", style = MaterialTheme.typography.bodyMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.firstServe == TeamSide.TEAM_A,
                                onClick = { viewModel.onFirstServeChanged(TeamSide.TEAM_A) },
                                label = { Text("Team A") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.firstServe == TeamSide.TEAM_B,
                                onClick = { viewModel.onFirstServeChanged(TeamSide.TEAM_B) },
                                label = { Text("Team B") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Info banner
                    if (uiState.isSkunkRuleActive) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (isDark) MaterialTheme.colorScheme.secondaryContainer else Color(0xFFFFF176).copy(alpha = 0.6f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null)
                                Text("7-0 Skunk Rule: Instant victory at 7-0", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = viewModel::onStartMatch,
                enabled = uiState.isFormValid && !uiState.isCreatingMatch,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CourtGreen)
            ) {
                Text("🏸 START MATCH")
            }
        }
    }
}

@Composable
fun TeamSection(
    teamName: String,
    teamColor: Color,
    matchType: MatchType,
    player1: PlayerEntity?,
    player2: PlayerEntity?,
    player1AvailablePlayers: List<PlayerEntity>,
    player2AvailablePlayers: List<PlayerEntity>,
    onPlayer1Selected: (PlayerEntity) -> Unit,
    onPlayer2Selected: (PlayerEntity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(teamName, style = MaterialTheme.typography.titleMedium, color = teamColor)
        
        PlayerDropdown(
            label = "Player 1",
            selectedPlayer = player1,
            players = player1AvailablePlayers,
            onPlayerSelected = onPlayer1Selected
        )
        
        if (matchType == MatchType.DOUBLES) {
            PlayerDropdown(
                label = "Player 2",
                selectedPlayer = player2,
                players = player2AvailablePlayers,
                onPlayerSelected = onPlayer2Selected
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDropdown(
    label: String,
    selectedPlayer: PlayerEntity?,
    players: List<PlayerEntity>,
    onPlayerSelected: (PlayerEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedPlayer?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            players.forEach { player ->
                DropdownMenuItem(
                    text = { Text(player.name) },
                    onClick = {
                        onPlayerSelected(player)
                        expanded = false
                    }
                )
            }
        }
    }
}
