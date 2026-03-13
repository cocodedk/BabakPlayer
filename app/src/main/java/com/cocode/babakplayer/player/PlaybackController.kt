package com.cocode.babakplayer.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.cocode.babakplayer.cast.CastManager
import com.cocode.babakplayer.model.PlaylistItem
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
    val isCasting: Boolean = false,
)

@OptIn(UnstableApi::class)
class PlaybackController(
    context: Context,
    private val onDecodeError: (String?) -> Unit,
) {
    val player: ExoPlayer = ExoPlayer.Builder(context).build()
    private var activePlayer: Player = player
    private var autoplayNext: Boolean = true
    private var currentQueue: List<PlaylistItem> = emptyList()

    private val scopeJob = SupervisorJob()
    private val scope = CoroutineScope(scopeJob + Dispatchers.Main.immediate)
    private var ticker: Job? = null
    private val _uiState = MutableStateFlow(PlaybackUiState())
    val uiState: StateFlow<PlaybackUiState> = _uiState.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) = publishState()

        override fun onPlaybackStateChanged(playbackState: Int) {
            publishState()
            if (playbackState == Player.STATE_READY) startTicker()
            if (playbackState == Player.STATE_ENDED) stopTicker()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO && !autoplayNext) {
                activePlayer.pause()
            }
            publishState(mediaItem?.mediaId)
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            val currentId = activePlayer.currentMediaItem?.mediaId
            onDecodeError(currentId)
            if (activePlayer.hasNextMediaItem()) {
                activePlayer.seekToNextMediaItem()
                activePlayer.prepare()
                activePlayer.playWhenReady = true
            } else {
                activePlayer.pause()
            }
        }
    }

    init {
        player.addListener(playerListener)
    }

    fun setQueue(items: List<PlaylistItem>, startItemId: String? = null) {
        currentQueue = items
        val mediaItems = items.map { item ->
            MediaItem.Builder()
                .setMediaId(item.itemId)
                .setMimeType(item.mimeType)
                .setUri(resolveMediaUri(item.localPath))
                .build()
        }
        player.setMediaItems(mediaItems, false)
        val startIndex = startItemId?.let { id -> mediaItems.indexOfFirst { it.mediaId == id } } ?: 0
        if (startIndex > 0) player.seekTo(startIndex, 0L)
        player.prepare()
        publishState()
    }

    fun switchToCast(castManager: CastManager) {
        val castPlayer = castManager.castPlayer ?: return
        val mediaServer = castManager.mediaServer

        // Save local state
        val currentIndex = player.currentMediaItemIndex
        val currentPosition = player.currentPosition
        val wasPlaying = player.isPlaying

        player.pause()
        player.removeListener(playerListener)

        // Register all queue items with the HTTP server
        currentQueue.forEach { item ->
            mediaServer.registerFile(item.itemId, item.localPath, item.mimeType, item.bytes)
        }

        // Build cast media items with HTTP URLs
        val castMediaItems = currentQueue.mapNotNull { item ->
            val url = mediaServer.getStreamUrl(item.itemId) ?: return@mapNotNull null
            MediaItem.Builder()
                .setMediaId(item.itemId)
                .setUri(url)
                .setMimeType(item.mimeType)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(item.originalDisplayName)
                        .build()
                )
                .build()
        }

        if (castMediaItems.isEmpty()) {
            player.addListener(playerListener)
            activePlayer = player
            player.seekTo(currentIndex, currentPosition)
            if (wasPlaying) player.play()
            startTicker()
            publishState()
            return
        }

        // Map original index to filtered list; fall back to 0 if the item was dropped
        val currentItemId = player.currentMediaItem?.mediaId
        val castStartIndex = if (currentItemId != null) {
            castMediaItems.indexOfFirst { it.mediaId == currentItemId }.coerceAtLeast(0)
        } else {
            0
        }

        activePlayer = castPlayer
        castPlayer.addListener(playerListener)
        castPlayer.setMediaItems(castMediaItems, castStartIndex, currentPosition)
        castPlayer.prepare()
        if (wasPlaying) castPlayer.play()
        startTicker()
        publishState()
    }

    fun switchToLocal(castManager: CastManager) {
        val castPlayer = castManager.castPlayer

        // Save cast state
        val currentIndex = castPlayer?.currentMediaItemIndex ?: 0
        val currentPosition = castPlayer?.currentPosition ?: 0L
        val wasPlaying = castPlayer?.isPlaying ?: false

        castPlayer?.removeListener(playerListener)
        castPlayer?.stop()

        activePlayer = player
        player.addListener(playerListener)

        if (player.mediaItemCount > 0) {
            val clampedIndex = currentIndex.coerceIn(0, (player.mediaItemCount - 1).coerceAtLeast(0))
            val clampedPosition = currentPosition.coerceAtLeast(0L)
            player.seekTo(clampedIndex, clampedPosition)
            if (wasPlaying) player.play()
        }
        startTicker()
        publishState()
    }

    fun play() {
        activePlayer.playWhenReady = true
        activePlayer.play()
        startTicker()
    }

    fun pause() {
        activePlayer.pause()
        publishState()
    }

    fun togglePlayPause() {
        if (activePlayer.isPlaying) pause() else play()
    }

    fun seekTo(positionMs: Long) {
        activePlayer.seekTo(positionMs)
        publishState()
    }

    fun seekBy(deltaMs: Long) {
        val duration = if (activePlayer.duration <= 0L || activePlayer.duration == C.TIME_UNSET) Long.MAX_VALUE else activePlayer.duration
        val target = (activePlayer.currentPosition + deltaMs).coerceIn(0L, duration)
        activePlayer.seekTo(target)
        publishState()
    }

    fun next() {
        if (activePlayer.hasNextMediaItem()) {
            activePlayer.seekToNextMediaItem()
            activePlayer.playWhenReady = true
        }
    }

    fun previous() {
        if (activePlayer.hasPreviousMediaItem()) {
            activePlayer.seekToPreviousMediaItem()
            activePlayer.playWhenReady = true
        } else {
            activePlayer.seekTo(0L)
        }
    }

    fun clearQueue() {
        activePlayer.clearMediaItems()
        activePlayer.pause()
        currentQueue = emptyList()
        publishState(null)
    }

    fun setAutoplayNext(enabled: Boolean) {
        autoplayNext = enabled
    }

    fun release() {
        ticker?.cancel()
        if (activePlayer !== player) {
            activePlayer.removeListener(playerListener)
        }
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
        val p = activePlayer
        val duration = p.duration.takeIf { it > 0L && it != C.TIME_UNSET } ?: 0L
        _uiState.value = PlaybackUiState(
            isPlaying = p.isPlaying,
            isBuffering = p.playbackState == Player.STATE_BUFFERING,
            currentItemId = forceItemId ?: p.currentMediaItem?.mediaId,
            positionMs = p.currentPosition.coerceAtLeast(0L),
            durationMs = duration,
            isCasting = activePlayer !== player,
        )
    }

    private fun resolveMediaUri(pathOrUri: String): Uri {
        require(pathOrUri.isNotBlank()) { "Media path/URI must not be blank" }
        val parsed = Uri.parse(pathOrUri)
        return if (parsed.scheme.isNullOrBlank()) Uri.fromFile(java.io.File(pathOrUri)) else parsed
    }
}
