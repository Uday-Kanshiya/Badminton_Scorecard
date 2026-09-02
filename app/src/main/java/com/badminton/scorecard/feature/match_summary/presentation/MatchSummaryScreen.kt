package com.badminton.scorecard.feature.match_summary.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.badminton.scorecard.core.designsystem.theme.TeamAColor
import com.badminton.scorecard.core.designsystem.theme.*
import com.badminton.scorecard.core.rules.MatchType
import com.badminton.scorecard.core.rules.SetScore
import com.badminton.scorecard.core.sharing.ShareUtils
import com.badminton.scorecard.feature.match_summary.presentation.charts.MomentumChart
import com.badminton.scorecard.feature.match_summary.presentation.charts.PointDistributionChart
import com.badminton.scorecard.feature.match_summary.presentation.charts.ScoreProgressionChart
import androidx.compose.ui.graphics.luminance
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchSummaryScreen(
    onNavigateHome: () -> Unit,
    viewModel: MatchSummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val graphicsLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val snackbarHostState = remember { SnackbarHostState() }

    fun createMatchSummaryBitmap(): android.graphics.Bitmap {
        return com.badminton.scorecard.core.sharing.ScorecardImageGenerator.generateMatchSummaryCard(
            context = context,
            winnerTeam = uiState.winnerTeam ?: "TEAM_A",
            teamANames = uiState.teamANames,
            teamBNames = uiState.teamBNames,
            setScores = uiState.setScores.map { SetScore(it.setNumber, it.teamAScore, it.teamBScore) },
            matchType = try { MatchType.valueOf(uiState.matchType) } catch (e: Exception) { MatchType.SINGLES },
            durationSeconds = uiState.durationSeconds,
            scoreProgression = uiState.scoreProgression.map { Pair(it.teamAScore, it.teamBScore) },
            momentumData = uiState.momentumData,
            teamAServePoints = uiState.teamAServePoints,
            teamAReturnPoints = uiState.teamAReturnPoints,
            teamBServePoints = uiState.teamBServePoints,
            teamBReturnPoints = uiState.teamBReturnPoints,
            teamALongestStreak = uiState.teamALongestStreak,
            teamBLongestStreak = uiState.teamBLongestStreak,
            totalRallies = uiState.totalRallies,
            matchDuration = uiState.matchDuration
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Match Summary") },
                actions = {
                    // 1. Save directly to Gallery
                    IconButton(onClick = {
                        coroutineScope.launch {
                            try {
                                val bitmap = createMatchSummaryBitmap()
                                val uri = ShareUtils.saveBitmapToGallery(context, bitmap, "match_${uiState.matchId}_summary")
                                if (uri != null) {
                                    snackbarHostState.showSnackbar("✅ Scorecard saved to Photos / Gallery!")
                                } else {
                                    snackbarHostState.showSnackbar("Saved to device storage.")
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                snackbarHostState.showSnackbar("Save failed: ${e.message}")
                            }
                        }
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Save to Gallery")
                    }

                    // 2. Share Graphic Card
                    TextButton(onClick = {
                        coroutineScope.launch {
                            try {
                                val bitmap = createMatchSummaryBitmap()
                                val uri = ShareUtils.saveBitmapAndGetUri(context, bitmap, "match_${uiState.matchId}_summary.png")
                                ShareUtils.shareImage(context, uri, "🏸 Badminton Match Summary")
                            } catch (e: Exception) {
                                e.printStackTrace()
                                // Fallback to text sharing if bitmap fails
                                val scoreText = buildString {
                                    appendLine("🏸 Badminton Match Result")
                                    appendLine("Winner: ${uiState.winnerTeam}")
                                    appendLine("Score: " + uiState.setScores.joinToString(", ") { "Set ${it.setNumber}: ${it.teamAScore}-${it.teamBScore}" })
                                }
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, scoreText)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Share Result"))
                            }
                        }
                    }) {
                        Text("📤 Share", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    drawContent()
                }
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Winner Banner (Rich Golden Yellow in Light Mode)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.primaryContainer else LightGoldContainer
                ),
                border = BorderStroke(2.dp, if (isDark) MaterialTheme.colorScheme.primary else LightGoldBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🏆 ${uiState.winnerTeam} WINS!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (uiState.winnerTeam == "TEAM_A") uiState.teamANames.joinToString(" & ") else uiState.teamBNames.joinToString(" & "),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Set Scores (Sleek Silver in Light Mode)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightSilverContainer
                ),
                border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else LightSilverBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Set Scores", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    uiState.setScores.forEach { setScore ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Set ${setScore.setNumber}")
                            Text("${setScore.teamAScore} - ${setScore.teamBScore}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Score Progression Chart (Sky Blue in Light Mode)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightSkyContainer
                ),
                border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else LightSkyBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Score Progression", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    ScoreProgressionChart(data = uiState.scoreProgression)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Team A", color = TeamAColor, modifier = Modifier.padding(end = 16.dp), fontWeight = FontWeight.Bold)
                        Text("Team B", color = TeamBColor, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Momentum Chart (Mint Green in Light Mode)
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
                    Text("Momentum", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    MomentumChart(data = uiState.momentumData)
                }
            }

            // Point Distribution (Silver in Light Mode)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightSilverContainer
                ),
                border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else LightSilverBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Point Distribution", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            PointDistributionChart(
                                servePoints = uiState.teamAServePoints,
                                returnPoints = uiState.teamAReturnPoints,
                                teamColor = TeamAColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Team A", fontWeight = FontWeight.Bold)
                            Text("Serve: ${uiState.teamAServePoints}", style = MaterialTheme.typography.bodySmall)
                            Text("Return: ${uiState.teamAReturnPoints}", style = MaterialTheme.typography.bodySmall)
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            PointDistributionChart(
                                servePoints = uiState.teamBServePoints,
                                returnPoints = uiState.teamBReturnPoints,
                                teamColor = TeamBColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Team B", fontWeight = FontWeight.Bold)
                            Text("Serve: ${uiState.teamBServePoints}", style = MaterialTheme.typography.bodySmall)
                            Text("Return: ${uiState.teamBReturnPoints}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Key Stats (Sky Blue in Light Mode)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightSkyContainer
                ),
                border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else LightSkyBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Key Stats", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Longest streak A: ${uiState.teamALongestStreak} points")
                    Text("Longest streak B: ${uiState.teamBLongestStreak} points")
                    Text("Total rallies: ${uiState.totalRallies}")
                    Text("Duration: ${uiState.matchDuration}")
                }
            }
            
            // Action Section: Share & Export Match Result (Warm Gold in Light Mode)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightGoldContainer
                ),
                border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else LightGoldBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Share & Export Match Result",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        val bitmap = createMatchSummaryBitmap()
                                        val uri = ShareUtils.saveBitmapAndGetUri(context, bitmap, "match_${uiState.matchId}_summary.png")
                                        ShareUtils.shareImage(context, uri, "🏸 Badminton Match Summary")
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        val scoreText = buildString {
                                            appendLine("🏸 Badminton Match Result")
                                            appendLine("Winner: ${uiState.winnerTeam}")
                                            appendLine("Score: " + uiState.setScores.joinToString(", ") { "Set ${it.setNumber}: ${it.teamAScore}-${it.teamBScore}" })
                                        }
                                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(android.content.Intent.EXTRA_TEXT, scoreText)
                                        }
                                        context.startActivity(android.content.Intent.createChooser(intent, "Share Result"))
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("📤 Share Image", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        val bitmap = createMatchSummaryBitmap()
                                        val uri = ShareUtils.saveBitmapToGallery(context, bitmap, "match_${uiState.matchId}_summary")
                                        if (uri != null) {
                                            snackbarHostState.showSnackbar("✅ Scorecard saved to Photos / Gallery!")
                                        } else {
                                            snackbarHostState.showSnackbar("Saved to device storage.")
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        snackbarHostState.showSnackbar("Save failed: ${e.message}")
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("💾 Save to Photos", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Share as Text button
                    TextButton(
                        onClick = {
                            val scoreText = buildString {
                                appendLine("🏸 Badminton Match Result")
                                appendLine("Winner: ${uiState.winnerTeam}")
                                appendLine("Team A: ${uiState.teamANames.joinToString(" / ")}")
                                appendLine("Team B: ${uiState.teamBNames.joinToString(" / ")}")
                                appendLine("Score: " + uiState.setScores.joinToString(", ") { "Set ${it.setNumber}: ${it.teamAScore}-${it.teamBScore}" })
                                appendLine("Duration: ${uiState.matchDuration}")
                            }
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, scoreText)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Share Result as Text"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📋 Share as Text Summary")
                    }
                }
            }

            Button(
                onClick = onNavigateHome,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Text("Done")
            }
        }
    }
}
