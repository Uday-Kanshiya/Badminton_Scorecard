package com.badminton.scorecard.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val matchType: String, // "SINGLES" or "DOUBLES"
    val targetPoints: Int = 21,
    val bestOfSets: Int = 1,
    val skunkRuleEnabled: Boolean = false,
    val status: String = "IN_PROGRESS", // IN_PROGRESS, COMPLETED, PAUSED, ABANDONED
    val winnerTeam: String? = null, // "TEAM_A" or "TEAM_B"
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val firebaseSyncId: String? = null,
    val serviceRotationEnabled: Boolean = true,
    val playerPointAttribution: Boolean = false
)
