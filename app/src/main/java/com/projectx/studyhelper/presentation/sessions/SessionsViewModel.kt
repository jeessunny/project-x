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
    val completedCountToday: Int = 0
)

class SessionsViewModel(
    private val streakRepository: StreakRepository
) : ViewModel() {

    val uiState: StateFlow<SessionsUiState> = combine(
        streakRepository.getRecentSessionsFlow(),
        streakRepository.getAccumulatedMinutesTodayFlow()
    ) { allSessions, accumulatedMinutes ->
        val today = LocalDate.now()
        val todaySessions = allSessions.filter { it.sessionDate == today }
        SessionsUiState(
            todaySessions = todaySessions,
            totalMinutesToday = accumulatedMinutes,
            completedCountToday = todaySessions.size
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
