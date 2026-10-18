package com.cocode.babakplayer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cocode.babakplayer.R
import com.cocode.babakplayer.model.Playlist
import com.cocode.babakplayer.model.PlaylistItem
import com.cocode.babakplayer.ui.components.DeleteItemDialog
import com.cocode.babakplayer.ui.components.DeletePlaylistDialog
import com.cocode.babakplayer.ui.components.PlaylistCard

@Composable
fun PlaylistBrowserScreen(
    playlists: List<Playlist>,
    selectedPlaylistId: String?,
    currentItemId: String?,
    onSelectAndPlay: (String) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onDeleteItem: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDeletePlaylist by remember { mutableStateOf<Playlist?>(null) }
    var pendingDeleteItem by remember { mutableStateOf<Pair<Playlist, PlaylistItem>?>(null) }
    var expandedIds by remember { mutableStateOf(emptySet<String>()) }

    if (playlists.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.playlists_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(items = playlists, key = { it.playlistId }) { playlist ->
                val id = playlist.playlistId
                val toggle = { expandedIds = if (id in expandedIds) expandedIds - id else expandedIds + id }
                PlaylistCard(
                    playlist = playlist,
                    selected = id == selectedPlaylistId,
                    expanded = id in expandedIds,
                    currentItemId = currentItemId,
                    onSelect = toggle,
                    onPlay = { onSelectAndPlay(id) },
                    onToggleExpanded = toggle,
                    onDeletePlaylist = { pendingDeletePlaylist = playlist },
                    onDeleteItem = { item -> pendingDeleteItem = playlist to item },
                )
            }
        }
    }

    pendingDeletePlaylist?.let { playlist ->
        DeletePlaylistDialog(
            playlist = playlist,
            onDismiss = { pendingDeletePlaylist = null },
            onDelete = onDeletePlaylist,
        )
    }
    pendingDeleteItem?.let { (playlist, item) ->
        DeleteItemDialog(
            playlist = playlist,
            item = item,
            onDismiss = { pendingDeleteItem = null },
            onDelete = onDeleteItem,
        )
    }
}
