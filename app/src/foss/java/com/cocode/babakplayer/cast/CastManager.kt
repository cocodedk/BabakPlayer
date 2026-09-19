package com.cocode.babakplayer.cast

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.cocode.babakplayer.model.PlaylistItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// The `foss` flavor ships no Google Play Services, so this is a no-op stand-in for
// the full flavor's Cast implementation. Same package, class name and constructor as
// the full flavor's CastManager (see CastController) so common code -- MainViewModel,
// PlaybackController, PlayerScreen -- can depend on the CastController interface
// without knowing which flavor it was compiled into.
class CastManager(@Suppress("UNUSED_PARAMETER") context: Context) : CastController {

    private val _connectionState = MutableStateFlow(CastConnectionState.NOT_AVAILABLE)
    override val connectionState: StateFlow<CastConnectionState> = _connectionState.asStateFlow()

    override val isCasting: Boolean = false
    override val castPlayer: Player? = null

    override fun initialize(onStarted: () -> Unit, onEnded: () -> Unit) {
        // No Cast SDK in this build; connectionState stays NOT_AVAILABLE.
    }

    override fun release() {
        // Nothing was ever started.
    }

    override fun prepareCastQueue(queue: List<PlaylistItem>): List<MediaItem> = emptyList()
}
