package com.projectx.studyhelper.domain.repository

import com.projectx.studyhelper.domain.model.StreakRecord
import com.projectx.studyhelper.domain.model.StudyPreset
import com.projectx.studyhelper.domain.model.StudySession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface StreakRepository {
    fun getStreakRecordFlow(): Flow<StreakRecord>
    suspend fun getStreakRecord(): StreakRecord
    suspend fun updateStreakRecord(streakRecord: StreakRecord)

    suspend fun recordStudySession(session: StudySession): Long
    fun getAccumulatedMinutesTodayFlow(): Flow<Int>
    suspend fun getAccumulatedMinutesForDate(date: LocalDate): Int

    fun getSelectedPresetFlow(): Flow<StudyPreset>
    suspend fun setSelectedPreset(preset: StudyPreset)

    fun getDailyGoalMinutesFlow(): Flow<Int>
    suspend fun setDailyGoalMinutes(minutes: Int)

    fun getStartedSessionsTodayFlow(): Flow<Int>
    fun getStoppedSessionsTodayFlow(): Flow<Int>
    suspend fun incrementStartedSessions()
    suspend fun incrementStoppedSessions()

    fun getRecentSessionsFlow(): Flow<List<StudySession>>
}
