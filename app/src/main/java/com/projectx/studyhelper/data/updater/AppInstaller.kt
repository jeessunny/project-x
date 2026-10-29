package com.projectx.studyhelper.data.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed interface DownloadProgress {
    data class Progress(val percent: Int, val bytesDownloaded: Long, val totalBytes: Long) : DownloadProgress
    data class Completed(val apkFile: File) : DownloadProgress
    data class Error(val throwable: Throwable) : DownloadProgress
}

class AppInstaller(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
) {

    private val updatesDir: File
        get() = File(context.cacheDir, "updates").apply { if (!exists()) mkdirs() }

    val downloadedApkFile: File
        get() = File(updatesDir, "app-update.apk")

    /**
     * Downloads APK from the remote URL with live progress emissions.
     */
    fun downloadApk(downloadUrl: String): Flow<DownloadProgress> = flow {
        // Immediate zero-progress emission so UI responds immediately
        emit(DownloadProgress.Progress(0, 0L, 0L))

        val request = Request.Builder()
            .url(downloadUrl)
            .header("User-Agent", "StudyHelper-Updater")
            .header("Accept", "application/octet-stream")
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                emit(DownloadProgress.Error(IOException("Download failed with HTTP ${response.code}")))
                return@flow
            }

            val body = response.body ?: run {
                emit(DownloadProgress.Error(IOException("Response body is empty")))
                return@flow
            }

            val totalBytes = body.contentLength()
            val destinationFile = downloadedApkFile
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            body.byteStream().use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    val buffer = ByteArray(32 * 1024)
                    var bytesRead: Int
                    var totalRead = 0L
                    var lastPercent = 0

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalRead += bytesRead

                        if (totalBytes > 0) {
                            val percent = ((totalRead * 100) / totalBytes).toInt()
                            if (percent > lastPercent || percent == 100) {
                                lastPercent = percent
                                emit(DownloadProgress.Progress(percent, totalRead, totalBytes))
                            }
                        } else {
                            emit(DownloadProgress.Progress(-1, totalRead, -1))
                        }
                    }
                    outputStream.flush()
                }
            }

            emit(DownloadProgress.Completed(destinationFile))
        } catch (e: Exception) {
            emit(DownloadProgress.Error(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Checks if the app has permission to install packages from unknown sources (Android 8.0+).
     */
    fun canInstallPackages(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Creates an intent to request unknown app installation permission in system settings.
     */
    fun createRequestInstallPermissionIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }

    /**
     * Initiates installation of the downloaded APK using Android's native package installer.
     * Uses FileProvider for secure content:// URI sharing.
     */
    fun promptInstall(apkFile: File = downloadedApkFile): Result<Unit> {
        return try {
            if (apkFile.exists()) {
                val authority = "${context.packageName}.fileprovider"
                val contentUri: Uri = FileProvider.getUriForFile(context, authority, apkFile)

                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(contentUri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(installIntent)
                Result.success(Unit)
            } else {
                openBrowserDownload()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            openBrowserDownload()
            Result.success(Unit)
        }
    }

    fun openBrowserDownload(url: String = "https://github.com/jeessunny/project-x/releases/latest") {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
