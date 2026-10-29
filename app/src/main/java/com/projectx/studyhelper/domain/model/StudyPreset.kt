package com.projectx.studyhelper.domain.model

enum class StudyPreset(val minutes: Int, val label: String) {
    FIFTEEN_MINUTES(15, "15 Min"),
    THIRTY_MINUTES(30, "30 Min");

    val durationSeconds: Long
        get() = minutes * 60L

    companion object {
        fun fromMinutes(minutes: Int): StudyPreset {
            return entries.firstOrNull { it.minutes == minutes } ?: FIFTEEN_MINUTES
        }
    }
}
