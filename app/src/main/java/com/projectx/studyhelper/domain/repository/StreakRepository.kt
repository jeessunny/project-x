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
    fun getRecentSessionsFlow(): Flow<List<StudySession>>
}
