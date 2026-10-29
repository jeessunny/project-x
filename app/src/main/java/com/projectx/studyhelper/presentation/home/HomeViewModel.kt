package com.projectx.studyhelper.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.projectx.studyhelper.data.updater.AppInstaller
import com.projectx.studyhelper.data.updater.DownloadProgress
import com.projectx.studyhelper.data.updater.UpdateChecker
import com.projectx.studyhelper.domain.manager.StreakManager
import com.projectx.studyhelper.domain.manager.StudyTimerManager
import com.projectx.studyhelper.domain.model.StudyPreset
import com.projectx.studyhelper.domain.model.UpdateInfo
import com.projectx.studyhelper.domain.model.UpdateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val streakManager: StreakManager,
    private val timerManager: StudyTimerManager,
    private val updateChecker: UpdateChecker,
    private val appInstaller: AppInstaller
) : ViewModel() {

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    private val _showUpdateDialog = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        combine(
            streakManager.streakRecordFlow,
            streakManager.accumulatedMinutesTodayFlow,
            streakManager.selectedPresetFlow
        ) { streak, minutes, preset ->
            Triple(streak, minutes, preset)
        },
        timerManager.timerState,
        _updateState,
        _showUpdateDialog
    ) { (streak, minutesToday, preset), timerState, updateState, showDialog ->
        HomeUiState(
            streakRecord = streak,
            accumulatedMinutesToday = minutesToday,
            selectedPreset = preset,
            timerState = timerState,
            updateState = updateState,
            showUpdateDialog = showDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        // Validate streak continuity on launch
        viewModelScope.launch {
            streakManager.evaluateStreakIntegrity()
        }
        // Auto-check for GitHub updates
        checkForUpdates()
    }

    fun selectPreset(preset: StudyPreset) {
        viewModelScope.launch {
            streakManager.setPreset(preset)
            timerManager.setPreset(preset)
        }
    }

    fun startTimer() {
        timerManager.startTimer(uiState.value.selectedPreset)
    }

    fun pauseTimer() {
        timerManager.pauseTimer()
    }

    fun resumeTimer() {
        timerManager.resumeTimer()
    }

    fun stopTimer() {
        timerManager.stopTimer(savePartial = true)
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _updateState.value = UpdateState.Checking
            val result = updateChecker.checkForUpdate()
            result.onSuccess { info ->
                if (info != null && info.isUpdateAvailable) {
                    _updateState.value = UpdateState.Available(info)
                } else {
                    _updateState.value = UpdateState.Idle
                }
            }.onFailure { error ->
                _updateState.value = UpdateState.Error(error.localizedMessage ?: "Failed to check for updates")
            }
        }
    }

    fun openUpdateDialog() {
        _showUpdateDialog.value = true
    }

    fun dismissUpdateDialog() {
        _showUpdateDialog.value = false
    }

    fun downloadUpdate(updateInfo: UpdateInfo) {
        viewModelScope.launch {
            appInstaller.downloadApk(updateInfo.downloadUrl).collect { progress ->
                when (progress) {
                    is DownloadProgress.Progress -> {
                        _updateState.value = UpdateState.Downloading(
                            progressPercent = progress.percent,
                            downloadedBytes = progress.bytesDownloaded,
                            totalBytes = progress.totalBytes
                        )
                    }
                    is DownloadProgress.Completed -> {
                        _updateState.value = UpdateState.ReadyToInstall(updateInfo)
                    }
                    is DownloadProgress.Error -> {
                        _updateState.value = UpdateState.Error(
                            progress.throwable.localizedMessage ?: "Download failed"
                        )
                    }
                }
            }
        }
    }

    fun triggerInstall(context: Context): Boolean {
        if (!appInstaller.canInstallPackages()) {
            val intent = appInstaller.createRequestInstallPermissionIntent()
            context.startActivity(intent)
            return false
        }

        val result = appInstaller.promptInstall()
        return result.isSuccess
    }

    class Factory(
        private val streakManager: StreakManager,
        private val timerManager: StudyTimerManager,
        private val updateChecker: UpdateChecker,
        private val appInstaller: AppInstaller
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(streakManager, timerManager, updateChecker, appInstaller) as T
        }
    }
}
