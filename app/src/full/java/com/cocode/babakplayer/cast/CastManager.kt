package com.cocode.babakplayer.cast

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.cast.CastPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.cocode.babakplayer.model.PlaylistItem
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManager
import com.google.android.gms.cast.framework.SessionManagerListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// The `full` flavor's real Cast implementation. See CastController for why common
// code never references CastPlayer or the gms Cast SDK directly.
@OptIn(UnstableApi::class)
class CastManager(private val context: Context) : CastController {

    private var castContext: CastContext? = null
    private var sessionManager: SessionManager? = null
    private var internalCastPlayer: CastPlayer? = null
    override val castPlayer: Player? get() = internalCastPlayer
    private val mediaServer = LocalMediaServer(context)

    private val _connectionState = MutableStateFlow(CastConnectionState.NOT_AVAILABLE)
    override val connectionState: StateFlow<CastConnectionState> = _connectionState.asStateFlow()

    private var onSessionStarted: (() -> Unit)? = null
    private var onSessionEnded: (() -> Unit)? = null

    private val sessionListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarting(session: CastSession) {
            _connectionState.value = CastConnectionState.CONNECTING
        }

        override fun onSessionStarted(session: CastSession, sessionId: String) {
            _connectionState.value = CastConnectionState.CONNECTED
            val ctx = castContext ?: run {
                Log.w(TAG, "castContext is null in onSessionStarted, cannot create CastPlayer")
                return
            }
            internalCastPlayer?.release()
            internalCastPlayer = CastPlayer(ctx)
            mediaServer.start()
            onSessionStarted?.invoke()
        }

        override fun onSessionStartFailed(session: CastSession, error: Int) {
            _connectionState.value = CastConnectionState.NOT_CONNECTED
        }

        // Intentionally empty — cleanup is handled in onSessionEnded
        override fun onSessionEnding(session: CastSession) {}

        override fun onSessionEnded(session: CastSession, error: Int) {
            onSessionEnded?.invoke()
            internalCastPlayer?.release()
            internalCastPlayer = null
            mediaServer.clearRegistry()
            mediaServer.stop()
            _connectionState.value = CastConnectionState.NOT_CONNECTED
        }

        override fun onSessionResuming(session: CastSession, sessionId: String) {
            _connectionState.value = CastConnectionState.CONNECTING
        }

        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
            _connectionState.value = CastConnectionState.CONNECTED
            val ctx = castContext ?: run {
                Log.w(TAG, "castContext is null in onSessionResumed, cannot create CastPlayer")
                return
            }
            internalCastPlayer?.release()
            internalCastPlayer = CastPlayer(ctx)
            mediaServer.start()
            onSessionStarted?.invoke()
        }

        override fun onSessionResumeFailed(session: CastSession, error: Int) {
            _connectionState.value = CastConnectionState.NOT_CONNECTED
        }

        override fun onSessionSuspended(session: CastSession, reason: Int) {
            _connectionState.value = CastConnectionState.NOT_CONNECTED
        }
    }

    override fun initialize(onStarted: () -> Unit, onEnded: () -> Unit) {
        onSessionStarted = onStarted
        onSessionEnded = onEnded
        try {
            castContext = CastContext.getSharedInstance(context)
            sessionManager = castContext!!.sessionManager
            sessionManager!!.addSessionManagerListener(sessionListener, CastSession::class.java)
            _connectionState.value = CastConnectionState.NOT_CONNECTED
        } catch (e: Exception) {
            Log.w(TAG, "Cast SDK initialization failed, casting will be unavailable", e)
            _connectionState.value = CastConnectionState.NOT_AVAILABLE
        }
    }

    override val isCasting: Boolean
        get() = _connectionState.value == CastConnectionState.CONNECTED && internalCastPlayer != null

    override fun release() {
        sessionManager?.removeSessionManagerListener(sessionListener, CastSession::class.java)
        internalCastPlayer?.release()
        internalCastPlayer = null
        mediaServer.clearRegistry()
        mediaServer.stop()
        onSessionStarted = null
        onSessionEnded = null
        castContext = null
        sessionManager = null
        _connectionState.value = CastConnectionState.NOT_AVAILABLE
    }

    override fun prepareCastQueue(queue: List<PlaylistItem>): List<MediaItem> {
        queue.forEach { item ->
            mediaServer.registerFile(item.itemId, item.localPath, item.mimeType, item.bytes)
        }
        return queue.mapNotNull { item ->
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
    }

    private companion object {
        private const val TAG = "CastManager"
    }
}
