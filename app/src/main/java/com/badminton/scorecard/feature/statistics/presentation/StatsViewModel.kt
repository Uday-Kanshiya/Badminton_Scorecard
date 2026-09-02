package com.badminton.scorecard.feature.statistics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.badminton.scorecard.core.designsystem.components.TimePeriod
import com.badminton.scorecard.feature.statistics.data.LeaderboardEntry
import com.badminton.scorecard.feature.statistics.data.MatchCountByDate
import com.badminton.scorecard.feature.statistics.data.MatchTypeDistribution
import com.badminton.scorecard.feature.statistics.data.OverallStats
import com.badminton.scorecard.feature.statistics.data.PartnershipWinRate
import com.badminton.scorecard.feature.statistics.data.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository
) : ViewModel() {

    data class StatsUiState(
        val selectedPeriod: TimePeriod = TimePeriod.ALL_TIME,
        val overallStats: OverallStats? = null,
        val leaderboard: List<LeaderboardEntry> = emptyList(),
        val matchTypeDistribution: MatchTypeDistribution? = null,
        val matchesOverTime: List<MatchCountByDate> = emptyList(),
        val partnerships: List<PartnershipWinRate> = emptyList(),
        val isLoading: Boolean = true
    )

    private val selectedPeriodFlow = MutableStateFlow(TimePeriod.ALL_TIME)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StatsUiState> = selectedPeriodFlow.flatMapLatest { period ->
        val (startTime, endTime) = getDateRangeForPeriod(period)

        combine(
            statsRepository.getOverallStats(startTime, endTime),
            statsRepository.getLeaderboard(), // Currently leaderboard is all-time in this impl
            statsRepository.getMatchTypeDistribution(startTime, endTime),
            statsRepository.getMatchesOverTime(startTime, endTime),
            statsRepository.getPartnershipStats(
                if (period == TimePeriod.ALL_TIME) null else startTime, 
                if (period == TimePeriod.ALL_TIME) null else endTime
            )
        ) { overall, leaderboard, matchType, matchesOverTime, partnerships ->
            StatsUiState(
                selectedPeriod = period,
                overallStats = overall,
                leaderboard = leaderboard,
                matchTypeDistribution = matchType,
                matchesOverTime = matchesOverTime,
                partnerships = partnerships,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )

    fun onPeriodSelected(period: TimePeriod) {
        selectedPeriodFlow.value = period
    }

    private fun getDateRangeForPeriod(period: TimePeriod): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        
        val startTime = when (period) {
            TimePeriod.DAILY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                calendar.timeInMillis
            }
            TimePeriod.WEEKLY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                calendar.timeInMillis
            }
            TimePeriod.MONTHLY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                calendar.timeInMillis
            }
            TimePeriod.ALL_TIME -> 0L
        }
        return Pair(startTime, endTime)
    }
}
