package com.cocode.babakplayer.ui.components

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

fun createPlayerView(context: Context, player: ExoPlayer): PlayerView {
    return PlayerView(context).apply {
        this.player = player
        useController = false
    }
}
