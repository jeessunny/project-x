package com.projectx.studyhelper.domain.model

import java.time.LocalDate

data class StreakRecord(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedDate: LocalDate? = null,
    val freezeTokensAvailable: Int = 2
) {
    val isStreakActiveToday: Boolean
        get() = lastCompletedDate == LocalDate.now()
}
