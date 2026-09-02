package com.badminton.scorecard.feature.player.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.badminton.scorecard.core.database.dao.PartnershipWinRate
import com.badminton.scorecard.core.database.dao.ServeStats
import com.badminton.scorecard.core.database.entity.PlayerEntity
import com.badminton.scorecard.core.database.entity.PlayerStatsCacheEntity
import com.badminton.scorecard.core.designsystem.components.TimePeriod
import com.badminton.scorecard.feature.player.data.DateRangeStats
import com.badminton.scorecard.feature.player.data.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlayerProfileViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val playerId: Long = savedStateHandle.get<Long>("playerId") ?: 0L

    data class PlayerProfileUiState(
        val player: PlayerEntity? = null,
        val stats: PlayerStatsCacheEntity? = null,
        val serveStats: ServeStats? = null,
        val partnerships: List<PartnershipWinRate> = emptyList(),
        val selectedPeriod: TimePeriod = TimePeriod.ALL_TIME,
        val periodStats: DateRangeStats? = null,
        val isLoading: Boolean = true
    )

    private val _selectedPeriod = MutableStateFlow(TimePeriod.ALL_TIME)

    private val periodStatsFlow = _selectedPeriod.flatMapLatest { period ->
        val now = System.currentTimeMillis()
        val startTime = when (period) {
            TimePeriod.DAILY -> now - 24 * 60 * 60 * 1000L
            TimePeriod.WEEKLY -> now - 7 * 24 * 60 * 60 * 1000L
            TimePeriod.MONTHLY -> now - 30 * 24 * 60 * 60 * 1000L
            TimePeriod.ALL_TIME -> 0L
        }
        playerRepository.getPlayerStatsInDateRange(playerId, startTime, now)
    }

    val uiState: StateFlow<PlayerProfileUiState> = combine(
        playerRepository.getPlayerById(playerId),
        playerRepository.getPlayerStats(playerId),
        playerRepository.getServeStatsForPlayer(playerId),
        playerRepository.getPartnershipStatsForPlayer(playerId),
        _selectedPeriod,
        periodStatsFlow
    ) { flows: Array<Any?> ->
        val player = flows[0] as PlayerEntity?
        val stats = flows[1] as PlayerStatsCacheEntity?
        val serveStats = flows[2] as ServeStats?
        @Suppress("UNCHECKED_CAST")
        val partnerships = flows[3] as List<PartnershipWinRate>
        val selectedPeriod = flows[4] as TimePeriod
        val periodStats = flows[5] as DateRangeStats?

        PlayerProfileUiState(
            player = player,
            stats = stats,
            serveStats = serveStats,
            partnerships = partnerships,
            selectedPeriod = selectedPeriod,
            periodStats = periodStats,
            isLoading = player == null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerProfileUiState()
    )

    fun onPeriodSelected(period: TimePeriod) {
        _selectedPeriod.value = period
    }

    fun updatePlayer(newName: String, newNickname: String?) {
        viewModelScope.launch {
            val currentPlayer = uiState.value.player ?: return@launch
            playerRepository.updatePlayer(
                currentPlayer.copy(
                    name = newName.trim(),
                    nickname = newNickname?.trim()?.ifBlank { null }
                )
            )
        }
    }

    fun deletePlayer(onDeleted: () -> Unit) {
        viewModelScope.launch {
            val currentPlayer = uiState.value.player ?: return@launch
            playerRepository.deletePlayer(currentPlayer)
            onDeleted()
        }
    }
}
