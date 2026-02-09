package com.cocode.babakplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cocode.babakplayer.R
import com.cocode.babakplayer.model.ItemStatus
import com.cocode.babakplayer.model.Playlist
import com.cocode.babakplayer.model.PlaylistItem
import com.cocode.babakplayer.ui.theme.NeonBlue
import com.cocode.babakplayer.ui.theme.NeonPink
import com.cocode.babakplayer.util.asReadableSize

@Composable
fun PlaylistCard(
    playlist: Playlist,
    selected: Boolean,
    currentItemId: String?,
    onSelect: () -> Unit,
    onPlay: () -> Unit,
    onDeletePlaylist: () -> Unit,
    onDeleteItem: (PlaylistItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = if (selected) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (selected) NeonPink.copy(alpha = 0.75f) else NeonBlue.copy(alpha = 0.48f),
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(containerColor = container),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            HeaderRow(
                title = playlist.title,
                bytes = playlist.totalBytes,
                count = playlist.itemCount,
                onPlay = onPlay,
                onDeletePlaylist = onDeletePlaylist,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                playlist.items.forEach { item ->
                    ItemRow(
                        item = item,
                        selected = item.itemId == currentItemId,
                        onDelete = { onDeleteItem(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderRow(
    title: String,
    bytes: Long,
    count: Int,
    onPlay: () -> Unit,
    onDeletePlaylist: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = stringResource(R.string.playlist_files_size, count, asReadableSize(bytes)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Row {
            IconButton(onClick = onPlay) {
                Icon(Icons.Outlined.PlayArrow, contentDescription = stringResource(R.string.content_play_playlist))
            }
            IconButton(onClick = onDeletePlaylist) {
                Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.content_delete_playlist))
            }
        }
    }
}

@Composable
private fun ItemRow(item: PlaylistItem, selected: Boolean, onDelete: () -> Unit) {
    val bgColor = if (selected) {
        NeonBlue.copy(alpha = 0.26f)
    } else {
        MaterialTheme.colorScheme.background.copy(alpha = 0.45f)
    }

    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(bgColor),
        color = bgColor,
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text = item.originalDisplayName, style = MaterialTheme.typography.bodyMedium)
                val status = when (item.status) {
                    ItemStatus.READY -> stringResource(R.string.status_ready)
                    ItemStatus.DECODE_FAILED -> stringResource(R.string.status_decode_failed)
                    ItemStatus.DELETED -> stringResource(R.string.status_deleted)
                }
                Text(
                    text = stringResource(R.string.item_status_size, status, asReadableSize(item.bytes)),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (item.status == ItemStatus.DECODE_FAILED) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.secondary
                    },
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.content_delete_file))
            }
        }
    }
}
