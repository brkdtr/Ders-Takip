package com.derstakip.app.data.local.relation

/**
 * Aggregated statistics for a specific course topic (e.g., Grammar, Vocabulary, Tenses).
 *
 * @property topic Topic category name (e.g., "Tenses", "Reading", "General").
 * @property totalCount Total number of videos covering this topic.
 * @property completedCount Number of completed videos for this topic.
 * @property totalDurationSeconds Combined total duration of all topic videos in seconds.
 * @property completedDurationSeconds Combined duration of completed topic videos in seconds.
 */
data class TopicStats(
    val topic: String,
    val totalCount: Int,
    val completedCount: Int,
    val totalDurationSeconds: Long,
    val completedDurationSeconds: Long
) {
    /**
     * Number of remaining uncompleted videos in this topic.
     */
    val remainingCount: Int
        get() = (totalCount - completedCount).coerceAtLeast(0)

    /**
     * Remaining study time in seconds for this topic.
     */
    val remainingDurationSeconds: Long
        get() = (totalDurationSeconds - completedDurationSeconds).coerceAtLeast(0L)

    /**
     * Topic completion percentage from 0.0 to 100.0.
     */
    val completionPercentage: Float
        get() = if (totalCount > 0) (completedCount.toFloat() / totalCount.toFloat()) * 100f else 0f

    /**
     * True if all videos in this topic are completed.
     */
    val isCompleted: Boolean
        get() = totalCount > 0 && completedCount >= totalCount
}
