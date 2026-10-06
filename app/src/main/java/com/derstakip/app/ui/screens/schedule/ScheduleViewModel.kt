package com.derstakip.app.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.derstakip.app.domain.engine.SmartSchedulingEngine
import com.derstakip.app.domain.model.DailySchedulePlan
import com.derstakip.app.domain.model.Playlist
import com.derstakip.app.domain.repository.PlaylistRepository
import com.derstakip.app.domain.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScheduleUiState(
    val dailyCapacityMinutes: Int = 120,
    val selectedDays: Set<Int> = setOf(1, 2, 3, 4, 5), // Monday to Friday (1..7)
    val startDate: String = SmartSchedulingEngine.todayDateString(),
    val activePlaylist: Playlist? = null,
    val schedulePlans: List<DailySchedulePlan> = emptyList(),
    val isGenerating: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        loadSettingsAndSchedule()
    }

    private fun loadSettingsAndSchedule() {
        viewModelScope.launch {
            scheduleRepository.getStudySettings().collectLatest { settings ->
                _uiState.value = _uiState.value.copy(
                    dailyCapacityMinutes = settings.dailyCapacityMinutes,
                    selectedDays = settings.selectedDaysOfWeek.toSet(),
                    startDate = if (settings.startDate.isNotBlank()) settings.startDate else SmartSchedulingEngine.todayDateString()
                )
            }
        }

        viewModelScope.launch {
            playlistRepository.getActivePlaylist().collectLatest { playlist ->
                _uiState.value = _uiState.value.copy(activePlaylist = playlist)
                if (playlist != null) {
                    observeSchedules(playlist.id)
                }
            }
        }
    }

    private fun observeSchedules(playlistId: String) {
        viewModelScope.launch {
            scheduleRepository.getAllSchedules(playlistId).collectLatest { plans ->
                _uiState.value = _uiState.value.copy(schedulePlans = plans)
            }
        }
    }

    fun setDailyCapacity(minutes: Int) {
        _uiState.value = _uiState.value.copy(dailyCapacityMinutes = minutes.coerceIn(15, 480))
    }

    fun toggleDay(isoDay: Int) {
        val currentDays = _uiState.value.selectedDays.toMutableSet()
        if (currentDays.contains(isoDay)) {
            if (currentDays.size > 1) { // keep at least 1 day selected
                currentDays.remove(isoDay)
            }
        } else {
            currentDays.add(isoDay)
        }
        _uiState.value = _uiState.value.copy(selectedDays = currentDays)
    }

    fun generateSchedule() {
        val playlist = _uiState.value.activePlaylist
        if (playlist == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Lütfen önce bir oynatma listesi içe aktarın.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGenerating = true, errorMessage = null, userMessage = null)
            val result = scheduleRepository.generateAndSaveSchedule(
                playlistId = playlist.id,
                dailyCapacityMinutes = _uiState.value.dailyCapacityMinutes,
                selectedDays = _uiState.value.selectedDays.toList().sorted(),
                startDate = _uiState.value.startDate
            )

            if (result.isSuccess) {
                val plans = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    schedulePlans = plans,
                    userMessage = "Akıllı program başarıyla oluşturuldu! (${plans.size} çalışma günü)"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Program oluşturulamadı."
                )
            }
        }
    }

    fun toggleVideoCompletion(videoId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            playlistRepository.toggleVideoCompletion(videoId, isCompleted)
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(userMessage = null, errorMessage = null)
    }
}
