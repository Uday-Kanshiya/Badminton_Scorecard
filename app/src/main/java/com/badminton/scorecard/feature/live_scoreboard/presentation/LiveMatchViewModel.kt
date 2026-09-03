package com.badminton.scorecard.feature.live_scoreboard.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.badminton.scorecard.core.database.dao.MatchDao
import com.badminton.scorecard.core.database.dao.PlayerDao
import com.badminton.scorecard.core.database.entity.MatchEventEntity
import com.badminton.scorecard.core.database.entity.PlayerStatsCacheEntity
import com.badminton.scorecard.core.database.entity.SetEntity
import com.badminton.scorecard.core.rules.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import com.badminton.scorecard.core.sync.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LiveMatchViewModel @Inject constructor(
    private val matchDao: MatchDao,
    private val playerDao: PlayerDao,
    private val syncManager: SyncManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val matchId: Long = savedStateHandle.get<Long>("matchId") ?: 0L
    private val rulesEngine = BadmintonRulesEngine()

    private val _uiState = MutableStateFlow(LiveMatchUiState(matchId = matchId))
    val uiState: StateFlow<LiveMatchUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LiveMatchEvent>()
    val events: SharedFlow<LiveMatchEvent> = _events.asSharedFlow()

    init {
        loadMatch()
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_uiState.value.isPaused && !_uiState.value.isMatchComplete) {
                    _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                }
            }
        }
    }

    private fun loadMatch() {
        viewModelScope.launch {
            val matchEntity = matchDao.getMatchById(matchId).firstOrNull() ?: return@launch
            val crossRefs = matchDao.getMatchPlayersForMatch(matchId).firstOrNull() ?: emptyList()
            val sets = matchDao.getSetsForMatch(matchId).firstOrNull() ?: emptyList()
            val currentSet = sets.lastOrNull() ?: return@launch

            val teamAPlayers = mutableListOf<PlayerInfo>()
            val teamBPlayers = mutableListOf<PlayerInfo>()
            val teamANames = mutableListOf<String>()
            val teamBNames = mutableListOf<String>()

            crossRefs.forEach { ref ->
                val player = playerDao.getPlayerById(ref.playerId).firstOrNull()
                if (player != null) {
                    val info = PlayerInfo(player.id, player.name)
                    if (ref.team == "TEAM_A") {
                        teamAPlayers.add(info)
                        teamANames.add(player.name)
                    } else {
                        teamBPlayers.add(info)
                        teamBNames.add(player.name)
                    }
                }
            }

            val firstServingTeam = if (teamAPlayers.any { it.id == currentSet.initialServerPlayerId }) {
                TeamSide.TEAM_A
            } else {
                TeamSide.TEAM_B
            }

            var state = rulesEngine.createInitialState(
                matchType = MatchType.valueOf(matchEntity.matchType),
                targetPoints = matchEntity.targetPoints,
                bestOfSets = matchEntity.bestOfSets,
                teamAPlayers = teamAPlayers.sortedBy { it.id },
                teamBPlayers = teamBPlayers.sortedBy { it.id },
                firstServingTeam = firstServingTeam,
                serviceRotationEnabled = matchEntity.serviceRotationEnabled
            )

            // Replay events if any
            val events = matchDao.getEventsForSet(currentSet.id).firstOrNull() ?: emptyList()
            events.forEach { event ->
                state = rulesEngine.recordPoint(state, TeamSide.valueOf(event.scoringTeam))
            }

            _uiState.update {
                it.copy(
                    matchType = MatchType.valueOf(matchEntity.matchType),
                    gameState = state,
                    teamAPlayerNames = teamANames,
                    teamBPlayerNames = teamBNames,
                    currentSetId = currentSet.id,
                    canUndo = rulesEngine.canUndo(),
                    playerPointAttribution = matchEntity.playerPointAttribution
                )
            }
        }
    }

    fun onTeamScored(team: TeamSide) {
        val state = _uiState.value
        val currentState = state.gameState ?: return
        if (currentState.isMatchOver) return

        // If player point attribution is enabled for doubles, show dialog first
        if (state.playerPointAttribution && state.matchType == MatchType.DOUBLES) {
            _uiState.update { it.copy(showScoringPlayerDialog = true, pendingScoringTeam = team) }
            return
        }

        recordPoint(team, scoringPlayerId = null)
    }

    private fun recordPoint(team: TeamSide, scoringPlayerId: Long?) {
        val state = _uiState.value
        val currentState = state.gameState ?: return
        if (currentState.isMatchOver) return

        viewModelScope.launch {
            val newState = rulesEngine.recordPoint(currentState, team)

            val event = MatchEventEntity(
                setId = state.currentSetId,
                rallyNumber = newState.rallyNumber,
                scoringTeam = team.name,
                servingPlayerId = currentState.serverPlayer.id,
                serverCourt = currentState.serverCourt.name,
                teamAScoreAfter = newState.teamA.score,
                teamBScoreAfter = newState.teamB.score,
                scoringPlayerId = scoringPlayerId
            )
            matchDao.insertEvent(event)

            var nextState = newState
            var currentSetId = state.currentSetId

            if (newState.isSetOver) {
                // Update set entity
                val sets = matchDao.getSetsForMatch(matchId).firstOrNull() ?: emptyList()
                val currentSet = sets.lastOrNull()
                if (currentSet != null) {
                    val winner = if (newState.teamA.score > newState.teamB.score) "TEAM_A" else "TEAM_B"
                    matchDao.updateSet(
                        currentSet.copy(
                            teamAScore = newState.teamA.score,
                            teamBScore = newState.teamB.score,
                            winnerTeam = winner,
                            endedAt = System.currentTimeMillis()
                        )
                    )
                }

                if (newState.isMatchOver) {
                    completeMatch(newState)
                } else {
                    nextState = rulesEngine.startNewSet(newState)
                    val newSet = SetEntity(
                        matchId = matchId,
                        setNumber = nextState.currentSetNumber,
                        initialServerPlayerId = nextState.serverPlayer.id
                    )
                    currentSetId = matchDao.insertSet(newSet)
                }
            }

            val skunk = (newState.isMatchOver || newState.isSetOver) && newState.skunkRuleEnabled &&
                    ((newState.teamA.score == 7 && newState.teamB.score == 0) ||
                            (newState.teamB.score == 7 && newState.teamA.score == 0))

            _uiState.update {
                it.copy(
                    gameState = nextState,
                    currentSetId = currentSetId,
                    canUndo = rulesEngine.canUndo(),
                    isMatchComplete = newState.isMatchOver,
                    isSkunkVictory = skunk
                )
            }
            
            // If match is over (whether regular win or skunk instant victory)
            // Complete match is already called above, now prepare navigation
            if (newState.isMatchOver) {
                // If not skunk, navigate directly. If skunk, user will see the celebration modal with "View Summary" or auto-navigate
                _events.emit(LiveMatchEvent.NavigateToSummary(matchId))
            }
        }
    }

    private suspend fun completeMatch(finalState: BadmintonLiveState) {
        val matchEntity = matchDao.getMatchById(matchId).firstOrNull() ?: return
        val winnerTeam = finalState.matchWinner?.name
        
        matchDao.updateMatch(
            matchEntity.copy(
                status = "COMPLETED",
                winnerTeam = winnerTeam,
                endedAt = System.currentTimeMillis()
            )
        )

        updatePlayerStats(finalState, winnerTeam)
        try {
            syncManager.syncMatch(matchId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun updatePlayerStats(finalState: BadmintonLiveState, winnerTeam: String?) {
        val crossRefs = matchDao.getMatchPlayersForMatch(matchId).firstOrNull() ?: return
        val matchEvents = matchDao.getEventsForMatch(matchId).firstOrNull() ?: return

        for (ref in crossRefs) {
            var stats = playerDao.getPlayerStats(ref.playerId).firstOrNull()
                ?: PlayerStatsCacheEntity(playerId = ref.playerId)

            val won = ref.team == winnerTeam
            val isSingles = finalState.matchType == MatchType.SINGLES

            val serveEvents = matchEvents.filter { it.servingPlayerId == ref.playerId }
            val pointsOnServe = serveEvents.count { it.scoringTeam == ref.team }
            
            val opponentServeEvents = matchEvents.filter { 
                it.servingPlayerId != ref.playerId && 
                crossRefs.find { cr -> cr.playerId == it.servingPlayerId }?.team != ref.team
            }
            val pointsOnReturn = opponentServeEvents.count { it.scoringTeam == ref.team }

            val myTeamEvents = matchEvents.filter { it.scoringTeam == ref.team }

            stats = stats.copy(
                totalMatchesPlayed = stats.totalMatchesPlayed + 1,
                totalWins = if (won) stats.totalWins + 1 else stats.totalWins,
                totalLosses = if (!won) stats.totalLosses + 1 else stats.totalLosses,
                singlesPlayed = if (isSingles) stats.singlesPlayed + 1 else stats.singlesPlayed,
                singlesWon = if (isSingles && won) stats.singlesWon + 1 else stats.singlesWon,
                doublesPlayed = if (!isSingles) stats.doublesPlayed + 1 else stats.doublesPlayed,
                doublesWon = if (!isSingles && won) stats.doublesWon + 1 else stats.doublesWon,
                totalPointsScored = stats.totalPointsScored + myTeamEvents.size,
                totalPointsOnServe = stats.totalPointsOnServe + pointsOnServe,
                totalServeRallies = stats.totalServeRallies + serveEvents.size,
                totalPointsOnReturn = stats.totalPointsOnReturn + pointsOnReturn,
                totalReturnRallies = stats.totalReturnRallies + opponentServeEvents.size,
                individualPointsScored = stats.individualPointsScored + matchEvents.count { it.scoringPlayerId == ref.playerId },
                lastUpdated = System.currentTimeMillis()
            )
            playerDao.insertOrUpdateStats(stats)
        }
    }

    fun onUndo() {
        val currState = _uiState.value.gameState ?: return
        val prevState = rulesEngine.undo(currState) ?: return
        _uiState.update {
            it.copy(
                gameState = prevState,
                canUndo = rulesEngine.canUndo(),
                canRedo = rulesEngine.canRedo()
            )
        }
    }

    fun onRedo() {
        val currState = _uiState.value.gameState ?: return
        val nextState = rulesEngine.redo(currState) ?: return
        _uiState.update {
            it.copy(
                gameState = nextState,
                canUndo = rulesEngine.canUndo(),
                canRedo = rulesEngine.canRedo()
            )
        }
    }

    fun onSwapPositions(team: TeamSide) {
        val currState = _uiState.value.gameState ?: return
        val newState = rulesEngine.swapTeamPositions(currState, team)
        _uiState.update {
            it.copy(
                gameState = newState,
                canUndo = rulesEngine.canUndo(),
                canRedo = rulesEngine.canRedo()
            )
        }
    }

    fun onToggleServingSide() {
        val currState = _uiState.value.gameState ?: return
        val newState = rulesEngine.toggleServingTeam(currState)
        _uiState.update {
            it.copy(
                gameState = newState,
                canUndo = rulesEngine.canUndo(),
                canRedo = rulesEngine.canRedo()
            )
        }
    }

    fun onSetServer(player: PlayerInfo) {
        val currState = _uiState.value.gameState ?: return
        val newState = rulesEngine.setServer(currState, player)
        _uiState.update {
            it.copy(
                gameState = newState,
                canUndo = rulesEngine.canUndo(),
                canRedo = rulesEngine.canRedo()
            )
        }
    }

    fun onToggleCourtSides() {
        val currState = _uiState.value.gameState ?: return
        val newState = rulesEngine.toggleCourtSides(currState)
        _uiState.update {
            it.copy(gameState = newState)
        }
    }

    fun onToggleAnnouncer() {
        _uiState.update { it.copy(isAnnouncerVisible = !it.isAnnouncerVisible) }
    }

    fun onPauseToggle() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
        viewModelScope.launch {
            val match = matchDao.getMatchById(matchId).firstOrNull()
            if (match != null) {
                val status = if (_uiState.value.isPaused) "PAUSED" else "IN_PROGRESS"
                matchDao.updateMatch(match.copy(status = status))
            }
        }
    }

    fun onViewSummary() {
        viewModelScope.launch {
            _events.emit(LiveMatchEvent.NavigateToSummary(matchId))
        }
    }

    fun onEndMatchRequested() {
        if (_uiState.value.isMatchComplete) {
            onViewSummary()
        } else {
            _uiState.update { it.copy(showEndMatchDialog = true) }
        }
    }

    fun onEndMatchConfirmed() {
        viewModelScope.launch {
            val match = matchDao.getMatchById(matchId).firstOrNull()
            if (match != null && match.status != "COMPLETED") {
                matchDao.updateMatch(match.copy(status = "ABANDONED", endedAt = System.currentTimeMillis()))
                _events.emit(LiveMatchEvent.NavigateBack)
            } else {
                _events.emit(LiveMatchEvent.NavigateToSummary(matchId))
            }
        }
    }

    fun onEndMatchDismissed() {
        _uiState.update { it.copy(showEndMatchDialog = false) }
    }

    fun onScoringPlayerSelected(player: PlayerInfo) {
        val team = _uiState.value.pendingScoringTeam ?: return
        _uiState.update { it.copy(showScoringPlayerDialog = false, pendingScoringTeam = null) }
        recordPoint(team, scoringPlayerId = player.id)
    }

    fun onScoringPlayerDialogDismissed() {
        _uiState.update { it.copy(showScoringPlayerDialog = false, pendingScoringTeam = null) }
    }
}
