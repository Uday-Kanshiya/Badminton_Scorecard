package com.badminton.scorecard.feature.settings.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.badminton.scorecard.core.designsystem.theme.*
import com.badminton.scorecard.core.preferences.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings & Data") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Appearance / Theme Card (Sky Blue Theme in Light Mode)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightSkyContainer
                ),
                border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else LightSkyBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Appearance & Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Choose whether you prefer a clean Light theme, immersive Dark theme, or match your device system settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.themeMode == ThemeMode.SYSTEM,
                            onClick = { viewModel.onThemeSelected(ThemeMode.SYSTEM) },
                            label = { Text("📱 System") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = uiState.themeMode == ThemeMode.LIGHT,
                            onClick = { viewModel.onThemeSelected(ThemeMode.LIGHT) },
                            label = { Text("☀️ Light") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = uiState.themeMode == ThemeMode.DARK,
                            onClick = { viewModel.onThemeSelected(ThemeMode.DARK) },
                            label = { Text("🌙 Dark") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 2. Google Account & Cloud Sync Card (Mint Green Theme in Light Mode)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightMintContainer
                ),
                border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else LightMintBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Google Cloud Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = if (uiState.isSignedIn) "Connected: ${uiState.userEmail ?: "Active Account"}" else "Connect to Google Cloud to securely back up match history and player statistics across devices.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!uiState.isSignedIn) {
                        Button(
                            onClick = viewModel::onConnectGoogle,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSyncing
                        ) {
                            if (uiState.isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connecting...")
                            } else {
                                Icon(Icons.Default.AccountCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connect to Google")
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = viewModel::onDownloadFromCloud,
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isSyncing
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync Now")
                            }

                            OutlinedButton(
                                onClick = viewModel::onSignOut,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Disconnect")
                            }
                        }
                    }
                }
            }

            // 3. Data Download & Export Card (Golden Yellow Theme in Light Mode)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightGoldContainer
                ),
                border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else LightGoldBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Download & Export Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Download a complete offline JSON copy of all your players, career stats, and match histories to save or transfer.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { viewModel.onExportAllData(context) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isExporting
                    ) {
                        if (uiState.isExporting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Preparing export...")
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("📥 Download All Data (Export JSON)")
                        }
                    }
                }
            }

            // 4. About Info Card (Sleek Silver/Platinum Theme in Light Mode)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightSilverContainer
                ),
                border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else LightSilverBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Badminton Scorecard Pro", fontWeight = FontWeight.Bold)
                    Text("Version 1.2.0 • Offline-first with Cloud Sync", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
