package com.cocode.babakplayer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import com.cocode.babakplayer.model.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Amber,
    secondary = NeonBlue,
    tertiary = NeonPink,
    background = Night,
    surface = NightElevated,
    onPrimary = Night,
    onSecondary = Night,
    onBackground = Mist,
    onSurface = Mist,
    error = Danger,
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5E4419),
    secondary = Color(0xFF15637D),
    tertiary = Color(0xFF3E4B5E),
    background = Color(0xFFF4F6FA),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF1C2332),
    error = Color(0xFFB91C1C),
)

@Composable
fun BabakPlayerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
