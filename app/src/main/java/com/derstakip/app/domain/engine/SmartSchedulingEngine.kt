package com.derstakip.app.domain.engine

import com.derstakip.app.domain.model.DailySchedulePlan
import com.derstakip.app.domain.model.Video
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Smart Scheduling Engine for course and video tracking.
 *
 * Implements intelligent packing and distribution:
 * 1. Respects daily capacity limit (e.g., 120 minutes per day).
 * 2. Only assigns study items to user's selected days of the week.
 * 3. Never splits an individual video across days unless the video alone exceeds the daily capacity limit.
 * 4. Preserves pedagogical sequence and groups lessons logically.
 */
object SmartSchedulingEngine {

    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Turkish day names mapping for ISO day numbers (1 = Monday ... 7 = Sunday).
     */
    val TURKISH_DAY_NAMES = mapOf(
        1 to "Pazartesi",
        2 to "Salı",
        3 to "Çarşamba",
        4 to "Perşembe",
        5 to "Cuma",
        6 to "Cumartesi",
        7 to "Pazar"
    )

    /**
     * Generates a study schedule plan.
     *
     * @param videos List of videos to schedule, ordered by course position.
     * @param dailyCapacityMinutes Daily study limit in minutes (e.g. 120).
     * @param selectedDaysOfWeek List of active ISO study days (1 = Mon .. 7 = Sun).
     * @param startDateString Starting date in "yyyy-MM-dd" format, or blank for today.
     * @return List of [DailySchedulePlan] representing consecutive scheduled study days.
     */
    fun generateSchedule(
        videos: List<Video>,
        dailyCapacityMinutes: Int,
        selectedDaysOfWeek: List<Int>,
        startDateString: String = ""
    ): List<DailySchedulePlan> {
        if (videos.isEmpty()) return emptyList()

        val safeCapacityMinutes = if (dailyCapacityMinutes <= 0) 120 else dailyCapacityMinutes
        val dailyCapacitySeconds = safeCapacityMinutes * 60L

        // Default to Mon-Fri if no days selected
        val activeDays = if (selectedDaysOfWeek.isEmpty()) {
            listOf(1, 2, 3, 4, 5)
        } else {
            selectedDaysOfWeek
        }.toSet()

        val calendar = Calendar.getInstance()
        if (startDateString.isNotBlank()) {
            try {
                val parsed = DATE_FORMAT.parse(startDateString)
                if (parsed != null) {
                    calendar.time = parsed
                }
            } catch (_: Exception) {
                // fallback to current calendar time
            }
        }

        // Zero-out hour, minute, second for clean date tracking
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val schedulePlans = mutableListOf<DailySchedulePlan>()
        val queue = ArrayDeque(videos)

        while (queue.isNotEmpty()) {
            // Advance calendar until we hit an active study day
            var currentIsoDay = getIsoDayOfWeek(calendar)
            while (!activeDays.contains(currentIsoDay)) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                currentIsoDay = getIsoDayOfWeek(calendar)
            }

            val currentDateStr = DATE_FORMAT.format(calendar.time)
            val dayName = TURKISH_DAY_NAMES[currentIsoDay] ?: "Gün"

            val assignedVideosForDay = mutableListOf<Video>()
            var accumulatedSeconds = 0L

            while (queue.isNotEmpty()) {
                val nextVideo = queue.first()
                val videoDuration = nextVideo.durationSeconds

                if (assignedVideosForDay.isEmpty()) {
                    // First video of the day: always accept it, even if single video exceeds capacity
                    queue.removeFirst()
                    assignedVideosForDay.add(nextVideo.copy(scheduledDate = currentDateStr))
                    accumulatedSeconds += videoDuration

                    // If single video duration already meets or exceeds daily capacity, end this day
                    if (accumulatedSeconds >= dailyCapacitySeconds) {
                        break
                    }
                } else {
                    // Subsequent videos: check if adding fits within capacity
                    if (accumulatedSeconds + videoDuration <= dailyCapacitySeconds) {
                        queue.removeFirst()
                        assignedVideosForDay.add(nextVideo.copy(scheduledDate = currentDateStr))
                        accumulatedSeconds += videoDuration
                    } else {
                        // Exceeds daily capacity. Rule: Do not split video! Close this day.
                        break
                    }
                }
            }

            if (assignedVideosForDay.isNotEmpty()) {
                schedulePlans.add(
                    DailySchedulePlan(
                        date = currentDateStr,
                        dayOfWeekName = dayName,
                        dayOfWeek = currentIsoDay,
                        totalDurationSeconds = accumulatedSeconds,
                        videos = assignedVideosForDay
                    )
                )
            }

            // Move to the next calendar day
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return schedulePlans
    }

    /**
     * Converts Calendar.DAY_OF_WEEK (Sun=1, Mon=2..Sat=7) to ISO-8601 (Mon=1..Sun=7).
     */
    fun getIsoDayOfWeek(calendar: Calendar): Int {
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    /**
     * Formats current date to ISO "yyyy-MM-dd".
     */
    fun todayDateString(): String {
        return DATE_FORMAT.format(Date())
    }
}
