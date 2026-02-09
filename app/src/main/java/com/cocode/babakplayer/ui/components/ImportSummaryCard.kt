package com.cocode.babakplayer.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.cocode.babakplayer.R
import com.cocode.babakplayer.model.ImportSummary
import com.cocode.babakplayer.ui.theme.NeonBlue
import com.cocode.babakplayer.util.asReadableSize

@Composable
fun ImportSummaryCard(summary: ImportSummary, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NeonBlue.copy(alpha = 0.58f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.summary_title, summary.title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            SummaryRow(label = stringResource(R.string.summary_imported), value = summary.importedCount.toString())
            SummaryRow(label = stringResource(R.string.summary_skipped), value = summary.skippedCount.toString())
            SummaryRow(label = stringResource(R.string.summary_unsupported), value = summary.unsupportedCount.toString())
            SummaryRow(label = stringResource(R.string.summary_total_size), value = asReadableSize(summary.totalBytes))
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
    }
}
