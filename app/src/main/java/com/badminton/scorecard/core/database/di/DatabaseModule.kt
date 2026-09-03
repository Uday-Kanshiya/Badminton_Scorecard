package com.badminton.scorecard.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.badminton.scorecard.core.database.AppDatabase
import com.badminton.scorecard.core.database.dao.MatchDao
import com.badminton.scorecard.core.database.dao.PlayerDao
import com.badminton.scorecard.core.database.dao.StatsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE matches ADD COLUMN serviceRotationEnabled INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE matches ADD COLUMN playerPointAttribution INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE match_events ADD COLUMN scoringPlayerId INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE player_stats_cache ADD COLUMN individualPointsScored INTEGER NOT NULL DEFAULT 0")
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "badminton_scorecard.db"
        ).addMigrations(MIGRATION_1_2).build()
    }

    @Provides
    fun providePlayerDao(database: AppDatabase): PlayerDao = database.playerDao()

    @Provides
    fun provideMatchDao(database: AppDatabase): MatchDao = database.matchDao()

    @Provides
    fun provideStatsDao(database: AppDatabase): StatsDao = database.statsDao()
}
