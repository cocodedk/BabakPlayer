package com.cocode.babakplayer.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenMultipleDocuments
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.cocode.babakplayer.ui.components.ImportSummaryCard
import com.cocode.babakplayer.ui.components.PlayerHeroCard
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
    onImportFromDevice: (List<Uri>) -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekBy: (Long) -> Unit,
    onSeekTo: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onDeleteItem: (String, String) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onDismissSummary: () -> Unit,
) {
    val selectedPlaylist = uiState.playlists.firstOrNull { it.playlistId == uiState.selectedPlaylistId }
    val currentItem = selectedPlaylist?.items?.firstOrNull { it.itemId == playbackState.currentItemId }
    var pendingDeleteItem by remember { mutableStateOf<Pair<Playlist, PlaylistItem>?>(null) }
    var pendingDeletePlaylist by remember { mutableStateOf<Playlist?>(null) }

    val importLauncher = rememberLauncherForActivityResult(OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) onImportFromDevice(uris)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            PlayerHeroCard(
                isImporting = uiState.isImporting,
                onImportFromDevice = { importLauncher.launch(arrayOf("audio/*", "video/*")) },
            )
        }

        uiState.importSummary?.let { summary ->
            item {
                ImportSummaryCard(summary = summary)
            }
            item {
                TextButton(onClick = onDismissSummary) {
                    Text(stringResource(R.string.dismiss_summary))
                }
            }
        }

        if (selectedPlaylist != null) {
            item {
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
        }

        if (uiState.playlists.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.empty_playlists),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        items(items = uiState.playlists, key = { it.playlistId }) { playlist ->
            PlaylistCard(
                playlist = playlist,
                selected = playlist.playlistId == selectedPlaylist?.playlistId,
                currentItemId = playbackState.currentItemId,
                onSelect = { onSelectPlaylist(playlist.playlistId, false) },
                onPlay = { onSelectPlaylist(playlist.playlistId, true) },
                onDeletePlaylist = { pendingDeletePlaylist = playlist },
                onDeleteItem = { item -> pendingDeleteItem = playlist to item },
            )
        }
    }

    pendingDeleteItem?.let { (playlist, item) ->
        AlertDialog(
            onDismissRequest = { pendingDeleteItem = null },
            title = { Text(stringResource(R.string.dialog_delete_file_title)) },
            text = { Text(stringResource(R.string.dialog_delete_file_message, item.originalDisplayName)) },
            confirmButton = {
                Button(onClick = {
                    onDeleteItem(playlist.playlistId, item.itemId)
                    pendingDeleteItem = null
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteItem = null }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }

    pendingDeletePlaylist?.let { playlist ->
        AlertDialog(
            onDismissRequest = { pendingDeletePlaylist = null },
            title = { Text(stringResource(R.string.dialog_delete_playlist_title)) },
            text = { Text(stringResource(R.string.dialog_delete_playlist_message, playlist.itemCount, playlist.title)) },
            confirmButton = {
                Button(onClick = {
                    onDeletePlaylist(playlist.playlistId)
                    pendingDeletePlaylist = null
                }) { Text(stringResource(R.string.action_delete_all)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletePlaylist = null }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}
