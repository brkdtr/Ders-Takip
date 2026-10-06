package com.derstakip.app.data.remote

import com.derstakip.app.util.TopicExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

data class ParsedPlaylistData(
    val id: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val videos: List<ParsedVideoData>
)

data class ParsedVideoData(
    val id: String,
    val title: String,
    val durationSeconds: Long,
    val position: Int,
    val topic: String,
    val thumbnailUrl: String
)

/**
 * Direct YouTube Playlist Web Parser.
 * Allows extracting YouTube playlist details and videos directly WITHOUT requiring a Google Cloud API Key.
 * Extracts data from YouTube's server-rendered ytInitialData JSON.
 */
@Singleton
class YouTubeDirectParser @Inject constructor(
    private val okHttpClient: OkHttpClient
) {

    private val YT_INITIAL_DATA_PATTERN = Pattern.compile(
        "var\\s+ytInitialData\\s*=\\s*(\\{.+?\\});\\s*</script>",
        Pattern.DOTALL
    )

    private val YT_INITIAL_DATA_WINDOW_PATTERN = Pattern.compile(
        "window\\[\"ytInitialData\"\\]\\s*=\\s*(\\{.+?\\});",
        Pattern.DOTALL
    )

    suspend fun fetchPlaylist(playlistId: String): Result<ParsedPlaylistData> = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.youtube.com/playlist?list=$playlistId&hl=tr"
            val request = Request.Builder()
                .url(url)
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                )
                .header("Accept-Language", "tr-TR,tr;q=0.9,en;q=0.8")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IOException("YouTube sayfası yüklenemedi (HTTP ${response.code}). Oynatma listesinin herkese açık olduğundan emin olun.")
                )
            }

            val html = response.body?.string().orEmpty()
            if (html.isBlank()) {
                return@withContext Result.failure(IOException("YouTube sayfası boş yanıt döndürdü."))
            }

            // Extract ytInitialData
            var jsonString: String? = null
            val matcher = YT_INITIAL_DATA_PATTERN.matcher(html)
            if (matcher.find()) {
                jsonString = matcher.group(1)
            } else {
                val windowMatcher = YT_INITIAL_DATA_WINDOW_PATTERN.matcher(html)
                if (windowMatcher.find()) {
                    jsonString = windowMatcher.group(1)
                }
            }

            if (jsonString == null) {
                // If regex failed, try finding start index
                val marker = "var ytInitialData = "
                val startIdx = html.indexOf(marker)
                if (startIdx != -1) {
                    val scriptEnd = html.indexOf(";</script>", startIdx)
                    if (scriptEnd != -1) {
                        jsonString = html.substring(startIdx + marker.length, scriptEnd)
                    }
                }
            }

            if (jsonString == null) {
                return@withContext Result.failure(
                    IllegalStateException("Oynatma listesi verileri ayrıştırılamadı. Liste gizli veya silinmiş olabilir.")
                )
            }

            val rootJson = JSONObject(jsonString)

            // Extract Playlist Title
            var playlistTitle = rootJson.optJSONObject("metadata")
                ?.optJSONObject("playlistMetadataRenderer")
                ?.optString("title")
                .orEmpty()

            if (playlistTitle.isBlank()) {
                playlistTitle = rootJson.optJSONObject("header")
                    ?.optJSONObject("playlistHeaderRenderer")
                    ?.optJSONObject("title")
                    ?.optString("simpleText")
                    .orEmpty()
            }

            if (playlistTitle.isBlank()) {
                playlistTitle = "YouTube Ders Listesi"
            }

            // Extract Channel Title
            var channelTitle = rootJson.optJSONObject("header")
                ?.optJSONObject("playlistHeaderRenderer")
                ?.optJSONObject("ownerText")
                ?.optJSONArray("runs")
                ?.optJSONObject(0)
                ?.optString("text")
                .orEmpty()

            if (channelTitle.isBlank()) {
                channelTitle = "YouTube Eğitmeni"
            }

            // Extract Playlist Thumbnail
            val playlistThumb = "https://img.youtube.com/vi/${playlistId}/hqdefault.jpg"

            // Recursively collect all playlistVideoRenderer objects
            val videoRenderers = mutableListOf<JSONObject>()
            extractVideoRenderers(rootJson, videoRenderers)

            if (videoRenderers.isEmpty()) {
                return@withContext Result.failure(
                    IllegalStateException("Oynatma listesinde oynatılabilir video bulunamadı. Lütfen listenin herkese açık (Public) olduğundan emin olun.")
                )
            }

            val parsedVideos = mutableListOf<ParsedVideoData>()
            for ((index, pvr) in videoRenderers.withIndex()) {
                val vid = pvr.optString("videoId")
                if (vid.isBlank()) continue

                // Extract Video Title
                var vTitle = pvr.optJSONObject("title")
                    ?.optJSONArray("runs")
                    ?.optJSONObject(0)
                    ?.optString("text")
                    .orEmpty()

                if (vTitle.isBlank()) {
                    vTitle = pvr.optJSONObject("title")?.optString("simpleText").orEmpty()
                }

                if (vTitle.isBlank()) {
                    vTitle = "Ders #${index + 1}"
                }

                // Extract Duration
                var durationSec = pvr.optString("lengthSeconds").toLongOrNull() ?: 0L
                if (durationSec <= 0L) {
                    val lengthText = pvr.optJSONObject("lengthText")?.optString("simpleText").orEmpty()
                    durationSec = parseLengthStringToSeconds(lengthText)
                }
                if (durationSec <= 0L) {
                    durationSec = 900L // 15 mins default fallback if duration hidden
                }

                // Extract Thumbnail
                val thumbUrl = "https://img.youtube.com/vi/$vid/hqdefault.jpg"

                // Extract Topic
                val topic = TopicExtractor.extractTopic(vTitle, playlistTitle)

                parsedVideos.add(
                    ParsedVideoData(
                        id = vid,
                        title = vTitle,
                        durationSeconds = durationSec,
                        position = index,
                        topic = topic,
                        thumbnailUrl = thumbUrl
                    )
                )
            }

            Result.success(
                ParsedPlaylistData(
                    id = playlistId,
                    title = playlistTitle,
                    channelTitle = channelTitle,
                    thumbnailUrl = playlistThumb,
                    videos = parsedVideos
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractVideoRenderers(obj: Any?, results: MutableList<JSONObject>) {
        when (obj) {
            is JSONObject -> {
                if (obj.has("playlistVideoRenderer")) {
                    val pvr = obj.optJSONObject("playlistVideoRenderer")
                    if (pvr != null) {
                        results.add(pvr)
                    }
                }
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    extractVideoRenderers(obj.opt(key), results)
                }
            }
            is JSONArray -> {
                for (i in 0 until obj.length()) {
                    extractVideoRenderers(obj.opt(i), results)
                }
            }
        }
    }

    /**
     * Parses time strings like "15:30" or "1:24:50" into total seconds.
     */
    private fun parseLengthStringToSeconds(timeStr: String): Long {
        if (timeStr.isBlank()) return 0L
        val parts = timeStr.trim().split(":")
        return try {
            when (parts.size) {
                2 -> {
                    val m = parts[0].toLong()
                    val s = parts[1].toLong()
                    (m * 60L) + s
                }
                3 -> {
                    val h = parts[0].toLong()
                    val m = parts[1].toLong()
                    val s = parts[2].toLong()
                    (h * 3600L) + (m * 60L) + s
                }
                else -> 0L
            }
        } catch (_: Exception) {
            0L
        }
    }
}
