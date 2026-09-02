package com.badminton.scorecard.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "player_stats_cache",
    foreignKeys = [
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlayerStatsCacheEntity(
    @PrimaryKey(autoGenerate = false) val playerId: Long,
    val totalMatchesPlayed: Int = 0,
    val totalWins: Int = 0,
    val totalLosses: Int = 0,
    val singlesPlayed: Int = 0,
    val singlesWon: Int = 0,
    val doublesPlayed: Int = 0,
    val doublesWon: Int = 0,
    val totalPointsScored: Int = 0,
    val totalPointsOnServe: Int = 0,
    val totalServeRallies: Int = 0,
    val totalPointsOnReturn: Int = 0,
    val totalReturnRallies: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val totalPointsOnOwnServe: Int get() = totalPointsOnServe
    val totalPointsOnPartnerServe: Int get() = 0
    val totalPointsOnOpponentServe: Int get() = totalPointsOnReturn
}
