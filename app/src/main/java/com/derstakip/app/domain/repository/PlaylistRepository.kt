package com.derstakip.app.domain.repository

import com.derstakip.app.domain.model.Playlist
import com.derstakip.app.domain.model.TopicProgress
import com.derstakip.app.domain.model.Video
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    suspend fun importPlaylist(urlOrId: String): Result<Playlist>
    fun getAllPlaylists(): Flow<List<Playlist>>
    fun getPlaylistById(playlistId: String): Flow<Playlist?>
    fun getActivePlaylist(): Flow<Playlist?>
    suspend fun setActivePlaylist(playlistId: String)
    fun getVideosForPlaylist(playlistId: String): Flow<List<Video>>
    suspend fun toggleVideoCompletion(videoId: String, isCompleted: Boolean)
    fun getTopicProgress(playlistId: String): Flow<List<TopicProgress>>
    suspend fun deletePlaylist(playlistId: String)
}
