package com.badminton.scorecard.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class PartnershipWinRate(
    val player1Id: Long, val player1Name: String,
    val player2Id: Long, val player2Name: String,
    val matchesPlayed: Int, val matchesWon: Int,
    val winPercentage: Float
)

data class ServeStats(
    val totalServes: Int,
    val pointsWonOnServe: Int,
    val totalReturns: Int,
    val pointsWonOnReturn: Int
)

data class PlayerMatchStats(
    val playerId: Long,
    val matchesPlayed: Int,
    val matchesWon: Int,
    val matchesLost: Int
)

@Dao
interface StatsDao {
    @Query("""
        SELECT 
            COUNT(id) AS totalServes,
            SUM(CASE WHEN scoringTeam = 
                (SELECT team FROM match_players mp 
                 JOIN sets s ON mp.matchId = s.matchId 
                 WHERE mp.playerId = :playerId AND s.id = match_events.setId LIMIT 1) 
                 THEN 1 ELSE 0 END) AS pointsWonOnServe,
            0 AS totalReturns,
            0 AS pointsWonOnReturn
        FROM match_events 
        WHERE servingPlayerId = :playerId
    """)
    fun getServeStatsForPlayer(playerId: Long): Flow<ServeStats>

    @Query("""
        WITH DoublesPartnerships AS (
            SELECT 
                mp1.matchId,
                CASE WHEN mp1.playerId < mp2.playerId THEN mp1.playerId ELSE mp2.playerId END AS p1_id,
                CASE WHEN mp1.playerId < mp2.playerId THEN mp2.playerId ELSE mp1.playerId END AS p2_id,
                mp1.team,
                m.winnerTeam
            FROM match_players mp1
            JOIN match_players mp2 
                ON mp1.matchId = mp2.matchId 
                AND mp1.team = mp2.team 
                AND mp1.playerId != mp2.playerId
            JOIN matches m ON m.id = mp1.matchId
            WHERE m.matchType = 'DOUBLES' AND m.status = 'COMPLETED'
            AND mp1.playerId < mp2.playerId
        )
        SELECT 
            p1.id AS player1Id, p1.name AS player1Name,
            p2.id AS player2Id, p2.name AS player2Name,
            COUNT(DISTINCT dp.matchId) AS matchesPlayed,
            SUM(CASE WHEN dp.winnerTeam = dp.team THEN 1 ELSE 0 END) AS matchesWon,
            CAST(SUM(CASE WHEN dp.winnerTeam = dp.team THEN 1 ELSE 0 END) AS FLOAT) / COUNT(DISTINCT dp.matchId) * 100.0 AS winPercentage
        FROM DoublesPartnerships dp
        JOIN players p1 ON p1.id = dp.p1_id
        JOIN players p2 ON p2.id = dp.p2_id
        GROUP BY dp.p1_id, dp.p2_id
        ORDER BY winPercentage DESC
    """)
    fun getPartnershipStats(): Flow<List<PartnershipWinRate>>

    @Query("""
        WITH DoublesPartnerships AS (
            SELECT 
                mp1.matchId,
                CASE WHEN mp1.playerId < mp2.playerId THEN mp1.playerId ELSE mp2.playerId END AS p1_id,
                CASE WHEN mp1.playerId < mp2.playerId THEN mp2.playerId ELSE mp1.playerId END AS p2_id,
                mp1.team,
                m.winnerTeam
            FROM match_players mp1
            JOIN match_players mp2 
                ON mp1.matchId = mp2.matchId 
                AND mp1.team = mp2.team 
                AND mp1.playerId != mp2.playerId
            JOIN matches m ON m.id = mp1.matchId
            WHERE m.matchType = 'DOUBLES' AND m.status = 'COMPLETED'
            AND mp1.playerId < mp2.playerId
        )
        SELECT 
            p1.id AS player1Id, p1.name AS player1Name,
            p2.id AS player2Id, p2.name AS player2Name,
            COUNT(DISTINCT dp.matchId) AS matchesPlayed,
            SUM(CASE WHEN dp.winnerTeam = dp.team THEN 1 ELSE 0 END) AS matchesWon,
            CAST(SUM(CASE WHEN dp.winnerTeam = dp.team THEN 1 ELSE 0 END) AS FLOAT) / COUNT(DISTINCT dp.matchId) * 100.0 AS winPercentage
        FROM DoublesPartnerships dp
        JOIN players p1 ON p1.id = dp.p1_id
        JOIN players p2 ON p2.id = dp.p2_id
        WHERE dp.p1_id = :playerId OR dp.p2_id = :playerId
        GROUP BY dp.p1_id, dp.p2_id
        ORDER BY winPercentage DESC
    """)
    fun getPartnershipStatsForPlayer(playerId: Long): Flow<List<PartnershipWinRate>>

