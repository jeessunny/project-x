package com.projectx.studyhelper

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.projectx.studyhelper.presentation.navigation.AppNavigation
import com.projectx.studyhelper.presentation.sessions.SessionsViewModel
import com.projectx.studyhelper.presentation.settings.SettingsViewModel
import com.projectx.studyhelper.presentation.study.TimerViewModel
import com.projectx.studyhelper.presentation.theme.StudyHelperTheme
import com.projectx.studyhelper.presentation.theme.ThemeConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // ViewModels use 'by viewModels' lazy delegation — factories run on first access only
    private val timerViewModel: TimerViewModel by viewModels {
        val app = application as StudyHelperApplication
        TimerViewModel.Factory(
            streakManager = app.streakManager,
            timerManager = app.timerManager
        )
    }

    private val sessionsViewModel: SessionsViewModel by viewModels {
        val app = application as StudyHelperApplication
        SessionsViewModel.Factory(
            streakRepository = app.streakRepository
        )
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        val app = application as StudyHelperApplication
        SettingsViewModel.Factory(
            themePreferences = app.themePreferences,
            streakRepository = app.streakRepository,
            updateManager = app.updateManager
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // IMPORTANT: super.onCreate() must come BEFORE enableEdgeToEdge().
        // The Window is only attached after super.onCreate() runs; calling
        // enableEdgeToEdge() before it throws IllegalStateException on Android 14+.
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d(TAG, "onCreate — starting")

        val app = application as StudyHelperApplication

        // Background update check — non-blocking, failures are swallowed silently
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                app.updateManager.checkForUpdates(isManual = false)
            } catch (e: Exception) {
                Log.w(TAG, "Background update check failed (non-fatal): ${e.message}")
            }
        }

        Log.d(TAG, "Calling setContent")

        setContent {
            val themeConfig by app.themePreferences.themeConfigFlow
                .collectAsStateWithLifecycle(initialValue = ThemeConfig())

            StudyHelperTheme(themeConfig = themeConfig) {
                val navController = rememberNavController()

                val timerUiState by timerViewModel.uiState.collectAsStateWithLifecycle()
                val sessionsUiState by sessionsViewModel.uiState.collectAsStateWithLifecycle()
                val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

                AppNavigation(
                    navController = navController,
                    timerViewModel = timerViewModel,
                    sessionsViewModel = sessionsViewModel,
                    settingsViewModel = settingsViewModel,
                    updateManager = app.updateManager,
                    timerUiState = timerUiState,
                    sessionsUiState = sessionsUiState,
                    settingsUiState = settingsUiState
                )
            }
        }

        Log.d(TAG, "onCreate — complete")
    }
}
