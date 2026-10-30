package com.projectx.studyhelper.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [StreakEntity::class, StudySessionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StudyDatabase : RoomDatabase() {
    abstract fun streakDao(): StreakDao
    abstract fun sessionDao(): SessionDao

    companion object {
        private const val TAG = "StudyDatabase"

        @Volatile
        private var INSTANCE: StudyDatabase? = null

        fun getInstance(context: Context): StudyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabaseWithFallback(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabaseWithFallback(context: Context): StudyDatabase {
            return try {
                buildDatabase(context)
            } catch (e: Exception) {
                Log.e(TAG, "Corrupt database detected. Purging old database file and rebuilding cleanly.", e)
                try {
                    context.deleteDatabase("study_helper.db")
                } catch (_: Exception) {}
                buildDatabase(context)
            }
        }

        private fun buildDatabase(context: Context): StudyDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                StudyDatabase::class.java,
                "study_helper.db"
            )
            .fallbackToDestructiveMigration()
            .build()
        }
    }
}
