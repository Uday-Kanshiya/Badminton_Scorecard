package com.badminton.scorecard.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val nickname: String? = null,
    val avatarUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
