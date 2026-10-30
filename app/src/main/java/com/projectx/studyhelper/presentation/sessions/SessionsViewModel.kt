package com.projectx.studyhelper.presentation.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.projectx.studyhelper.domain.model.StudySession
import com.projectx.studyhelper.domain.repository.StreakRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class SessionsUiState(
    val todaySessions: List<StudySession> = emptyList(),
    val totalMinutesToday: Int = 0,
    val completedCountToday: Int = 0,
    val startedCountToday: Int = 0,
    val stoppedCountToday: Int = 0
)

class SessionsViewModel(
    private val streakRepository: StreakRepository
) : ViewModel() {

    val uiState: StateFlow<SessionsUiState> = combine(
        combine(
            streakRepository.getRecentSessionsFlow(),
            streakRepository.getAccumulatedMinutesTodayFlow()
        ) { allSessions, accumulatedMinutes ->
            val today = LocalDate.now()
            val todaySessions = allSessions.filter { it.sessionDate == today }
            Pair(todaySessions, accumulatedMinutes)
        },
        combine(
            streakRepository.getStartedSessionsTodayFlow(),
            streakRepository.getStoppedSessionsTodayFlow()
        ) { started, stopped ->
            Pair(started, stopped)
        }
    ) { (todaySessions, accumulatedMinutes), (started, stopped) ->
        val completedCount = todaySessions.size
        // Ensure started count is at least completed + stopped for logical consistency
        val totalStarted = maxOf(started, completedCount + stopped)
        SessionsUiState(
            todaySessions = todaySessions,
            totalMinutesToday = accumulatedMinutes,
            completedCountToday = completedCount,
            startedCountToday = totalStarted,
            stoppedCountToday = stopped
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SessionsUiState()
    )

    class Factory(private val streakRepository: StreakRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SessionsViewModel(streakRepository) as T
        }
    }
}
