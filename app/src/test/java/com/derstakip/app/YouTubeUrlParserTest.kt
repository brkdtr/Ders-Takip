package com.derstakip.app

import com.derstakip.app.util.YouTubeUrlParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YouTubeUrlParserTest {

    @Test
    fun `extractPlaylistId extracts playlist ID from various YouTube URL variants`() {
        // Standard playlist link
        assertEquals(
            "PL1234567890abcdef",
            YouTubeUrlParser.extractPlaylistId("https://www.youtube.com/playlist?list=PL1234567890abcdef")
        )

        // Without www
        assertEquals(
            "PL1234567890abcdef",
            YouTubeUrlParser.extractPlaylistId("https://youtube.com/playlist?list=PL1234567890abcdef")
        )

        // Mobile link
        assertEquals(
            "PL1234567890abcdef",
            YouTubeUrlParser.extractPlaylistId("https://m.youtube.com/playlist?list=PL1234567890abcdef")
        )

        // Video watch link with list parameter
        assertEquals(
            "PL1234567890abcdef",
            YouTubeUrlParser.extractPlaylistId("https://www.youtube.com/watch?v=dQw4w9WgXcQ&list=PL1234567890abcdef")
        )

        // youtu.be short link
        assertEquals(
            "PL1234567890abcdef",
            YouTubeUrlParser.extractPlaylistId("https://youtu.be/dQw4w9WgXcQ?list=PL1234567890abcdef")
        )

        // URL with si tracking parameter
        assertEquals(
            "PL1234567890abcdef",
            YouTubeUrlParser.extractPlaylistId("https://www.youtube.com/playlist?si=track123&list=PL1234567890abcdef")
        )

        // Shared message containing a link
        assertEquals(
            "PL1234567890abcdef",
            YouTubeUrlParser.extractPlaylistId("Harika dersler burada: https://youtube.com/playlist?list=PL1234567890abcdef izlemelisin")
        )

        // Direct playlist ID
        assertEquals(
            "PL1234567890abcdef",
            YouTubeUrlParser.extractPlaylistId("PL1234567890abcdef")
        )
    }

    @Test
    fun `extractPlaylistId returns null for invalid inputs`() {
        assertNull(YouTubeUrlParser.extractPlaylistId(null))
        assertNull(YouTubeUrlParser.extractPlaylistId(""))
        assertNull(YouTubeUrlParser.extractPlaylistId("    "))
        assertNull(YouTubeUrlParser.extractPlaylistId("https://google.com"))
    }
}
