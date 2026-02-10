package com.cocode.babakplayer.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector
import com.cocode.babakplayer.R

enum class AppTab(
    @StringRes val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    PLAYER(R.string.tab_player, Icons.Filled.PlayCircle, Icons.Outlined.PlayCircleOutline),
    PLAYLISTS(R.string.tab_playlists, Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary),
    SETTINGS(R.string.tab_settings, Icons.Filled.Settings, Icons.Outlined.Settings),
    ABOUT(R.string.tab_about, Icons.Filled.Info, Icons.Outlined.Info),
}
