package com.cocode.babakplayer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.cocode.babakplayer.R
import com.cocode.babakplayer.ui.components.FullscreenVideoPlayer
import com.cocode.babakplayer.ui.screens.AboutScreen
import com.cocode.babakplayer.ui.screens.PlayerScreen
import com.cocode.babakplayer.ui.screens.PlaylistBrowserScreen
import com.cocode.babakplayer.ui.screens.rememberImportFromDeviceAction
import com.cocode.babakplayer.ui.screens.SettingsScreen
import com.cocode.babakplayer.ui.theme.Night
import com.cocode.babakplayer.ui.theme.NeonBlue
import com.cocode.babakplayer.ui.theme.NeonPink
import com.cocode.babakplayer.ui.theme.NeonViolet
import com.cocode.babakplayer.ui.theme.Ocean
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BabakPlayerApp(mainViewModel: MainViewModel, appViewModel: AppViewModel) {
    val mainState by mainViewModel.uiState.collectAsState()
    val playbackState by mainViewModel.playbackState.collectAsState()
    val appState by appViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHost = remember { SnackbarHostState() }
    val onOpenImport = rememberImportFromDeviceAction(mainViewModel::importFromDeviceUris)

    BackHandler(enabled = appState.isFullscreen) {
        appViewModel.setFullscreen(false)
    }

    LaunchedEffect(mainState.noticeResId) {
        val messageRes = mainState.noticeResId ?: return@LaunchedEffect
        snackbarHost.showSnackbar(context.getString(messageRes))
        mainViewModel.clearNotice()
    }

    LaunchedEffect(appState.settings.autoplayNext) {
        mainViewModel.setAutoplayNext(appState.settings.autoplayNext)
    }

    LaunchedEffect(mainState.importSummary, appState.settings.autoDismissSummary) {
        if (!appState.settings.autoDismissSummary || mainState.importSummary == null) return@LaunchedEffect
        delay(4500)
        mainViewModel.clearImportSummary()
    }

    if (appState.isFullscreen) {
        FullscreenVideoPlayer(
            player = mainViewModel.player,
            playback = playbackState,
            seekIntervalSec = appState.settings.seekIntervalSec,
            onExitFullscreen = { appViewModel.setFullscreen(false) },
            onTogglePlayPause = mainViewModel::togglePlayPause,
            onSeekBy = mainViewModel::seekBy,
            onSeekTo = mainViewModel::seekTo,
            onPrevious = mainViewModel::previous,
            onNext = mainViewModel::next,
        )
        return
    }

    Scaffold(
        topBar = {
            if (appState.selectedTab != AppTab.PLAYER) {
                TopAppBar(
                    title = { Text(stringResource(appState.selectedTab.titleRes)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                        titleContentColor = if (appState.selectedTab == AppTab.PLAYLISTS) NeonBlue else MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f)) {
                AppTab.entries.forEach { tab ->
                    val selected = tab == appState.selectedTab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { appViewModel.selectTab(tab) },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = stringResource(tab.titleRes),
                            )
                        },
                    )
                }
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        appViewModel.selectTab(AppTab.PLAYER)
                        onOpenImport()
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = stringResource(R.string.nav_add),
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonPink,
                        unselectedIconColor = NeonPink,
                        selectedTextColor = NeonPink,
                        unselectedTextColor = NeonPink,
                        indicatorColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                    ),
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHost) },
    ) { innerPadding ->
        val usePlayerGradient = appState.selectedTab == AppTab.PLAYER || appState.selectedTab == AppTab.PLAYLISTS
        val backgroundBrush = if (usePlayerGradient) {
            Brush.linearGradient(colors = listOf(Night, NeonViolet.copy(alpha = 0.52f), Ocean, NeonPink.copy(alpha = 0.34f)))
        } else {
            Brush.verticalGradient(colors = listOf(Night, Ocean))
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding),
        ) {
            when (appState.selectedTab) {
                AppTab.PLAYER -> PlayerScreen(
                    uiState = mainState,
                    playbackState = playbackState,
                    player = mainViewModel.player,
                    seekIntervalSec = appState.settings.seekIntervalSec,
                    onTogglePlayPause = mainViewModel::togglePlayPause,
                    onSeekBy = mainViewModel::seekBy,
                    onSeekTo = mainViewModel::seekTo,
                    onNext = mainViewModel::next,
                    onPrevious = mainViewModel::previous,
                    onEnterFullscreen = { appViewModel.setFullscreen(true) },
                    isImporting = mainState.isImporting,
                    onImportFromDevice = onOpenImport,
                )

                AppTab.PLAYLISTS -> PlaylistBrowserScreen(
                    playlists = mainState.playlists,
                    selectedPlaylistId = mainState.selectedPlaylistId,
                    onSelectAndPlay = { playlistId ->
                        mainViewModel.selectPlaylist(playlistId, true)
                        appViewModel.selectTab(AppTab.PLAYER)
                    },
                    onDeletePlaylist = mainViewModel::deletePlaylist,
                )

                AppTab.SETTINGS -> SettingsScreen(
                    settings = appState.settings,
                    onThemeModeChange = appViewModel::setThemeMode,
                    onLanguageChange = appViewModel::setLanguage,
                    onAutoplayChange = appViewModel::setAutoplayNext,
                    onSeekIntervalChange = appViewModel::setSeekInterval,
                    onAutoDismissChange = appViewModel::setAutoDismissSummary,
                    onResetSettings = appViewModel::resetSettings,
                )

                AppTab.ABOUT -> AboutScreen()
            }
        }
    }
}
