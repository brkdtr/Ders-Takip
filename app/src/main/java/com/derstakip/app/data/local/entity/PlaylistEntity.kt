package com.derstakip.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a YouTube course playlist imported for study tracking.
 *
 * @property id Unique YouTube playlist identifier (e.g., "PLxxxxxx").
 * @property title Title of the playlist/course.
 * @property description Detailed description of the playlist.
 * @property channelTitle Channel or instructor name providing the course.
 * @property thumbnailUrl URL of the playlist cover image/thumbnail.
 * @property itemCount Total count of videos in the playlist.
 * @property totalDurationSeconds Combined duration of all videos in seconds.
 * @property createdAt Timestamp when the playlist was imported into the app.
 * @property updatedAt Timestamp when the playlist data was last updated or synced.
 * @property isActive Flag indicating whether this is currently the active study course.
 */
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String = "",
    val channelTitle: String = "",
    val thumbnailUrl: String = "",
    val itemCount: Int = 0,
    val totalDurationSeconds: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = false
)
