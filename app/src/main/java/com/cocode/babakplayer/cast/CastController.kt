package com.cocode.babakplayer.cast

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.cocode.babakplayer.model.PlaylistItem
import kotlinx.coroutines.flow.StateFlow

/**
 * Abstraction over Google Cast so common code (PlaybackController, MainViewModel,
 * PlayerScreen) never imports the Cast SDK or Play Services directly.
 *
 * The `full` flavor implements this against media3-cast + play-services-cast-framework.
 * The `foss` flavor -- which ships no Google Play Services -- is a no-op that keeps the
 * app buildable and fully functional (minus casting) without those dependencies. Both
 * flavors provide a `CastManager` class with this same package and constructor, so
 * common code can instantiate it by name and get whichever implementation the flavor
 * that's compiling supplies.
 */
interface CastController {
    val connectionState: StateFlow<CastConnectionState>
    val isCasting: Boolean

    // Exposed through the flavor-agnostic Player interface -- CastPlayer (media3-cast)
    // implements it -- so common code never needs to import androidx.media3.cast.
    val castPlayer: Player?

    fun initialize(onStarted: () -> Unit, onEnded: () -> Unit)
    fun release()

    /**
     * Registers [queue] with the local HTTP media server for streaming to a cast
     * device and returns the resulting cast-ready media items. Returns an empty list
     * when casting is unavailable (always, in the foss no-op implementation).
     */
    fun prepareCastQueue(queue: List<PlaylistItem>): List<MediaItem>
}
