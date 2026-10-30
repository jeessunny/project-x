package com.projectx.studyhelper.data.repository

import com.projectx.studyhelper.data.local.SessionDao
import com.projectx.studyhelper.data.local.StreakDao
import com.projectx.studyhelper.data.local.StreakEntity
import com.projectx.studyhelper.data.local.StreakPreferences
import com.projectx.studyhelper.data.local.StudySessionEntity
import com.projectx.studyhelper.domain.model.StreakRecord
import com.projectx.studyhelper.domain.model.StudyPreset
import com.projectx.studyhelper.domain.model.StudySession
import com.projectx.studyhelper.domain.repository.StreakRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class StreakRepositoryImpl(
    private val streakDao: StreakDao,
    private val sessionDao: SessionDao,
    private val streakPreferences: StreakPreferences
) : StreakRepository {

    override fun getStreakRecordFlow(): Flow<StreakRecord> {
        return streakDao.getStreakRecordFlow().map { entity ->
            entity?.toDomain() ?: StreakRecord()
        }
    }

    override suspend fun getStreakRecord(): StreakRecord {
        return streakDao.getStreakRecord()?.toDomain() ?: StreakRecord()
    }

    override suspend fun updateStreakRecord(streakRecord: StreakRecord) {
        streakDao.upsertStreakRecord(streakRecord.toEntity())
    }

    override suspend fun recordStudySession(session: StudySession): Long {
        val entity = StudySessionEntity(
            timestamp = session.timestamp,
            elapsedMinutes = session.elapsedMinutes,
            targetPresetMinutes = session.targetPreset.minutes,
            sessionDate = session.sessionDate
        )
        return sessionDao.insertSession(entity)
    }

    override fun getAccumulatedMinutesTodayFlow(): Flow<Int> {
        return sessionDao.getAccumulatedMinutesForDateFlow(LocalDate.now())
    }

    override suspend fun getAccumulatedMinutesForDate(date: LocalDate): Int {
        return sessionDao.getAccumulatedMinutesForDate(date)
    }

    override fun getSelectedPresetFlow(): Flow<StudyPreset> {
        return streakPreferences.selectedPresetFlow
    }

    override suspend fun setSelectedPreset(preset: StudyPreset) {
        streakPreferences.saveSelectedPreset(preset)
    }

    override fun getDailyGoalMinutesFlow(): Flow<Int> {
        return streakPreferences.dailyGoalMinutesFlow
    }

    override suspend fun setDailyGoalMinutes(minutes: Int) {
        streakPreferences.saveDailyGoalMinutes(minutes)
    }

    override fun getRecentSessionsFlow(): Flow<List<StudySession>> {
        return sessionDao.getAllRecentSessions().map { list ->
            list.map { entity ->
                StudySession(
                    id = entity.id,
                    timestamp = entity.timestamp,
                    elapsedMinutes = entity.elapsedMinutes,
                    targetPreset = StudyPreset.fromMinutes(entity.targetPresetMinutes),
                    sessionDate = entity.sessionDate
                )
            }
        }
    }

    private fun StreakEntity.toDomain(): StreakRecord {
        return StreakRecord(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            lastCompletedDate = lastCompletedDate,
            freezeTokensAvailable = freezeTokensAvailable
        )
    }

    private fun StreakRecord.toEntity(): StreakEntity {
        return StreakEntity(
            id = 1,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            lastCompletedDate = lastCompletedDate,
            freezeTokensAvailable = freezeTokensAvailable
        )
    }
}
