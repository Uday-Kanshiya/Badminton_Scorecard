package com.badminton.scorecard.core.database.di

import android.content.Context
import androidx.room.Room
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

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "badminton_scorecard.db"
        ).build()
    }

    @Provides
    fun providePlayerDao(database: AppDatabase): PlayerDao = database.playerDao()

    @Provides
    fun provideMatchDao(database: AppDatabase): MatchDao = database.matchDao()

    @Provides
    fun provideStatsDao(database: AppDatabase): StatsDao = database.statsDao()
}
