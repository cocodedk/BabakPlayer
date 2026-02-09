package com.cocode.babakplayer.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.cocode.babakplayer.model.PlaylistItem
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlaybackUiState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentItemId: String? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
)

class PlaybackController(
    context: Context,
    private val onDecodeError: (String?) -> Unit,
) {
    val player: ExoPlayer = ExoPlayer.Builder(context).build()
    private var autoplayNext: Boolean = true

    private val scopeJob = SupervisorJob()
    private val scope = CoroutineScope(scopeJob + Dispatchers.Main.immediate)
    private var ticker: Job? = null
    private val _uiState = MutableStateFlow(PlaybackUiState())
    val uiState: StateFlow<PlaybackUiState> = _uiState.asStateFlow()

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) = publishState()

            override fun onPlaybackStateChanged(playbackState: Int) {
                publishState()
                if (playbackState == Player.STATE_READY) startTicker()
                if (playbackState == Player.STATE_ENDED) stopTicker()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO && !autoplayNext) {
                    player.pause()
                }
                publishState(mediaItem?.mediaId)
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                val currentId = player.currentMediaItem?.mediaId
                onDecodeError(currentId)
                if (player.hasNextMediaItem()) {
                    player.seekToNextMediaItem()
                    player.prepare()
                    player.playWhenReady = true
                } else {
                    player.pause()
                }
            }
        })
    }

    fun setQueue(items: List<PlaylistItem>, startItemId: String? = null) {
        val mediaItems = items.map { item ->
            MediaItem.Builder()
                .setMediaId(item.itemId)
                .setMimeType(item.mimeType)
                .setUri(Uri.fromFile(File(item.localPath)))
                .build()
        }
        player.setMediaItems(mediaItems, false)
        val startIndex = startItemId?.let { id -> mediaItems.indexOfFirst { it.mediaId == id } } ?: 0
        if (startIndex > 0) player.seekTo(startIndex, 0L)
        player.prepare()
        publishState()
    }

    fun play() {
        player.playWhenReady = true
        player.play()
        startTicker()
    }

    fun pause() {
        player.pause()
        publishState()
    }

    fun togglePlayPause() {
        if (player.isPlaying) pause() else play()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        publishState()
    }

    fun seekBy(deltaMs: Long) {
        val duration = if (player.duration <= 0L || player.duration == C.TIME_UNSET) Long.MAX_VALUE else player.duration
        val target = (player.currentPosition + deltaMs).coerceIn(0L, duration)
        player.seekTo(target)
        publishState()
    }

    fun next() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
            player.playWhenReady = true
        }
    }

    fun previous() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
            player.playWhenReady = true
        } else {
            player.seekTo(0L)
        }
    }

    fun clearQueue() {
        player.clearMediaItems()
        player.pause()
        publishState(null)
    }

    fun setAutoplayNext(enabled: Boolean) {
        autoplayNext = enabled
    }

    fun release() {
        ticker?.cancel()
        player.release()
        scope.cancel()
        scopeJob.cancel()
    }

    private fun startTicker() {
        if (ticker?.isActive == true) return
        ticker = scope.launch {
            while (isActive) {
                publishState()
                delay(350)
            }
        }
    }

    private fun stopTicker() {
        ticker?.cancel()
        ticker = null
    }

    private fun publishState(forceItemId: String? = null) {
        val duration = player.duration.takeIf { it > 0L && it != C.TIME_UNSET } ?: 0L
        _uiState.value = PlaybackUiState(
            isPlaying = player.isPlaying,
            isBuffering = player.playbackState == Player.STATE_BUFFERING,
            currentItemId = forceItemId ?: player.currentMediaItem?.mediaId,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = duration,
        )
    }
}
