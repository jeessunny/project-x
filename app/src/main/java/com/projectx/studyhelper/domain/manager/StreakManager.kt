package com.projectx.studyhelper.domain.manager

import com.projectx.studyhelper.domain.model.StreakRecord
import com.projectx.studyhelper.domain.model.StudyPreset
import com.projectx.studyhelper.domain.model.StudySession
import com.projectx.studyhelper.domain.repository.StreakRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class StreakManager(
    private val streakRepository: StreakRepository
) {

    val streakRecordFlow: Flow<StreakRecord> = streakRepository.getStreakRecordFlow()
    val accumulatedMinutesTodayFlow: Flow<Int> = streakRepository.getAccumulatedMinutesTodayFlow()
    val selectedPresetFlow: Flow<StudyPreset> = streakRepository.getSelectedPresetFlow()
    val dailyGoalMinutesFlow: Flow<Int> = streakRepository.getDailyGoalMinutesFlow()

    /**
     * Evaluates streak continuity on app launch or date rollover.
     * Consumes freeze tokens if calendar days were missed, or resets the streak to 0.
     */
    suspend fun evaluateStreakIntegrity(today: LocalDate = LocalDate.now()): StreakRecord {
        val currentRecord = streakRepository.getStreakRecord()
        val lastDate = currentRecord.lastCompletedDate ?: return currentRecord

        if (lastDate == today || lastDate == today.minusDays(1)) {
            // Streak is current and active
            return currentRecord
        }

        val daysMissed = ChronoUnit.DAYS.between(lastDate, today) - 1
        return if (daysMissed > 0) {
            if (currentRecord.freezeTokensAvailable >= daysMissed) {
                // Protect streak using freeze tokens
                val updatedRecord = currentRecord.copy(
                    freezeTokensAvailable = (currentRecord.freezeTokensAvailable - daysMissed.toInt()).coerceAtLeast(0),
                    lastCompletedDate = today.minusDays(1) // bridged
                )
                streakRepository.updateStreakRecord(updatedRecord)
                updatedRecord
            } else {
                // Streak broken due to missed days
                val updatedRecord = currentRecord.copy(
                    currentStreak = 0,
                    freezeTokensAvailable = 0
                )
                streakRepository.updateStreakRecord(updatedRecord)
                updatedRecord
            }
        } else {
            currentRecord
        }
    }

    /**
     * Processes a newly completed study session, stores it, and increments the streak
     * if the daily accumulated study minutes cross the configurable daily goal threshold.
     */
    suspend fun onSessionCompleted(
        elapsedMinutes: Int,
        preset: StudyPreset,
        sessionDate: LocalDate = LocalDate.now()
    ): StreakRecord {
        val session = StudySession(
            elapsedMinutes = elapsedMinutes,
            targetPreset = preset,
            sessionDate = sessionDate
        )
        streakRepository.recordStudySession(session)

        val totalMinutesToday = streakRepository.getAccumulatedMinutesForDate(sessionDate)
        val dailyGoal = streakRepository.getDailyGoalMinutesFlow().first()
        // Threshold: daily goal (default 45 min) or the completed preset duration
        val threshold = minOf(dailyGoal, preset.minutes)
        var record = streakRepository.getStreakRecord()

        if (totalMinutesToday >= threshold) {
            val lastDate = record.lastCompletedDate

            if (lastDate == sessionDate) {
                // Already reached and credited today; keep current streak count
                return record
            }

            val newStreak = when {
                lastDate == null -> 1
                lastDate == sessionDate.minusDays(1) -> record.currentStreak + 1
                else -> {
                    // Check if missed gap can be frozen
                    val daysMissed = ChronoUnit.DAYS.between(lastDate, sessionDate) - 1
                    if (record.freezeTokensAvailable >= daysMissed) {
                        record = record.copy(
                            freezeTokensAvailable = record.freezeTokensAvailable - daysMissed.toInt()
                        )
                        record.currentStreak + 1
                    } else {
                        // Reset and start at 1
                        1
                    }
                }
            }

            val updatedRecord = record.copy(
                currentStreak = newStreak,
                longestStreak = maxOf(record.longestStreak, newStreak),
                lastCompletedDate = sessionDate
            )

            streakRepository.updateStreakRecord(updatedRecord)
            return updatedRecord
        }

        return record
    }

    suspend fun setPreset(preset: StudyPreset) {
        streakRepository.setSelectedPreset(preset)
    }

    suspend fun setDailyGoalMinutes(minutes: Int) {
        streakRepository.setDailyGoalMinutes(minutes)
    }
}
