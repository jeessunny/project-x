package com.projectx.studyhelper.domain.model

sealed class StudyPreset(val minutes: Int, val label: String) {
    data object Fifteen : StudyPreset(15, "15m")
    data object Twenty : StudyPreset(20, "20m")
    data object FortyFive : StudyPreset(45, "45m")
    data class Custom(val customMinutes: Int) : StudyPreset(customMinutes, "${customMinutes}m")

    val durationSeconds: Long
        get() = minutes * 60L

    val isTwentyMinuteRule: Boolean
        get() = minutes == 20

    val isLongSession: Boolean
        get() = minutes >= 45

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StudyPreset) return false
        return minutes == other.minutes
    }

    override fun hashCode(): Int = minutes.hashCode()

    companion object {
        val FIFTEEN_MINUTES: StudyPreset
            get() = Fifteen

        val FORTY_FIVE_MINUTES: StudyPreset
            get() = FortyFive

        // Only 15m, 20m, and 45m standard presets, plus Custom
        val standardPresets: List<StudyPreset>
            get() = listOf(Fifteen, Twenty, FortyFive)

        fun fromMinutes(minutes: Int): StudyPreset {
            return standardPresets.firstOrNull { it.minutes == minutes } ?: Custom(minutes)
        }
    }
}
