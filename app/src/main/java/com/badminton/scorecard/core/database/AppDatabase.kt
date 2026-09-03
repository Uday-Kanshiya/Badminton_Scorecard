package com.badminton.scorecard.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.badminton.scorecard.core.database.dao.MatchDao
import com.badminton.scorecard.core.database.dao.PlayerDao
import com.badminton.scorecard.core.database.dao.StatsDao
import com.badminton.scorecard.core.database.entity.MatchEntity
import com.badminton.scorecard.core.database.entity.MatchEventEntity
import com.badminton.scorecard.core.database.entity.MatchPlayerCrossRef
import com.badminton.scorecard.core.database.entity.PlayerEntity
import com.badminton.scorecard.core.database.entity.PlayerStatsCacheEntity
import com.badminton.scorecard.core.database.entity.SetEntity

@Database(
    entities = [
        PlayerEntity::class,
        PlayerStatsCacheEntity::class,
        MatchEntity::class,
        MatchPlayerCrossRef::class,
        SetEntity::class,
        MatchEventEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun matchDao(): MatchDao
    abstract fun statsDao(): StatsDao
}
