package com.projectx.studyhelper.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long,
    val elapsedMinutes: Int,
    val targetPresetMinutes: Int,
    val sessionDate: LocalDate
)

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity): Long

    @Query("SELECT * FROM study_sessions WHERE sessionDate = :date ORDER BY timestamp DESC")
    fun getSessionsForDateFlow(date: LocalDate): Flow<List<StudySessionEntity>>

    @Query("SELECT COALESCE(SUM(elapsedMinutes), 0) FROM study_sessions WHERE sessionDate = :date")
    fun getAccumulatedMinutesForDateFlow(date: LocalDate): Flow<Int>

    @Query("SELECT COALESCE(SUM(elapsedMinutes), 0) FROM study_sessions WHERE sessionDate = :date")
    suspend fun getAccumulatedMinutesForDate(date: LocalDate): Int

    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC LIMIT 50")
    fun getAllRecentSessions(): Flow<List<StudySessionEntity>>
}
