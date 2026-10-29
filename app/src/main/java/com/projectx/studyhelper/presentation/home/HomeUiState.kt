package com.projectx.studyhelper.presentation.home

import com.projectx.studyhelper.domain.manager.TimerState
import com.projectx.studyhelper.domain.model.StreakRecord
import com.projectx.studyhelper.domain.model.StudyPreset
import com.projectx.studyhelper.domain.model.UpdateState

data class HomeUiState(
    val streakRecord: StreakRecord = StreakRecord(),
    val accumulatedMinutesToday: Int = 0,
    val selectedPreset: StudyPreset = StudyPreset.FIFTEEN_MINUTES,
    val timerState: TimerState = TimerState(),
    val updateState: UpdateState = UpdateState.Idle,
    val showUpdateDialog: Boolean = false,
    val isAppSourcesPermissionRequired: Boolean = false
) {
    val dailyProgress: Float
        get() = (accumulatedMinutesToday.toFloat() / selectedPreset.minutes).coerceIn(0f, 1f)

    val isDailyGoalReached: Boolean
        get() = accumulatedMinutesToday >= selectedPreset.minutes
}
