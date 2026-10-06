package com.derstakip.app.domain.model

data class Playlist(
    val id: String,
    val title: String,
    val description: String = "",
    val channelTitle: String = "",
    val thumbnailUrl: String = "",
    val videoCount: Int = 0,
    val totalDurationSeconds: Long = 0L,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
