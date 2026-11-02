Studyio v1.4.4 Release Notes

Key Improvements & Fixes in this Release:

• Official Material 3 Expressive CircularWavyProgressIndicator: Upgraded Android Jetpack Compose Material 3 library to support Google's official Material 3 Expressive APIs (`@OptIn(ExperimentalMaterial3ExpressiveApi::class)`). Replaced custom canvas wavy loader with Google's official `CircularWavyProgressIndicator`.

• Google Play Store App Downloading Spinner Behavior: The active traveling wave is bound to the focus session's completion ratio (`progress = { currentProgress }`) via draw-phase lambda execution without causing unnecessary main-thread recompositions. The undulating wave amplitude, wavelength, and frequency mirror the official Google Play Store app downloading spinner.

• Theme & AMOLED Compatibility: Seamlessly colored with `MaterialTheme.colorScheme.primary` for the active traveling wave and `MaterialTheme.colorScheme.surfaceContainerHighest` for the underlying motionless track, cleanly enveloping the centered countdown time and label with full AMOLED pitch-black and dynamic theme compatibility.

• First-Launch & User Settings Preservation: Configured Wiggly Wave as the default timer dial style on launch while honoring user settings and all existing dial options (Classic Circle, Material Segmented, and Glow).

• Google Sans Flex Variable Typography: Full application typography powered by Google Sans Flex variable font across all 15 Material 3 text styles.

• Retained Stability: Offline crash diagnostics screen remains active on an isolated process.
