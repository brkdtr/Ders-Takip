package com.derstakip.app.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.derstakip.app.domain.model.Playlist
import com.derstakip.app.domain.model.TopicProgress
import com.derstakip.app.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProgressUiState(
    val activePlaylist: Playlist? = null,
    val topicProgressList: List<TopicProgress> = emptyList(),
    val totalVideos: Int = 0,
    val completedVideos: Int = 0,
    val totalDurationSeconds: Long = 0L,
    val completedDurationSeconds: Long = 0L,
    val isLoading: Boolean = false
) {
    val overallPercentage: Float
        get() = if (totalVideos > 0) completedVideos.toFloat() / totalVideos.toFloat() else 0f

    val remainingDurationSeconds: Long
        get() = (totalDurationSeconds - completedDurationSeconds).coerceAtLeast(0L)
}

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState(isLoading = true))
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        observeActivePlaylist()
    }

    private fun observeActivePlaylist() {
        viewModelScope.launch {
            playlistRepository.getActivePlaylist().collectLatest { playlist ->
                _uiState.value = _uiState.value.copy(activePlaylist = playlist)
                if (playlist != null) {
                    observeTopicProgress(playlist.id)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    private fun observeTopicProgress(playlistId: String) {
        viewModelScope.launch {
            playlistRepository.getTopicProgress(playlistId).collectLatest { list ->
                val totalV = list.sumOf { it.totalVideos }
                val completedV = list.sumOf { it.completedVideos }
                val totalD = list.sumOf { it.totalDurationSeconds }
                val completedD = list.sumOf { it.completedDurationSeconds }

                _uiState.value = _uiState.value.copy(
                    topicProgressList = list,
                    totalVideos = totalV,
                    completedVideos = completedV,
                    totalDurationSeconds = totalD,
                    completedDurationSeconds = completedD,
                    isLoading = false
                )
            }
        }
    }
}
