Studyio v1.4.5 Release Notes

Key Improvements & Fixes in this Release:

• Immediate Full-Amplitude Waves: Configured `amplitude = { 1f }` to completely eliminate the default 10% progress threshold where the indicator was previously flat. Waves are now active, distinct, and undulating immediately from the very first moment.

• Tight, Defined Wavy Geometry: Optimized wave wavelength to `24.dp` and stroke to `8.dp`, creating 26 tight, well-defined, energetic wave cycles around the dial instead of loose or blurred ripples.

• Persistent Motionless Wavy Background Track: Layered a stationary 360° wavy track (`waveSpeed = 0.dp`) in `trackColor` beneath the active traveling loader. The wavy circle is now prominently visible before the timer starts and serves as the continuous track during the session.

• Google Play Store Undulating Active Loader: Active traveling wave advances clockwise with dynamic wave undulation (`waveSpeed = wavelength`) in `primaryColor` over the motionless background track.

• AMOLED Pitch-Black & Dynamic Theming: Retains 100% compatibility with Material You dynamic color, custom themes, and pitch-black dark mode without unnecessary main-thread recompositions.
