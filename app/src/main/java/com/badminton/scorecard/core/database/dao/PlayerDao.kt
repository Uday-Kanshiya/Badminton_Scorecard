package com.badminton.scorecard.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.badminton.scorecard.core.database.entity.PlayerEntity
import com.badminton.scorecard.core.database.entity.PlayerStatsCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity): Long

    @Update
    suspend fun updatePlayer(player: PlayerEntity)

    @Delete
    suspend fun deletePlayer(player: PlayerEntity)

    @Query("SELECT * FROM players ORDER BY name ASC")
    fun getAllPlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE id = :id")
    fun getPlayerById(id: Long): Flow<PlayerEntity?>

    @Query("SELECT * FROM players WHERE name LIKE '%' || :query || '%' OR nickname LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchPlayersByName(query: String): Flow<List<PlayerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: PlayerStatsCacheEntity)

    @Query("SELECT * FROM player_stats_cache WHERE playerId = :playerId")
    fun getPlayerStats(playerId: Long): Flow<PlayerStatsCacheEntity?>

    @Query("SELECT * FROM player_stats_cache")
    fun getAllPlayerStats(): Flow<List<PlayerStatsCacheEntity>>
}
