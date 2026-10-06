package com.derstakip.app

import com.derstakip.app.domain.engine.SmartSchedulingEngine
import com.derstakip.app.domain.model.Video
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartSchedulingEngineTest {

    @Test
    fun `generateSchedule respects daily capacity and does not split individual video`() {
        // Daily capacity: 60 minutes = 3600 seconds
        val capacityMinutes = 60

        // Create 3 videos: 25 mins (1500s), 30 mins (1800s), 20 mins (1200s)
        // Day 1 can hold Video 1 (1500s) + Video 2 (1800s) = 3300s <= 3600s
        // Video 3 (1200s) would exceed 3600s, so it must go to Day 2 without being split!
        val videos = listOf(
            Video(id = "1", playlistId = "pl1", title = "Video 1", durationSeconds = 1500L, position = 0),
            Video(id = "2", playlistId = "pl1", title = "Video 2", durationSeconds = 1800L, position = 1),
            Video(id = "3", playlistId = "pl1", title = "Video 3", durationSeconds = 1200L, position = 2)
        )

        val schedule = SmartSchedulingEngine.generateSchedule(
            videos = videos,
            dailyCapacityMinutes = capacityMinutes,
            selectedDaysOfWeek = listOf(1, 2, 3, 4, 5), // Mon-Fri
            startDateString = "2026-10-12" // Monday
        )

        assertEquals("Should produce 2 days", 2, schedule.size)

        // Day 1
        assertEquals("Day 1 should have 2 videos", 2, schedule[0].videos.size)
        assertEquals("Day 1 total duration should be 3300s", 3300L, schedule[0].totalDurationSeconds)
        assertEquals("Day 1 date should be Monday 2026-10-12", "2026-10-12", schedule[0].date)

        // Day 2
        assertEquals("Day 2 should have 1 video", 1, schedule[1].videos.size)
        assertEquals("Day 2 total duration should be 1200s", 1200L, schedule[1].totalDurationSeconds)
        assertEquals("Day 2 date should be Tuesday 2026-10-13", "2026-10-13", schedule[1].date)
    }

    @Test
    fun `generateSchedule skips non-study days properly`() {
        // Only Mon (1) and Wed (3) are selected
        val selectedDays = listOf(1, 3)

        val videos = listOf(
            Video(id = "1", playlistId = "pl1", title = "Video 1", durationSeconds = 3600L, position = 0),
            Video(id = "2", playlistId = "pl1", title = "Video 2", durationSeconds = 3600L, position = 1)
        )

        val schedule = SmartSchedulingEngine.generateSchedule(
            videos = videos,
            dailyCapacityMinutes = 60, // 3600s each video fills the day
            selectedDaysOfWeek = selectedDays,
            startDateString = "2026-10-12" // Monday (1)
        )

        assertEquals("Should produce 2 days", 2, schedule.size)
        assertEquals("Day 1 must be Monday", "2026-10-12", schedule[0].date)
        assertEquals("Day 2 must skip Tuesday and be Wednesday", "2026-10-14", schedule[1].date)
        assertEquals("Day 2 ISO day must be 3 (Wednesday)", 3, schedule[1].dayOfWeek)
    }

    @Test
    fun `generateSchedule handles video exceeding daily capacity without splitting`() {
        // Daily capacity: 30 minutes (1800s)
        // Video length: 90 minutes (5400s)
        val videos = listOf(
            Video(id = "1", playlistId = "pl1", title = "Long Lecture", durationSeconds = 5400L, position = 0),
            Video(id = "2", playlistId = "pl1", title = "Short Review", durationSeconds = 1200L, position = 1)
        )

        val schedule = SmartSchedulingEngine.generateSchedule(
            videos = videos,
            dailyCapacityMinutes = 30,
            selectedDaysOfWeek = listOf(1, 2, 3),
            startDateString = "2026-10-12" // Monday
        )

        assertEquals("Should produce 2 days", 2, schedule.size)
        assertEquals("Day 1 has the long video alone", 1, schedule[0].videos.size)
        assertEquals(5400L, schedule[0].totalDurationSeconds)
        assertEquals("Day 2 has the second video", 1, schedule[1].videos.size)
        assertEquals(1200L, schedule[1].totalDurationSeconds)
    }

    @Test
    fun `generateSchedule with empty list returns empty plan`() {
        val schedule = SmartSchedulingEngine.generateSchedule(
            videos = emptyList(),
            dailyCapacityMinutes = 120,
            selectedDaysOfWeek = listOf(1, 2, 3)
        )
        assertTrue("Schedule must be empty", schedule.isEmpty())
    }
}
