package com.cocode.babakplayer

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.cocode.babakplayer.ui.BabakPlayerApp
import com.cocode.babakplayer.ui.AppViewModel
import com.cocode.babakplayer.ui.AppTab
import com.cocode.babakplayer.ui.MainViewModel
import com.cocode.babakplayer.ui.theme.BabakPlayerTheme
import com.cocode.babakplayer.util.ShareIntentParser

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel>()
    private val appViewModel by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleShareIntent(intent)
        enableEdgeToEdge()
        setContent {
            val appState by appViewModel.uiState.collectAsState()
            BabakPlayerTheme(themeMode = appState.settings.themeMode) {
                LaunchedEffect(appState.isFullscreen) {
                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                    if (appState.isFullscreen) {
                        insetsController.hide(WindowInsetsCompat.Type.systemBars())
                        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    } else {
                        insetsController.show(WindowInsetsCompat.Type.systemBars())
                    }
                }
                BabakPlayerApp(mainViewModel = viewModel, appViewModel = appViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        val sourcePackage = callingPackage ?: referrer?.host ?: intent?.`package`
        val payload = ShareIntentParser.parse(intent, sourcePackage) ?: return
        appViewModel.selectTab(AppTab.PLAYER)
        viewModel.importSharePayload(payload)
    }
}
