package com.derstakip.app.util

import java.util.regex.Pattern

object IsoDurationParser {

    private val ISO_PATTERN = Pattern.compile(
        "^P(?:(\\d+)D)?(?:T(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?)?$"
    )

    /**
     * Parses ISO 8601 duration format (e.g. PT1H23M45S, PT15M, PT30S, P1DT2H) into total seconds.
     */
    fun parseToSeconds(isoDuration: String?): Long {
        if (isoDuration.isNullOrBlank()) return 0L

        val matcher = ISO_PATTERN.matcher(isoDuration.trim())
        if (!matcher.matches()) return 0L

        val days = matcher.group(1)?.toLongOrNull() ?: 0L
        val hours = matcher.group(2)?.toLongOrNull() ?: 0L
        val minutes = matcher.group(3)?.toLongOrNull() ?: 0L
        val seconds = matcher.group(4)?.toLongOrNull() ?: 0L

        return (days * 86400L) + (hours * 3600L) + (minutes * 60L) + seconds
    }

    /**
     * Formats duration into Turkish friendly readable string:
     * Examples:
     * - 5400s -> "1 sa 30 dk"
     * - 1800s -> "30 dk"
     * - 45s -> "45 sn"
     * - 7200s -> "2 sa"
     */
    fun formatDuration(totalSeconds: Long): String {
        if (totalSeconds <= 0L) return "0 dk"

        val hours = totalSeconds / 3600L
        val remainingAfterHours = totalSeconds % 3600L
        val minutes = remainingAfterHours / 60L
        val seconds = remainingAfterHours % 60L

        return when {
            hours > 0 && minutes > 0 -> "${hours} sa ${minutes} dk"
            hours > 0 && minutes == 0L -> "${hours} sa"
            minutes > 0 && seconds > 0 -> "${minutes} dk ${seconds} sn"
            minutes > 0 -> "${minutes} dk"
            else -> "${seconds} sn"
        }
    }

    /**
     * Formats duration in compact digital format:
     * Examples:
     * - 5400s -> "01:30:00"
     * - 933s -> "15:33"
     * - 45s -> "00:45"
     */
    fun formatDigital(totalSeconds: Long): String {
        if (totalSeconds <= 0L) return "00:00"

        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    /**
     * Formats total minutes into readable hours and minutes:
     */
    fun formatMinutes(minutes: Int): String {
        if (minutes <= 0) return "0 dk"
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h > 0 && m > 0 -> "${h} sa ${m} dk"
            h > 0 -> "${h} sa"
            else -> "${m} dk"
        }
    }
}
