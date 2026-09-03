package com.badminton.scorecard.feature.settings.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.onGoogleSignInResult(result.data)
    }

    var showManualEmailDialog by remember { mutableStateOf(false) }
    var manualEmailInput by remember { mutableStateOf("") }
    var manualNameInput by remember { mutableStateOf("") }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings & Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
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

            // Match Defaults Card
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
                        Icon(Icons.Default.SportsScore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Match Defaults", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Set default options for new matches. These can be changed per-match in the setup screen.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text("Default Point Attribution (Doubles)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !uiState.defaultPlayerPointAttribution,
                            onClick = { viewModel.onDefaultPlayerPointAttributionChanged(false) },
                            label = { Text("Team Points 👥") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = uiState.defaultPlayerPointAttribution,
                            onClick = { viewModel.onDefaultPlayerPointAttributionChanged(true) },
                            label = { Text("Player Points 👤") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 2. Google Account & Cloud Save Card (Mint Green Theme in Light Mode)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surface else LightMintContainer
                ),
                border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else LightMintBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Google Play & Cloud Save", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        if (uiState.isSignedIn) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF00E676).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF00E676))
                            ) {
                                Text(
                                    text = "Cloud Save Active 🟢",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF69F0AE) else Color(0xFF1B5E20),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (!uiState.isSignedIn) {
                        Text(
                            text = "Sign in with your Google account to automatically back up and cache your matches, set history, and player stats across devices.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                try {
                                    val intent = viewModel.getGoogleSignInIntent(context)
                                    googleSignInLauncher.launch(intent)
                                } catch (e: Exception) {
                                    showManualEmailDialog = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSyncing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (uiState.isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Signing in & caching data...")
                            } else {
                                Icon(Icons.Default.AccountCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sign in with Google", fontWeight = FontWeight.Bold)
                            }
                        }

                        TextButton(
                            onClick = { showManualEmailDialog = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Or connect with Gmail address manually", style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        // User Profile Info
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White)
                                .padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (uiState.userName?.firstOrNull() ?: uiState.userEmail?.firstOrNull() ?: 'G').uppercase(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = uiState.userName ?: "Google Player",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = uiState.userEmail ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Last sync info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Last synced: ${uiState.lastSyncTime ?: "Just now"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Auto-cached",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = viewModel::onSyncNow,
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isSyncing
                            ) {
                                if (uiState.isSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Syncing...")
                                } else {
                                    Icon(Icons.Default.CloudSync, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sync Now")
                                }
                            }

                            OutlinedButton(
                                onClick = { viewModel.onSignOut(context) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Sign Out")
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
                    Text("Badminton Scorer", fontWeight = FontWeight.Bold)
                    Text("Version 2.0.0 • Offline-first with Cloud Sync", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (showManualEmailDialog) {
        AlertDialog(
            onDismissRequest = { showManualEmailDialog = false },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Connect Google Account") 
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Enter your Gmail address to associate and restore your cloud match history and player stats across devices.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = manualEmailInput,
                        onValueChange = { manualEmailInput = it },
                        label = { Text("Gmail Address") },
                        placeholder = { Text("example@gmail.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = manualNameInput,
                        onValueChange = { manualNameInput = it },
                        label = { Text("Display Name (Optional)") },
                        placeholder = { Text("Your Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (manualEmailInput.isNotBlank()) {
                            viewModel.onManualEmailSignIn(manualEmailInput.trim(), manualNameInput.trim().ifEmpty { null })
                            showManualEmailDialog = false
                        }
                    },
                    enabled = manualEmailInput.isNotBlank() && manualEmailInput.contains("@")
                ) {
                    Text("Connect & Cache")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualEmailDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
