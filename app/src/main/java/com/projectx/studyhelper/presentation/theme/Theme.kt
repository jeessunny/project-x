package com.projectx.studyhelper.presentation.theme

import android.app.Activity
import android.os.Build
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

    val baseColorScheme: ColorScheme = when {
        themeConfig.useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> PurpleDarkColorScheme
        else -> PurpleLightColorScheme
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
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = finalColorScheme.surface.toArgb()
            window.navigationBarColor = finalColorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = finalColorScheme,
        typography = Typography,
        content = content
    )
}
