package com.projectx.studyhelper.presentation.theme

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 1. Classic Purple Palette
private val PurpleLightColorScheme = lightColorScheme(
    primary = md_purple_light_primary,
    onPrimary = md_purple_light_onPrimary,
    primaryContainer = md_purple_light_primaryContainer,
    onPrimaryContainer = md_purple_light_onPrimaryContainer,
    secondary = md_purple_light_secondary,
    onSecondary = md_purple_light_onSecondary,
    secondaryContainer = md_purple_light_secondaryContainer,
    onSecondaryContainer = md_purple_light_onSecondaryContainer,
    tertiary = md_purple_light_tertiary,
    onTertiary = md_purple_light_onTertiary,
    tertiaryContainer = md_purple_light_tertiaryContainer,
    onTertiaryContainer = md_purple_light_onTertiaryContainer,
    error = md_theme_error,
    errorContainer = md_theme_errorContainer,
    onError = md_theme_onError,
    onErrorContainer = md_theme_onErrorContainer,
    background = md_purple_light_background,
    onBackground = md_purple_light_onBackground,
    surface = md_purple_light_surface,
    onSurface = md_purple_light_onSurface,
    surfaceVariant = md_purple_light_surfaceVariant,
    onSurfaceVariant = md_purple_light_onSurfaceVariant,
    outline = md_purple_light_outline
)

private val PurpleDarkColorScheme = darkColorScheme(
    primary = md_purple_dark_primary,
    onPrimary = md_purple_dark_onPrimary,
    primaryContainer = md_purple_dark_primaryContainer,
    onPrimaryContainer = md_purple_dark_onPrimaryContainer,
    secondary = md_purple_dark_secondary,
    onSecondary = md_purple_dark_onSecondary,
    secondaryContainer = md_purple_dark_secondaryContainer,
    onSecondaryContainer = md_purple_dark_onSecondaryContainer,
    tertiary = md_purple_dark_tertiary,
    onTertiary = md_purple_dark_onTertiary,
    tertiaryContainer = md_purple_dark_tertiaryContainer,
    onTertiaryContainer = md_purple_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_purple_dark_background,
    onBackground = md_purple_dark_onBackground,
    surface = md_purple_dark_surface,
    onSurface = md_purple_dark_onSurface,
    surfaceVariant = md_purple_dark_surfaceVariant,
    onSurfaceVariant = md_purple_dark_onSurfaceVariant,
    outline = md_purple_dark_outline
)

// 2. Ocean Blue Palette
private val BlueLightColorScheme = lightColorScheme(
    primary = md_blue_light_primary,
    onPrimary = md_blue_light_onPrimary,
    primaryContainer = md_blue_light_primaryContainer,
    onPrimaryContainer = md_blue_light_onPrimaryContainer,
    secondary = md_blue_light_secondary,
    onSecondary = md_blue_light_onSecondary,
    secondaryContainer = md_blue_light_secondaryContainer,
    onSecondaryContainer = md_blue_light_onSecondaryContainer,
    tertiary = md_blue_light_tertiary,
    onTertiary = md_blue_light_onTertiary,
    tertiaryContainer = md_blue_light_tertiaryContainer,
    onTertiaryContainer = md_blue_light_onTertiaryContainer,
    error = md_theme_error,
    errorContainer = md_theme_errorContainer,
    onError = md_theme_onError,
    onErrorContainer = md_theme_onErrorContainer,
    background = md_blue_light_background,
    onBackground = md_blue_light_onBackground,
    surface = md_blue_light_surface,
    onSurface = md_blue_light_onSurface,
    surfaceVariant = md_blue_light_surfaceVariant,
    onSurfaceVariant = md_blue_light_onSurfaceVariant,
    outline = md_blue_light_outline
)

private val BlueDarkColorScheme = darkColorScheme(
    primary = md_blue_dark_primary,
    onPrimary = md_blue_dark_onPrimary,
    primaryContainer = md_blue_dark_primaryContainer,
    onPrimaryContainer = md_blue_dark_onPrimaryContainer,
    secondary = md_blue_dark_secondary,
    onSecondary = md_blue_dark_onSecondary,
    secondaryContainer = md_blue_dark_secondaryContainer,
    onSecondaryContainer = md_blue_dark_onSecondaryContainer,
    tertiary = md_blue_dark_tertiary,
    onTertiary = md_blue_dark_onTertiary,
    tertiaryContainer = md_blue_dark_tertiaryContainer,
    onTertiaryContainer = md_blue_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_blue_dark_background,
    onBackground = md_blue_dark_onBackground,
    surface = md_blue_dark_surface,
    onSurface = md_blue_dark_onSurface,
    surfaceVariant = md_blue_dark_surfaceVariant,
    onSurfaceVariant = md_blue_dark_onSurfaceVariant,
    outline = md_blue_dark_outline
)

