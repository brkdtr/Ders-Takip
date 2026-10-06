package com.derstakip.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.derstakip.app.data.local.entity.DailyScheduleEntity
import com.derstakip.app.data.local.entity.ScheduleVideoItemEntity
import com.derstakip.app.data.local.entity.VideoEntity

/**
 * Composite model representing a daily schedule joined with all assigned videos.
 * Utilizes [ScheduleVideoItemEntity] as a Room [Junction].
 *
 * @property schedule The daily schedule record.
 * @property videos The list of videos mapped to this schedule.
 */
data class DailyScheduleWithVideos(
    @Embedded
    val schedule: DailyScheduleEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ScheduleVideoItemEntity::class,
            parentColumn = "scheduleId",
            entityColumn = "videoId"
        )
    )
    val videos: List<VideoEntity> = emptyList()
) {
    /**
     * Total video count assigned for this day.
     */
    val videoCount: Int
        get() = videos.size

    /**
     * Count of videos completed by user for this day.
     */
    val completedVideoCount: Int
        get() = videos.count { it.isCompleted }

    /**
     * Sum of duration in seconds of all assigned videos.
     */
    val calculatedTotalDurationSeconds: Long
        get() = videos.sumOf { it.durationSeconds }

    /**
     * Sum of duration in seconds of completed videos for this day.
     */
    val completedDurationSeconds: Long
        get() = videos.filter { it.isCompleted }.sumOf { it.durationSeconds }

    /**
     * Completion progress percentage (0.0 to 100.0).
     */
    val progressPercentage: Float
        get() = if (videos.isNotEmpty()) (completedVideoCount.toFloat() / videos.size.toFloat()) * 100f else 0f

    /**
     * True if all assigned videos for this day are marked completed.
     */
    val isAllCompleted: Boolean
        get() = videos.isNotEmpty() && videos.all { it.isCompleted }
}
