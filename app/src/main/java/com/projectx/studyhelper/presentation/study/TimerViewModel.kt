package com.projectx.studyhelper.presentation.study

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.projectx.studyhelper.data.updater.AppInstaller
import com.projectx.studyhelper.data.updater.DownloadProgress
import com.projectx.studyhelper.data.updater.UpdateChecker
import com.projectx.studyhelper.domain.manager.StreakManager
import com.projectx.studyhelper.domain.manager.StudyTimerManager
import com.projectx.studyhelper.domain.manager.TimerState
import com.projectx.studyhelper.domain.manager.TimerStatus
import com.projectx.studyhelper.domain.model.StreakRecord
import com.projectx.studyhelper.domain.model.StudyPreset
import com.projectx.studyhelper.domain.model.UpdateInfo
import com.projectx.studyhelper.domain.model.UpdateState
import com.projectx.studyhelper.presentation.service.StudyTimerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudyUiState(
    val streakRecord: StreakRecord = StreakRecord(),
    val accumulatedMinutesToday: Int = 0,
    val selectedPreset: StudyPreset = StudyPreset.Fifteen,
    val timerState: TimerState = TimerState(),
    val isCustomPickerOpen: Boolean = false,
    val isAbortDialogOpen: Boolean = false,
    val updateState: UpdateState = UpdateState.Idle,
    val showUpdateDialog: Boolean = false
) {
    val dailyProgress: Float
        get() = (accumulatedMinutesToday.toFloat() / selectedPreset.minutes).coerceIn(0f, 1f)

    val isDailyGoalReached: Boolean
        get() = accumulatedMinutesToday >= selectedPreset.minutes
}

class TimerViewModel(
    private val streakManager: StreakManager,
    private val timerManager: StudyTimerManager,
    private val updateChecker: UpdateChecker,
    private val appInstaller: AppInstaller
) : ViewModel() {

    private val _isCustomPickerOpen = MutableStateFlow(false)
    private val _isAbortDialogOpen = MutableStateFlow(false)
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    private val _showUpdateDialog = MutableStateFlow(false)

    val uiState: StateFlow<StudyUiState> = combine(
        combine(
            streakManager.streakRecordFlow,
            streakManager.accumulatedMinutesTodayFlow,
            streakManager.selectedPresetFlow
        ) { streak, minutes, preset ->
            Triple(streak, minutes, preset)
        },
        combine(
            timerManager.timerState,
            _isCustomPickerOpen,
            _isAbortDialogOpen
        ) { timerState, customPicker, abortDialog ->
            Triple(timerState, customPicker, abortDialog)
        },
        _updateState,
        _showUpdateDialog
    ) { (streak, minutesToday, preset), (timerState, customPicker, abortDialog), updateState, showDialog ->
        StudyUiState(
            streakRecord = streak,
            accumulatedMinutesToday = minutesToday,
            selectedPreset = preset,
            timerState = timerState,
            isCustomPickerOpen = customPicker,
            isAbortDialogOpen = abortDialog,
            updateState = updateState,
            showUpdateDialog = showDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StudyUiState()
    )

    init {
        viewModelScope.launch {
            streakManager.evaluateStreakIntegrity()
        }
        // Automatic background check on launch
        checkForUpdates(silent = true)
    }

    fun selectPreset(preset: StudyPreset) {
        viewModelScope.launch {
            streakManager.setPreset(preset)
            timerManager.setPreset(preset)
        }
    }

    fun openCustomPicker() {
        _isCustomPickerOpen.value = true
    }

    fun closeCustomPicker() {
        _isCustomPickerOpen.value = false
    }

    fun applyCustomMinutes(minutes: Int) {
        val customPreset = StudyPreset.Custom(minutes.coerceIn(1, 180))
        selectPreset(customPreset)
        closeCustomPicker()
    }

    fun startTimer(context: Context) {
        timerManager.startTimer(uiState.value.selectedPreset)
        StudyTimerService.start(context)
    }

    fun promptAbortSession() {
        _isAbortDialogOpen.value = true
    }

    fun dismissAbortDialog() {
        _isAbortDialogOpen.value = false
    }

    fun confirmAbortSession(context: Context) {
        _isAbortDialogOpen.value = false
        timerManager.abortSession()
        StudyTimerService.stop(context)
    }

    fun resetCompleted() {
        timerManager.resetCompleted()
    }

    fun checkForUpdates(silent: Boolean = false) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Checking
            val result = updateChecker.checkForUpdate()
            result.onSuccess { info ->
                if (info != null && info.isUpdateAvailable) {
                    _updateState.value = UpdateState.Available(info)
                    if (!silent) {
                        _showUpdateDialog.value = true
                    }
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
            return TimerViewModel(streakManager, timerManager, updateChecker, appInstaller) as T
        }
    }
}
