package com.badminton.scorecard.feature.player.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.badminton.scorecard.core.designsystem.components.StatCard
import com.badminton.scorecard.core.designsystem.components.TimePeriodSelector
import com.badminton.scorecard.core.designsystem.theme.*
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: PlayerProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Player Profile", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val player = uiState.player
                    val stats = uiState.stats
                    if (player != null) {
                        // 1. Edit Player Name & Nickname
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Player")
                        }

                        // 2. Delete Player
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Player", tint = MaterialTheme.colorScheme.error)
                        }

                        // 3. Download/Save Stats Image
                        IconButton(onClick = {
                            coroutineScope.launch {
                                try {
                                     val bestPartner = uiState.partnershipContributions.maxByOrNull { it.winPercentage }
                                     val partnerWinPct = bestPartner?.winPercentage?.roundToInt() ?: 0
                                     val partnerName = bestPartner?.partnerName
                                     val playerPts = bestPartner?.playerPoints ?: 0
                                     val partnerPts = bestPartner?.partnerPoints ?: 0

                                     val bitmap = com.badminton.scorecard.core.sharing.ScorecardImageGenerator.generatePlayerStatsCard(
                                         context = context,
                                         player = player,
                                         stats = stats,
                                         bestPartnerName = partnerName,
                                         partnerWinRate = partnerWinPct,
                                         doublesPoints = uiState.doublesStats.totalDoublesPoints,
                                         doublesMatches = uiState.doublesStats.doublesMatchesCount,
                                         doublesAvgPoints = uiState.doublesStats.avgPointsPerMatch,
                                         doublesTeamSharePct = uiState.doublesStats.shareOfTeamPointsPct,
                                         partnerPlayerPoints = playerPts,
                                         partnerPartnerPoints = partnerPts
                                     )
                                     val uri = com.badminton.scorecard.core.sharing.ShareUtils.saveBitmapToGallery(
                                         context,
                                         bitmap,
                                         "player_${player.id}_analytics"
                                     )
                                     if (uri != null) {
                                         snackbarHostState.showSnackbar("✅ Analytics card saved to Photos / Gallery!")
                                     } else {
                                         snackbarHostState.showSnackbar("Saved to device storage.")
                                     }
                                 } catch (e: Exception) {
                                     e.printStackTrace()
                                     snackbarHostState.showSnackbar("Save failed: ${e.message}")
                                 }
                             }
                         }) {
                             Icon(Icons.Default.Download, contentDescription = "Save Stats Image")
                         }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!uiState.isLoading) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val player = uiState.player ?: return@ExtendedFloatingActionButton
                        val stats = uiState.stats
                        coroutineScope.launch {
                            try {
                                val bestPartner = uiState.partnershipContributions.maxByOrNull { it.winPercentage }
                                val partnerWinPct = bestPartner?.winPercentage?.roundToInt() ?: 0
                                val partnerName = bestPartner?.partnerName
                                val playerPts = bestPartner?.playerPoints ?: 0
                                val partnerPts = bestPartner?.partnerPoints ?: 0

                                val bitmap = com.badminton.scorecard.core.sharing.ScorecardImageGenerator.generatePlayerStatsCard(
                                    context = context,
                                    player = player,
                                    stats = stats,
                                    bestPartnerName = partnerName,
                                    partnerWinRate = partnerWinPct,
                                    doublesPoints = uiState.doublesStats.totalDoublesPoints,
                                    doublesMatches = uiState.doublesStats.doublesMatchesCount,
                                    doublesAvgPoints = uiState.doublesStats.avgPointsPerMatch,
                                    doublesTeamSharePct = uiState.doublesStats.shareOfTeamPointsPct,
                                    partnerPlayerPoints = playerPts,
                                    partnerPartnerPoints = partnerPts
                                )
                                val uri = com.badminton.scorecard.core.sharing.ShareUtils.saveBitmapAndGetUri(
                                    context,
                                    bitmap,
                                    "player_${player.id}_analytics.png"
                                )
                                com.badminton.scorecard.core.sharing.ShareUtils.shareImage(
                                    context,
                                    uri,
                                    "🏸 ${player.name}'s Career Stats"
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                                val played = stats?.totalMatchesPlayed ?: 0
                                val won = stats?.totalWins ?: 0
                                val lost = stats?.totalLosses ?: 0
                                val winRate = if (played > 0) ((won.toFloat() / played) * 100).roundToInt() else 0

                                val shareText = buildString {
                                    appendLine("🏸 Badminton Scorecard — Player Profile")
                                    appendLine("Player: ${player.name}${if (!player.nickname.isNullOrBlank()) " (${player.nickname})" else ""}")
                                    appendLine("Matches: $played | Won: $won | Lost: $lost")
                                    appendLine("Win Rate: $winRate%")
                                    if (uiState.doublesStats.totalDoublesPoints > 0) {
                                        appendLine("Doubles Points Scored: ${uiState.doublesStats.totalDoublesPoints} pts (Avg ${String.format(Locale.getDefault(), "%.1f", uiState.doublesStats.avgPointsPerMatch)}/game)")
                                    }
                                }

                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                val shareIntent = android.content.Intent.createChooser(sendIntent, "Share Player Stats")
                                context.startActivity(shareIntent)
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Share, contentDescription = "Share") },
                    text = { Text("Share Stats 📊") }
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            uiState.player?.let { player ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val initials = player.name.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("")
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = player.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!player.nickname.isNullOrBlank()) {
                                    Text(
                                        text = "\"${player.nickname}\"",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item {
                        val matchesPlayed = uiState.periodStats?.matchesPlayed ?: uiState.stats?.totalMatchesPlayed ?: 0
                        val matchesWon = uiState.periodStats?.wins ?: uiState.stats?.totalWins ?: 0
                        val matchesLost = uiState.periodStats?.losses ?: uiState.stats?.totalLosses ?: 0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                label = "Played",
                                value = matchesPlayed.toString(),
                                containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightSkyContainer,
                                borderColor = if (isDark) MaterialTheme.colorScheme.outlineVariant else LightSkyBorder,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                label = "Won",
                                value = matchesWon.toString(),
                                containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightMintContainer,
                                borderColor = if (isDark) MaterialTheme.colorScheme.outlineVariant else LightMintBorder,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                label = "Lost",
                                value = matchesLost.toString(),
                                containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightGoldContainer,
                                borderColor = if (isDark) MaterialTheme.colorScheme.outlineVariant else LightGoldBorder,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        val matchesPlayed = uiState.periodStats?.matchesPlayed ?: uiState.stats?.totalMatchesPlayed ?: 0
                        val matchesWon = uiState.periodStats?.wins ?: uiState.stats?.totalWins ?: 0
                        val winRate = if (matchesPlayed > 0) ((matchesWon.toFloat() / matchesPlayed) * 100).roundToInt() else 0

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Win Rate",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "$winRate%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { if (matchesPlayed > 0) matchesWon.toFloat() / matchesPlayed else 0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(CircleShape),
                            )
                        }
                    }

                    item {
                        TimePeriodSelector(
                            selectedPeriod = uiState.selectedPeriod,
                            onPeriodSelected = viewModel::onPeriodSelected
                        )
                    }

                    item {
                        Text(
                            text = "Serve Analytics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    item {
                        val serveStats = uiState.serveStats
                        val serveHoldRate = if (serveStats != null && serveStats.totalServes > 0) {
                            ((serveStats.pointsWonOnServe.toFloat() / serveStats.totalServes) * 100).roundToInt()
                        } else 0
                        val returnWinRate = if (serveStats != null && serveStats.totalReturns > 0) {
                            ((serveStats.pointsWonOnReturn.toFloat() / serveStats.totalReturns) * 100).roundToInt()
                        } else 0
                        val partnerServeRate = serveHoldRate 

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Serve Hold Rate")
                                        Text("$serveHoldRate%", fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Return Win Rate")
                                        Text("$returnWinRate%", fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Partner Serve Rate")
                                        Text("$partnerServeRate%", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        val stats = uiState.stats
                        val wins = uiState.periodStats?.wins ?: stats?.totalWins ?: 0
                        val losses = uiState.periodStats?.losses ?: stats?.totalLosses ?: 0
                        WinLossDonutChartSection(wins = wins, losses = losses)
                    }

                    item {
                        ServeVsReturnChartSection(serveStats = uiState.serveStats)
                    }

                    item {
                        val stats = uiState.stats
                        val wins = uiState.periodStats?.wins ?: stats?.totalWins ?: 0
                        val losses = uiState.periodStats?.losses ?: stats?.totalLosses ?: 0
                        WinRateTrendSection(wins = wins, losses = losses)
                    }

                    item {
                        Text(
                            text = "Doubles Scoring Impact",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                    }

                    item {
                        val doublesStats = uiState.doublesStats
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightSkyContainer
                            ),
                            border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else LightSkyBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StatCard(
                                        label = "Doubles Pts",
                                        value = doublesStats.totalDoublesPoints.toString(),
                                        containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White,
                                        borderColor = if (isDark) MaterialTheme.colorScheme.outlineVariant else LightSkyBorder,
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatCard(
                                        label = "Pts / Game",
                                        value = String.format(Locale.getDefault(), "%.1f", doublesStats.avgPointsPerMatch),
                                        containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White,
                                        borderColor = if (isDark) MaterialTheme.colorScheme.outlineVariant else LightSkyBorder,
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatCard(
                                        label = "Team Share",
                                        value = "${doublesStats.shareOfTeamPointsPct.roundToInt()}%",
                                        containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White,
                                        borderColor = if (isDark) MaterialTheme.colorScheme.outlineVariant else LightSkyBorder,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (doublesStats.recentMatchPoints.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Individual Points in Recent Doubles Matches",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    DoublesRecentPointsBarChart(entries = doublesStats.recentMatchPoints, isDark = isDark)
                                } else {
                                    Text(
                                        text = "Matches played with player point attribution will display individual winning shots here.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Best Partners",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                    }

                    if (uiState.partnerships.isEmpty()) {
                        item {
                            Text(
                                text = "No doubles matches played yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        val medals = listOf("🥇", "🥈", "🥉")
                        val partnershipsList = uiState.partnershipContributions.ifEmpty { 
                            uiState.partnerships.map { p ->
                                val partnerId = if (p.player1Id == player.id) p.player2Id else p.player1Id
                                val partnerName = if (p.player1Id == player.id) p.player2Name else p.player1Name
                                com.badminton.scorecard.feature.player.data.PartnershipContribution(
                                    partnerId = partnerId,
                                    partnerName = partnerName,
                                    matchesPlayed = p.matchesPlayed,
                                    matchesWon = p.matchesWon,
                                    winPercentage = p.winPercentage,
                                    playerPoints = 0,
                                    partnerPoints = 0,
                                    totalPairPoints = 0,
                                    playerContributionPct = 50f
                                )
                            }
                        }

                        itemsIndexed(partnershipsList) { index, partnership ->
                            val medal = medals.getOrElse(index) { "•" }
                            val winPct = partnership.winPercentage.roundToInt()
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(text = medal, fontSize = 16.sp)
                                            Text(
                                                text = partnership.partnerName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "$winPct%",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "(${partnership.matchesPlayed} matches)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Win Rate Progress Bar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(if (winPct > 0) (winPct / 100f).coerceIn(0.03f, 1f) else 0f)
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    }

                                    // Teammate point contribution as a pair
                                    if (partnership.totalPairPoints > 0) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else LightSkyContainer)
                                                .padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "You: ${partnership.playerPoints} pts (${partnership.playerContributionPct.roundToInt()}%)",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF6C63FF)
                                                )
                                                Text(
                                                    text = "${partnership.partnerName}: ${partnership.partnerPoints} pts (${(100 - partnership.playerContributionPct).roundToInt()}%)",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isDark) Color(0xFF00E5FF) else Color(0xFF00838F)
                                                )
                                            }

                                            // Dual-colored split contribution bar
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(8.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                            ) {
                                                val playerWeight = (partnership.playerContributionPct / 100f).coerceIn(0.05f, 0.95f)
                                                Box(
                                                    modifier = Modifier
                                                        .weight(playerWeight)
                                                        .fillMaxHeight()
                                                        .background(Color(0xFF6C63FF))
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f - playerWeight)
                                                        .fillMaxHeight()
                                                        .background(if (isDark) Color(0xFF00E5FF) else Color(0xFF00838F))
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // 1. Edit Player Dialog
    if (showEditDialog && uiState.player != null) {
        val player = uiState.player!!
        EditPlayerDialog(
            initialName = player.name,
            initialNickname = player.nickname,
            onDismiss = { showEditDialog = false },
            onUpdatePlayer = { newName, newNickname ->
                viewModel.updatePlayer(newName, newNickname)
                showEditDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("✅ Player profile updated!")
                }
            }
        )
    }

    // 2. Delete Player Dialog
    if (showDeleteDialog && uiState.player != null) {
        val player = uiState.player!!
        DeletePlayerDialog(
            playerName = player.name,
            onDismiss = { showDeleteDialog = false },
            onConfirmDelete = {
                showDeleteDialog = false
                viewModel.deletePlayer {
                    onNavigateBack()
                }
            }
        )
    }
}

@Composable
fun WinLossDonutChartSection(wins: Int, losses: Int) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightMintContainer
        ),
        border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else LightMintBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Match Outcome Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            val total = wins + losses
            val winPct = if (total > 0) ((wins.toFloat() / total) * 100).roundToInt() else 0
            val winSweep = if (total > 0) (wins.toFloat() / total) * 360f else 0f
            val lossSweep = if (total > 0) 360f - winSweep else 0f

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Canvas Donut
                Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(120.dp)) {
                        val strokeWidth = 18.dp.toPx()
                        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                        if (total == 0) {
                            drawArc(
                                color = Color(0xFFB0BEC5),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth)
                            )
                        } else {
                            if (wins > 0) {
                                drawArc(
                                    color = Color(0xFF2E7D32),
                                    startAngle = -90f,
                                    sweepAngle = winSweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }
                            if (losses > 0) {
                                drawArc(
                                    color = Color(0xFFE53935),
                                    startAngle = -90f + winSweep,
                                    sweepAngle = lossSweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$winPct%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Win Rate",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFF2E7D32)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Won: $wins matches", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFFE53935)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Lost: $losses matches", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🏸 Total: $total played", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun ServeVsReturnChartSection(serveStats: com.badminton.scorecard.core.database.dao.ServeStats?) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightSkyContainer
        ),
        border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else LightSkyBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Point Attribution Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val ownServe = serveStats?.pointsWonOnServe ?: 0
            val partnerServe = 0
            val returnPts = serveStats?.pointsWonOnReturn ?: 0
            val totalPts = ownServe + partnerServe + returnPts

            val ownPct = if (totalPts > 0) ((ownServe.toFloat() / totalPts) * 100).roundToInt() else 0
            val partnerPct = if (totalPts > 0) ((partnerServe.toFloat() / totalPts) * 100).roundToInt() else 0
            val returnPct = if (totalPts > 0) ((returnPts.toFloat() / totalPts) * 100).roundToInt() else 0

            PointBarItem(label = "Points on Own Serve", count = ownServe, percentage = ownPct, color = Color(0xFF1E88E5))
            PointBarItem(label = "Points on Partner's Serve", count = partnerServe, percentage = partnerPct, color = Color(0xFF8E24AA))
            PointBarItem(label = "Points on Return (Opponent Serve)", count = returnPts, percentage = returnPct, color = Color(0xFF00897B))
        }
    }
}

@Composable
private fun PointBarItem(label: String, count: Int, percentage: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text("$count pts ($percentage%)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(color.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (percentage > 0) (percentage / 100f).coerceIn(0.02f, 1f) else 0f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun WinRateTrendSection(wins: Int, losses: Int) {
    val total = wins + losses
    val winRate = if (total > 0) ((wins.toFloat() / total) * 100).roundToInt() else 0
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightGoldContainer
        ),
        border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else LightGoldBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Win Rate Momentum",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (winRate >= 60) "🔥 Strong Form" else if (winRate >= 40) "📈 Stable" else "⚡ Developing",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                val w = size.width
                val h = size.height
                val padY = 16f

                // Draw horizontal guide lines
                val linePaint = Color(0xFFCFD8DC).copy(alpha = 0.35f)
                drawLine(linePaint, Offset(0f, padY), Offset(w, padY), strokeWidth = 1f)
                drawLine(linePaint, Offset(0f, h / 2), Offset(w, h / 2), strokeWidth = 1f)
                drawLine(linePaint, Offset(0f, h - padY), Offset(w, h - padY), strokeWidth = 1f)

                // Simulated progression curve based on winRate
                val curvePath = Path()
                val fillPath = Path()

                val y0 = h - padY
                val yMid = h - ((winRate * 0.7f / 100f) * (h - 2 * padY)) - padY
                val yFinal = h - ((winRate / 100f) * (h - 2 * padY)) - padY

                curvePath.moveTo(0f, y0)
                curvePath.cubicTo(w * 0.35f, y0, w * 0.65f, yMid, w, yFinal)

                fillPath.addPath(curvePath)
                fillPath.lineTo(w, h)
                fillPath.lineTo(0f, h)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2E7D32).copy(alpha = 0.35f), Color.Transparent)
                    )
                )

                drawPath(
                    path = curvePath,
                    color = Color(0xFF2E7D32),
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Current point
                drawCircle(Color(0xFF2E7D32), radius = 6.dp.toPx(), center = Offset(w, yFinal))
                drawCircle(Color.White, radius = 3.dp.toPx(), center = Offset(w, yFinal))
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Initial", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Career Progression", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Current: $winRate%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun PartnershipBarChartSection(partnerships: List<com.badminton.scorecard.core.database.dao.PartnershipWinRate>, currentPlayerId: Long) {
    if (partnerships.isEmpty()) return
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightSilverContainer
        ),
        border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else LightSilverBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Doubles Partnership Win Rates 🏸",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val medals = listOf("🥇", "🥈", "🥉")
            partnerships.take(4).forEachIndexed { index, p ->
                val partnerName = if (p.player1Id == currentPlayerId) p.player2Name else p.player1Name
                val winPct = p.winPercentage.roundToInt()
                val medal = medals.getOrElse(index) { "•" }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("$medal $partnerName", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("$winPct% (${p.matchesPlayed} matches)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E88E5).copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (winPct > 0) (winPct / 100f).coerceIn(0.02f, 1f) else 0f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1E88E5))
                        )
                    }
            }
        }
    }
}
}

@Composable
fun DoublesRecentPointsBarChart(
    entries: List<com.badminton.scorecard.feature.player.data.DoublesMatchPointEntry>,
    isDark: Boolean
) {
    val maxPoints = (entries.maxOfOrNull { it.pointsScored } ?: 1).coerceAtLeast(1)
    val chronological = remember(entries) { entries.reversed() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        chronological.forEach { entry ->
            val fillFraction = (entry.pointsScored.toFloat() / maxPoints).coerceIn(0.15f, 1f)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = entry.pointsScored.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFF00E5FF) else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .fillMaxHeight(fillFraction)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF6C63FF),
                                    if (isDark) Color(0xFF00E5FF) else Color(0xFF0288D1)
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.opponentLabel.take(6),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview
@Composable
fun PlayerProfileScreenPreview() {
    BadmintonScorecardTheme {
        // Preview content
    }
}
