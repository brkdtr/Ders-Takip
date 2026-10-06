package com.derstakip.app.domain.model

data class DailySchedulePlan(
    val date: String, // "YYYY-MM-DD"
    val dayOfWeekName: String, // e.g. "Pazartesi"
    val dayOfWeek: Int, // 1..7 (1 = Monday)
    val totalDurationSeconds: Long,
    val videos: List<Video>
) {
    val totalVideos: Int
        get() = videos.size

    val completedVideos: Int
        get() = videos.count { it.isCompleted }

    val isAllCompleted: Boolean
        get() = videos.isNotEmpty() && videos.all { it.isCompleted }

    val completionPercentage: Float
        get() = if (videos.isNotEmpty()) completedVideos.toFloat() / videos.size else 0f
}
