package com.cocode.babakplayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cocode.babakplayer.R
import com.cocode.babakplayer.data.local.MediaBrowseCategory

enum class ImportOption {
    VIDEOS,
    AUDIO,
    WHATSAPP,
    DOWNLOADS,
    MANUAL,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportMediaBottomSheet(
    onDismiss: () -> Unit,
    onSelect: (ImportOption) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.import_menu_title),
                style = MaterialTheme.typography.titleLarge,
            )
            OptionRow(text = stringResource(R.string.import_option_videos)) { onSelect(ImportOption.VIDEOS) }
            OptionRow(text = stringResource(R.string.import_option_audio)) { onSelect(ImportOption.AUDIO) }
            OptionRow(text = stringResource(R.string.import_option_whatsapp)) { onSelect(ImportOption.WHATSAPP) }
            OptionRow(text = stringResource(R.string.import_option_downloads)) { onSelect(ImportOption.DOWNLOADS) }
            OptionRow(text = stringResource(R.string.import_option_manual)) { onSelect(ImportOption.MANUAL) }
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun OptionRow(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp),
        style = MaterialTheme.typography.titleMedium,
    )
}

fun ImportOption.toCategoryOrNull(): MediaBrowseCategory? {
    return when (this) {
        ImportOption.AUDIO -> MediaBrowseCategory.AUDIO
        ImportOption.WHATSAPP -> MediaBrowseCategory.WHATSAPP
        ImportOption.DOWNLOADS -> MediaBrowseCategory.DOWNLOADS
        else -> null
    }
}
