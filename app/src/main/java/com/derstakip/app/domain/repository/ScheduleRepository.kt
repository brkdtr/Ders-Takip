package com.derstakip.app.domain.repository

import com.derstakip.app.domain.model.DailySchedulePlan
import com.derstakip.app.domain.model.StudySettings
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    suspend fun generateAndSaveSchedule(
        playlistId: String,
        dailyCapacityMinutes: Int,
        selectedDays: List<Int>,
        startDate: String
    ): Result<List<DailySchedulePlan>>

    fun getTodaySchedule(): Flow<DailySchedulePlan?>
    fun getAllSchedules(playlistId: String): Flow<List<DailySchedulePlan>>
    fun getStudySettings(): Flow<StudySettings>
    suspend fun saveStudySettings(settings: StudySettings)
}
