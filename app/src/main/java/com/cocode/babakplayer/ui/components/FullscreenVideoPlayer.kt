package com.cocode.babakplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import com.cocode.babakplayer.R
import com.cocode.babakplayer.player.PlaybackUiState
import com.cocode.babakplayer.ui.theme.NeonPink
import com.cocode.babakplayer.util.asDurationText
import kotlinx.coroutines.delay

@Composable
fun FullscreenVideoPlayer(
    player: ExoPlayer,
    playback: PlaybackUiState,
    seekIntervalSec: Int,
    onExitFullscreen: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekBy: (Long) -> Unit,
    onSeekTo: (Long) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    var controlsVisible by remember { mutableStateOf(true) }
    var dragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(playback.positionMs, playback.durationMs, dragging) {
        if (!dragging) {
            dragValue = playback.positionMs.coerceAtLeast(0L).toFloat()
        }
    }

    LaunchedEffect(controlsVisible, dragging) {
        if (controlsVisible && !dragging) {
            delay(3000)
            controlsVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) {
                controlsVisible = !controlsVisible
            },
    ) {
        AndroidView(
            factory = { context -> createPlayerView(context, player) },
            modifier = Modifier.fillMaxSize(),
        )

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
            ) {
                // Exit fullscreen - top right
                IconButton(
                    onClick = onExitFullscreen,
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                ) {
                    Icon(
                        Icons.Filled.FullscreenExit,
                        contentDescription = stringResource(R.string.content_exit_fullscreen),
                        tint = Color.White,
                    )
                }

                // Center play/pause
                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    Icon(
                        imageVector = if (playback.isPlaying) Icons.Outlined.PauseCircle else Icons.Outlined.PlayCircle,
                        contentDescription = if (playback.isPlaying) stringResource(R.string.content_pause) else stringResource(R.string.content_play),
                        tint = NeonPink,
                        modifier = Modifier.size(64.dp),
                    )
                }

                // Bottom controls: seek bar + transport
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    val max = playback.durationMs.coerceAtLeast(1L).toFloat()
                    Slider(
                        value = dragValue.coerceIn(0f, max),
                        onValueChange = {
                            dragging = true
                            dragValue = it
                        },
                        onValueChangeFinished = {
                            dragging = false
                            onSeekTo(dragValue.toLong())
                        },
                        valueRange = 0f..max,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = asDurationText(if (dragging) dragValue.toLong() else playback.positionMs),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f),
                        )
                        Text(
                            text = asDurationText(playback.durationMs),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        IconButton(onClick = onPrevious) {
                            Icon(Icons.Outlined.SkipPrevious, contentDescription = stringResource(R.string.content_previous), tint = Color.White)
                        }
                        IconButton(onClick = { onSeekBy(-(seekIntervalSec * 1000L)) }) {
                            Icon(Icons.Outlined.Replay, contentDescription = stringResource(R.string.content_seek_back), tint = Color.White)
                        }
                        IconButton(onClick = onTogglePlayPause) {
                            Icon(
                                imageVector = if (playback.isPlaying) Icons.Outlined.PauseCircle else Icons.Outlined.PlayCircle,
                                contentDescription = if (playback.isPlaying) stringResource(R.string.content_pause) else stringResource(R.string.content_play),
                                tint = NeonPink,
                            )
                        }
                        IconButton(onClick = { onSeekBy(seekIntervalSec * 1000L) }) {
                            Icon(Icons.Outlined.FastForward, contentDescription = stringResource(R.string.content_seek_forward), tint = Color.White)
                        }
                        IconButton(onClick = onNext) {
                            Icon(Icons.Outlined.SkipNext, contentDescription = stringResource(R.string.content_next), tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}
