package com.derstakip.app.data.remote

import com.derstakip.app.data.remote.dto.PlaylistListResponse
import com.derstakip.app.data.remote.dto.PlaylistItemListResponse
import com.derstakip.app.data.remote.dto.VideoListResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for YouTube Data API v3
 * Base URL: https://www.googleapis.com/youtube/v3/
 */
interface YouTubeApiService {

    @GET("playlists")
    suspend fun getPlaylistDetails(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("id") playlistId: String,
        @Query("key") apiKey: String
    ): PlaylistListResponse

    @GET("playlistItems")
    suspend fun getPlaylistItems(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("playlistId") playlistId: String,
        @Query("maxResults") maxResults: Int = 50,
        @Query("pageToken") pageToken: String? = null,
        @Query("key") apiKey: String
    ): PlaylistItemListResponse

    @GET("videos")
    suspend fun getVideoDetails(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("id") videoIds: String, // Comma-separated list of video IDs (up to 50)
        @Query("key") apiKey: String
    ): VideoListResponse
}
