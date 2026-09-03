package com.badminton.scorecard.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.badminton.scorecard.core.database.entity.MatchEntity
import com.badminton.scorecard.core.database.entity.MatchEventEntity
import com.badminton.scorecard.core.database.entity.MatchPlayerCrossRef
import com.badminton.scorecard.core.database.entity.SetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity): Long

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Query("SELECT * FROM matches WHERE id = :id")
    fun getMatchById(id: Long): Flow<MatchEntity?>

    @Query("SELECT * FROM matches WHERE status = 'COMPLETED' ORDER BY startedAt DESC")
    fun getAllCompletedMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches ORDER BY startedAt DESC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Query("""
        SELECT m.* FROM matches m
        INNER JOIN match_players mp ON m.id = mp.matchId
        WHERE mp.playerId = :playerId AND m.status = 'COMPLETED'
        ORDER BY m.startedAt DESC
    """)
    fun getMatchesByPlayerIdCompleted(playerId: Long): Flow<List<MatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchPlayers(vararg crossRefs: MatchPlayerCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: SetEntity): Long

    @Update
    suspend fun updateSet(set: SetEntity)

    @Query("SELECT * FROM sets WHERE matchId = :matchId ORDER BY setNumber ASC")
    fun getSetsForMatch(matchId: Long): Flow<List<SetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: MatchEventEntity): Long

    @Query("SELECT * FROM match_events WHERE setId = :setId ORDER BY timestamp ASC, rallyNumber ASC")
    fun getEventsForSet(setId: Long): Flow<List<MatchEventEntity>>

    @Query("""
        SELECT e.* FROM match_events e
        INNER JOIN sets s ON e.setId = s.id
        WHERE s.matchId = :matchId
        ORDER BY e.timestamp ASC, e.rallyNumber ASC
    """)
    fun getEventsForMatch(matchId: Long): Flow<List<MatchEventEntity>>

    @Query("DELETE FROM matches WHERE id = :matchId")
    suspend fun deleteMatchById(matchId: Long)

    @Query("SELECT * FROM match_players WHERE matchId = :matchId")
    fun getMatchPlayersForMatch(matchId: Long): Flow<List<MatchPlayerCrossRef>>

    @Query("SELECT * FROM matches WHERE status = 'COMPLETED' AND startedAt >= :startTime AND startedAt <= :endTime ORDER BY startedAt DESC")
    fun getCompletedMatchesInDateRange(startTime: Long, endTime: Long): Flow<List<MatchEntity>>
}