    @Query("""
        WITH DoublesPartnerships AS (
            SELECT 
                mp1.matchId,
                CASE WHEN mp1.playerId < mp2.playerId THEN mp1.playerId ELSE mp2.playerId END AS p1_id,
                CASE WHEN mp1.playerId < mp2.playerId THEN mp2.playerId ELSE mp1.playerId END AS p2_id,
                mp1.team,
                m.winnerTeam
            FROM match_players mp1
            JOIN match_players mp2 
                ON mp1.matchId = mp2.matchId 
                AND mp1.team = mp2.team 
                AND mp1.playerId != mp2.playerId
            JOIN matches m ON m.id = mp1.matchId
            WHERE m.matchType = 'DOUBLES' AND m.status = 'COMPLETED'
            AND m.startedAt >= :startTime AND m.startedAt <= :endTime
            AND mp1.playerId < mp2.playerId
        )
        SELECT 
            p1.id AS player1Id, p1.name AS player1Name,
            p2.id AS player2Id, p2.name AS player2Name,
            COUNT(DISTINCT dp.matchId) AS matchesPlayed,
            SUM(CASE WHEN dp.winnerTeam = dp.team THEN 1 ELSE 0 END) AS matchesWon,
            CAST(SUM(CASE WHEN dp.winnerTeam = dp.team THEN 1 ELSE 0 END) AS FLOAT) / COUNT(DISTINCT dp.matchId) * 100.0 AS winPercentage
        FROM DoublesPartnerships dp
        JOIN players p1 ON p1.id = dp.p1_id
        JOIN players p2 ON p2.id = dp.p2_id
        WHERE dp.p1_id = :playerId OR dp.p2_id = :playerId
        GROUP BY dp.p1_id, dp.p2_id
        ORDER BY winPercentage DESC
    """)
    fun getPartnershipStatsInDateRange(playerId: Long, startTime: Long, endTime: Long): Flow<List<PartnershipWinRate>>

    @Query("""
        WITH DoublesPartnerships AS (
            SELECT 
                mp1.matchId,
                CASE WHEN mp1.playerId < mp2.playerId THEN mp1.playerId ELSE mp2.playerId END AS p1_id,
                CASE WHEN mp1.playerId < mp2.playerId THEN mp2.playerId ELSE mp1.playerId END AS p2_id,
                mp1.team,
                m.winnerTeam
            FROM match_players mp1
            JOIN match_players mp2 
                ON mp1.matchId = mp2.matchId 
                AND mp1.team = mp2.team 
                AND mp1.playerId != mp2.playerId
            JOIN matches m ON m.id = mp1.matchId
            WHERE m.matchType = 'DOUBLES' AND m.status = 'COMPLETED'
            AND m.startedAt >= :startTime AND m.startedAt <= :endTime
            AND mp1.playerId < mp2.playerId
        )
        SELECT 
            p1.id AS player1Id, p1.name AS player1Name,
            p2.id AS player2Id, p2.name AS player2Name,
            COUNT(DISTINCT dp.matchId) AS matchesPlayed,
            SUM(CASE WHEN dp.winnerTeam = dp.team THEN 1 ELSE 0 END) AS matchesWon,
            CAST(SUM(CASE WHEN dp.winnerTeam = dp.team THEN 1 ELSE 0 END) AS FLOAT) / COUNT(DISTINCT dp.matchId) * 100.0 AS winPercentage
        FROM DoublesPartnerships dp
        JOIN players p1 ON p1.id = dp.p1_id
        JOIN players p2 ON p2.id = dp.p2_id
        GROUP BY dp.p1_id, dp.p2_id
        ORDER BY winPercentage DESC
    """)
    fun getAllPartnershipStatsInDateRange(startTime: Long, endTime: Long): Flow<List<PartnershipWinRate>>

    @Query("""
        SELECT 
            mp.playerId AS playerId,
            COUNT(m.id) AS matchesPlayed,
            SUM(CASE WHEN m.winnerTeam = mp.team THEN 1 ELSE 0 END) AS matchesWon,
            SUM(CASE WHEN m.winnerTeam != mp.team AND m.winnerTeam IS NOT NULL THEN 1 ELSE 0 END) AS matchesLost
        FROM match_players mp
        JOIN matches m ON m.id = mp.matchId
        WHERE mp.playerId = :playerId AND m.status = 'COMPLETED'
        AND m.startedAt >= :startTime AND m.startedAt <= :endTime
        GROUP BY mp.playerId
    """)
    fun getPlayerMatchStatsInDateRange(playerId: Long, startTime: Long, endTime: Long): Flow<PlayerMatchStats>

    @Query("""
        SELECT 
            COUNT(e.id) AS totalServes,
            SUM(CASE WHEN e.scoringTeam = 
                (SELECT team FROM match_players mp WHERE mp.playerId = :playerId AND mp.matchId = :matchId LIMIT 1) 
                 THEN 1 ELSE 0 END) AS pointsWonOnServe,
            0 AS totalReturns,
            0 AS pointsWonOnReturn
        FROM match_events e
        JOIN sets s ON e.setId = s.id
        WHERE e.servingPlayerId = :playerId AND s.matchId = :matchId
    """)
    fun getServeStatsForPlayerInMatch(playerId: Long, matchId: Long): Flow<ServeStats>
}
