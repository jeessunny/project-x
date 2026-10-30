package com.projectx.studyhelper.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.projectx.studyhelper.domain.model.StudyPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "study_preferences")

class StreakPreferences(private val context: Context) {

    private object PreferencesKeys {
        val SELECTED_PRESET_MINUTES = intPreferencesKey("selected_preset_minutes")
        val DAILY_GOAL_MINUTES = intPreferencesKey("daily_goal_minutes")
    }

    companion object {
        const val DEFAULT_DAILY_GOAL_MINUTES = 45
    }

    val selectedPresetFlow: Flow<StudyPreset> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val minutes = preferences[PreferencesKeys.SELECTED_PRESET_MINUTES] ?: StudyPreset.FIFTEEN_MINUTES.minutes
            StudyPreset.fromMinutes(minutes)
        }

    val dailyGoalMinutesFlow: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.DAILY_GOAL_MINUTES] ?: DEFAULT_DAILY_GOAL_MINUTES
        }

    suspend fun saveSelectedPreset(preset: StudyPreset) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_PRESET_MINUTES] = preset.minutes
        }
    }

    suspend fun saveDailyGoalMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_GOAL_MINUTES] = minutes.coerceIn(10, 360)
        }
    }
}
