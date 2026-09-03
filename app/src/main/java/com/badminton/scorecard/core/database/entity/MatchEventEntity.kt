package com.badminton.scorecard.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "match_events",
    foreignKeys = [
        ForeignKey(
            entity = SetEntity::class,
            parentColumns = ["id"],
            childColumns = ["setId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["setId"]),
        Index(value = ["timestamp"])
    ]
)
data class MatchEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val setId: Long,
    val rallyNumber: Int,
    val scoringTeam: String, // "TEAM_A" or "TEAM_B"
    val servingPlayerId: Long,
    val serverCourt: String, // "RIGHT" or "LEFT"
    val teamAScoreAfter: Int,
    val teamBScoreAfter: Int,
    val scoringPlayerId: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)
