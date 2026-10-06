package com.derstakip.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.derstakip.app.data.local.converter.Converters
import com.derstakip.app.data.local.dao.DailyScheduleDao
import com.derstakip.app.data.local.dao.PlaylistDao
import com.derstakip.app.data.local.dao.StudySettingsDao
import com.derstakip.app.data.local.dao.VideoDao
import com.derstakip.app.data.local.entity.DailyScheduleEntity
import com.derstakip.app.data.local.entity.PlaylistEntity
import com.derstakip.app.data.local.entity.ScheduleVideoItemEntity
import com.derstakip.app.data.local.entity.StudySettingsEntity
import com.derstakip.app.data.local.entity.VideoEntity

/**
 * Main Android Room Database for the Course & Study Schedule Tracking application.
 *
 * Persists playlists, videos/lessons, daily generated schedules, schedule mappings,
 * and study pace settings.
 */
@Database(
    entities = [
        PlaylistEntity::class,
        VideoEntity::class,
        DailyScheduleEntity::class,
        ScheduleVideoItemEntity::class,
        StudySettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playlistDao(): PlaylistDao
    abstract fun videoDao(): VideoDao
    abstract fun dailyScheduleDao(): DailyScheduleDao
    abstract fun studySettingsDao(): StudySettingsDao

    companion object {
        const val DATABASE_NAME = "ders_takip_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns singleton database instance using double-checked locking pattern.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
