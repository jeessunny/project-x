package com.projectx.studyhelper.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.projectx.studyhelper.BuildConfig
import com.projectx.studyhelper.data.updater.AppInstaller
import com.projectx.studyhelper.data.updater.DownloadProgress
import com.projectx.studyhelper.data.updater.UpdateChecker
import com.projectx.studyhelper.domain.model.StudyPreset
import com.projectx.studyhelper.domain.model.UpdateInfo
import com.projectx.studyhelper.domain.model.UpdateState
import com.projectx.studyhelper.domain.repository.StreakRepository
import com.projectx.studyhelper.presentation.theme.ThemeConfig
import com.projectx.studyhelper.presentation.theme.ThemeMode
import com.projectx.studyhelper.presentation.theme.ThemePreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeConfig: ThemeConfig = ThemeConfig(),
    val defaultPreset: StudyPreset = StudyPreset.Fifteen,
    val isCheckingForUpdate: Boolean = false,
    val updateState: UpdateState = UpdateState.Idle,
    val showUpdateDialog: Boolean = false,
    val appVersion: String = BuildConfig.VERSION_NAME
)

class SettingsViewModel(
    private val themePreferences: ThemePreferences,
    private val streakRepository: StreakRepository,
    private val updateChecker: UpdateChecker,
    private val appInstaller: AppInstaller
) : ViewModel() {

    private val _isCheckingUpdate = MutableStateFlow(false)
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    private val _showUpdateDialog = MutableStateFlow(false)
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        themePreferences.themeConfigFlow,
        streakRepository.getSelectedPresetFlow(),
        _isCheckingUpdate,
        _updateState,
        _showUpdateDialog
    ) { themeConfig, defaultPreset, isChecking, updateState, showDialog ->
        SettingsUiState(
            themeConfig = themeConfig,
            defaultPreset = defaultPreset,
            isCheckingForUpdate = isChecking,
            updateState = updateState,
            showUpdateDialog = showDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.setThemeMode(mode)
        }
    }

    fun setAmoledBlack(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setAmoledBlack(enabled)
        }
    }

    fun setUseDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setUseDynamicColor(enabled)
        }
    }

    fun setDefaultPreset(preset: StudyPreset) {
        viewModelScope.launch {
            streakRepository.setSelectedPreset(preset)
        }
    }

    fun checkForUpdatesManual() {
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            _updateState.value = UpdateState.Checking
            val result = updateChecker.checkForUpdate()
            _isCheckingUpdate.value = false

            result.onSuccess { info ->
                if (info != null && info.isUpdateAvailable) {
                    _updateState.value = UpdateState.Available(info)
                    _showUpdateDialog.value = true
                } else {
                    _updateState.value = UpdateState.Idle
                    _snackbarMessage.emit("You are on the latest version (${BuildConfig.VERSION_NAME}).")
                }
            }.onFailure { error ->
                _updateState.value = UpdateState.Error(error.localizedMessage ?: "Failed to check for updates")
                _snackbarMessage.emit("Update check failed: ${error.localizedMessage}")
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
        private val themePreferences: ThemePreferences,
        private val streakRepository: StreakRepository,
        private val updateChecker: UpdateChecker,
        private val appInstaller: AppInstaller
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(themePreferences, streakRepository, updateChecker, appInstaller) as T
        }
    }
}
