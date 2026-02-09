package com.cocode.babakplayer.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cocode.babakplayer.R

@Composable
fun AboutScreen() {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(text = stringResource(R.string.about_title), style = MaterialTheme.typography.headlineMedium)
        }

        item {
            AboutCard {
                Text(stringResource(R.string.about_purpose), style = MaterialTheme.typography.bodyLarge)
                Text(stringResource(R.string.about_companion), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.about_local_first), style = MaterialTheme.typography.bodyMedium)
            }
        }

        item {
            AboutCard {
                Text(stringResource(R.string.about_supported_formats), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.about_license), style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = stringResource(R.string.about_version, appVersionName(context)),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        item {
            AboutCard {
                Button(onClick = {
                    openLink(context, context.getString(R.string.about_website_url))
                }) {
                    Text(stringResource(R.string.about_open_website))
                }
                Button(onClick = {
                    openLink(context, context.getString(R.string.about_repo_url))
                }) {
                    Text(stringResource(R.string.about_open_repo))
                }
            }
        }
    }
}

@Composable
private fun AboutCard(content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

private fun appVersionName(context: android.content.Context): String {
    val info = context.packageManager.getPackageInfo(context.packageName, 0)
    return info.versionName ?: "1.0"
}

private fun openLink(context: android.content.Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    runCatching { context.startActivity(intent) }
}
