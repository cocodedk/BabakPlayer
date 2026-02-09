package com.cocode.babakplayer.data.local

import android.content.Context
import com.cocode.babakplayer.model.AppLanguage
import com.cocode.babakplayer.model.AppSettings
import com.cocode.babakplayer.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("babakplayer_settings", Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(readSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun updateThemeMode(mode: ThemeMode) = update { it.copy(themeMode = mode) }

    fun updateLanguage(language: AppLanguage) = update { it.copy(language = language) }

    fun updateAutoplayNext(enabled: Boolean) = update { it.copy(autoplayNext = enabled) }

    fun updateSeekInterval(seconds: Int) = update {
        it.copy(seekIntervalSec = seconds.coerceIn(5, 60))
    }

    fun updateAutoDismissSummary(enabled: Boolean) = update { it.copy(autoDismissSummary = enabled) }

    fun resetDefaults() {
        writeSettings(AppSettings())
    }

    private fun update(transform: (AppSettings) -> AppSettings) {
        val updated = transform(_settings.value)
        writeSettings(updated)
    }

    private fun writeSettings(settings: AppSettings) {
        _settings.value = settings
        prefs.edit()
            .putString(KEY_THEME_MODE, settings.themeMode.name)
            .putString(KEY_LANGUAGE, settings.language.tag)
            .putBoolean(KEY_AUTOPLAY_NEXT, settings.autoplayNext)
            .putInt(KEY_SEEK_INTERVAL, settings.seekIntervalSec)
            .putBoolean(KEY_AUTO_DISMISS_SUMMARY, settings.autoDismissSummary)
            .apply()
    }

    private fun readSettings(): AppSettings {
        val theme = prefs.getString(KEY_THEME_MODE, null)
            ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: AppSettings().themeMode
        val language = AppLanguage.fromTag(prefs.getString(KEY_LANGUAGE, "en") ?: "en")
        val autoplay = prefs.getBoolean(KEY_AUTOPLAY_NEXT, true)
        val interval = prefs.getInt(KEY_SEEK_INTERVAL, 10).coerceIn(5, 60)
        val autoDismiss = prefs.getBoolean(KEY_AUTO_DISMISS_SUMMARY, false)
        return AppSettings(
            themeMode = theme,
            language = language,
            autoplayNext = autoplay,
            seekIntervalSec = interval,
            autoDismissSummary = autoDismiss,
        )
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_AUTOPLAY_NEXT = "autoplay_next"
        private const val KEY_SEEK_INTERVAL = "seek_interval_sec"
        private const val KEY_AUTO_DISMISS_SUMMARY = "auto_dismiss_summary"
    }
}
