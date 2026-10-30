package com.projectx.studyhelper.presentation.study

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.projectx.studyhelper.domain.manager.StreakManager
import com.projectx.studyhelper.domain.manager.StudyTimerManager
import com.projectx.studyhelper.domain.manager.TimerState
import com.projectx.studyhelper.domain.model.StreakRecord
import com.projectx.studyhelper.domain.model.StudyPreset
import com.projectx.studyhelper.presentation.service.StudyTimerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StudyUiState(
    val streakRecord: StreakRecord = StreakRecord(),
    val accumulatedMinutesToday: Int = 0,
    val selectedPreset: StudyPreset = StudyPreset.Fifteen,
    val dailyGoalMinutes: Int = 45,
    val timerState: TimerState = TimerState(),
    val isCustomPickerOpen: Boolean = false,
    val isAbortDialogOpen: Boolean = false
) {
    val dailyProgress: Float
        get() = if (dailyGoalMinutes > 0) {
            (accumulatedMinutesToday.toFloat() / dailyGoalMinutes).coerceIn(0f, 1f)
        } else {
            0f
        }

    val isDailyGoalReached: Boolean
        get() = accumulatedMinutesToday >= dailyGoalMinutes
}

class TimerViewModel(
    private val streakManager: StreakManager,
    private val timerManager: StudyTimerManager
) : ViewModel() {

    private val _isCustomPickerOpen = MutableStateFlow(false)
    private val _isAbortDialogOpen = MutableStateFlow(false)

    val uiState: StateFlow<StudyUiState> = combine(
        combine(
            streakManager.streakRecordFlow,
            streakManager.accumulatedMinutesTodayFlow,
            streakManager.selectedPresetFlow,
            streakManager.dailyGoalMinutesFlow
        ) { streak, minutes, preset, goal ->
            Quadruple(streak, minutes, preset, goal)
        },
        combine(
            timerManager.timerState,
            _isCustomPickerOpen,
            _isAbortDialogOpen
        ) { timerState, customPicker, abortDialog ->
            Triple(timerState, customPicker, abortDialog)
        }
    ) { (streak, minutesToday, preset, goal), (timerState, customPicker, abortDialog) ->
        StudyUiState(
            streakRecord = streak,
            accumulatedMinutesToday = minutesToday,
            selectedPreset = preset,
            dailyGoalMinutes = goal,
            timerState = timerState,
            isCustomPickerOpen = customPicker,
            isAbortDialogOpen = abortDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StudyUiState()
    )

    init {
        viewModelScope.launch {
            streakManager.evaluateStreakIntegrity()
        }
    }

    fun selectPreset(preset: StudyPreset) {
        viewModelScope.launch {
            streakManager.setPreset(preset)
            timerManager.setPreset(preset)
        }
    }

    fun openCustomPicker() {
        _isCustomPickerOpen.value = true
    }

    fun closeCustomPicker() {
        _isCustomPickerOpen.value = false
    }

    fun applyCustomMinutes(minutes: Int) {
        val customPreset = StudyPreset.Custom(minutes.coerceIn(1, 180))
        selectPreset(customPreset)
        closeCustomPicker()
    }

    fun startTimer(context: Context) {
        timerManager.startTimer(uiState.value.selectedPreset)
        StudyTimerService.start(context)
    }

    fun promptAbortSession() {
        _isAbortDialogOpen.value = true
    }

    fun dismissAbortDialog() {
        _isAbortDialogOpen.value = false
    }

    fun confirmAbortSession(context: Context) {
        _isAbortDialogOpen.value = false
        timerManager.abortSession()
        StudyTimerService.stop(context)
    }

    fun resetCompleted() {
        timerManager.resetCompleted()
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    class Factory(
        private val streakManager: StreakManager,
        private val timerManager: StudyTimerManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TimerViewModel(streakManager, timerManager) as T
        }
    }
}
