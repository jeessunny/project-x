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
    // All dependencies are lazy so Application.onCreate() is instant and safe.

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
        Log.d(TAG, "Application created — dependencies are lazily initialized")
    }

    /**
     * Writes the full crash stack trace to EXTERNAL storage so it can be
     * read from Termux without root after running `termux-setup-storage`.
     *
     * Path: /sdcard/Android/data/com.projectx.studyhelper/files/crash_log.txt
     * Read with: cat ~/storage/shared/Android/data/com.projectx.studyhelper/files/crash_log.txt
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
                    appendLine()
                }

                Log.e(TAG, logEntry)

                // Write to EXTERNAL app-specific storage (readable via Termux after
                // running: termux-setup-storage)
                // Path: ~/storage/shared/Android/data/com.projectx.studyhelper/files/crash_log.txt
                val externalDir: File? = getExternalFilesDir(null)
                if (externalDir != null) {
                    externalDir.mkdirs()
                    val externalCrashFile = File(externalDir, "crash_log.txt")
                    externalCrashFile.appendText(logEntry)
                    Log.e(TAG, "Crash written to: ${externalCrashFile.absolutePath}")
                }

                // Also attempt internal storage as fallback
                try {
                    val internalCrashFile = File(filesDir, "crash_log.txt")
                    internalCrashFile.appendText(logEntry)
                } catch (_: Exception) {}

            } catch (e: Exception) {
                Log.e(TAG, "Crash logger itself failed: ${e.message}")
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
