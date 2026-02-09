package com.cocode.babakplayer.ui

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import com.cocode.babakplayer.data.local.SettingsStore
import com.cocode.babakplayer.model.AppLanguage
import com.cocode.babakplayer.model.AppSettings
import com.cocode.babakplayer.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AppUiState(
    val selectedTab: AppTab = AppTab.PLAYER,
    val settings: AppSettings = AppSettings(),
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsStore = SettingsStore(application)
    private val _uiState = MutableStateFlow(AppUiState(settings = settingsStore.settings.value))
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        applyLanguage(_uiState.value.settings.language)
    }

    fun selectTab(tab: AppTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setThemeMode(mode: ThemeMode) {
        settingsStore.updateThemeMode(mode)
        syncSettings()
    }

    fun setLanguage(language: AppLanguage) {
        settingsStore.updateLanguage(language)
        syncSettings()
        applyLanguage(language)
    }

    fun setAutoplayNext(enabled: Boolean) {
        settingsStore.updateAutoplayNext(enabled)
        syncSettings()
    }

    fun setSeekInterval(seconds: Int) {
        settingsStore.updateSeekInterval(seconds)
        syncSettings()
    }

    fun setAutoDismissSummary(enabled: Boolean) {
        settingsStore.updateAutoDismissSummary(enabled)
        syncSettings()
    }

    fun resetSettings() {
        settingsStore.resetDefaults()
        syncSettings()
        applyLanguage(_uiState.value.settings.language)
    }

    private fun syncSettings() {
        _uiState.update { it.copy(settings = settingsStore.settings.value) }
    }

    private fun applyLanguage(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
    }
}
