package com.cocode.babakplayer.ui.components

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

fun createPlayerView(context: android.content.Context, player: ExoPlayer): PlayerView {
    return PlayerView(context).apply {
        this.player = player
        useController = false
    }
}
