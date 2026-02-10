package com.cocode.babakplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.cocode.babakplayer.R
import com.cocode.babakplayer.model.Playlist
import com.cocode.babakplayer.model.PlaylistItem
import com.cocode.babakplayer.player.PlaybackUiState
import com.cocode.babakplayer.ui.MainUiState
import com.cocode.babakplayer.ui.components.PlayerHeroCard
import com.cocode.babakplayer.ui.components.createPlayerView
import com.cocode.babakplayer.ui.theme.NeonBlue
import com.cocode.babakplayer.ui.theme.NeonPink
import com.cocode.babakplayer.ui.theme.Night
import com.cocode.babakplayer.util.asDurationText

@Composable
fun PlayerScreen(
    uiState: MainUiState,
    playbackState: PlaybackUiState,
    player: ExoPlayer,
    seekIntervalSec: Int,
    onTogglePlayPause: () -> Unit,
    onSeekBy: (Long) -> Unit,
    onSeekTo: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onEnterFullscreen: () -> Unit,
    isImporting: Boolean,
    onImportFromDevice: () -> Unit,
) {
    val selectedPlaylist = uiState.playlists.firstOrNull { it.playlistId == uiState.selectedPlaylistId }
    val currentItem = selectedPlaylist?.items?.firstOrNull { it.itemId == playbackState.currentItemId }
    val currentIndex = selectedPlaylist?.items?.indexOfFirst { it.itemId == playbackState.currentItemId } ?: -1

    if (selectedPlaylist == null) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            PlayerHeroCard(
                isImporting = isImporting,
                onImportFromDevice = onImportFromDevice,
            )
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        VideoSurface(
            player = player,
            onEnterFullscreen = onEnterFullscreen,
        )

        PlaylistInfoSection(
            playlist = selectedPlaylist,
            currentItem = currentItem,
            currentIndex = currentIndex,
        )

        SeekBar(
            playback = playbackState,
            onSeekTo = onSeekTo,
        )

        TransportControls(
            playback = playbackState,
            seekIntervalSec = seekIntervalSec,
            onTogglePlayPause = onTogglePlayPause,
            onSeekBy = onSeekBy,
            onPrevious = onPrevious,
            onNext = onNext,
        )

        PartsListSection(
            items = selectedPlaylist.items,
            currentItemId = playbackState.currentItemId,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun VideoSurface(
    player: ExoPlayer,
    onEnterFullscreen: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Night),
    ) {
        AndroidView(
            factory = { context -> createPlayerView(context, player) },
            modifier = Modifier.fillMaxSize(),
        )
        IconButton(
            onClick = onEnterFullscreen,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
        ) {
            Icon(
                Icons.Filled.Fullscreen,
                contentDescription = stringResource(R.string.content_fullscreen),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun PlaylistInfoSection(
    playlist: Playlist,
    currentItem: PlaylistItem?,
    currentIndex: Int,
) {
    var titleExpanded by remember { mutableStateOf(false) }
    var titleOverflows by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = currentItem?.originalDisplayName ?: playlist.title,
            style = MaterialTheme.typography.titleSmall,
            color = NeonPink,
            fontWeight = FontWeight.SemiBold,
            maxLines = if (titleExpanded) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result -> titleOverflows = result.hasVisualOverflow },
        )
        if (titleOverflows || titleExpanded) {
            Text(
                text = if (titleExpanded) stringResource(R.string.action_show_less) else stringResource(R.string.action_show_more),
                style = MaterialTheme.typography.labelSmall,
                color = NeonBlue,
                modifier = Modifier.clickable { titleExpanded = !titleExpanded },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (currentIndex >= 0) {
                Text(
                    text = stringResource(R.string.player_part_label, currentIndex + 1, playlist.items.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            Text(
                text = stringResource(R.string.playlist_files_size, playlist.itemCount, com.cocode.babakplayer.util.asReadableSize(playlist.totalBytes)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeekBar(
    playback: PlaybackUiState,
    onSeekTo: (Long) -> Unit,
) {
    var dragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(playback.positionMs, playback.durationMs, dragging) {
        if (!dragging) {
            dragValue = playback.positionMs.coerceAtLeast(0L).toFloat()
        }
    }

    val max = playback.durationMs.coerceAtLeast(1L).toFloat()

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
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
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    thumbSize = DpSize(4.dp, 16.dp),
                )
            },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
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
    }
}

@Composable
private fun TransportControls(
    playback: PlaybackUiState,
    seekIntervalSec: Int,
    onTogglePlayPause: () -> Unit,
    onSeekBy: (Long) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Outlined.SkipPrevious, contentDescription = stringResource(R.string.content_previous))
        }
        IconButton(onClick = { onSeekBy(-(seekIntervalSec * 1000L)) }) {
            Icon(Icons.Outlined.Replay, contentDescription = stringResource(R.string.content_seek_back))
        }
        IconButton(onClick = onTogglePlayPause) {
            Icon(
                imageVector = if (playback.isPlaying) Icons.Outlined.PauseCircle else Icons.Outlined.PlayCircle,
                contentDescription = if (playback.isPlaying) stringResource(R.string.content_pause) else stringResource(R.string.content_play),
                tint = NeonPink,
            )
        }
        IconButton(onClick = { onSeekBy(seekIntervalSec * 1000L) }) {
            Icon(Icons.Outlined.FastForward, contentDescription = stringResource(R.string.content_seek_forward))
        }
        IconButton(onClick = onNext) {
            Icon(Icons.Outlined.SkipNext, contentDescription = stringResource(R.string.content_next))
        }
    }
}

@Composable
private fun PartsListSection(
    items: List<PlaylistItem>,
    currentItemId: String?,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        itemsIndexed(items = items, key = { _, item -> item.itemId }) { index, item ->
            val isCurrent = item.itemId == currentItemId
            val bgColor = if (isCurrent) NeonBlue.copy(alpha = 0.26f) else MaterialTheme.colorScheme.background.copy(alpha = 0.45f)
            Surface(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
                color = bgColor,
                shape = RoundedCornerShape(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrent) NeonPink else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        text = item.originalDisplayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrent) NeonPink else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
