package com.cocode.babakplayer.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import java.util.Locale

/**
 * A string in the language chosen in the app. Before Android 13 the application context's resources
 * can lag behind a language picked in Settings, while the default locale already follows it.
 * BabakPlayer ships APKs, not an app bundle, so no language is split off or downloaded later.
 */
@SuppressLint("AppBundleLocaleChanges")
fun Context.getStringInAppLanguage(@StringRes id: Int): String {
    val configuration = Configuration(resources.configuration).apply { setLocale(Locale.getDefault()) }
    return createConfigurationContext(configuration).getString(id)
}
