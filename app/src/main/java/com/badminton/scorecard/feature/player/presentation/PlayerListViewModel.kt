package com.badminton.scorecard.feature.player.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.badminton.scorecard.core.database.entity.PlayerEntity
import com.badminton.scorecard.core.database.entity.PlayerStatsCacheEntity
import com.badminton.scorecard.feature.player.data.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerListViewModel @Inject constructor(
    private val playerRepository: PlayerRepository
) : ViewModel() {

    data class PlayerListUiState(
        val players: List<PlayerWithStats> = emptyList(),
        val searchQuery: String = "",
        val isLoading: Boolean = true,
        val showAddPlayerDialog: Boolean = false
    )

    data class PlayerWithStats(
        val player: PlayerEntity,
        val stats: PlayerStatsCacheEntity?
    )

    private val _searchQuery = MutableStateFlow("")
    private val _showAddPlayerDialog = MutableStateFlow(false)

    val uiState: StateFlow<PlayerListUiState> = combine(
        playerRepository.getAllPlayers(),
        playerRepository.getAllPlayerStats(),
        _searchQuery,
        _showAddPlayerDialog
    ) { players, stats, query, showDialog ->
        val filteredPlayers = if (query.isBlank()) {
            players
        } else {
            players.filter {
                it.name.contains(query, ignoreCase = true) ||
                (it.nickname?.contains(query, ignoreCase = true) == true)
            }
        }

        val playersWithStats = filteredPlayers.map { player ->
            PlayerWithStats(
                player = player,
                stats = stats.find { it.playerId == player.id }
            )
        }

        PlayerListUiState(
            players = playersWithStats,
            searchQuery = query,
            isLoading = false,
            showAddPlayerDialog = showDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerListUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onAddPlayerClicked() {
        _showAddPlayerDialog.value = true
    }

    fun onDismissAddDialog() {
        _showAddPlayerDialog.value = false
    }

    fun onPlayerAdded(name: String, nickname: String?) {
        viewModelScope.launch {
            playerRepository.addPlayer(name, nickname)
            _showAddPlayerDialog.value = false
        }
    }

    fun onUpdatePlayer(player: PlayerEntity, newName: String, newNickname: String?) {
        viewModelScope.launch {
            playerRepository.updatePlayer(
                player.copy(
                    name = newName.trim(),
                    nickname = newNickname?.trim()?.ifBlank { null }
                )
            )
        }
    }

    fun onDeletePlayer(player: PlayerEntity) {
        viewModelScope.launch {
            playerRepository.deletePlayer(player)
        }
    }
}
