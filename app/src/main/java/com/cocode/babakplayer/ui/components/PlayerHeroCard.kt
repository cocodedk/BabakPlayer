package com.cocode.babakplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cocode.babakplayer.R
import com.cocode.babakplayer.ui.theme.NeonBlue
import com.cocode.babakplayer.ui.theme.NeonPink
import com.cocode.babakplayer.ui.theme.NeonViolet

@Composable
fun PlayerHeroCard(
    isImporting: Boolean,
    onImportFromDevice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        tonalElevation = 0.dp,
        shadowElevation = 10.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NeonBlue.copy(alpha = 0.65f), RoundedCornerShape(18.dp)),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.player_header),
                style = MaterialTheme.typography.headlineMedium,
                color = NeonPink,
            )
            Text(
                text = if (isImporting) stringResource(R.string.player_importing) else stringResource(R.string.player_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = stringResource(R.string.player_import_from_device_hint),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )
            Button(onClick = onImportFromDevice, enabled = !isImporting) {
                Text(stringResource(R.string.player_import_from_device_action))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Brush.horizontalGradient(listOf(NeonBlue, NeonViolet, NeonPink))),
            )
        }
    }
}
