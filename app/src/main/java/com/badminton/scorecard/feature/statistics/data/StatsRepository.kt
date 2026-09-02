package com.badminton.scorecard.feature.statistics.data

import com.badminton.scorecard.core.database.dao.MatchDao
import com.badminton.scorecard.core.database.dao.PlayerDao
import com.badminton.scorecard.core.database.dao.StatsDao
import com.badminton.scorecard.core.database.entity.PlayerEntity
import com.badminton.scorecard.core.database.entity.PlayerStatsCacheEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class LeaderboardEntry(
    val rank: Int,
    val player: PlayerEntity,
    val stats: PlayerStatsCacheEntity,
    val winPercentage: Float
)

data class MatchCountByDate(
    val dateLabel: String,
    val count: Int
)

data class OverallStats(
    val totalMatches: Int,
    val totalPlayers: Int,
    val avgMatchDuration: Long?,
    val mostActivePlayer: PlayerEntity?,
    val mostActivePlayerMatches: Int
)

data class MatchTypeDistribution(
    val singlesCount: Int,
    val doublesCount: Int
)

typealias PartnershipWinRate = com.badminton.scorecard.core.database.dao.PartnershipWinRate

@Singleton
class StatsRepository @Inject constructor(
    private val playerDao: PlayerDao,
    private val matchDao: MatchDao,
    private val statsDao: StatsDao
) {
    fun getLeaderboard(): Flow<List<LeaderboardEntry>> {
        return combine(playerDao.getAllPlayers(), playerDao.getAllPlayerStats()) { players, stats ->
            val statsMap = stats.associateBy { it.playerId }
            players.mapNotNull { player ->
                val playerStats = statsMap[player.id] ?: return@mapNotNull null
                val winPercentage = if (playerStats.totalMatchesPlayed > 0) {
                    (playerStats.totalWins.toFloat() / playerStats.totalMatchesPlayed) * 100f
                } else {
                    0f
                }
                LeaderboardEntry(
                    rank = 0,
                    player = player,
                    stats = playerStats,
                    winPercentage = winPercentage
                )
            }.sortedWith(compareByDescending<LeaderboardEntry> { it.winPercentage }.thenByDescending { it.stats.totalMatchesPlayed })
             .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
        }
    }

    fun getMatchCountInRange(startTime: Long, endTime: Long): Flow<Int> {
        return matchDao.getCompletedMatchesInDateRange(startTime, endTime).map { it.size }
    }

    fun getMatchesOverTime(startTime: Long, endTime: Long): Flow<List<MatchCountByDate>> {
        return matchDao.getCompletedMatchesInDateRange(startTime, endTime).map { matches ->
            val format = SimpleDateFormat("MMM dd", Locale.getDefault())
            val grouped = matches.groupBy { format.format(Date(it.startedAt)) }
            grouped.map { entry ->
                MatchCountByDate(entry.key, entry.value.size)
            }.sortedBy { item ->
                val firstMatch = matches.find { format.format(Date(it.startedAt)) == item.dateLabel }
                firstMatch?.startedAt ?: 0L
            }
        }
    }

    fun getPartnershipStats(startTime: Long? = null, endTime: Long? = null): Flow<List<PartnershipWinRate>> {
        return if (startTime != null && endTime != null) {
            statsDao.getAllPartnershipStatsInDateRange(startTime, endTime)
        } else {
            statsDao.getPartnershipStats()
        }
    }

    fun getOverallStats(startTime: Long, endTime: Long): Flow<OverallStats> {
        return combine(
            matchDao.getCompletedMatchesInDateRange(startTime, endTime),
            playerDao.getAllPlayers(),
            playerDao.getAllPlayerStats()
        ) { matches, players, stats ->
            val totalMatches = matches.size
            val totalPlayers = players.size
            
            val validDurations = matches.mapNotNull { 
                if (it.endedAt != null && it.endedAt > it.startedAt) it.endedAt - it.startedAt else null 
            }
            val avgMatchDuration = if (validDurations.isNotEmpty()) {
                validDurations.sum() / validDurations.size
            } else {
                null
            }

            val statsMap = stats.associateBy { it.playerId }
            // MVP = Player with most wins, tie-breaker: win rate & total matches
            val mvpPlayer = players.filter { (statsMap[it.id]?.totalMatchesPlayed ?: 0) > 0 }
                .maxByOrNull { player ->
                    val s = statsMap[player.id]
                    val wins = s?.totalWins ?: 0
                    val total = s?.totalMatchesPlayed ?: 0
                    val winRate = if (total > 0) wins.toFloat() / total else 0f
                    wins * 1000f + winRate * 100f
                }
            val mvpWins = statsMap[mvpPlayer?.id]?.totalWins ?: 0
            
            OverallStats(
                totalMatches = totalMatches,
                totalPlayers = totalPlayers,
                avgMatchDuration = avgMatchDuration,
                mostActivePlayer = mvpPlayer,
                mostActivePlayerMatches = mvpWins
            )
        }
    }

    fun getMatchTypeDistribution(startTime: Long, endTime: Long): Flow<MatchTypeDistribution> {
        return matchDao.getCompletedMatchesInDateRange(startTime, endTime).map { matches ->
            val singlesCount = matches.count { it.matchType == "SINGLES" }
            val doublesCount = matches.count { it.matchType == "DOUBLES" }
            MatchTypeDistribution(singlesCount, doublesCount)
        }
    }
}
