package com.derstakip.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.derstakip.app.data.local.entity.StudySettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [StudySettingsEntity].
 * Manages user preferences and settings for study pace calculations.
 */
@Dao
interface StudySettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: StudySettingsEntity)

    @Query("SELECT * FROM study_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<StudySettingsEntity?>

    @Query("SELECT * FROM study_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): StudySettingsEntity?

    @Query("UPDATE study_settings SET dailyCapacityMinutes = :dailyCapacityMinutes, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateDailyCapacity(
        dailyCapacityMinutes: Int,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE study_settings SET selectedDaysOfWeek = :selectedDaysOfWeek, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateSelectedDays(
        selectedDaysOfWeek: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE study_settings SET activePlaylistId = :playlistId, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateActivePlaylist(
        playlistId: String?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE study_settings SET startDate = :startDate, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateStartDate(
        startDate: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE study_settings SET targetCompletionDate = :targetCompletionDate, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateTargetCompletionDate(
        targetCompletionDate: String?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM study_settings")
    suspend fun clearSettings()
}
