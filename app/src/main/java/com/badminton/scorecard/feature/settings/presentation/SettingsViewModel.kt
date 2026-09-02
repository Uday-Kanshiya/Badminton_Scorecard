package com.badminton.scorecard.feature.settings.presentation

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.badminton.scorecard.core.auth.AuthRepository
import com.badminton.scorecard.core.database.dao.MatchDao
import com.badminton.scorecard.core.database.dao.PlayerDao
import com.badminton.scorecard.core.preferences.ThemeMode
import com.badminton.scorecard.core.preferences.ThemePreferences
import com.badminton.scorecard.core.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isSignedIn: Boolean = false,
    val userEmail: String? = null,
    val isSyncing: Boolean = false,
    val isExporting: Boolean = false,
    val statusMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themePreferences: ThemePreferences,
    private val authRepository: AuthRepository,
    private val syncManager: SyncManager,
    private val playerDao: PlayerDao,
    private val matchDao: MatchDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            themePreferences.themeMode.collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }

        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update {
                    it.copy(
                        isSignedIn = user != null,
                        userEmail = user?.email ?: if (user?.isAnonymous == true) "Anonymous User" else null
                    )
                }
            }
        }
    }

    fun onThemeSelected(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.setThemeMode(mode)
        }
    }

    fun onConnectGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, statusMessage = "Connecting...") }
            val result = authRepository.signInAnonymously()
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        statusMessage = "Connected to Firebase Cloud successfully!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        statusMessage = "Sign-in error: ${result.exceptionOrNull()?.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun onSignOut() {
        authRepository.signOut()
        _uiState.update { it.copy(statusMessage = "Signed out of Cloud") }
    }

    fun onDownloadFromCloud() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, statusMessage = "Downloading from Cloud...") }
            syncManager.pullFromCloud()
            _uiState.update { it.copy(isSyncing = false, statusMessage = "Cloud data downloaded & synced!") }
        }
    }

    fun onExportAllData(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, statusMessage = "Generating export file...") }
            try {
                val players = playerDao.getAllPlayers().firstOrNull() ?: emptyList()
                val stats = playerDao.getAllPlayerStats().firstOrNull() ?: emptyList()
                val matches = matchDao.getAllMatches().firstOrNull() ?: emptyList()

                val rootJson = JSONObject().apply {
                    put("export_version", 1)
                    put("exported_at", System.currentTimeMillis())

                    val playersArray = JSONArray()
                    players.forEach { p ->
                        playersArray.put(JSONObject().apply {
                            put("id", p.id)
                            put("name", p.name)
                            put("nickname", p.nickname ?: "")
                            put("createdAt", p.createdAt)
                        })
                    }
                    put("players", playersArray)

                    val statsArray = JSONArray()
                    stats.forEach { s ->
                        statsArray.put(JSONObject().apply {
                            put("playerId", s.playerId)
                            put("totalMatches", s.totalMatchesPlayed)
                            put("wins", s.totalWins)
                            put("losses", s.totalLosses)
                            put("pointsOnServe", s.totalPointsOnOwnServe)
                        })
                    }
                    put("stats", statsArray)

                    val matchesArray = JSONArray()
                    matches.forEach { m ->
                        matchesArray.put(JSONObject().apply {
                            put("id", m.id)
                            put("matchType", m.matchType)
                            put("targetPoints", m.targetPoints)
                            put("bestOfSets", m.bestOfSets)
                            put("winnerTeam", m.winnerTeam ?: "")
                            put("status", m.status)
                            put("startedAt", m.startedAt)
                            put("endedAt", m.endedAt ?: 0)
                        })
                    }
                    put("matches", matchesArray)
                }

                val cachePath = File(context.cacheDir, "exports")
                cachePath.mkdirs()
                val exportFile = File(cachePath, "badminton_data_export_${System.currentTimeMillis()}.json")
                FileOutputStream(exportFile).use { out ->
                    out.write(rootJson.toString(2).toByteArray(Charsets.UTF_8))
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    exportFile
                )

                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Badminton Scorecard Data Export")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(sendIntent, "Download / Save Data Export")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)

                _uiState.update { it.copy(isExporting = false, statusMessage = "Export complete! File ready to save.") }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isExporting = false, statusMessage = "Export failed: ${e.message}") }
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
