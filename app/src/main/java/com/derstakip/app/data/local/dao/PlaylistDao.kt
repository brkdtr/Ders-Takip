package com.derstakip.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.derstakip.app.data.local.entity.PlaylistEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [PlaylistEntity] operations.
 */
@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylists(playlists: List<PlaylistEntity>): List<Long>

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: String)

    @Query("DELETE FROM playlists")
    suspend fun deleteAllPlaylists()

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    fun getPlaylistById(playlistId: String): Flow<PlaylistEntity?>

    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    suspend fun getPlaylistByIdDirect(playlistId: String): PlaylistEntity?

    @Query("SELECT * FROM playlists WHERE isActive = 1 LIMIT 1")
    fun getActivePlaylist(): Flow<PlaylistEntity?>

    @Query("SELECT * FROM playlists WHERE isActive = 1 LIMIT 1")
    suspend fun getActivePlaylistDirect(): PlaylistEntity?

    @Query("UPDATE playlists SET isActive = CASE WHEN id = :playlistId THEN 1 ELSE 0 END")
    suspend fun setActivePlaylist(playlistId: String)

    @Query("SELECT COUNT(*) FROM playlists")
    fun getPlaylistCount(): Flow<Int>

    @Query("UPDATE playlists SET itemCount = :count, totalDurationSeconds = :totalDurationSeconds, updatedAt = :updatedAt WHERE id = :playlistId")
    suspend fun updatePlaylistStats(
        playlistId: String,
        count: Int,
        totalDurationSeconds: Long,
        updatedAt: Long = System.currentTimeMillis()
    )
}
