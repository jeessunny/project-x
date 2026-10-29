package com.projectx.studyhelper.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Entity(tableName = "streak_record")
data class StreakEntity(
    @PrimaryKey
    val id: Int = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedDate: LocalDate? = null,
    val freezeTokensAvailable: Int = 2
)

@Dao
interface StreakDao {
    @Query("SELECT * FROM streak_record WHERE id = 1 LIMIT 1")
    fun getStreakRecordFlow(): Flow<StreakEntity?>

    @Query("SELECT * FROM streak_record WHERE id = 1 LIMIT 1")
    suspend fun getStreakRecord(): StreakEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStreakRecord(streakEntity: StreakEntity)
}