// 3. Emerald Green Palette
private val GreenLightColorScheme = lightColorScheme(
    primary = md_green_light_primary,
    onPrimary = md_green_light_onPrimary,
    primaryContainer = md_green_light_primaryContainer,
    onPrimaryContainer = md_green_light_onPrimaryContainer,
    secondary = md_green_light_secondary,
    onSecondary = md_green_light_onSecondary,
    secondaryContainer = md_green_light_secondaryContainer,
    onSecondaryContainer = md_green_light_onSecondaryContainer,
    tertiary = md_green_light_tertiary,
    onTertiary = md_green_light_onTertiary,
    tertiaryContainer = md_green_light_tertiaryContainer,
    onTertiaryContainer = md_green_light_onTertiaryContainer,
    error = md_theme_error,
    errorContainer = md_theme_errorContainer,
    onError = md_theme_onError,
    onErrorContainer = md_theme_onErrorContainer,
    background = md_green_light_background,
    onBackground = md_green_light_onBackground,
    surface = md_green_light_surface,
    onSurface = md_green_light_onSurface,
    surfaceVariant = md_green_light_surfaceVariant,
    onSurfaceVariant = md_green_light_onSurfaceVariant,
    outline = md_green_light_outline
)

private val GreenDarkColorScheme = darkColorScheme(
    primary = md_green_dark_primary,
    onPrimary = md_green_dark_onPrimary,
    primaryContainer = md_green_dark_primaryContainer,
    onPrimaryContainer = md_green_dark_onPrimaryContainer,
    secondary = md_green_dark_secondary,
    onSecondary = md_green_dark_onSecondary,
    secondaryContainer = md_green_dark_secondaryContainer,
    onSecondaryContainer = md_green_dark_onSecondaryContainer,
    tertiary = md_green_dark_tertiary,
    onTertiary = md_green_dark_onTertiary,
    tertiaryContainer = md_green_dark_tertiaryContainer,
    onTertiaryContainer = md_green_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_green_dark_background,
    onBackground = md_green_dark_onBackground,
    surface = md_green_dark_surface,
    onSurface = md_green_dark_onSurface,
    surfaceVariant = md_green_dark_surfaceVariant,
    onSurfaceVariant = md_green_dark_onSurfaceVariant,
    outline = md_green_dark_outline
)

// 4. Nordic Teal Palette
private val TealLightColorScheme = lightColorScheme(
    primary = md_teal_light_primary,
    onPrimary = md_teal_light_onPrimary,
    primaryContainer = md_teal_light_primaryContainer,
    onPrimaryContainer = md_teal_light_onPrimaryContainer,
    secondary = md_teal_light_secondary,
    onSecondary = md_teal_light_onSecondary,
    secondaryContainer = md_teal_light_secondaryContainer,
    onSecondaryContainer = md_teal_light_onSecondaryContainer,
    tertiary = md_teal_light_tertiary,
    onTertiary = md_teal_light_onTertiary,
    tertiaryContainer = md_teal_light_tertiaryContainer,
    onTertiaryContainer = md_teal_light_onTertiaryContainer,
    error = md_theme_error,
    errorContainer = md_theme_errorContainer,
    onError = md_theme_onError,
    onErrorContainer = md_theme_onErrorContainer,
    background = md_teal_light_background,
    onBackground = md_teal_light_onBackground,
    surface = md_teal_light_surface,
    onSurface = md_teal_light_onSurface,
    surfaceVariant = md_teal_light_surfaceVariant,
    onSurfaceVariant = md_teal_light_onSurfaceVariant,
    outline = md_teal_light_outline
)

private val TealDarkColorScheme = darkColorScheme(
    primary = md_teal_dark_primary,
    onPrimary = md_teal_dark_onPrimary,
    primaryContainer = md_teal_dark_primaryContainer,
    onPrimaryContainer = md_teal_dark_onPrimaryContainer,
    secondary = md_teal_dark_secondary,
    onSecondary = md_teal_dark_onSecondary,
    secondaryContainer = md_teal_dark_secondaryContainer,
    onSecondaryContainer = md_teal_dark_onSecondaryContainer,
    tertiary = md_teal_dark_tertiary,
    onTertiary = md_teal_dark_onTertiary,
    tertiaryContainer = md_teal_dark_tertiaryContainer,
    onTertiaryContainer = md_teal_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_teal_dark_background,
    onBackground = md_teal_dark_onBackground,
    surface = md_teal_dark_surface,
    onSurface = md_teal_dark_onSurface,
    surfaceVariant = md_teal_dark_surfaceVariant,
    onSurfaceVariant = md_teal_dark_onSurfaceVariant,
    outline = md_teal_dark_outline
)

