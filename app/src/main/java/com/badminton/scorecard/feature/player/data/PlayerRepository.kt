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

import com.badminton.scorecard.core.sync.SyncManager

data class DateRangeStats(
    val matchesPlayed: Int,
    val wins: Int,
    val losses: Int
)

@Singleton
class PlayerRepository @Inject constructor(
    private val playerDao: PlayerDao,
    private val matchDao: MatchDao,
    private val statsDao: StatsDao,
    private val syncManager: SyncManager
) {
    fun getAllPlayers(): Flow<List<PlayerEntity>> = playerDao.getAllPlayers()

    fun getPlayerById(id: Long): Flow<PlayerEntity?> = playerDao.getPlayerById(id)

    fun searchPlayers(query: String): Flow<List<PlayerEntity>> = playerDao.searchPlayersByName(query)

    suspend fun addPlayer(name: String, nickname: String?): Long {
        val id = playerDao.insertPlayer(PlayerEntity(name = name, nickname = nickname))
        try { syncManager.syncPlayer(id) } catch (e: Exception) { e.printStackTrace() }
        return id
    }

    suspend fun updatePlayer(player: PlayerEntity) {
        playerDao.updatePlayer(player)
        try { syncManager.syncPlayer(player.id) } catch (e: Exception) { e.printStackTrace() }
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

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun getDoublesScoringStatsForPlayer(playerId: Long): Flow<DoublesScoringStats> {
        return matchDao.getMatchesByPlayerIdCompleted(playerId).flatMapLatest { matches ->
            val doublesMatches = matches.filter { it.matchType == "DOUBLES" }
            if (doublesMatches.isEmpty()) {
                return@flatMapLatest kotlinx.coroutines.flow.flowOf(DoublesScoringStats())
            }

            val matchFlows = doublesMatches.map { match ->
                combine(
                    matchDao.getEventsForMatch(match.id),
                    matchDao.getMatchPlayersForMatch(match.id),
                    playerDao.getAllPlayers()
                ) { events, players, allPlayersList ->
                    val allPlayersMap = allPlayersList.associateBy { it.id }
                    val playerTeam = players.firstOrNull { it.playerId == playerId }?.team
                    val myPoints = events.count { it.scoringPlayerId == playerId }
                    val teamPoints = if (playerTeam != null) events.count { it.scoringTeam == playerTeam } else 0
                    val opponentNames = players
                        .filter { it.team != playerTeam }
                        .mapNotNull { allPlayersMap[it.playerId]?.name }
                    val opponentLabel = opponentNames.joinToString(" & ").ifBlank { "Opponents" }

                    Triple(myPoints, teamPoints, DoublesMatchPointEntry(match.id, myPoints, match.startedAt, opponentLabel))
                }
            }

            combine(matchFlows) { resultsArray: Array<Triple<Int, Int, DoublesMatchPointEntry>> ->
                var totalPoints = 0
                var totalTeamPoints = 0
                val recentEntries = mutableListOf<DoublesMatchPointEntry>()

                resultsArray.forEach { triple ->
                    val myPoints = triple.first
                    val teamPoints = triple.second
                    val entry = triple.third
                    totalPoints += myPoints
                    totalTeamPoints += teamPoints
                    recentEntries.add(entry)
                }

                val count = doublesMatches.size
                val avg = if (count > 0) totalPoints.toFloat() / count else 0f
                val share = if (totalTeamPoints > 0) (totalPoints.toFloat() / totalTeamPoints) * 100f else 0f

                DoublesScoringStats(
                    totalDoublesPoints = totalPoints,
                    doublesMatchesCount = count,
                    avgPointsPerMatch = avg,
                    totalTeamDoublesPoints = totalTeamPoints,
                    shareOfTeamPointsPct = share,
                    recentMatchPoints = recentEntries.sortedByDescending { it.matchDate }.take(7)
                )
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun getPartnershipContributionsForPlayer(playerId: Long): Flow<List<PartnershipContribution>> {
        return combine(
            statsDao.getPartnershipStatsForPlayer(playerId),
            matchDao.getMatchesByPlayerIdCompleted(playerId)
        ) { partnerships: List<PartnershipWinRate>, matches: List<MatchEntity> ->
            Pair(partnerships, matches)
        }.flatMapLatest { pair ->
            val partnerships = pair.first
            val matches = pair.second
            val doublesMatches = matches.filter { it.matchType == "DOUBLES" }
            if (partnerships.isEmpty() || doublesMatches.isEmpty()) {
                val list = partnerships.map { p ->
                    val partnerId = if (p.player1Id == playerId) p.player2Id else p.player1Id
                    val partnerName = if (p.player1Id == playerId) p.player2Name else p.player1Name
                    PartnershipContribution(
                        partnerId = partnerId,
                        partnerName = partnerName,
                        matchesPlayed = p.matchesPlayed,
                        matchesWon = p.matchesWon,
                        winPercentage = p.winPercentage,
                        playerPoints = 0,
                        partnerPoints = 0,
                        totalPairPoints = 0,
                        playerContributionPct = 50f
                    )
                }
                return@flatMapLatest kotlinx.coroutines.flow.flowOf(list)
            }

            val matchFlows = doublesMatches.map { match ->
                combine(
                    matchDao.getEventsForMatch(match.id),
                    matchDao.getMatchPlayersForMatch(match.id)
                ) { events, players ->
                    val playerTeam = players.firstOrNull { it.playerId == playerId }?.team
                    val partner = players.firstOrNull { it.playerId != playerId && it.team == playerTeam }
                    val partnerId = partner?.playerId
                    val myPoints = events.count { it.scoringPlayerId == playerId }
                    val partnerPoints = if (partnerId != null) events.count { it.scoringPlayerId == partnerId } else 0

                    Pair(partnerId, Pair(myPoints, partnerPoints))
                }
            }

            combine(matchFlows) { matchResults: Array<Pair<Long?, Pair<Int, Int>>> ->
                val pointsByPartner = mutableMapOf<Long, Pair<Int, Int>>()
                matchResults.forEach { item ->
                    val partnerId = item.first
                    val pointsPair = item.second
                    if (partnerId != null) {
                        val current = pointsByPartner[partnerId] ?: Pair(0, 0)
                        pointsByPartner[partnerId] = Pair(current.first + pointsPair.first, current.second + pointsPair.second)
                    }
                }

                partnerships.map { p ->
                    val partnerId = if (p.player1Id == playerId) p.player2Id else p.player1Id
                    val partnerName = if (p.player1Id == playerId) p.player2Name else p.player1Name
                    val pairPoints = pointsByPartner[partnerId] ?: Pair(0, 0)
                    val playerPts = pairPoints.first
                    val partnerPts = pairPoints.second
                    val totalPair = playerPts + partnerPts
                    val pct = if (totalPair > 0) (playerPts.toFloat() / totalPair) * 100f else 50f

                    PartnershipContribution(
                        partnerId = partnerId,
                        partnerName = partnerName,
                        matchesPlayed = p.matchesPlayed,
                        matchesWon = p.matchesWon,
                        winPercentage = p.winPercentage,
                        playerPoints = playerPts,
                        partnerPoints = partnerPts,
                        totalPairPoints = totalPair,
                        playerContributionPct = pct
                    )
                }
            }
        }
    }
}

data class DoublesScoringStats(
    val totalDoublesPoints: Int = 0,
    val doublesMatchesCount: Int = 0,
    val avgPointsPerMatch: Float = 0f,
    val totalTeamDoublesPoints: Int = 0,
    val shareOfTeamPointsPct: Float = 0f,
    val recentMatchPoints: List<DoublesMatchPointEntry> = emptyList()
)

data class DoublesMatchPointEntry(
    val matchId: Long,
    val pointsScored: Int,
    val matchDate: Long,
    val opponentLabel: String
)

data class PartnershipContribution(
    val partnerId: Long,
    val partnerName: String,
    val matchesPlayed: Int,
    val matchesWon: Int,
    val winPercentage: Float,
    val playerPoints: Int,
    val partnerPoints: Int,
    val totalPairPoints: Int,
    val playerContributionPct: Float
)
