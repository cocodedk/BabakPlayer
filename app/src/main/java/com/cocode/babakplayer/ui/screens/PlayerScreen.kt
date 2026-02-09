package com.cocode.babakplayer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import com.cocode.babakplayer.R
import com.cocode.babakplayer.model.Playlist
import com.cocode.babakplayer.model.PlaylistItem
import com.cocode.babakplayer.player.PlaybackUiState
import com.cocode.babakplayer.ui.MainUiState
import com.cocode.babakplayer.ui.components.DeleteItemDialog
import com.cocode.babakplayer.ui.components.DeletePlaylistDialog
import com.cocode.babakplayer.ui.components.PlayerPanel
import com.cocode.babakplayer.ui.components.PlaylistCard
import com.cocode.babakplayer.ui.components.createPlayerView

@Composable
fun PlayerScreen(
    uiState: MainUiState,
    playbackState: PlaybackUiState,
    player: ExoPlayer,
    seekIntervalSec: Int,
    onSelectPlaylist: (String, Boolean) -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekBy: (Long) -> Unit,
    onSeekTo: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onDeleteItem: (String, String) -> Unit,
    onDeletePlaylist: (String) -> Unit,
) {
    val selectedPlaylist = uiState.playlists.firstOrNull { it.playlistId == uiState.selectedPlaylistId }
    val currentItem = selectedPlaylist?.items?.firstOrNull { it.itemId == playbackState.currentItemId }
    var expandedPlaylistIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var pendingDeleteItem by remember { mutableStateOf<Pair<Playlist, PlaylistItem>?>(null) }
    var pendingDeletePlaylist by remember { mutableStateOf<Playlist?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (selectedPlaylist != null) {
            PlayerPanel(
                playerViewFactory = { context -> createPlayerView(context, player) },
                playback = playbackState,
                currentTitle = currentItem?.originalDisplayName ?: selectedPlaylist.title,
                onTogglePlayPause = onTogglePlayPause,
                onSeekBackward = { onSeekBy(-(seekIntervalSec * 1000L)) },
                onSeekForward = { onSeekBy(seekIntervalSec * 1000L) },
                onPrevious = onPrevious,
                onNext = onNext,
                onSeekTo = onSeekTo,
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (uiState.playlists.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.empty_playlists),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            items(items = uiState.playlists, key = { it.playlistId }) { playlist ->
                val isExpanded = expandedPlaylistIds.contains(playlist.playlistId)
                PlaylistCard(
                    playlist = playlist,
                    selected = playlist.playlistId == selectedPlaylist?.playlistId,
                    expanded = isExpanded,
                    currentItemId = playbackState.currentItemId,
                    onSelect = { onSelectPlaylist(playlist.playlistId, false) },
                    onPlay = { onSelectPlaylist(playlist.playlistId, true) },
                    onToggleExpanded = {
                        expandedPlaylistIds = if (isExpanded) {
                            expandedPlaylistIds - playlist.playlistId
                        } else {
                            expandedPlaylistIds + playlist.playlistId
                        }
                    },
                    onDeletePlaylist = { pendingDeletePlaylist = playlist },
                    onDeleteItem = { item -> pendingDeleteItem = playlist to item },
                )
            }
        }
    }

    pendingDeleteItem?.let { (playlist, item) ->
        DeleteItemDialog(
            playlist = playlist,
            item = item,
            onDismiss = { pendingDeleteItem = null },
            onDelete = onDeleteItem,
        )
    }

    pendingDeletePlaylist?.let { playlist ->
        DeletePlaylistDialog(
            playlist = playlist,
            onDismiss = { pendingDeletePlaylist = null },
            onDelete = onDeletePlaylist,
        )
    }
}
