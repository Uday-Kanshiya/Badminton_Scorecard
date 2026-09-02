package com.badminton.scorecard.feature.player.data

import com.badminton.scorecard.core.database.dao.MatchDao
import com.badminton.scorecard.core.database.dao.PlayerDao
import com.badminton.scorecard.core.database.dao.StatsDao
import com.badminton.scorecard.core.database.dao.PartnershipWinRate
import com.badminton.scorecard.core.database.dao.ServeStats
import com.badminton.scorecard.core.database.entity.MatchEntity
import com.badminton.scorecard.core.database.entity.PlayerEntity
import com.badminton.scorecard.core.database.entity.PlayerStatsCacheEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class DateRangeStats(
    val matchesPlayed: Int,
    val wins: Int,
    val losses: Int
)

@Singleton
class PlayerRepository @Inject constructor(
    private val playerDao: PlayerDao,
    private val matchDao: MatchDao,
    private val statsDao: StatsDao
) {
    fun getAllPlayers(): Flow<List<PlayerEntity>> = playerDao.getAllPlayers()

    fun getPlayerById(id: Long): Flow<PlayerEntity?> = playerDao.getPlayerById(id)

    fun searchPlayers(query: String): Flow<List<PlayerEntity>> = playerDao.searchPlayersByName(query)

    suspend fun addPlayer(name: String, nickname: String?): Long {
        return playerDao.insertPlayer(PlayerEntity(name = name, nickname = nickname))
    }

    suspend fun updatePlayer(player: PlayerEntity) {
        playerDao.updatePlayer(player)
    }

    suspend fun deletePlayer(player: PlayerEntity) {
        playerDao.deletePlayer(player)
    }

    fun getPlayerStats(playerId: Long): Flow<PlayerStatsCacheEntity?> = playerDao.getPlayerStats(playerId)

    fun getAllPlayerStats(): Flow<List<PlayerStatsCacheEntity>> = playerDao.getAllPlayerStats()

    fun getPlayerMatches(playerId: Long): Flow<List<MatchEntity>> = matchDao.getMatchesByPlayerIdCompleted(playerId)

    fun getPartnershipStatsForPlayer(playerId: Long): Flow<List<PartnershipWinRate>> = statsDao.getPartnershipStatsForPlayer(playerId)

    fun getServeStatsForPlayer(playerId: Long): Flow<ServeStats> {
        return matchDao.getMatchesByPlayerIdCompleted(playerId).flatMapLatest { matches ->
            val matchIds = matches.map { it.id }
            val eventsFlows = matchIds.map { matchId ->
                combine(
                    matchDao.getEventsForMatch(matchId),
                    matchDao.getMatchPlayersForMatch(matchId)
                ) { events, players ->
                    val playerTeam = players.firstOrNull { it.playerId == playerId }?.team
                    var pointsWonOnServe = 0
                    var totalServes = 0
                    var pointsWonOnReturn = 0
                    var totalReturns = 0

                    if (playerTeam != null) {
                        events.forEach { event ->
                            val isPlayerServing = event.servingPlayerId == playerId
                            val servingPlayerTeam = players.find { it.playerId == event.servingPlayerId }?.team
                            
                            if (isPlayerServing) {
                                totalServes++
                                if (event.scoringTeam == playerTeam) {
                                    pointsWonOnServe++
                                }
                            } else {
                                // If the opposing team is serving
                                if (servingPlayerTeam != null && servingPlayerTeam != playerTeam) {
                                    totalReturns++
                                    if (event.scoringTeam == playerTeam) {
                                        pointsWonOnReturn++
                                    }
                                }
                            }
                        }
                    }
                    ServeStats(totalServes, pointsWonOnServe, totalReturns, pointsWonOnReturn)
                }
            }

            if (eventsFlows.isEmpty()) {
                kotlinx.coroutines.flow.flowOf(ServeStats(0, 0, 0, 0))
            } else {
                combine(eventsFlows) { statsArray ->
                    statsArray.fold(ServeStats(0, 0, 0, 0)) { acc, stats ->
                        ServeStats(
                            totalServes = acc.totalServes + stats.totalServes,
                            pointsWonOnServe = acc.pointsWonOnServe + stats.pointsWonOnServe,
                            totalReturns = acc.totalReturns + stats.totalReturns,
                            pointsWonOnReturn = acc.pointsWonOnReturn + stats.pointsWonOnReturn
                        )
                    }
                }
            }
        }
    }

    fun getPlayerStatsInDateRange(playerId: Long, startTime: Long, endTime: Long): Flow<DateRangeStats> {
        return statsDao.getPlayerMatchStatsInDateRange(playerId, startTime, endTime).map { stats ->
            if (stats != null) {
                DateRangeStats(
                    matchesPlayed = stats.matchesPlayed,
                    wins = stats.matchesWon,
                    losses = stats.matchesLost
                )
            } else {
                DateRangeStats(0, 0, 0)
            }
        }
    }
}
