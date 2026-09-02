package com.badminton.scorecard.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "match_players",
    primaryKeys = ["matchId", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["matchId"]),
        Index(value = ["playerId"])
    ]
)
data class MatchPlayerCrossRef(
    val matchId: Long,
    val playerId: Long,
    val team: String, // "TEAM_A" or "TEAM_B"
    val playerOrder: Int
)
