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
    val currentQuote: String = ""
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
            "Your future is built in the minutes you dedicate right now.",
            "Discipline is choosing between what you want now and what you want most.",
            "Action cures anxiety. Dive deeper into the problem.",
            "The mind expands through focused struggle, not comfortable ease.",
            "Mastery is the product of silent, unbroken repetitions.",
            "Do not count the minutes; make the minutes count.",
            "Silence the noise. Amplify your depth.",
            "Consistency transforms ordinary students into masters.",
            "One topic, one page, one concept at a time.",
            "Great minds are forged in moments of solitary concentration.",
            "Every minute you spend learning is an investment with infinite return.",
            "Train your attention like a muscle. Hold the line.",
            "Distraction is the thief of potential. Stay present.",
            "You don't need motivation; you need relentless discipline.",
            "When the session is hard, progress is happening fastest.",
            "Clarity follows engagement. Keep reading.",
            "The summit is reached step by step, not in a single leap.",
            "Turn off outside validation. Focus on internal mastery.",
            "Your future self will thank you for not giving up today.",
            "Immersion breeds comprehension.",
            "A disciplined session is a victory no one can take from you.",
            "Be patient with difficult concepts. Breakthroughs take time.",
            "High focus beats high hours every single time.",
            "Stay humble, stay quiet, keep studying.",
            "The difference between good and great is five more minutes of focus.",
            "Effort today is freedom tomorrow.",
            "Build knowledge that cannot be taken away.",
            "Eliminate all distractions. Your goals deserve undivided attention.",
            "Confidence is born from preparation, not hope.",
            "Breathe, reset your posture, and dive right back in.",
            "Every expert was once a beginner who refused to quit.",
            "Sustain the tempo. You are doing important work right now."
        )
    }

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private var timerJob: Job? = null

    init {
        scope.launch {
            streakManager.selectedPresetFlow.collect { preset ->
                if (_timerState.value.status == TimerStatus.Idle) {
                    setPreset(preset)
                }
            }
        }
    }

    fun setPreset(preset: StudyPreset) {
        if (_timerState.value.status == TimerStatus.Idle) {
            _timerState.update {
                it.copy(
                    selectedPreset = preset,
                    targetSeconds = preset.durationSeconds,
                    elapsedSeconds = 0L,
                    currentQuote = ""
                )
            }
        }
    }

    fun startTimer(preset: StudyPreset = _timerState.value.selectedPreset) {
        timerJob?.cancel()
        val initialQuote = MOTIVATIONAL_QUOTES.random()
        _timerState.update {
            it.copy(
                status = TimerStatus.Running,
                selectedPreset = preset,
                targetSeconds = preset.durationSeconds,
                elapsedSeconds = 0L,
                currentQuote = initialQuote
            )
        }

        // Track session start
        scope.launch {
            streakManager.onSessionStarted()
        }

        timerJob = scope.launch {
            while (_timerState.value.status == TimerStatus.Running) {
                delay(1000L)
                val currentElapsed = _timerState.value.elapsedSeconds + 1
                val target = _timerState.value.targetSeconds

                // Rotate to a random motivational quote every 30 seconds
                val activeQuote = if (currentElapsed > 0 && currentElapsed % 30 == 0L) {
                    MOTIVATIONAL_QUOTES.random()
                } else {
                    _timerState.value.currentQuote
                }

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
     * tracks as stopped midway, and does NOT credit streak.
     */
    fun abortSession() {
        timerJob?.cancel()
        _timerState.update {
            it.copy(
                status = TimerStatus.Idle,
                elapsedSeconds = 0L,
                currentQuote = ""
            )
        }

        // Track midway stopped session
        scope.launch {
            streakManager.onSessionAborted()
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
                currentQuote = ""
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
