package com.derstakip.app

import com.derstakip.app.util.IsoDurationParser
import org.junit.Assert.assertEquals
import org.junit.Test

class IsoDurationParserTest {

    @Test
    fun `parseToSeconds parses various ISO 8601 duration formats accurately`() {
        assertEquals(933L, IsoDurationParser.parseToSeconds("PT15M33S"))
        assertEquals(5025L, IsoDurationParser.parseToSeconds("PT1H23M45S"))
        assertEquals(3600L, IsoDurationParser.parseToSeconds("PT1H"))
        assertEquals(1800L, IsoDurationParser.parseToSeconds("PT30M"))
        assertEquals(45L, IsoDurationParser.parseToSeconds("PT45S"))
        assertEquals(90000L, IsoDurationParser.parseToSeconds("P1DT1H"))
        assertEquals(0L, IsoDurationParser.parseToSeconds(null))
        assertEquals(0L, IsoDurationParser.parseToSeconds(""))
        assertEquals(0L, IsoDurationParser.parseToSeconds("INVALID"))
    }

    @Test
    fun `formatDuration formats to Turkish user-friendly representation`() {
        assertEquals("1 sa 25 dk", IsoDurationParser.formatDuration(5100L))
        assertEquals("2 sa", IsoDurationParser.formatDuration(7200L))
        assertEquals("30 dk", IsoDurationParser.formatDuration(1800L))
        assertEquals("45 sn", IsoDurationParser.formatDuration(45L))
        assertEquals("0 dk", IsoDurationParser.formatDuration(0L))
    }

    @Test
    fun `formatDigital formats correctly to digital clock string`() {
        assertEquals("15:33", IsoDurationParser.formatDigital(933L))
        assertEquals("01:23:45", IsoDurationParser.formatDigital(5025L))
        assertEquals("00:45", IsoDurationParser.formatDigital(45L))
        assertEquals("00:00", IsoDurationParser.formatDigital(0L))
    }
}
