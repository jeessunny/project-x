# Project X — Study Helper App (Phase 1)

Production-grade Android application built with Kotlin 2.0+, Jetpack Compose, Material 3 / Material You dynamic theming, and Clean Architecture (`data`, `domain`, `presentation`).

---

## Deliverables in Phase 1

1. **GitHub Actions CI/CD Pipeline (`.github/workflows/build-apk.yml`)**:
   - Automated workflow triggered on pushes to `main`, tags `v*.*.*`, or manual dispatch.
   - Decodes release keystore from GitHub Secrets (`KEYSTORE_BASE64`, `KEY_ALIAS`, `KEY_PASSWORD`, `STORE_PASSWORD`).
   - Builds signed Release APK with `./gradlew assembleRelease`.
   - Attaches signed APK and SHA-256 checksums to GitHub Releases.

2. **Native In-App Self-Updater (`UpdateChecker.kt`, `AppInstaller.kt`, `UpdateDialog.kt`)**:
   - Checks `https://api.github.com/repos/{owner}/{repo}/releases/latest` for new APK assets.
   - Evaluates semantic version updates against `BuildConfig.VERSION_NAME`.
   - Downloads APK inside the app with live percentage and byte progress indicators.
   - Uses Android `FileProvider` (`content://...`) to launch native package installer via `Intent.ACTION_VIEW`.
   - Gracefully handles unknown app sources permission check (`REQUEST_INSTALL_PACKAGES` / `Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES`) on Android 8.0+.

3. **Pure Material 3 / Material You Dynamic Theme (`Theme.kt`, `Color.kt`)**:
   - Dynamic color extraction via `dynamicLightColorScheme` and `dynamicDarkColorScheme` for Android 12+ (API 31+).
   - High-contrast, accessibility-tested fallback Material 3 color palettes on older Android versions.
   - Pure tonal elevations; zero glassmorphism.

4. **Streak & Study Timer Engine (`StreakManager.kt`, `StudyTimerManager.kt`, Room DB, DataStore)**:
   - Configurable presets: 15-minute and 30-minute thresholds.
   - Tracks daily accumulated study minutes from midnight to midnight local time.
   - Increments streak when cumulative daily minutes meet or exceed the preset.
   - Prevents duplicate streak increments within the same calendar day.
   - Evaluates missed days: consumes freeze tokens if available, or resets streak to 0 if the gap was unprotected.
   - State-based study countdown timer with live progress, pause, resume, and stop controls.

---

## Tech Stack & Architecture

- **OS / Target**: Min SDK 26 (Android 8.0) | Target & Compile SDK 35 (Android 15)
- **UI Toolkit**: Jetpack Compose with Material 3 (`androidx.compose.material3:material3`)
- **Persistence**: Room Database 2.6.1 + Jetpack DataStore Preferences
- **Networking & Serialization**: OkHttp 4.12 + Kotlinx Serialization JSON 1.7.3
- **Architecture**: Single Activity, MVVM/MVI Clean Architecture (`domain` -> `data` -> `presentation`)

---

## How to Build

### Local Debug Build
```bash
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

### Local Release Build (Unsigned or with fallback)
```bash
./gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/app-release.apk`

### GitHub Actions Secrets Setup
In your repository settings (`Settings -> Secrets and variables -> Actions`):
- `KEYSTORE_BASE64`: Base64 encoded string of your release `.jks` file:
  ```bash
  base64 -w 0 release.jks
  ```
- `KEY_ALIAS`: Keystore key alias.
- `KEY_PASSWORD`: Keystore key password.
- `STORE_PASSWORD`: Keystore store password.
