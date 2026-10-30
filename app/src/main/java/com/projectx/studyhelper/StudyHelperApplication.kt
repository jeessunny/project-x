package com.projectx.studyhelper

import android.app.Application
import android.os.Build
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

        // If this is the isolated :crash process, do not run app initialization
        val isCrashProcess = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getProcessName().endsWith(":crash")
        } else {
            false
        }
        if (isCrashProcess) {
            return
        }

        installCrashLogger()
        Log.d(TAG, "Application created — crash handler active, dependencies lazy")
    }

    /**
     * Intercepts uncaught exceptions and immediately opens the dedicated
     * CrashReportActivity in an isolated process. This guarantees that users
     * see the exact error on screen instead of a generic "has stopped" dialog.
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
                    appendLine("=== PROJECT X CRASH REPORT ===")
                    appendLine("Timestamp : $timestamp")
                    appendLine("Device    : ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT})")
                    appendLine("Thread    : ${thread.name}")
                    appendLine("Exception : ${throwable.javaClass.name}")
                    appendLine("Message   : ${throwable.message}")
                    appendLine("--- Stack Trace ---")
                    appendLine(stackTrace)
                    appendLine("==============================")
                }

                Log.e(TAG, logEntry)

                // Launch crash diagnostic screen
                val intent = CrashReportActivity.createIntent(this, logEntry)
                startActivity(intent)

                // Also save to internal files
                try {
                    File(filesDir, "crash_log.txt").writeText(logEntry)
                } catch (_: Exception) {}

                // Terminate the crashed process so the crash activity can take over cleanly
                android.os.Process.killProcess(android.os.Process.myPid())
                System.exit(10)

            } catch (e: Exception) {
                Log.e(TAG, "Crash handler failed: ${e.message}")
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
