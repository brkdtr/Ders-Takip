package com.derstakip.app.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class TodayUiState(
    val todayPlan: DailySchedulePlan? = null,
    val activePlaylist: Playlist? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState(isLoading = true))
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        observeActivePlaylist()
        observeTodaySchedule()
    }

    private fun observeActivePlaylist() {
        viewModelScope.launch {
            playlistRepository.getActivePlaylist().collectLatest { playlist ->
                _uiState.value = _uiState.value.copy(activePlaylist = playlist)
            }
        }
    }

    private fun observeTodaySchedule() {
        viewModelScope.launch {
            scheduleRepository.getTodaySchedule().collectLatest { plan ->
                _uiState.value = _uiState.value.copy(
                    todayPlan = plan,
                    isLoading = false
                )
            }
        }
    }

    fun toggleVideoCompletion(videoId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            playlistRepository.toggleVideoCompletion(videoId, isCompleted)
        }
    }
}
