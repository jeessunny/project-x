package com.projectx.studyhelper.data.updater

import com.projectx.studyhelper.BuildConfig
import com.projectx.studyhelper.domain.model.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class UpdateChecker(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build(),
    private val owner: String = BuildConfig.GITHUB_OWNER,
    private val repo: String = BuildConfig.GITHUB_REPO,
    private val currentVersionName: String = BuildConfig.VERSION_NAME
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun checkForUpdate(): Result<UpdateInfo?> = withContext(Dispatchers.IO) {
        val url = "https://api.github.com/repos/$owner/$repo/releases/latest"
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/vnd.github.v3+json")
            .header("User-Agent", "StudyHelper-Updater")
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    if (response.code == 404) {
                        return@withContext Result.success(null) // No releases published yet
                    }
                    return@withContext Result.failure(
                        IOException("HTTP error ${response.code}: ${response.message}")
                    )
                }

                val responseBody = response.body?.string()
                    ?: return@withContext Result.failure(IOException("Empty response body"))

                val releaseDto = json.decodeFromString<GithubReleaseDto>(responseBody)

                // Match APK asset
                val apkAsset = releaseDto.assets.firstOrNull {
                    it.name.endsWith(".apk", ignoreCase = true)
                } ?: return@withContext Result.failure(
                    IllegalStateException("No APK asset found in latest GitHub release (${releaseDto.tagName})")
                )

                val remoteVersionClean = releaseDto.tagName.removePrefix("v").trim()
                val currentVersionClean = currentVersionName.removePrefix("v").trim()

                val isNewer = isVersionNewer(remoteVersionClean, currentVersionClean)

                val updateInfo = UpdateInfo(
                    versionName = releaseDto.tagName,
                    releaseNotes = releaseDto.body ?: "No release notes provided.",
                    downloadUrl = apkAsset.browserDownloadUrl,
                    fileSize = apkAsset.size,
                    isUpdateAvailable = isNewer
                )

                Result.success(updateInfo)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Compares two semantic version strings (e.g., "1.2.1" vs "1.2.0").
     * Returns true if remoteVersion is strictly greater than currentVersion.
     */
    internal fun isVersionNewer(remoteVersion: String, currentVersion: String): Boolean {
        if (remoteVersion == currentVersion) return false

        val remoteParts = remoteVersion.split(".").mapNotNull { it.takeWhile { ch -> ch.isDigit() }.toIntOrNull() }
        val currentParts = currentVersion.split(".").mapNotNull { it.takeWhile { ch -> ch.isDigit() }.toIntOrNull() }

        val maxLength = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLength) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }

        return false
    }
}
