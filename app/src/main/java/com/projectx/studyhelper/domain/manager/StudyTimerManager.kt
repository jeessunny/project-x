package com.projectx.studyhelper.domain.manager

import com.projectx.studyhelper.domain.model.StudyPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface TimerStatus {
    data object Idle : TimerStatus
    data object Running : TimerStatus
    data object Paused : TimerStatus
    data object Completed : TimerStatus
}

data class TimerState(
    val status: TimerStatus = TimerStatus.Idle,
    val selectedPreset: StudyPreset = StudyPreset.FIFTEEN_MINUTES,
    val elapsedSeconds: Long = 0L,
    val targetSeconds: Long = StudyPreset.FIFTEEN_MINUTES.durationSeconds
) {
    val remainingSeconds: Long
        get() = (targetSeconds - elapsedSeconds).coerceAtLeast(0L)

    val progress: Float
        get() = if (targetSeconds > 0) (elapsedSeconds.toFloat() / targetSeconds).coerceIn(0f, 1f) else 0f

    val formattedRemainingTime: String
        get() {
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }

    val formattedElapsedTime: String
        get() {
            val minutes = elapsedSeconds / 60
            val seconds = elapsedSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
}

class StudyTimerManager(
    private val streakManager: StreakManager,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private var timerJob: Job? = null

    fun setPreset(preset: StudyPreset) {
        if (_timerState.value.status == TimerStatus.Idle) {
            _timerState.update {
                it.copy(
                    selectedPreset = preset,
                    targetSeconds = preset.durationSeconds,
                    elapsedSeconds = 0L
                )
            }
        }
    }

    fun startTimer(preset: StudyPreset = _timerState.value.selectedPreset) {
        timerJob?.cancel()
        _timerState.update {
            it.copy(
                status = TimerStatus.Running,
                selectedPreset = preset,
                targetSeconds = preset.durationSeconds,
                elapsedSeconds = if (it.status == TimerStatus.Paused) it.elapsedSeconds else 0L
            )
        }

        timerJob = scope.launch {
            while (_timerState.value.status == TimerStatus.Running) {
                delay(1000L)
                val currentElapsed = _timerState.value.elapsedSeconds + 1
                val target = _timerState.value.targetSeconds

                if (currentElapsed >= target) {
                    _timerState.update {
                        it.copy(
                            elapsedSeconds = target,
                            status = TimerStatus.Completed
                        )
                    }
                    // Auto-record completed session
                    completeAndRecordSession()
                    break
                } else {
                    _timerState.update { it.copy(elapsedSeconds = currentElapsed) }
                }
            }
        }
    }

    fun pauseTimer() {
        if (_timerState.value.status == TimerStatus.Running) {
            timerJob?.cancel()
            _timerState.update { it.copy(status = TimerStatus.Paused) }
        }
    }

    fun resumeTimer() {
        if (_timerState.value.status == TimerStatus.Paused) {
            startTimer(_timerState.value.selectedPreset)
        }
    }

    fun stopTimer(savePartial: Boolean = true) {
        timerJob?.cancel()
        val currentState = _timerState.value
        val elapsedMinutes = (currentState.elapsedSeconds / 60).toInt()

        if (savePartial && elapsedMinutes > 0) {
            scope.launch {
                streakManager.onSessionCompleted(
                    elapsedMinutes = elapsedMinutes,
                    preset = currentState.selectedPreset
                )
            }
        }

        _timerState.update {
            it.copy(
                status = TimerStatus.Idle,
                elapsedSeconds = 0L
            )
        }
    }

    private suspend fun completeAndRecordSession() {
        val currentState = _timerState.value
        val elapsedMinutes = currentState.selectedPreset.minutes
        streakManager.onSessionCompleted(
            elapsedMinutes = elapsedMinutes,
            preset = currentState.selectedPreset
        )
    }
}
