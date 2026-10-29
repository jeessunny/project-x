package com.projectx.studyhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.projectx.studyhelper.presentation.navigation.AppNavigation
import com.projectx.studyhelper.presentation.sessions.SessionsViewModel
import com.projectx.studyhelper.presentation.settings.SettingsViewModel
import com.projectx.studyhelper.presentation.study.TimerViewModel
import com.projectx.studyhelper.presentation.theme.StudyHelperTheme
import com.projectx.studyhelper.presentation.theme.ThemeConfig

class MainActivity : ComponentActivity() {

    private val timerViewModel: TimerViewModel by viewModels {
        val app = application as StudyHelperApplication
        TimerViewModel.Factory(
            streakManager = app.streakManager,
            timerManager = app.timerManager,
            updateChecker = app.updateChecker,
            appInstaller = app.appInstaller
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
            updateChecker = app.updateChecker,
            appInstaller = app.appInstaller
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val app = application as StudyHelperApplication

        setContent {
            val themeConfig by app.themePreferences.themeConfigFlow
                .collectAsState(initial = ThemeConfig())

            StudyHelperTheme(themeConfig = themeConfig) {
                val navController = rememberNavController()

                val timerUiState by timerViewModel.uiState.collectAsState()
                val sessionsUiState by sessionsViewModel.uiState.collectAsState()
                val settingsUiState by settingsViewModel.uiState.collectAsState()

                AppNavigation(
                    navController = navController,
                    timerViewModel = timerViewModel,
                    sessionsViewModel = sessionsViewModel,
                    settingsViewModel = settingsViewModel,
                    timerUiState = timerUiState,
                    sessionsUiState = sessionsUiState,
                    settingsUiState = settingsUiState
                )
            }
        }
    }
}
