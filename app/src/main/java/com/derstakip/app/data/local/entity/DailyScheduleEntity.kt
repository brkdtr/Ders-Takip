package com.derstakip.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a planned study day within the generated course curriculum.
 *
 * @property id Auto-generated primary key for the schedule record.
 * @property playlistId Parent playlist ID this schedule plan belongs to.
 * @property scheduledDate Formatted date string in "YYYY-MM-DD" format (e.g., "2026-10-06").
 * @property dayOfWeek Day of the week integer (1 = Monday, 2 = Tuesday, ..., 7 = Sunday).
 * @property totalAssignedSeconds Sum of duration in seconds for all videos assigned to this day.
 * @property isCompleted Flag indicating if all lessons planned for this day have been completed.
 * @property notes Optional study goal or note for this day.
 */
@Entity(
    tableName = "daily_schedules",
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
        Index(value = ["scheduledDate"]),
        Index(value = ["playlistId", "scheduledDate"], unique = true)
    ]
)
data class DailyScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val playlistId: String,
    val scheduledDate: String,
    val dayOfWeek: Int,
    val totalAssignedSeconds: Long = 0L,
    val isCompleted: Boolean = false,
    val notes: String? = null
)
