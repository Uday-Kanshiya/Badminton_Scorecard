package com.badminton.scorecard.feature.match_setup.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.badminton.scorecard.core.database.dao.MatchDao
import com.badminton.scorecard.core.database.dao.PlayerDao
import com.badminton.scorecard.core.database.entity.MatchEntity
import com.badminton.scorecard.core.database.entity.MatchPlayerCrossRef
import com.badminton.scorecard.core.database.entity.PlayerEntity
import com.badminton.scorecard.core.database.entity.SetEntity
import com.badminton.scorecard.core.rules.MatchType
import com.badminton.scorecard.core.rules.TeamSide
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MatchSetupUiState(
    val matchType: MatchType = MatchType.DOUBLES,
    val availablePlayers: List<PlayerEntity> = emptyList(),
    val teamAPlayer1: PlayerEntity? = null,
    val teamAPlayer2: PlayerEntity? = null,
    val teamBPlayer1: PlayerEntity? = null,
    val teamBPlayer2: PlayerEntity? = null,
    val targetPoints: Int = 21,
    val customPointsText: String = "",
    val bestOfSets: Int = 1,
    val firstServe: TeamSide = TeamSide.TEAM_A,
    val isSkunkRuleActive: Boolean = true,
    val serviceRotationEnabled: Boolean = true,
    val playerPointAttribution: Boolean = false,
    val isFormValid: Boolean = false,
    val isCreatingMatch: Boolean = false,
    val createdMatchId: Long? = null
)

@HiltViewModel
class MatchSetupViewModel @Inject constructor(
    private val playerDao: PlayerDao,
    private val matchDao: MatchDao,
    private val themePreferences: com.badminton.scorecard.core.preferences.ThemePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchSetupUiState())
    val uiState: StateFlow<MatchSetupUiState> = _uiState.asStateFlow()

    init {
        loadPlayers()
        viewModelScope.launch {
            themePreferences.defaultPlayerPointAttribution.collect { default ->
                _uiState.update { it.copy(playerPointAttribution = default) }
            }
        }
    }

    private fun loadPlayers() {
        viewModelScope.launch {
            playerDao.getAllPlayers().collect { players ->
                _uiState.update { 
                    it.copy(availablePlayers = players) 
                }.also { validateForm() }
            }
        }
    }

    private fun validateForm() {
        _uiState.update { state ->
            val pA1 = state.teamAPlayer1
            val pA2 = state.teamAPlayer2
            val pB1 = state.teamBPlayer1
            val pB2 = state.teamBPlayer2

            val players = mutableListOf<PlayerEntity>()
            if (pA1 != null) players.add(pA1)
            if (pB1 != null) players.add(pB1)

            if (state.matchType == MatchType.DOUBLES) {
                if (pA2 != null) players.add(pA2)
                if (pB2 != null) players.add(pB2)
            }

            val allFilled = if (state.matchType == MatchType.SINGLES) {
                pA1 != null && pB1 != null
            } else {
                pA1 != null && pA2 != null && pB1 != null && pB2 != null
            }

            val noDuplicates = players.size == players.distinctBy { it.id }.size
            val isPointsValid = state.targetPoints > 0

            val skunk = state.matchType == MatchType.DOUBLES && state.targetPoints == 21

            state.copy(
                isFormValid = allFilled && noDuplicates && isPointsValid,
                isSkunkRuleActive = skunk
            )
        }
    }

    fun onMatchTypeChanged(matchType: MatchType) {
        _uiState.update { it.copy(matchType = matchType) }
        validateForm()
    }

    fun onPlayerSelected(team: TeamSide, position: Int, player: PlayerEntity) {
        _uiState.update { state ->
            when (team) {
                TeamSide.TEAM_A -> {
                    if (position == 1) state.copy(teamAPlayer1 = player)
                    else state.copy(teamAPlayer2 = player)
                }
                TeamSide.TEAM_B -> {
                    if (position == 1) state.copy(teamBPlayer1 = player)
                    else state.copy(teamBPlayer2 = player)
                }
            }
        }
        validateForm()
    }

    fun onTargetPointsSelected(points: Int) {
        _uiState.update { it.copy(targetPoints = points, customPointsText = "") }
        validateForm()
    }

    fun onCustomPointsChanged(pointsText: String) {
        val points = pointsText.toIntOrNull() ?: 0
        _uiState.update { it.copy(targetPoints = points, customPointsText = pointsText) }
        validateForm()
    }

    fun onBestOfSetsSelected(sets: Int) {
        _uiState.update { it.copy(bestOfSets = sets) }
    }

    fun onFirstServeChanged(team: TeamSide) {
        _uiState.update { it.copy(firstServe = team) }
    }

    fun onServiceRotationChanged(enabled: Boolean) {
        _uiState.update { it.copy(serviceRotationEnabled = enabled) }
    }

    fun onPlayerPointAttributionChanged(enabled: Boolean) {
        _uiState.update { it.copy(playerPointAttribution = enabled) }
    }

    fun onStartMatch() {
        val state = _uiState.value
        if (!state.isFormValid) return

        _uiState.update { it.copy(isCreatingMatch = true) }

        viewModelScope.launch {
            try {
                // 1. Insert MatchEntity
                val match = MatchEntity(
                    matchType = state.matchType.name,
                    targetPoints = state.targetPoints,
                    bestOfSets = state.bestOfSets,
                    skunkRuleEnabled = state.isSkunkRuleActive,
                    serviceRotationEnabled = state.serviceRotationEnabled,
                    playerPointAttribution = state.playerPointAttribution,
                    status = "IN_PROGRESS"
                )
                val matchId = matchDao.insertMatch(match)

                // 2. Insert MatchPlayerCrossRef
                val refs = mutableListOf<MatchPlayerCrossRef>()
                state.teamAPlayer1?.let {
                    refs.add(MatchPlayerCrossRef(matchId, it.id, "TEAM_A", 1))
                }
                if (state.matchType == MatchType.DOUBLES) {
                    state.teamAPlayer2?.let {
                        refs.add(MatchPlayerCrossRef(matchId, it.id, "TEAM_A", 2))
                    }
                }
                state.teamBPlayer1?.let {
                    refs.add(MatchPlayerCrossRef(matchId, it.id, "TEAM_B", 1))
                }
                if (state.matchType == MatchType.DOUBLES) {
                    state.teamBPlayer2?.let {
                        refs.add(MatchPlayerCrossRef(matchId, it.id, "TEAM_B", 2))
                    }
                }
                matchDao.insertMatchPlayers(*refs.toTypedArray())

                // 3. Insert initial SetEntity
                val initialServerPlayerId = if (state.firstServe == TeamSide.TEAM_A) {
                    state.teamAPlayer1!!.id
                } else {
                    state.teamBPlayer1!!.id
                }

                val initialSet = SetEntity(
                    matchId = matchId,
                    setNumber = 1,
                    initialServerPlayerId = initialServerPlayerId
                )
                matchDao.insertSet(initialSet)

                // 4. Set createdMatchId
                _uiState.update {
                    it.copy(
                        isCreatingMatch = false,
                        createdMatchId = matchId
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isCreatingMatch = false) }
            }
        }
    }
}
