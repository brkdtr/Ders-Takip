package com.derstakip.app.util

import java.net.URLDecoder
import java.util.regex.Pattern

object YouTubeUrlParser {

    private val PLAYLIST_URL_PATTERN = Pattern.compile(
        "(?:https?://)?(?:www\\.|m\\.)?(?:youtube\\.com/(?:playlist\\?|watch\\?.*?[&?])|youtu\\.be/.*?\\?)list=([a-zA-Z0-9_-]+)",
        Pattern.CASE_INSENSITIVE
    )

    private val DIRECT_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{10,40}$")

    /**
     * Extracts YouTube Playlist ID from a URL or raw ID string.
     * Supported formats:
     * - https://www.youtube.com/playlist?list=PL123456789
     * - https://youtube.com/playlist?list=PL123456789
     * - https://m.youtube.com/playlist?list=PL123456789
     * - https://www.youtube.com/watch?v=abcd&list=PL123456789
     * - https://youtu.be/abcd?list=PL123456789
     * - Direct ID: PL123456789
     */
    fun extractPlaylistId(input: String?): String? {
        if (input.isNullOrBlank()) return null

        val trimmed = input.trim()

        val matcher = PLAYLIST_URL_PATTERN.matcher(trimmed)
        if (matcher.find()) {
            return matcher.group(1)
        }

        // Direct ID match
        if (DIRECT_ID_PATTERN.matcher(trimmed).matches()) {
            return trimmed
        }

        // Try extracting query param directly if URL parsing helps
        try {
            val queryStart = trimmed.indexOf('?')
            if (queryStart != -1 && queryStart < trimmed.length - 1) {
                val query = trimmed.substring(queryStart + 1)
                val pairs = query.split("&")
                for (pair in pairs) {
                    val idx = pair.indexOf("=")
                    if (idx > 0) {
                        val key = URLDecoder.decode(pair.substring(0, idx), "UTF-8")
                        val value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
                        if (key.equals("list", ignoreCase = true) && value.isNotBlank()) {
                            return value
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore parse exception and return null
        }

        return null
    }
}
