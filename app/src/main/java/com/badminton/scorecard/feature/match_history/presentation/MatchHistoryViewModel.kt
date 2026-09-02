package com.badminton.scorecard.feature.match_history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.badminton.scorecard.core.database.dao.MatchDao
import com.badminton.scorecard.core.database.dao.PlayerDao
import com.badminton.scorecard.core.rules.MatchType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MatchHistoryViewModel @Inject constructor(
    private val matchDao: MatchDao,
    private val playerDao: PlayerDao
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MatchHistoryUiState())
    val uiState: StateFlow<MatchHistoryUiState> = _uiState.asStateFlow()
    
    private var observeJob: kotlinx.coroutines.Job? = null

    init {
        observeMatches()
    }
    
    fun refresh() {
        observeMatches()
    }

    private fun observeMatches() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            matchDao.getAllCompletedMatches().collect { matches ->
                val allPlayers = playerDao.getAllPlayers().first().associateBy { it.id }

                val historyItems = matches.map { match ->
                    val crossRefs = matchDao.getMatchPlayersForMatch(match.id).first()
                    val teamAPlayerIds = crossRefs.filter { it.team == "TEAM_A" }.map { it.playerId }
                    val teamBPlayerIds = crossRefs.filter { it.team == "TEAM_B" }.map { it.playerId }

                    val teamANames = teamAPlayerIds.mapNotNull { allPlayers[it]?.name }
                    val teamBNames = teamBPlayerIds.mapNotNull { allPlayers[it]?.name }

                    val sets = matchDao.getSetsForMatch(match.id).first()
                    
                    var setsWonA = 0
                    var setsWonB = 0
                    sets.forEach { set ->
                        if (set.teamAScore > set.teamBScore) setsWonA++
                        else if (set.teamBScore > set.teamAScore) setsWonB++
                    }
                    
                    val dateHeaderFormatter = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
                    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
                    val dateHeader = dateHeaderFormatter.format(Date(match.startedAt))
                    val timeStr = timeFormatter.format(Date(match.startedAt))
                    
                    MatchHistoryItem(
                        matchId = match.id,
                        matchType = match.matchType,
                        teamANames = teamANames,
                        teamBNames = teamBNames,
                        teamAScore = setsWonA,
                        teamBScore = setsWonB,
                        setScores = sets.map { Pair(it.teamAScore, it.teamBScore) },
                        winnerTeam = match.winnerTeam,
                        dateHeader = dateHeader,
                        timeFormatted = timeStr,
                        status = match.status,
                        timestamp = match.startedAt
                    )
                }.sortedByDescending { it.timestamp }
                
                _uiState.update { it.copy(matches = historyItems, isLoading = false) }
            }
        }
    }
    
    fun onFilterChanged(filter: MatchTypeFilter) {
        _uiState.update { it.copy(filterType = filter) }
    }
    
    fun onDeleteMatch(matchId: Long) {
        _uiState.update { it.copy(showDeleteDialog = true, matchToDelete = matchId) }
    }
    
    fun onConfirmDelete() {
        val matchId = _uiState.value.matchToDelete ?: return
        viewModelScope.launch {
            matchDao.deleteMatchById(matchId)
            _uiState.update { it.copy(showDeleteDialog = false, matchToDelete = null) }
        }
    }
    
    fun onDismissDelete() {
        _uiState.update { it.copy(showDeleteDialog = false, matchToDelete = null) }
    }
    
    data class MatchHistoryUiState(
        val matches: List<MatchHistoryItem> = emptyList(),
        val isLoading: Boolean = true,
        val showDeleteDialog: Boolean = false,
        val matchToDelete: Long? = null,
        val filterType: MatchTypeFilter = MatchTypeFilter.ALL
    ) {
        val filteredMatches: List<MatchHistoryItem>
            get() = when (filterType) {
                MatchTypeFilter.ALL -> matches
                MatchTypeFilter.SINGLES -> matches.filter { it.matchType.equals(MatchType.SINGLES.name, ignoreCase = true) }
                MatchTypeFilter.DOUBLES -> matches.filter { it.matchType.equals(MatchType.DOUBLES.name, ignoreCase = true) }
            }
            
        val groupedMatches: Map<String, List<MatchHistoryItem>>
            get() = filteredMatches.groupBy { it.dateHeader }
    }
    
    data class MatchHistoryItem(
        val matchId: Long,
        val matchType: String,
        val teamANames: List<String>,
        val teamBNames: List<String>,
        val teamAScore: Int,
        val teamBScore: Int,
        val setScores: List<Pair<Int, Int>>,
        val winnerTeam: String?,
        val dateHeader: String,
        val timeFormatted: String,
        val status: String,
        val timestamp: Long
    ) {
        val date: String get() = "$dateHeader $timeFormatted"
    }
    
    enum class MatchTypeFilter { ALL, SINGLES, DOUBLES }
}
