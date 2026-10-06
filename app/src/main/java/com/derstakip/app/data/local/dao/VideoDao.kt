package com.derstakip.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.derstakip.app.data.local.entity.VideoEntity
import com.derstakip.app.data.local.relation.TopicStats
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [VideoEntity] operations.
 */
@Dao
interface VideoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>): List<Long>

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Delete
    suspend fun deleteVideo(video: VideoEntity)

    @Query("DELETE FROM videos WHERE id = :videoId")
    suspend fun deleteVideoById(videoId: String)

    @Query("DELETE FROM videos WHERE playlistId = :playlistId")
    suspend fun deleteVideosByPlaylistId(playlistId: String)

    @Query("SELECT * FROM videos WHERE id = :videoId LIMIT 1")
    fun getVideoById(videoId: String): Flow<VideoEntity?>

    @Query("SELECT * FROM videos WHERE id = :videoId LIMIT 1")
    suspend fun getVideoByIdDirect(videoId: String): VideoEntity?

    @Query("SELECT * FROM videos WHERE playlistId = :playlistId ORDER BY position ASC")
    fun getVideosForPlaylist(playlistId: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE playlistId = :playlistId ORDER BY position ASC")
    suspend fun getVideosForPlaylistDirect(playlistId: String): List<VideoEntity>

    @Query("SELECT * FROM videos WHERE playlistId = :playlistId AND topic = :topic ORDER BY position ASC")
    fun getVideosForTopic(playlistId: String, topic: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE playlistId = :playlistId AND isCompleted = 1 ORDER BY completedAt DESC, position ASC")
    fun getCompletedVideos(playlistId: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE playlistId = :playlistId AND isCompleted = 0 ORDER BY position ASC")
    fun getPendingVideos(playlistId: String): Flow<List<VideoEntity>>

    @Query("UPDATE videos SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :videoId")
    suspend fun updateCompletionStatus(
        videoId: String,
        isCompleted: Boolean,
        completedAt: Long?
    )

    /**
     * Convenience method to toggle or set completion with automatic timestamp management.
     */
    suspend fun setCompletion(videoId: String, isCompleted: Boolean) {
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        updateCompletionStatus(videoId, isCompleted, completedAt)
    }

    @Query("UPDATE videos SET topic = :topic WHERE id = :videoId")
    suspend fun updateTopic(videoId: String, topic: String?)

    @Query("UPDATE videos SET notes = :notes WHERE id = :videoId")
    suspend fun updateNotes(videoId: String, notes: String?)

    @Query("UPDATE videos SET isCompleted = :isCompleted, completedAt = :completedAt WHERE playlistId = :playlistId")
    suspend fun setAllVideosCompleted(
        playlistId: String,
        isCompleted: Boolean,
        completedAt: Long?
    )

    suspend fun markAllVideosCompleted(playlistId: String, isCompleted: Boolean) {
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        setAllVideosCompleted(playlistId, isCompleted, completedAt)
    }

    @Query("SELECT DISTINCT topic FROM videos WHERE playlistId = :playlistId AND topic IS NOT NULL AND topic != '' ORDER BY topic ASC")
    fun getTopicsForPlaylist(playlistId: String): Flow<List<String>>

    @Query("""
        SELECT 
            COALESCE(topic, 'General') AS topic,
            COUNT(*) AS totalCount,
            SUM(CASE WHEN isCompleted = 1 THEN 1 ELSE 0 END) AS completedCount,
            COALESCE(SUM(durationSeconds), 0) AS totalDurationSeconds,
            COALESCE(SUM(CASE WHEN isCompleted = 1 THEN durationSeconds ELSE 0 END), 0) AS completedDurationSeconds
        FROM videos
        WHERE playlistId = :playlistId
        GROUP BY topic
        ORDER BY topic ASC
    """)
    fun getTopicStats(playlistId: String): Flow<List<TopicStats>>

    @Query("SELECT COUNT(*) FROM videos WHERE playlistId = :playlistId")
    fun getTotalVideoCount(playlistId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM videos WHERE playlistId = :playlistId AND isCompleted = 1")
    fun getCompletedVideoCount(playlistId: String): Flow<Int>

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM videos WHERE playlistId = :playlistId")
    fun getTotalDurationSeconds(playlistId: String): Flow<Long>

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM videos WHERE playlistId = :playlistId AND isCompleted = 1")
    fun getCompletedDurationSeconds(playlistId: String): Flow<Long>

    @Query("SELECT * FROM videos WHERE playlistId = :playlistId AND (title LIKE '%' || :searchQuery || '%' OR topic LIKE '%' || :searchQuery || '%') ORDER BY position ASC")
    fun searchVideos(playlistId: String, searchQuery: String): Flow<List<VideoEntity>>
}
