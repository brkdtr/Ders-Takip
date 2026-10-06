package com.derstakip.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Objects (DTOs) for YouTube Data API v3.
 * Represents responses and nested entities for playlists, playlistItems, and videos endpoints.
 */

// ============================================================================
// Playlist Responses
// ============================================================================

data class PlaylistListResponse(
    @SerializedName("kind")
    val kind: String? = null,
    @SerializedName("etag")
    val etag: String? = null,
    @SerializedName("nextPageToken")
    val nextPageToken: String? = null,
    @SerializedName("prevPageToken")
    val prevPageToken: String? = null,
    @SerializedName("pageInfo")
    val pageInfo: PageInfoDto? = null,
    @SerializedName("items")
    val items: List<PlaylistDto> = emptyList()
)

data class PlaylistDto(
    @SerializedName("kind")
    val kind: String? = null,
    @SerializedName("etag")
    val etag: String? = null,
    @SerializedName("id")
    val id: String,
    @SerializedName("snippet")
    val snippet: PlaylistSnippetDto? = null,
    @SerializedName("contentDetails")
    val contentDetails: PlaylistContentDetailsDto? = null
)

data class PlaylistSnippetDto(
    @SerializedName("publishedAt")
    val publishedAt: String? = null,
    @SerializedName("channelId")
    val channelId: String? = null,
    @SerializedName("title")
    val title: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("thumbnails")
    val thumbnails: ThumbnailsDto? = null,
    @SerializedName("channelTitle")
    val channelTitle: String = "",
    @SerializedName("defaultLanguage")
    val defaultLanguage: String? = null
)

data class PlaylistContentDetailsDto(
    @SerializedName("itemCount")
    val itemCount: Int = 0
)

// ============================================================================
// PlaylistItem Responses
// ============================================================================

data class PlaylistItemListResponse(
    @SerializedName("kind")
    val kind: String? = null,
    @SerializedName("etag")
    val etag: String? = null,
    @SerializedName("nextPageToken")
    val nextPageToken: String? = null,
    @SerializedName("prevPageToken")
    val prevPageToken: String? = null,
    @SerializedName("pageInfo")
    val pageInfo: PageInfoDto? = null,
    @SerializedName("items")
    val items: List<PlaylistItemDto> = emptyList()
)

data class PlaylistItemDto(
    @SerializedName("kind")
    val kind: String? = null,
    @SerializedName("etag")
    val etag: String? = null,
    @SerializedName("id")
    val id: String,
    @SerializedName("snippet")
    val snippet: PlaylistItemSnippetDto? = null,
    @SerializedName("contentDetails")
    val contentDetails: PlaylistItemContentDetailsDto? = null
)

data class PlaylistItemSnippetDto(
    @SerializedName("publishedAt")
    val publishedAt: String? = null,
    @SerializedName("channelId")
    val channelId: String? = null,
    @SerializedName("title")
    val title: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("thumbnails")
    val thumbnails: ThumbnailsDto? = null,
    @SerializedName("channelTitle")
    val channelTitle: String = "",
    @SerializedName("playlistId")
    val playlistId: String? = null,
    @SerializedName("position")
    val position: Int = 0,
    @SerializedName("resourceId")
    val resourceId: ResourceIdDto? = null,
    @SerializedName("videoOwnerChannelTitle")
    val videoOwnerChannelTitle: String? = null,
    @SerializedName("videoOwnerChannelId")
    val videoOwnerChannelId: String? = null
)

data class PlaylistItemContentDetailsDto(
    @SerializedName("videoId")
    val videoId: String? = null,
    @SerializedName("startAt")
    val startAt: String? = null,
    @SerializedName("endAt")
    val endAt: String? = null,
    @SerializedName("note")
    val note: String? = null,
    @SerializedName("videoPublishedAt")
    val videoPublishedAt: String? = null
)

data class ResourceIdDto(
    @SerializedName("kind")
    val kind: String? = null,
    @SerializedName("videoId")
    val videoId: String? = null
)

// ============================================================================
// Video Responses
// ============================================================================

data class VideoListResponse(
    @SerializedName("kind")
    val kind: String? = null,
    @SerializedName("etag")
    val etag: String? = null,
    @SerializedName("nextPageToken")
    val nextPageToken: String? = null,
    @SerializedName("prevPageToken")
    val prevPageToken: String? = null,
    @SerializedName("pageInfo")
    val pageInfo: PageInfoDto? = null,
    @SerializedName("items")
    val items: List<VideoDto> = emptyList()
)

data class VideoDto(
    @SerializedName("kind")
    val kind: String? = null,
    @SerializedName("etag")
    val etag: String? = null,
    @SerializedName("id")
    val id: String,
    @SerializedName("snippet")
    val snippet: VideoSnippetDto? = null,
    @SerializedName("contentDetails")
    val contentDetails: VideoContentDetailsDto? = null,
    @SerializedName("statistics")
    val statistics: VideoStatisticsDto? = null
)

data class VideoSnippetDto(
    @SerializedName("publishedAt")
    val publishedAt: String? = null,
    @SerializedName("channelId")
    val channelId: String? = null,
    @SerializedName("title")
    val title: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("thumbnails")
    val thumbnails: ThumbnailsDto? = null,
    @SerializedName("channelTitle")
    val channelTitle: String = "",
    @SerializedName("tags")
    val tags: List<String>? = null,
    @SerializedName("categoryId")
    val categoryId: String? = null,
    @SerializedName("liveBroadcastContent")
    val liveBroadcastContent: String? = null,
    @SerializedName("defaultLanguage")
    val defaultLanguage: String? = null
)

data class VideoContentDetailsDto(
    @SerializedName("duration")
    val duration: String? = null,
    @SerializedName("dimension")
    val dimension: String? = null,
    @SerializedName("definition")
    val definition: String? = null,
    @SerializedName("caption")
    val caption: String? = null,
    @SerializedName("licensedContent")
    val licensedContent: Boolean? = null
)

data class VideoStatisticsDto(
    @SerializedName("viewCount")
    val viewCount: String? = null,
    @SerializedName("likeCount")
    val likeCount: String? = null,
    @SerializedName("dislikeCount")
    val dislikeCount: String? = null,
    @SerializedName("favoriteCount")
    val favoriteCount: String? = null,
    @SerializedName("commentCount")
    val commentCount: String? = null
)

// ============================================================================
// Common DTOs
// ============================================================================

data class PageInfoDto(
    @SerializedName("totalResults")
    val totalResults: Int = 0,
    @SerializedName("resultsPerPage")
    val resultsPerPage: Int = 0
)

data class ThumbnailsDto(
    @SerializedName("default")
    val defaultThumbnail: ThumbnailDetailsDto? = null,
    @SerializedName("medium")
    val medium: ThumbnailDetailsDto? = null,
    @SerializedName("high")
    val high: ThumbnailDetailsDto? = null,
    @SerializedName("standard")
    val standard: ThumbnailDetailsDto? = null,
    @SerializedName("maxres")
    val maxres: ThumbnailDetailsDto? = null
) {
    /**
     * Resolves the best available thumbnail URL, prioritising higher resolution.
     */
    val bestUrl: String?
        get() = maxres?.url
            ?: standard?.url
            ?: high?.url
            ?: medium?.url
            ?: defaultThumbnail?.url
}

data class ThumbnailDetailsDto(
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("width")
    val width: Int? = null,
    @SerializedName("height")
    val height: Int? = null
)
