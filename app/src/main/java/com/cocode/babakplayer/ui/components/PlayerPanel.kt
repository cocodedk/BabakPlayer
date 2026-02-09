package com.cocode.babakplayer.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.cocode.babakplayer.R
import com.cocode.babakplayer.player.PlaybackUiState
import com.cocode.babakplayer.ui.theme.NeonBlue
import com.cocode.babakplayer.ui.theme.NeonPink
import com.cocode.babakplayer.util.asDurationText

@Composable
fun PlayerPanel(
    playerViewFactory: (android.content.Context) -> PlayerView,
    playback: PlaybackUiState,
    currentTitle: String,
    onTogglePlayPause: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekForward: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(playback.positionMs, playback.durationMs, dragging) {
        if (!dragging) {
            dragValue = playback.positionMs.coerceAtLeast(0L).toFloat()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NeonBlue.copy(alpha = 0.62f), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = stringResource(R.string.current_playing_title), style = MaterialTheme.typography.labelMedium)
            Text(text = currentTitle, style = MaterialTheme.typography.titleMedium)

            AndroidView(
                factory = playerViewFactory,
                modifier = Modifier.fillMaxWidth().height(200.dp),
            )

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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = asDurationText(if (dragging) dragValue.toLong() else playback.positionMs),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = asDurationText(playback.durationMs),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                IconButton(onClick = onPrevious) {
                    Icon(Icons.Outlined.SkipPrevious, contentDescription = stringResource(R.string.content_previous))
                }
                IconButton(onClick = onSeekBackward) {
                    Icon(Icons.Outlined.Replay, contentDescription = stringResource(R.string.content_seek_back))
                }
                IconButton(onClick = onTogglePlayPause) {
                    Icon(
                        imageVector = if (playback.isPlaying) Icons.Outlined.PauseCircle else Icons.Outlined.PlayCircle,
                        contentDescription = if (playback.isPlaying) stringResource(R.string.content_pause) else stringResource(R.string.content_play),
                        tint = NeonPink,
                    )
                }
                IconButton(onClick = onSeekForward) {
                    Icon(Icons.Outlined.FastForward, contentDescription = stringResource(R.string.content_seek_forward))
                }
                IconButton(onClick = onNext) {
                    Icon(Icons.Outlined.SkipNext, contentDescription = stringResource(R.string.content_next))
                }
            }
        }
    }
}

fun createPlayerView(context: android.content.Context, player: androidx.media3.exoplayer.ExoPlayer): PlayerView {
    return PlayerView(context).apply {
        this.player = player
        useController = false
    }
}
