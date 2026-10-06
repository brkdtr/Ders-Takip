package com.derstakip.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Singleton entity storing user preferences and configuration for schedule generation.
 * Enforced as a single row with [id] = 1.
 *
 * @property id Primary key, always 1 for single-row configuration.
 * @property activePlaylistId The currently active playlist being studied/tracked.
 * @property dailyCapacityMinutes Maximum study duration per day in minutes (default 120 minutes = 2 hours).
 * @property selectedDaysOfWeek Comma-separated day indices of the week for study (e.g., "1,2,3,4,5" for Mon-Fri).
 * @property startDate Starting date for schedule distribution in "YYYY-MM-DD" format.
 * @property targetCompletionDate Optional calculated target completion date in "YYYY-MM-DD" format.
 * @property updatedAt Timestamp when settings were last modified.
 */
@Entity(tableName = "study_settings")
data class StudySettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val activePlaylistId: String? = null,
    val dailyCapacityMinutes: Int = 120,
    val selectedDaysOfWeek: String = "1,2,3,4,5",
    val startDate: String = "",
    val targetCompletionDate: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Converts comma-separated string of day numbers into a list of integers.
     * 1 = Monday, 2 = Tuesday, ..., 7 = Sunday.
     */
    fun getSelectedDaysList(): List<Int> {
        if (selectedDaysOfWeek.isBlank()) return emptyList()
        return selectedDaysOfWeek.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
    }

    /**
     * Total daily study capacity in seconds for precise calculations.
     */
    val dailyCapacitySeconds: Long
        get() = dailyCapacityMinutes * 60L
}
