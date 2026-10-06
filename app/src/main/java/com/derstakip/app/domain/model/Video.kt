package com.derstakip.app.domain.model

data class Video(
    val id: String,
    val playlistId: String,
    val title: String,
    val description: String = "",
    val thumbnailUrl: String = "",
    val durationSeconds: Long = 0L,
    val position: Int = 0,
    val topic: String = "Genel",
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val scheduledDate: String? = null
) {
    val durationMinutes: Int
        get() = (durationSeconds / 60L).toInt()
}
