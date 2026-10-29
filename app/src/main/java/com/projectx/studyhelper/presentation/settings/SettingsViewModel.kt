package com.projectx.studyhelper.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.projectx.studyhelper.BuildConfig
import com.projectx.studyhelper.data.updater.UpdateManager
import com.projectx.studyhelper.domain.model.StudyPreset
import com.projectx.studyhelper.domain.model.UpdateInfo
import com.projectx.studyhelper.domain.model.UpdateState
import com.projectx.studyhelper.domain.repository.StreakRepository
import com.projectx.studyhelper.presentation.theme.ThemeConfig
import com.projectx.studyhelper.presentation.theme.ThemeMode
import com.projectx.studyhelper.presentation.theme.ThemePreferences
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeConfig: ThemeConfig = ThemeConfig(),
    val defaultPreset: StudyPreset = StudyPreset.Fifteen,
    val isCheckingForUpdate: Boolean = false,
    val appVersion: String = BuildConfig.VERSION_NAME
)

class SettingsViewModel(
    private val themePreferences: ThemePreferences,
    private val streakRepository: StreakRepository,
    private val updateManager: UpdateManager
) : ViewModel() {

    val snackbarMessage: SharedFlow<String> = updateManager.userFeedbackMessage

    val uiState: StateFlow<SettingsUiState> = combine(
        themePreferences.themeConfigFlow,
        streakRepository.getSelectedPresetFlow(),
        updateManager.isChecking
    ) { themeConfig, defaultPreset, isChecking ->
        SettingsUiState(
            themeConfig = themeConfig,
            defaultPreset = defaultPreset,
            isCheckingForUpdate = isChecking
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
        updateManager.checkForUpdates(isManual = true)
    }

    fun downloadUpdate(updateInfo: UpdateInfo) {
        updateManager.startDownload(updateInfo)
    }

    fun triggerInstall(context: Context): Boolean {
        return updateManager.installDownloadedApk(context)
    }

    fun dismissUpdateDialog() {
        updateManager.dismissDialog()
    }

    class Factory(
        private val themePreferences: ThemePreferences,
        private val streakRepository: StreakRepository,
        private val updateManager: UpdateManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(themePreferences, streakRepository, updateManager) as T
        }
    }
}
