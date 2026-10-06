package com.derstakip.app.domain.model

data class TopicProgress(
    val topic: String,
    val totalVideos: Int,
    val completedVideos: Int,
    val totalDurationSeconds: Long,
    val completedDurationSeconds: Long
) {
    val remainingVideos: Int
        get() = (totalVideos - completedVideos).coerceAtLeast(0)

    val remainingDurationSeconds: Long
        get() = (totalDurationSeconds - completedDurationSeconds).coerceAtLeast(0L)

    val progressPercentage: Float
        get() = if (totalVideos > 0) completedVideos.toFloat() / totalVideos.toFloat() else 0f

    val isCompleted: Boolean
        get() = totalVideos > 0 && completedVideos >= totalVideos
}
