package com.derstakip.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Junction entity mapping videos to specific daily study schedules.
 *
 * @property scheduleId Reference to [DailyScheduleEntity.id].
 * @property videoId Reference to [VideoEntity.id].
 * @property orderInDay Sequential playback order of this video within the scheduled study day (1-based or 0-based).
 */
@Entity(
    tableName = "schedule_video_items",
    primaryKeys = ["scheduleId", "videoId"],
    foreignKeys = [
        ForeignKey(
            entity = DailyScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = VideoEntity::class,
            parentColumns = ["id"],
            childColumns = ["videoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["scheduleId"]),
        Index(value = ["videoId"]),
        Index(value = ["scheduleId", "orderInDay"])
    ]
)
data class ScheduleVideoItemEntity(
    val scheduleId: Long,
    val videoId: String,
    val orderInDay: Int = 0
)
