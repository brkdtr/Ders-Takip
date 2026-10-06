package com.derstakip.app.di

import android.content.Context
import com.derstakip.app.data.local.AppDatabase
import com.derstakip.app.data.local.dao.DailyScheduleDao
import com.derstakip.app.data.local.dao.PlaylistDao
import com.derstakip.app.data.local.dao.StudySettingsDao
import com.derstakip.app.data.local.dao.VideoDao
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
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun providePlaylistDao(database: AppDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    fun provideVideoDao(database: AppDatabase): VideoDao {
        return database.videoDao()
    }

    @Provides
    fun provideDailyScheduleDao(database: AppDatabase): DailyScheduleDao {
        return database.dailyScheduleDao()
    }

    @Provides
    fun provideStudySettingsDao(database: AppDatabase): StudySettingsDao {
        return database.studySettingsDao()
    }
}
