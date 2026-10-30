package com.projectx.studyhelper

import android.os.Bundle
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
        // super.onCreate() MUST be called BEFORE enableEdgeToEdge().
        // Calling enableEdgeToEdge() before super.onCreate() throws
        // IllegalStateException on Android 14+ because the Window is not
        // yet attached to the Activity at that point.
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as StudyHelperApplication

        // Non-blocking background update check
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                app.updateManager.checkForUpdates(isManual = false)
            } catch (e: Exception) {
                android.util.Log.w("MainActivity", "Update check failed (non-fatal): ${e.message}")
            }
        }

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
    }
}
