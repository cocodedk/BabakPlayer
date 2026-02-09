package com.cocode.babakplayer.ui.components

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cocode.babakplayer.R
import com.cocode.babakplayer.data.local.BrowsableMedia
import com.cocode.babakplayer.data.local.MediaBrowseCategory
import com.cocode.babakplayer.util.asReadableSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaSelectionBottomSheet(
    category: MediaBrowseCategory,
    mediaItems: List<BrowsableMedia>,
    selectedUris: Set<String>,
    loading: Boolean,
    onDismiss: () -> Unit,
    onToggle: (String) -> Unit,
    onCreatePlaylist: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = stringResource(category.titleRes()), style = MaterialTheme.typography.titleLarge)

            when {
                loading -> Text(text = stringResource(R.string.media_loading), style = MaterialTheme.typography.bodyMedium)
                mediaItems.isEmpty() -> Text(text = stringResource(R.string.media_empty), style = MaterialTheme.typography.bodyMedium)
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(mediaItems, key = { it.uri.toString() }) { item ->
                            MediaRow(
                                item = item,
                                checked = selectedUris.contains(item.uri.toString()),
                                onToggle = { onToggle(item.uri.toString()) },
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onCreatePlaylist,
                enabled = selectedUris.isNotEmpty() && !loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.media_create_playlist))
            }
            Spacer(modifier = Modifier.padding(bottom = 18.dp))
        }
    }
}

@Composable
private fun MediaRow(item: BrowsableMedia, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(item.displayName, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = asReadableSize(item.sizeBytes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
    }
}

private fun MediaBrowseCategory.titleRes(): Int {
    return when (this) {
        MediaBrowseCategory.AUDIO -> R.string.media_category_audio
        MediaBrowseCategory.WHATSAPP -> R.string.media_category_whatsapp
        MediaBrowseCategory.DOWNLOADS -> R.string.media_category_downloads
    }
}

fun selectedUrisInBrowseOrder(mediaItems: List<BrowsableMedia>, selected: Set<String>): List<Uri> {
    return mediaItems.mapNotNull { media ->
        if (selected.contains(media.uri.toString())) media.uri else null
    }
}
