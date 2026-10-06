package com.derstakip.app.data.repository

import com.derstakip.app.data.local.dao.DailyScheduleDao
import com.derstakip.app.data.local.dao.StudySettingsDao
import com.derstakip.app.data.local.dao.VideoDao
import com.derstakip.app.data.local.entity.DailyScheduleEntity
import com.derstakip.app.data.local.entity.ScheduleVideoItemEntity
import com.derstakip.app.data.local.entity.StudySettingsEntity
import com.derstakip.app.domain.engine.SmartSchedulingEngine
import com.derstakip.app.domain.model.DailySchedulePlan
import com.derstakip.app.domain.model.StudySettings
import com.derstakip.app.domain.model.Video
import com.derstakip.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val dailyScheduleDao: DailyScheduleDao,
    private val videoDao: VideoDao,
    private val studySettingsDao: StudySettingsDao
) : ScheduleRepository {

    override suspend fun generateAndSaveSchedule(
        playlistId: String,
        dailyCapacityMinutes: Int,
        selectedDays: List<Int>,
        startDate: String
    ): Result<List<DailySchedulePlan>> = withContext(Dispatchers.IO) {
        val videoEntities = videoDao.getVideosForPlaylistDirect(playlistId)
        if (videoEntities.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("Program oluşturmak için oynatma listesinde video bulunmalıdır."))
        }

        val domainVideos = videoEntities.map { entity ->
            Video(
                id = entity.id,
                playlistId = entity.playlistId,
                title = entity.title,
                description = entity.description,
                thumbnailUrl = entity.thumbnailUrl,
                durationSeconds = entity.durationSeconds,
                position = entity.position,
                topic = entity.topic ?: "Genel",
                isCompleted = entity.isCompleted,
                completedAt = entity.completedAt
            )
        }

        val effectiveStartDate = if (startDate.isBlank()) {
            SmartSchedulingEngine.todayDateString()
        } else {
            startDate
        }

        val plans = SmartSchedulingEngine.generateSchedule(
            videos = domainVideos,
            dailyCapacityMinutes = dailyCapacityMinutes,
            selectedDaysOfWeek = selectedDays,
            startDateString = effectiveStartDate
        )

        // Convert generated plans to Room database records
        val scheduleWithItems = plans.map { plan ->
            val scheduleEntity = DailyScheduleEntity(
                id = 0L,
                playlistId = playlistId,
                scheduledDate = plan.date,
                dayOfWeek = plan.dayOfWeek,
                totalAssignedSeconds = plan.totalDurationSeconds,
                isCompleted = plan.isAllCompleted
            )

            val items = plan.videos.mapIndexed { index, video ->
                ScheduleVideoItemEntity(
                    scheduleId = 0L, // will be replaced atomically in replaceSchedulePlan
                    videoId = video.id,
                    orderInDay = index
                )
            }

            scheduleEntity to items
        }

        dailyScheduleDao.replaceSchedulePlan(playlistId, scheduleWithItems)

        // Save settings to database
        studySettingsDao.insertOrUpdateSettings(
            StudySettingsEntity(
                id = 1,
                activePlaylistId = playlistId,
                dailyCapacityMinutes = dailyCapacityMinutes,
                selectedDaysOfWeek = selectedDays.joinToString(","),
                startDate = effectiveStartDate,
                targetCompletionDate = plans.lastOrNull()?.date
            )
        )

        Result.success(plans)
    }

    override fun getTodaySchedule(): Flow<DailySchedulePlan?> {
        val today = SmartSchedulingEngine.todayDateString()
        return dailyScheduleDao.getScheduleWithVideosByDate(today).map { scheduleWithVideos ->
            if (scheduleWithVideos != null && scheduleWithVideos.videos.isNotEmpty()) {
                DailySchedulePlan(
                    date = scheduleWithVideos.schedule.scheduledDate,
                    dayOfWeekName = SmartSchedulingEngine.TURKISH_DAY_NAMES[scheduleWithVideos.schedule.dayOfWeek] ?: "Bugün",
                    dayOfWeek = scheduleWithVideos.schedule.dayOfWeek,
                    totalDurationSeconds = scheduleWithVideos.calculatedTotalDurationSeconds,
                    videos = scheduleWithVideos.videos.map { entity ->
                        Video(
                            id = entity.id,
                            playlistId = entity.playlistId,
                            title = entity.title,
                            description = entity.description,
                            thumbnailUrl = entity.thumbnailUrl,
                            durationSeconds = entity.durationSeconds,
                            position = entity.position,
                            topic = entity.topic ?: "Genel",
                            isCompleted = entity.isCompleted,
                            completedAt = entity.completedAt,
                            scheduledDate = scheduleWithVideos.schedule.scheduledDate
                        )
                    }
                )
            } else {
                null
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun getAllSchedules(playlistId: String): Flow<List<DailySchedulePlan>> {
        return dailyScheduleDao.getAllSchedulesWithVideos(playlistId).map { list ->
            list.map { item ->
                DailySchedulePlan(
                    date = item.schedule.scheduledDate,
                    dayOfWeekName = SmartSchedulingEngine.TURKISH_DAY_NAMES[item.schedule.dayOfWeek] ?: "Gün",
                    dayOfWeek = item.schedule.dayOfWeek,
                    totalDurationSeconds = item.calculatedTotalDurationSeconds,
                    videos = item.videos.map { entity ->
                        Video(
                            id = entity.id,
                            playlistId = entity.playlistId,
                            title = entity.title,
                            description = entity.description,
                            thumbnailUrl = entity.thumbnailUrl,
                            durationSeconds = entity.durationSeconds,
                            position = entity.position,
                            topic = entity.topic ?: "Genel",
                            isCompleted = entity.isCompleted,
                            completedAt = entity.completedAt,
                            scheduledDate = item.schedule.scheduledDate
                        )
                    }
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun getStudySettings(): Flow<StudySettings> {
        return studySettingsDao.getSettings().map { entity ->
            if (entity != null) {
                StudySettings(
                    dailyCapacityMinutes = entity.dailyCapacityMinutes,
                    selectedDaysOfWeek = entity.getSelectedDaysList().ifEmpty { listOf(1, 2, 3, 4, 5) },
                    startDate = entity.startDate,
                    activePlaylistId = entity.activePlaylistId
                )
            } else {
                StudySettings()
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun saveStudySettings(settings: StudySettings) = withContext(Dispatchers.IO) {
        studySettingsDao.insertOrUpdateSettings(
            StudySettingsEntity(
                id = 1,
                activePlaylistId = settings.activePlaylistId,
                dailyCapacityMinutes = settings.dailyCapacityMinutes,
                selectedDaysOfWeek = settings.selectedDaysOfWeek.joinToString(","),
                startDate = settings.startDate
            )
        )
    }
}
