package com.badminton.scorecard.feature.match_summary.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.badminton.scorecard.core.database.dao.MatchDao
import com.badminton.scorecard.core.database.dao.PlayerDao
import com.badminton.scorecard.core.rules.TeamSide
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchSummaryViewModel @Inject constructor(
    private val matchDao: MatchDao,
    private val playerDao: PlayerDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val matchId: Long = savedStateHandle.get<Long>("matchId") ?: 0L

    private val _uiState = MutableStateFlow(MatchSummaryUiState())
    val uiState: StateFlow<MatchSummaryUiState> = _uiState.asStateFlow()

    init {
        loadMatchDetails()
    }

    private fun loadMatchDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val match = matchDao.getMatchById(matchId).first()
            
            if (match == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            val crossRefs = matchDao.getMatchPlayersForMatch(matchId).first()
            val allPlayers = playerDao.getAllPlayers().first().associateBy { it.id }

            val teamAPlayerIds = crossRefs.filter { it.team == "TEAM_A" }.map { it.playerId }
            val teamBPlayerIds = crossRefs.filter { it.team == "TEAM_B" }.map { it.playerId }

            val teamANames = teamAPlayerIds.mapNotNull { allPlayers[it]?.name }
            val teamBNames = teamBPlayerIds.mapNotNull { allPlayers[it]?.name }

            val sets = matchDao.getSetsForMatch(matchId).first()
            val events = matchDao.getEventsForMatch(matchId).first()
            
            val setScoreDisplays = sets.map { setInfo ->
                SetScoreDisplay(
                    setNumber = setInfo.setNumber,
                    teamAScore = setInfo.teamAScore,
                    teamBScore = setInfo.teamBScore,
                    winner = if (setInfo.teamAScore > setInfo.teamBScore) "Team A" else if (setInfo.teamBScore > setInfo.teamAScore) "Team B" else null
                )
            }
            
            var teamAScore = 0
            var teamBScore = 0
            var teamAServePoints = 0
            var teamAReturnPoints = 0
            var teamBServePoints = 0
            var teamBReturnPoints = 0
            var currentTeamAStreak = 0
            var currentTeamBStreak = 0
            var maxTeamAStreak = 0
            var maxTeamBStreak = 0
            
            val scoreProgression = mutableListOf<ScorePoint>()
            val momentumData = mutableListOf<Int>()
            
            scoreProgression.add(ScorePoint(0, 0, 0))
            momentumData.add(0)
            
            events.forEachIndexed { index, event ->
                val rallyNumber = index + 1
                val isTeamAScorer = event.scoringTeam == TeamSide.TEAM_A.name
                val isServerTeamA = teamAPlayerIds.contains(event.servingPlayerId)
                
                if (isTeamAScorer) {
                    teamAScore++
                    currentTeamAStreak++
                    currentTeamBStreak = 0
                    if (currentTeamAStreak > maxTeamAStreak) maxTeamAStreak = currentTeamAStreak
                    
                    if (isServerTeamA) teamAServePoints++ else teamAReturnPoints++
                } else {
                    teamBScore++
                    currentTeamBStreak++
                    currentTeamAStreak = 0
                    if (currentTeamBStreak > maxTeamBStreak) maxTeamBStreak = currentTeamBStreak
                    
                    if (!isServerTeamA) teamBServePoints++ else teamBReturnPoints++
                }
                
                scoreProgression.add(ScorePoint(rallyNumber, teamAScore, teamBScore))
                momentumData.add(teamAScore - teamBScore)
            }
            
            val durationMs = (match.endedAt ?: System.currentTimeMillis()) - match.startedAt
            val durationMins = durationMs / (1000 * 60)
            val matchDuration = "$durationMins min"
            
            _uiState.update {
                it.copy(
                    matchId = matchId,
                    matchType = match.matchType,
                    winnerTeam = match.winnerTeam,
                    teamANames = teamANames,
                    teamBNames = teamBNames,
                    setScores = setScoreDisplays,
                    scoreProgression = scoreProgression,
                    teamAServePoints = teamAServePoints,
                    teamAReturnPoints = teamAReturnPoints,
                    teamBServePoints = teamBServePoints,
                    teamBReturnPoints = teamBReturnPoints,
                    teamALongestStreak = maxTeamAStreak,
                    teamBLongestStreak = maxTeamBStreak,
                    momentumData = momentumData,
                    totalRallies = events.size,
                    matchDuration = matchDuration,
                    durationSeconds = durationMs / 1000,
                    isLoading = false
                )
            }
        }
    }

    data class MatchSummaryUiState(
        val matchId: Long = 0L,
        val matchType: String = "",
        val winnerTeam: String? = null,
        val teamANames: List<String> = emptyList(),
        val teamBNames: List<String> = emptyList(),
        val setScores: List<SetScoreDisplay> = emptyList(),
        val scoreProgression: List<ScorePoint> = emptyList(),
        val teamAServePoints: Int = 0,
        val teamAReturnPoints: Int = 0,
        val teamBServePoints: Int = 0,
        val teamBReturnPoints: Int = 0,
        val teamALongestStreak: Int = 0,
        val teamBLongestStreak: Int = 0,
        val momentumData: List<Int> = emptyList(),
        val totalRallies: Int = 0,
        val matchDuration: String = "",
        val durationSeconds: Long = 0L,
        val isLoading: Boolean = true
    )

    data class SetScoreDisplay(
        val setNumber: Int,
        val teamAScore: Int,
        val teamBScore: Int,
        val winner: String?
    )

    data class ScorePoint(
        val rallyNumber: Int,
        val teamAScore: Int,
        val teamBScore: Int
    )
}
