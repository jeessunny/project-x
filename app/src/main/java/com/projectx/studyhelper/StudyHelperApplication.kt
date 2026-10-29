package com.projectx.studyhelper

import android.app.Application
import com.projectx.studyhelper.data.local.StreakPreferences
import com.projectx.studyhelper.data.local.StudyDatabase
import com.projectx.studyhelper.data.repository.StreakRepositoryImpl
import com.projectx.studyhelper.data.updater.AppInstaller
import com.projectx.studyhelper.data.updater.UpdateChecker
import com.projectx.studyhelper.domain.manager.StreakManager
import com.projectx.studyhelper.domain.manager.StudyTimerManager
import com.projectx.studyhelper.domain.repository.StreakRepository

class StudyHelperApplication : Application() {

    lateinit var database: StudyDatabase
        private set

    lateinit var streakPreferences: StreakPreferences
        private set

    lateinit var streakRepository: StreakRepository
        private set

    lateinit var streakManager: StreakManager
        private set

    lateinit var timerManager: StudyTimerManager
        private set

    lateinit var updateChecker: UpdateChecker
        private set

    lateinit var appInstaller: AppInstaller
        private set

    override fun onCreate() {
        super.onCreate()

        database = StudyDatabase.getInstance(this)
        streakPreferences = StreakPreferences(this)
        streakRepository = StreakRepositoryImpl(
            streakDao = database.streakDao(),
            sessionDao = database.sessionDao(),
            streakPreferences = streakPreferences
        )
        streakManager = StreakManager(streakRepository)
        timerManager = StudyTimerManager(streakManager)
        updateChecker = UpdateChecker()
        appInstaller = AppInstaller(this)
    }
}
