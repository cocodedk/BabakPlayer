package com.cocode.babakplayer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cocode.babakplayer.R
import com.cocode.babakplayer.model.AppLanguage
import com.cocode.babakplayer.model.AppSettings
import com.cocode.babakplayer.model.ThemeMode

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onAutoplayChange: (Boolean) -> Unit,
    onSeekIntervalChange: (Int) -> Unit,
    onAutoDismissChange: (Boolean) -> Unit,
    onResetSettings: () -> Unit,
) {
    var showResetDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(text = stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)
        }

        item {
            SettingCard(title = stringResource(R.string.settings_section_appearance)) {
                Text(stringResource(R.string.settings_theme_mode), style = MaterialTheme.typography.titleMedium)
                ThemeOption(settings.themeMode, ThemeMode.SYSTEM, R.string.theme_system, onThemeModeChange)
                ThemeOption(settings.themeMode, ThemeMode.DARK, R.string.theme_dark, onThemeModeChange)
                ThemeOption(settings.themeMode, ThemeMode.LIGHT, R.string.theme_light, onThemeModeChange)
            }
        }

        item {
            SettingCard(title = stringResource(R.string.settings_section_language)) {
                Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleMedium)
                LanguageOption(settings.language, AppLanguage.ENGLISH, R.string.language_english, onLanguageChange)
                LanguageOption(settings.language, AppLanguage.PERSIAN, R.string.language_persian, onLanguageChange)
            }
        }

        item {
            SettingCard(title = stringResource(R.string.settings_section_playback)) {
                ToggleRow(
                    title = stringResource(R.string.settings_autoplay_next),
                    subtitle = stringResource(R.string.settings_autoplay_next_desc),
                    checked = settings.autoplayNext,
                    onChecked = onAutoplayChange,
                )
                Text(stringResource(R.string.settings_seek_interval), style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = settings.seekIntervalSec.toFloat(),
                    onValueChange = { onSeekIntervalChange(it.toInt()) },
                    valueRange = 5f..60f,
                )
                Text(
                    text = stringResource(R.string.settings_seek_interval_value, settings.seekIntervalSec),
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }

        item {
            SettingCard(title = stringResource(R.string.settings_section_import)) {
                ToggleRow(
                    title = stringResource(R.string.settings_auto_dismiss),
                    subtitle = stringResource(R.string.settings_auto_dismiss_desc),
                    checked = settings.autoDismissSummary,
                    onChecked = onAutoDismissChange,
                )
            }
        }

        item {
            SettingCard(title = stringResource(R.string.settings_reset)) {
                Text(stringResource(R.string.settings_reset_desc), color = MaterialTheme.colorScheme.secondary)
                Button(onClick = { showResetDialog = true }) {
                    Text(stringResource(R.string.settings_reset_confirm))
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.dialog_reset_settings_title)) },
            text = { Text(stringResource(R.string.dialog_reset_settings_message)) },
            confirmButton = {
                Button(onClick = {
                    onResetSettings()
                    showResetDialog = false
                }) { Text(stringResource(R.string.settings_reset_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@Composable
private fun SettingCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = {
                Text(title, style = MaterialTheme.typography.titleLarge)
                content()
            },
        )
    }
}

@Composable
private fun ThemeOption(selected: ThemeMode, value: ThemeMode, textRes: Int, onChange: (ThemeMode) -> Unit) {
    RowWithRadio(selected = selected == value, text = stringResource(textRes), onClick = { onChange(value) })
}

@Composable
private fun LanguageOption(selected: AppLanguage, value: AppLanguage, textRes: Int, onChange: (AppLanguage) -> Unit) {
    RowWithRadio(selected = selected == value, text = stringResource(textRes), onClick = { onChange(value) })
}

@Composable
private fun RowWithRadio(selected: Boolean, text: String, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = text, modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
