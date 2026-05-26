package com.doorguardclock

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.doorguardclock.ui.ClockScreen
import com.doorguardclock.ui.SettingsScreen
import com.doorguardclock.viewmodel.ClockViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable fullscreen mode
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Keep screen on while app is active
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            val viewModel: ClockViewModel = viewModel()
            var showSettings by remember { mutableStateOf(false) }

            // Handle brightness
            val settings by viewModel.settings.collectAsState()

            DisposableEffect(settings.autoBrightness, settings.manualBrightness) {
                if (settings.autoBrightness) {
                    // Let system handle brightness
                } else {
                    // Set manual brightness
                    val brightness = settings.manualBrightness / 255f
                    val layoutParams = window.attributes
                    layoutParams.screenBrightness = brightness
                    window.attributes = layoutParams
                }

                onDispose { }
            }

            Surface(modifier = Modifier.fillMaxSize()) {
                if (showSettings) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { showSettings = false }
                    )
                } else {
                    ClockScreen(
                        viewModel = viewModel,
                        onNavigateToSettings = { showSettings = true }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}