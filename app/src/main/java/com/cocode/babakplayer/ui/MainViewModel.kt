package com.cocode.babakplayer.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cocode.babakplayer.R
import com.cocode.babakplayer.data.PlaylistRepository
import com.cocode.babakplayer.model.ImportSummary
import com.cocode.babakplayer.model.Playlist
import com.cocode.babakplayer.player.PlaybackController
import com.cocode.babakplayer.player.PlaybackUiState
import com.cocode.babakplayer.util.SharePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MainUiState(
    val playlists: List<Playlist> = emptyList(),
    val selectedPlaylistId: String? = null,
    val importSummary: ImportSummary? = null,
    val isImporting: Boolean = false,
    val noticeResId: Int? = null,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PlaylistRepository(application)
    private val playbackController = PlaybackController(application, ::handleDecodeError)

    val player = playbackController.player
    val playbackState: StateFlow<PlaybackUiState> = playbackController.uiState

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refreshPlaylists()
    }

    fun importSharePayload(payload: SharePayload) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, noticeResId = null) }
            val result = withContext(Dispatchers.IO) { repository.importPayload(payload) }
            val latest = withContext(Dispatchers.IO) { repository.loadPlaylists() }
            val selectedId = result.playlist?.playlistId ?: latest.firstOrNull()?.playlistId
            _uiState.update {
                it.copy(
                    playlists = latest,
                    selectedPlaylistId = selectedId,
                    importSummary = result.summary,
                    isImporting = false,
                )
            }
            result.playlist?.let { preparePlaylist(it, autoPlay = true) }
        }
    }

    fun importFromDeviceUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        importSharePayload(
            SharePayload(
                uris = uris,
                caption = null,
                firstDescription = null,
                sourceApp = DEVICE_STORAGE_SOURCE_APP,
            ),
        )
    }

    fun refreshPlaylists() {
        viewModelScope.launch {
            val playlists = withContext(Dispatchers.IO) { repository.loadPlaylists() }
            val selected = _uiState.value.selectedPlaylistId?.takeIf { id ->
                playlists.any { it.playlistId == id }
            } ?: playlists.firstOrNull()?.playlistId
            _uiState.update { it.copy(playlists = playlists, selectedPlaylistId = selected) }
            if (player.mediaItemCount == 0) {
                playlists.firstOrNull { it.playlistId == selected }?.let { preparePlaylist(it, autoPlay = false) }
            }
        }
    }

    fun selectPlaylist(playlistId: String, autoPlay: Boolean = false) {
        _uiState.update { it.copy(selectedPlaylistId = playlistId) }
        _uiState.value.playlists.firstOrNull { it.playlistId == playlistId }?.let {
            preparePlaylist(it, autoPlay)
        }
    }

    fun togglePlayPause() = playbackController.togglePlayPause()

    fun seekTo(positionMs: Long) = playbackController.seekTo(positionMs)

    fun seekBy(deltaMs: Long) = playbackController.seekBy(deltaMs)

    fun next() = playbackController.next()

    fun previous() = playbackController.previous()

    fun setAutoplayNext(enabled: Boolean) {
        playbackController.setAutoplayNext(enabled)
    }

    fun deleteItem(playlistId: String, itemId: String) {
        viewModelScope.launch {
            val before = _uiState.value.playlists.firstOrNull { it.playlistId == playlistId }
            val deletedIndex = before?.items?.indexOfFirst { it.itemId == itemId } ?: -1
            val activeItem = playbackState.value.currentItemId
            val wasPlaying = playbackState.value.isPlaying
            val resumeItemId = if (activeItem == itemId && deletedIndex >= 0) {
                before?.items?.getOrNull(deletedIndex + 1)?.itemId
            } else {
                activeItem
            }

            withContext(Dispatchers.IO) { repository.deleteItem(playlistId, itemId) }
            val playlists = withContext(Dispatchers.IO) { repository.loadPlaylists() }
            val selected = if (playlists.any { it.playlistId == playlistId }) playlistId else playlists.firstOrNull()?.playlistId
            _uiState.update { it.copy(playlists = playlists, selectedPlaylistId = selected) }
            val selectedPlaylist = playlists.firstOrNull { it.playlistId == selected }
            if (selectedPlaylist == null) playbackController.clearQueue()
            else preparePlaylist(selectedPlaylist, autoPlay = wasPlaying, startItemId = resumeItemId)
            _uiState.update { it.copy(noticeResId = R.string.notice_file_deleted) }
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            val wasPlaying = playbackState.value.isPlaying
            withContext(Dispatchers.IO) { repository.deletePlaylist(playlistId) }
            val playlists = withContext(Dispatchers.IO) { repository.loadPlaylists() }
            val selected = playlists.firstOrNull()?.playlistId
            _uiState.update { it.copy(playlists = playlists, selectedPlaylistId = selected) }
            val first = playlists.firstOrNull()
            if (first == null) playbackController.clearQueue() else preparePlaylist(first, autoPlay = wasPlaying)
            _uiState.update { it.copy(noticeResId = R.string.notice_playlist_deleted) }
        }
    }

    fun clearImportSummary() {
        _uiState.update { it.copy(importSummary = null) }
    }

    fun clearNotice() {
        _uiState.update { it.copy(noticeResId = null) }
    }

    private fun preparePlaylist(playlist: Playlist, autoPlay: Boolean, startItemId: String? = null) {
        playbackController.setQueue(playlist.items, startItemId)
        if (autoPlay) playbackController.play()
    }

    private fun handleDecodeError(itemId: String?) {
        val playlistId = _uiState.value.selectedPlaylistId ?: return
        if (itemId == null) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.markItemDecodeFailed(playlistId, itemId) }
            _uiState.update { it.copy(noticeResId = R.string.notice_decode_skip) }
            refreshPlaylists()
        }
    }

    override fun onCleared() {
        playbackController.release()
        super.onCleared()
    }

    private companion object {
        private const val DEVICE_STORAGE_SOURCE_APP = "device_storage"
    }
}
