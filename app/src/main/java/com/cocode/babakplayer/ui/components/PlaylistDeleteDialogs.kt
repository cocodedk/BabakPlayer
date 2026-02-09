package com.cocode.babakplayer.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cocode.babakplayer.R
import com.cocode.babakplayer.model.Playlist
import com.cocode.babakplayer.model.PlaylistItem

@Composable
fun DeleteItemDialog(
    playlist: Playlist,
    item: PlaylistItem,
    onDismiss: () -> Unit,
    onDelete: (String, String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_delete_file_title)) },
        text = { Text(stringResource(R.string.dialog_delete_file_message, item.originalDisplayName)) },
        confirmButton = {
            Button(onClick = {
                onDelete(playlist.playlistId, item.itemId)
                onDismiss()
            }) { Text(stringResource(R.string.action_delete)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
fun DeletePlaylistDialog(
    playlist: Playlist,
    onDismiss: () -> Unit,
    onDelete: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_delete_playlist_title)) },
        text = { Text(stringResource(R.string.dialog_delete_playlist_message, playlist.itemCount, playlist.title)) },
        confirmButton = {
            Button(onClick = {
                onDelete(playlist.playlistId)
                onDismiss()
            }) { Text(stringResource(R.string.action_delete_all)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