// 5. Rose Pink Palette
private val RoseLightColorScheme = lightColorScheme(
    primary = md_rose_light_primary,
    onPrimary = md_rose_light_onPrimary,
    primaryContainer = md_rose_light_primaryContainer,
    onPrimaryContainer = md_rose_light_onPrimaryContainer,
    secondary = md_rose_light_secondary,
    onSecondary = md_rose_light_onSecondary,
    secondaryContainer = md_rose_light_secondaryContainer,
    onSecondaryContainer = md_rose_light_onSecondaryContainer,
    tertiary = md_rose_light_tertiary,
    onTertiary = md_rose_light_onTertiary,
    tertiaryContainer = md_rose_light_tertiaryContainer,
    onTertiaryContainer = md_rose_light_onTertiaryContainer,
    error = md_theme_error,
    errorContainer = md_theme_errorContainer,
    onError = md_theme_onError,
    onErrorContainer = md_theme_onErrorContainer,
    background = md_rose_light_background,
    onBackground = md_rose_light_onBackground,
    surface = md_rose_light_surface,
    onSurface = md_rose_light_onSurface,
    surfaceVariant = md_rose_light_surfaceVariant,
    onSurfaceVariant = md_rose_light_onSurfaceVariant,
    outline = md_rose_light_outline
)

private val RoseDarkColorScheme = darkColorScheme(
    primary = md_rose_dark_primary,
    onPrimary = md_rose_dark_onPrimary,
    primaryContainer = md_rose_dark_primaryContainer,
    onPrimaryContainer = md_rose_dark_onPrimaryContainer,
    secondary = md_rose_dark_secondary,
    onSecondary = md_rose_dark_onSecondary,
    secondaryContainer = md_rose_dark_secondaryContainer,
    onSecondaryContainer = md_rose_dark_onSecondaryContainer,
    tertiary = md_rose_dark_tertiary,
    onTertiary = md_rose_dark_onTertiary,
    tertiaryContainer = md_rose_dark_tertiaryContainer,
    onTertiaryContainer = md_rose_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_rose_dark_background,
    onBackground = md_rose_dark_onBackground,
    surface = md_rose_dark_surface,
    onSurface = md_rose_dark_onSurface,
    surfaceVariant = md_rose_dark_surfaceVariant,
    onSurfaceVariant = md_rose_dark_onSurfaceVariant,
    outline = md_rose_dark_outline
)

private fun getStaticColorScheme(colorTheme: AppColorTheme, isDark: Boolean): ColorScheme {
    return when (colorTheme) {
        AppColorTheme.PURPLE -> if (isDark) PurpleDarkColorScheme else PurpleLightColorScheme
        AppColorTheme.BLUE -> if (isDark) BlueDarkColorScheme else BlueLightColorScheme
        AppColorTheme.GREEN -> if (isDark) GreenDarkColorScheme else GreenLightColorScheme
        AppColorTheme.TEAL -> if (isDark) TealDarkColorScheme else TealLightColorScheme
        AppColorTheme.ROSE -> if (isDark) RoseDarkColorScheme else RoseLightColorScheme
    }
}

@Composable
fun StudyHelperTheme(
    themeConfig: ThemeConfig = ThemeConfig(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()

    val isDark = when (themeConfig.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    // Dynamic Material You (Monet) theme extraction on Android 12+ (API 31+)
    // Safely wrapped in try-catch to ensure graceful fallback if device wallpaper colors cannot be resolved
    val baseColorScheme: ColorScheme = when {
        themeConfig.useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            try {
                if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } catch (e: Throwable) {
                Log.w("StudyHelperTheme", "Dynamic color extraction fallback to static palette: ${e.message}")
                getStaticColorScheme(themeConfig.colorTheme, isDark)
            }
        }
        else -> getStaticColorScheme(themeConfig.colorTheme, isDark)
    }

    val finalColorScheme = if (isDark && themeConfig.amoledBlack) {
        baseColorScheme.copy(
            surface = AmoledBlack,
            background = AmoledBlack,
            surfaceDim = AmoledSurfaceDim,
            surfaceContainerLowest = AmoledSurfaceContainerLowest,
            surfaceContainerLow = AmoledSurfaceContainerLow,
            surfaceContainer = AmoledSurfaceContainer,
            surfaceContainerHigh = AmoledSurfaceContainerHigh,
            surfaceContainerHighest = AmoledSurfaceContainerHighest
        )
    } else {
        baseColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            try {
                val window = (view.context as? Activity)?.window ?: return@SideEffect
                val bgColor = finalColorScheme.background.toArgb()
                window.setBackgroundDrawable(ColorDrawable(bgColor))
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !isDark
                    isAppearanceLightNavigationBars = !isDark
                }
            } catch (e: Throwable) {
                Log.w("StudyHelperTheme", "Insets controller setup skipped: ${e.message}")
            }
        }
    }

    MaterialTheme(
        colorScheme = finalColorScheme,
        typography = Typography,
        content = content
    )
}
