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

class StudyHelperApplication : Application() {

    companion object {
        private const val TAG = "StudyHelperApp"
    }

    // Lazy initialization — nothing is built until first access.
    // This ensures Application.onCreate() returns instantly and never crashes the process.
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
        // Application.onCreate() is intentionally left minimal.
        // All dependencies are initialized lazily on first access in MainActivity/ViewModels.
        // This prevents ANY crash in Application.onCreate() from killing the process before
        // the UI can even render.
        Log.d(TAG, "Application.onCreate() complete — dependencies are lazily initialized")
    }
}
