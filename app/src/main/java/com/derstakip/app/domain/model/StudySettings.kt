package com.derstakip.app.domain.model

data class StudySettings(
    val dailyCapacityMinutes: Int = 120, // default 120 minutes (2 hours)
    val selectedDaysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5), // Monday to Friday (1..7)
    val startDate: String = "", // "YYYY-MM-DD"
    val activePlaylistId: String? = null
) {
    val dailyCapacitySeconds: Long
        get() = dailyCapacityMinutes * 60L
}
