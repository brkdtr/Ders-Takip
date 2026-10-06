package com.derstakip.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.derstakip.app.data.local.entity.DailyScheduleEntity
import com.derstakip.app.data.local.entity.ScheduleVideoItemEntity
import com.derstakip.app.data.local.relation.DailyScheduleWithVideos
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [DailyScheduleEntity] and [ScheduleVideoItemEntity] operations.
 */
@Dao
interface DailyScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: DailyScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<DailyScheduleEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleItem(item: ScheduleVideoItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleItems(items: List<ScheduleVideoItemEntity>)

    @Update
    suspend fun updateSchedule(schedule: DailyScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: DailyScheduleEntity)

    @Query("DELETE FROM daily_schedules WHERE id = :scheduleId")
    suspend fun deleteScheduleById(scheduleId: Long)

    @Query("DELETE FROM daily_schedules WHERE playlistId = :playlistId")
    suspend fun clearAllSchedulesForPlaylist(playlistId: String)

    @Query("DELETE FROM daily_schedules")
    suspend fun clearAllSchedules()

    @Query("DELETE FROM schedule_video_items WHERE scheduleId = :scheduleId")
    suspend fun deleteScheduleItemsByScheduleId(scheduleId: Long)

    @Query("DELETE FROM schedule_video_items")
    suspend fun clearAllScheduleItems()

    @Query("SELECT * FROM daily_schedules WHERE scheduledDate = :date LIMIT 1")
    fun getScheduleByDate(date: String): Flow<DailyScheduleEntity?>

    @Query("SELECT * FROM daily_schedules WHERE scheduledDate = :date LIMIT 1")
    suspend fun getScheduleByDateDirect(date: String): DailyScheduleEntity?

    @Query("SELECT * FROM daily_schedules WHERE scheduledDate >= :startDate AND playlistId = :playlistId ORDER BY scheduledDate ASC")
    fun getUpcomingSchedules(startDate: String, playlistId: String): Flow<List<DailyScheduleEntity>>

    @Transaction
    @Query("SELECT * FROM daily_schedules WHERE scheduledDate = :todayDate LIMIT 1")
    fun getTodayScheduleWithVideos(todayDate: String): Flow<DailyScheduleWithVideos?>

    @Transaction
    @Query("SELECT * FROM daily_schedules WHERE scheduledDate = :date LIMIT 1")
    fun getScheduleWithVideosByDate(date: String): Flow<DailyScheduleWithVideos?>

    @Transaction
    @Query("SELECT * FROM daily_schedules WHERE scheduledDate = :date LIMIT 1")
    suspend fun getScheduleWithVideosByDateDirect(date: String): DailyScheduleWithVideos?

    @Transaction
    @Query("SELECT * FROM daily_schedules WHERE id = :scheduleId LIMIT 1")
    fun getScheduleWithVideosById(scheduleId: Long): Flow<DailyScheduleWithVideos?>

    @Transaction
    @Query("SELECT * FROM daily_schedules WHERE playlistId = :playlistId ORDER BY scheduledDate ASC")
    fun getAllSchedulesWithVideos(playlistId: String): Flow<List<DailyScheduleWithVideos>>

    @Transaction
    @Query("SELECT * FROM daily_schedules WHERE playlistId = :playlistId AND scheduledDate >= :startDate ORDER BY scheduledDate ASC")
    fun getUpcomingSchedulesWithVideos(startDate: String, playlistId: String): Flow<List<DailyScheduleWithVideos>>

    @Query("SELECT * FROM schedule_video_items WHERE scheduleId = :scheduleId ORDER BY orderInDay ASC")
    fun getItemsForSchedule(scheduleId: Long): Flow<List<ScheduleVideoItemEntity>>

    @Query("UPDATE daily_schedules SET isCompleted = :isCompleted WHERE id = :scheduleId")
    suspend fun updateScheduleCompletionStatus(scheduleId: Long, isCompleted: Boolean)

    /**
     * Atomically clears and regenerates schedule records and assigned items for a playlist.
     */
    @Transaction
    suspend fun replaceSchedulePlan(
        playlistId: String,
        scheduleWithItems: List<Pair<DailyScheduleEntity, List<ScheduleVideoItemEntity>>>
    ) {
        clearAllSchedulesForPlaylist(playlistId)
        for ((schedule, items) in scheduleWithItems) {
            val generatedId = insertSchedule(schedule)
            val updatedItems = items.map { it.copy(scheduleId = generatedId) }
            insertScheduleItems(updatedItems)
        }
    }
}
