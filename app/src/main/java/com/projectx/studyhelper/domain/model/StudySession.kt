package com.projectx.studyhelper.domain.model

import java.time.LocalDate

data class StudySession(
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val elapsedMinutes: Int,
    val targetPreset: StudyPreset,
    val sessionDate: LocalDate = LocalDate.now()
)
