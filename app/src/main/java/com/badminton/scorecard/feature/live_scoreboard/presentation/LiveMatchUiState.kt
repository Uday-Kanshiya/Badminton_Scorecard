package com.badminton.scorecard.feature.live_scoreboard.presentation

import com.badminton.scorecard.core.rules.BadmintonLiveState
import com.badminton.scorecard.core.rules.MatchType

data class LiveMatchUiState(
    val matchId: Long = 0,
    val matchType: MatchType = MatchType.SINGLES,
    val gameState: BadmintonLiveState? = null,
    val teamAPlayerNames: List<String> = emptyList(),
    val teamBPlayerNames: List<String> = emptyList(),
    val currentSetId: Long = 0,
    val isPaused: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val elapsedSeconds: Long = 0,
    val isAnnouncerVisible: Boolean = true,
    val showEndMatchDialog: Boolean = false,
    val isMatchComplete: Boolean = false,
    val isSkunkVictory: Boolean = false,
    val playerPointAttribution: Boolean = false,
    val showScoringPlayerDialog: Boolean = false,
    val pendingScoringTeam: com.badminton.scorecard.core.rules.TeamSide? = null
)

sealed class LiveMatchEvent {
    data class NavigateToSummary(val matchId: Long) : LiveMatchEvent()
    object NavigateBack : LiveMatchEvent()
}
