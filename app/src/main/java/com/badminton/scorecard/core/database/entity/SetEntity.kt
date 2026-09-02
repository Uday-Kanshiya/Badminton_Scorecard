package com.badminton.scorecard.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sets",
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["matchId"])
    ]
)
data class SetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val matchId: Long,
    val setNumber: Int,
    val teamAScore: Int = 0,
    val teamBScore: Int = 0,
    val winnerTeam: String? = null, // "TEAM_A" or "TEAM_B" or null
    val initialServerPlayerId: Long,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null
)
