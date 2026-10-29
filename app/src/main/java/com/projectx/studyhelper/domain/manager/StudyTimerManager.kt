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
    data object Completed : TimerStatus
}

data class TimerState(
    val status: TimerStatus = TimerStatus.Idle,
    val selectedPreset: StudyPreset = StudyPreset.Fifteen,
    val elapsedSeconds: Long = 0L,
    val targetSeconds: Long = StudyPreset.Fifteen.durationSeconds,
    val currentQuote: String = "Focus on your book, not your screen — the timer works on its own"
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
    companion object {
        val MOTIVATIONAL_QUOTES = listOf(
            "Focus on your book, not your screen — the timer works on its own.",
            "Deep work requires deep stillness.",
            "Halfway there. Maintain the rhythm.",
            "Keep your eyes on the material. The timer runs quietly.",
            "Small daily efforts compound into extraordinary results.",
            "Protect your focus; it is your greatest competitive advantage.",
            "Resist the impulse to switch tabs. Stay locked in.",
            "Your future is built in the minutes you dedicate right now."
        )
    }

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private var timerJob: Job? = null

    fun setPreset(preset: StudyPreset) {
        if (_timerState.value.status == TimerStatus.Idle) {
            _timerState.update {
                it.copy(
                    selectedPreset = preset,
                    targetSeconds = preset.durationSeconds,
                    elapsedSeconds = 0L,
                    currentQuote = MOTIVATIONAL_QUOTES.first()
                )
            }
        }
    }

    fun startTimer(preset: StudyPreset = _timerState.value.selectedPreset) {
        timerJob?.cancel()
        val quote = MOTIVATIONAL_QUOTES.random()
        _timerState.update {
            it.copy(
                status = TimerStatus.Running,
                selectedPreset = preset,
                targetSeconds = preset.durationSeconds,
                elapsedSeconds = 0L,
                currentQuote = quote
            )
        }

        timerJob = scope.launch {
            while (_timerState.value.status == TimerStatus.Running) {
                delay(1000L)
                val currentElapsed = _timerState.value.elapsedSeconds + 1
                val target = _timerState.value.targetSeconds

                // Rotate motivational quote every 60 seconds
                val quoteIndex = ((currentElapsed / 60) % MOTIVATIONAL_QUOTES.size).toInt()
                val activeQuote = MOTIVATIONAL_QUOTES[quoteIndex]

                if (currentElapsed >= target) {
                    _timerState.update {
                        it.copy(
                            elapsedSeconds = target,
                            status = TimerStatus.Completed,
                            currentQuote = "Session completed! Outstanding discipline."
                        )
                    }
                    completeAndRecordSession()
                    break
                } else {
                    _timerState.update {
                        it.copy(
                            elapsedSeconds = currentElapsed,
                            currentQuote = activeQuote
                        )
                    }
                }
            }
        }
    }

    /**
     * Strict Focus Mode: Abandons current session, discards all elapsed time,
     * and does NOT credit streak.
     */
    fun abortSession() {
        timerJob?.cancel()
        _timerState.update {
            it.copy(
                status = TimerStatus.Idle,
                elapsedSeconds = 0L,
                currentQuote = MOTIVATIONAL_QUOTES.first()
            )
        }
    }

    /**
     * Resets completed timer back to idle state ready for the next session.
     */
    fun resetCompleted() {
        _timerState.update {
            it.copy(
                status = TimerStatus.Idle,
                elapsedSeconds = 0L,
                currentQuote = MOTIVATIONAL_QUOTES.first()
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
