package com.cocode.babakplayer.model

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT,
}

enum class AppLanguage(val tag: String) {
    ENGLISH("en"),
    PERSIAN("fa");

    companion object {
        fun fromTag(tag: String): AppLanguage {
            return entries.firstOrNull { it.tag == tag } ?: ENGLISH
        }
    }
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val autoplayNext: Boolean = true,
    val seekIntervalSec: Int = 10,
    val autoDismissSummary: Boolean = false,
)
