package com.projectx.studyhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.projectx.studyhelper.presentation.home.HomeScreen
import com.projectx.studyhelper.presentation.home.HomeViewModel
import com.projectx.studyhelper.presentation.theme.StudyHelperTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels {
        val app = application as StudyHelperApplication
        HomeViewModel.Factory(
            streakManager = app.streakManager,
            timerManager = app.timerManager,
            updateChecker = app.updateChecker,
            appInstaller = app.appInstaller
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            StudyHelperTheme {
                val uiState by viewModel.uiState.collectAsState()

                HomeScreen(
                    uiState = uiState,
                    onSelectPreset = viewModel::selectPreset,
                    onStartTimer = viewModel::startTimer,
                    onPauseTimer = viewModel::pauseTimer,
                    onResumeTimer = viewModel::resumeTimer,
                    onStopTimer = viewModel::stopTimer,
                    onCheckForUpdates = viewModel::checkForUpdates,
                    onOpenUpdateDialog = viewModel::openUpdateDialog,
                    onDismissUpdateDialog = viewModel::dismissUpdateDialog,
                    onConfirmUpdateDownload = viewModel::downloadUpdate,
                    onTriggerInstall = { ctx -> viewModel.triggerInstall(ctx) }
                )
            }
        }
    }
}
