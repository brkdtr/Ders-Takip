package com.derstakip.app.ui.screens.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.derstakip.app.domain.model.Playlist
import com.derstakip.app.domain.model.Video
import com.derstakip.app.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistUiState(
    val urlInput: String = "",
    val isLoading: Boolean = false,
    val activePlaylist: Playlist? = null,
    val allPlaylists: List<Playlist> = emptyList(),
    val videos: List<Video> = emptyList(),
    val userMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistUiState())
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()

    init {
        observePlaylists()
        observeActivePlaylist()
    }

    private fun observePlaylists() {
        viewModelScope.launch {
            playlistRepository.getAllPlaylists().collectLatest { playlists ->
                _uiState.value = _uiState.value.copy(allPlaylists = playlists)
            }
        }
    }

    private fun observeActivePlaylist() {
        viewModelScope.launch {
            playlistRepository.getActivePlaylist().collectLatest { playlist ->
                _uiState.value = _uiState.value.copy(activePlaylist = playlist)
                if (playlist != null) {
                    observeVideos(playlist.id)
                }
            }
        }
    }

    private fun observeVideos(playlistId: String) {
        viewModelScope.launch {
            playlistRepository.getVideosForPlaylist(playlistId).collectLatest { videos ->
                _uiState.value = _uiState.value.copy(videos = videos)
            }
        }
    }

    fun onUrlChanged(newUrl: String) {
        _uiState.value = _uiState.value.copy(urlInput = newUrl, errorMessage = null)
    }

    fun importPlaylist(urlOverride: String? = null) {
        val targetUrl = urlOverride ?: _uiState.value.urlInput
        if (targetUrl.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Lütfen bir YouTube oynatma listesi linki veya ID'si girin.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, userMessage = null)
            val result = playlistRepository.importPlaylist(targetUrl)
            if (result.isSuccess) {
                val playlist = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    urlInput = "",
                    userMessage = "\"${playlist.title}\" başarıyla içe aktarıldı!"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Oynatma listesi içe aktarılırken bir hata oluştu."
                )
            }
        }
    }

    fun selectPlaylist(playlistId: String) {
        viewModelScope.launch {
            playlistRepository.setActivePlaylist(playlistId)
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlistId)
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
