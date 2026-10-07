package com.cocode.babakplayer.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cocode.babakplayer.R

/** The About tab. Sections follow the cocode-apps About-page standard, in this order. */
@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val language = LocalConfiguration.current.locales[0].language
    val open = { link: AboutLink -> openLink(context, aboutUrl(link, language)) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.about_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() },
            )
        }

        item {
            AboutSection(R.string.about_section_version) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
                Text(
                    text = stringResource(R.string.about_version, appVersionName(context)),
                    style = MaterialTheme.typography.bodyMedium,
                )
                AboutBody(R.string.about_update_note)
                AboutButton(R.string.about_check_updates) { open(AboutLink.Update) }
            }
        }

        item {
            AboutSection(R.string.about_section_what) {
                Text(stringResource(R.string.about_purpose), style = MaterialTheme.typography.bodyLarge)
                AboutBody(R.string.about_supported_formats)
            }
        }

        item {
            AboutSection(R.string.about_section_privacy) {
                AboutBody(R.string.about_privacy_body)
                AboutButton(R.string.about_privacy_link) { open(AboutLink.Privacy) }
            }
        }

        item {
            AboutSection(R.string.about_section_links) {
                AboutButton(R.string.about_website) { open(AboutLink.Website) }
                AboutButton(R.string.about_source) { open(AboutLink.Source) }
                AboutButton(R.string.about_report) { open(AboutLink.Issues) }
            }
        }

        item {
            AboutSection(R.string.about_credits) {
                AboutBody(R.string.about_license)
                AboutBody(R.string.about_credits_libraries)
            }
        }

        item {
            AboutSection(R.string.about_made_by) {
                AboutButton(R.string.about_made_by_link) { open(AboutLink.Cocode) }
            }
        }

        // Support slot: intentionally empty. Nothing is shown until the Support phase of the
        // cocode-apps standard (standard/support.md) fills it.
    }
}

private fun appVersionName(context: Context): String {
    val info = context.packageManager.getPackageInfo(context.packageName, 0)
    return info.versionName ?: "1.0"
}

private fun openLink(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    runCatching { context.startActivity(intent) }
}
