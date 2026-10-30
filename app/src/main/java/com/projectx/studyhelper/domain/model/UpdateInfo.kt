package com.projectx.studyhelper.domain.model

data class UpdateInfo(
    val versionName: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val fileSize: Long,
    val isUpdateAvailable: Boolean
)

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class Available(val updateInfo: UpdateInfo) : UpdateState
    data class Downloading(val progressPercent: Int, val downloadedBytes: Long, val totalBytes: Long) : UpdateState
    data class ReadyToInstall(val updateInfo: UpdateInfo) : UpdateState
    data class UpToDate(val currentVersion: String) : UpdateState
    data class Error(val message: String) : UpdateState
}
