package com.projectx.studyhelper

import android.app.Application
import android.util.Log
import com.projectx.studyhelper.data.local.StreakPreferences
import com.projectx.studyhelper.data.local.StudyDatabase
import com.projectx.studyhelper.data.repository.StreakRepositoryImpl
import com.projectx.studyhelper.data.updater.AppInstaller
import com.projectx.studyhelper.data.updater.UpdateChecker
import com.projectx.studyhelper.data.updater.UpdateManager
import com.projectx.studyhelper.domain.manager.StreakManager
import com.projectx.studyhelper.domain.manager.StudyTimerManager
import com.projectx.studyhelper.domain.repository.StreakRepository
import com.projectx.studyhelper.presentation.theme.ThemePreferences
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudyHelperApplication : Application() {

    companion object {
        private const val TAG = "StudyHelperApp"
    }

    // ── Lazy dependency graph ─────────────────────────────────────────────────
    // Nothing is constructed until first access. Application.onCreate() is empty
    // so a failing dependency CANNOT kill the process before the UI even renders.

    val database: StudyDatabase by lazy {
        Log.d(TAG, "Initializing Room database")
        StudyDatabase.getInstance(this)
    }

    val streakPreferences: StreakPreferences by lazy {
        Log.d(TAG, "Initializing StreakPreferences")
        StreakPreferences(this)
    }

    val themePreferences: ThemePreferences by lazy {
        Log.d(TAG, "Initializing ThemePreferences")
        ThemePreferences(this)
    }

    val streakRepository: StreakRepository by lazy {
        Log.d(TAG, "Initializing StreakRepository")
        StreakRepositoryImpl(
            streakDao = database.streakDao(),
            sessionDao = database.sessionDao(),
            streakPreferences = streakPreferences
        )
    }

    val streakManager: StreakManager by lazy {
        Log.d(TAG, "Initializing StreakManager")
        StreakManager(streakRepository)
    }

    val timerManager: StudyTimerManager by lazy {
        Log.d(TAG, "Initializing StudyTimerManager")
        StudyTimerManager(streakManager)
    }

    val updateChecker: UpdateChecker by lazy {
        Log.d(TAG, "Initializing UpdateChecker")
        UpdateChecker()
    }

    val appInstaller: AppInstaller by lazy {
        Log.d(TAG, "Initializing AppInstaller")
        AppInstaller(this)
    }

    val updateManager: UpdateManager by lazy {
        Log.d(TAG, "Initializing UpdateManager")
        UpdateManager(updateChecker, appInstaller)
    }

    override fun onCreate() {
        super.onCreate()
        installCrashLogger()
        Log.d(TAG, "Application.onCreate() — all dependencies are lazily initialized")
    }

    /**
     * Writes the full stack trace to a file in the app's files directory
     * so it can be retrieved even without ADB. File: /data/data/.../files/crash_log.txt
     */
    private fun installCrashLogger() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val stackTrace = sw.toString()

                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                val logEntry = buildString {
                    appendLine("=== CRASH REPORT ===")
                    appendLine("Timestamp : $timestamp")
                    appendLine("Thread    : ${thread.name}")
                    appendLine("Exception : ${throwable.javaClass.name}")
                    appendLine("Message   : ${throwable.message}")
                    appendLine("--- Stack Trace ---")
                    appendLine(stackTrace)
                    appendLine("===================")
                }

                Log.e(TAG, logEntry)

                val crashFile = File(filesDir, "crash_log.txt")
                crashFile.appendText(logEntry)
            } catch (e: Exception) {
                Log.e(TAG, "Crash logger itself failed: ${e.message}")
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
