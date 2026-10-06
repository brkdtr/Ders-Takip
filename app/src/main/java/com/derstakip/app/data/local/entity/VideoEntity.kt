package com.derstakip.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents an individual video/lesson belonging to a course playlist.
 *
 * @property id YouTube video ID (e.g. "dQw4w9WgXcQ").
 * @property playlistId ID of the parent playlist (Foreign Key referencing [PlaylistEntity.id]).
 * @property title Lesson or video title.
 * @property description Video description snippet.
 * @property thumbnailUrl Video thumbnail image URL.
 * @property durationSeconds Duration of the video in seconds.
 * @property position 0-based ordering position within the playlist.
 * @property topic Linguistic topic categorization (e.g., "Tenses", "Grammar", "Vocabulary", "Speaking").
 * @property isCompleted Flag indicating if the user has completed watching/studying this video.
 * @property completedAt Timestamp in milliseconds when marked completed, null if not completed.
 * @property notes Personal study notes or teacher remarks for this lesson.
 * @property videoUrl Full web or intent URL to view the video.
 */
@Entity(
    tableName = "videos",
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["playlistId"]),
        Index(value = ["topic"]),
        Index(value = ["isCompleted"]),
        Index(value = ["playlistId", "position"]),
        Index(value = ["playlistId", "isCompleted"])
    ]
)
data class VideoEntity(
    @PrimaryKey
    val id: String,
    val playlistId: String,
    val title: String,
    val description: String = "",
    val thumbnailUrl: String = "",
    val durationSeconds: Long = 0L,
    val position: Int = 0,
    val topic: String? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val notes: String? = null,
    val videoUrl: String = "https://www.youtube.com/watch?v=$id"
)
