package com.projectx.studyhelper.data.updater

import android.content.Context
import com.projectx.studyhelper.domain.model.UpdateInfo
import com.projectx.studyhelper.domain.model.UpdateState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UpdateManager(
    private val updateChecker: UpdateChecker,
    private val appInstaller: AppInstaller,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _userFeedbackMessage = MutableSharedFlow<String>()
    val userFeedbackMessage: SharedFlow<String> = _userFeedbackMessage.asSharedFlow()

    private var downloadJob: Job? = null

    /**
     * Checks GitHub API for newer releases with immediate visual feedback.
     * @param isManual True if initiated by the user via Settings button.
     */
    fun checkForUpdates(isManual: Boolean = false) {
        if (_isChecking.value) return

        scope.launch {
            _isChecking.value = true
            if (isManual) {
                _userFeedbackMessage.emit("Gathering release info & checking for updates...")
            }

            val result = updateChecker.checkForUpdate()
            _isChecking.value = false

            result.onSuccess { info ->
                if (info != null && info.isUpdateAvailable) {
                    _updateState.value = UpdateState.Available(info)
                    _showDialog.value = true
                    if (isManual) {
                        _userFeedbackMessage.emit("New update found: ${info.versionName}")
                    }
                } else {
                    _updateState.value = UpdateState.Idle
                    if (isManual) {
                        _userFeedbackMessage.emit("You are running the latest version of Project X.")
                    }
                }
            }.onFailure { error ->
                _updateState.value = UpdateState.Error(error.localizedMessage ?: "Failed to check for updates")
                if (isManual) {
                    _userFeedbackMessage.emit("Unable to check for updates: ${error.localizedMessage}")
                }
            }
        }
    }

    fun openDialog() {
        _showDialog.value = true
    }

    fun dismissDialog() {
        _showDialog.value = false
    }

    /**
     * Initiates download with instant feedback notification and zero-delay UI transition.
     */
    fun startDownload(updateInfo: UpdateInfo) {
        downloadJob?.cancel()

        // Immediate responsive UI state: indeterminate connecting/gathering phase
        _updateState.value = UpdateState.Downloading(
            progressPercent = -1,
            downloadedBytes = 0L,
            totalBytes = updateInfo.fileSize
        )
        _showDialog.value = true

        scope.launch {
            _userFeedbackMessage.emit("Gathering update package & connecting to server...")
        }

        downloadJob = scope.launch {
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
                        _userFeedbackMessage.emit("Download complete! Ready to install.")
                    }
                    is DownloadProgress.Error -> {
                        _updateState.value = UpdateState.Error(
                            progress.throwable.localizedMessage ?: "Download failed. Please check your network connection."
                        )
                        _userFeedbackMessage.emit("Download failed: ${progress.throwable.localizedMessage}")
                    }
                }
            }
        }
    }

    fun installDownloadedApk(context: Context): Boolean {
        scope.launch {
            _userFeedbackMessage.emit("Preparing Android Package Installer...")
        }

        if (!appInstaller.canInstallPackages()) {
            val intent = appInstaller.createRequestInstallPermissionIntent()
            context.startActivity(intent)
            return false
        }
        val result = appInstaller.promptInstall()
        return result.isSuccess
    }
}
