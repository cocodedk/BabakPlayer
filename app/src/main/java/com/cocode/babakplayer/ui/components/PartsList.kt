package com.cocode.babakplayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cocode.babakplayer.R
import com.cocode.babakplayer.model.ItemStatus
import com.cocode.babakplayer.model.PlaylistItem
import com.cocode.babakplayer.ui.theme.NeonBlue
import com.cocode.babakplayer.ui.theme.NeonPink

/** The player tab's list of parts: tap a row to play it; a part that failed to decode says so. */
@Composable
fun PartsListSection(
    items: List<PlaylistItem>,
    currentItemId: String?,
    onPlayItem: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        itemsIndexed(items = items, key = { _, item -> item.itemId }) { index, item ->
            val isCurrent = item.itemId == currentItemId
            val failed = item.status == ItemStatus.DECODE_FAILED
            val bgColor = if (isCurrent) NeonBlue.copy(alpha = 0.26f) else MaterialTheme.colorScheme.background.copy(alpha = 0.45f)
            val textColor = when {
                failed -> MaterialTheme.colorScheme.error
                isCurrent -> NeonPink
                else -> MaterialTheme.colorScheme.onSurface
            }
            val weight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { onPlayItem(item.itemId) },
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
                        fontWeight = weight,
                        color = textColor,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = item.originalDisplayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = weight,
                            color = textColor,
                        )
                        if (failed) {
                            Text(
                                text = stringResource(R.string.status_decode_failed),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }
}
