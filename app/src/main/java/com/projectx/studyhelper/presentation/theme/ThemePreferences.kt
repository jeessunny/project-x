package com.projectx.studyhelper.presentation.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppColorTheme(
    val id: String,
    val displayName: String,
    val previewColor: Color
) {
    PURPLE("purple", "Violet", md_purple_light_primary),
    BLUE("blue", "Ocean", md_blue_light_primary),
    GREEN("green", "Emerald", md_green_light_primary),
    TEAL("teal", "Teal", md_teal_light_primary),
    ROSE("rose", "Rose", md_rose_light_primary)
}

enum class TimerAnimationStyle(
    val id: String,
    val displayName: String,
    val description: String
) {
    DEFAULT("classic", "Classic Circle", "Standard clean circular progress ring"),
    WIGGLY("wiggly", "Wiggly Wave", "Dynamic undulating wavy progress"),
    SEGMENTED("segmented", "Material Segmented", "Stepped radial tick marks & dashes"),
    GLOW("glow", "Glow", "Radiant single ring with glowing neon effect")
}

data class ThemeConfig(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val amoledBlack: Boolean = false,
    val useDynamicColor: Boolean = true,
    val colorTheme: AppColorTheme = AppColorTheme.PURPLE,
    val timerAnimation: TimerAnimationStyle = TimerAnimationStyle.DEFAULT
)

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

class ThemePreferences(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AMOLED_BLACK = booleanPreferencesKey("amoled_black")
        val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val COLOR_THEME = stringPreferencesKey("color_theme")
        val TIMER_ANIMATION = stringPreferencesKey("timer_animation")
    }

    val themeConfigFlow: Flow<ThemeConfig> = context.themeDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val modeString = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            val themeMode = try {
                ThemeMode.valueOf(modeString)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }
            val amoledBlack = prefs[Keys.AMOLED_BLACK] ?: false
            val useDynamicColor = prefs[Keys.USE_DYNAMIC_COLOR] ?: true

            val colorThemeString = prefs[Keys.COLOR_THEME] ?: AppColorTheme.PURPLE.name
            val colorTheme = try {
                when (colorThemeString) {
                    "STUDYIO", "studyio", "ORANGE", "orange" -> AppColorTheme.PURPLE
                    else -> AppColorTheme.valueOf(colorThemeString)
                }
            } catch (e: Exception) {
                AppColorTheme.PURPLE
            }

            val timerAnimString = prefs[Keys.TIMER_ANIMATION] ?: TimerAnimationStyle.DEFAULT.name
            val timerAnimation = try {
                if (timerAnimString == "PULSING_GLOW" || timerAnimString == "pulsing") {
                    TimerAnimationStyle.GLOW
                } else {
                    TimerAnimationStyle.valueOf(timerAnimString)
                }
            } catch (e: Exception) {
                TimerAnimationStyle.DEFAULT
            }

            ThemeConfig(
                themeMode = themeMode,
                amoledBlack = amoledBlack,
                useDynamicColor = useDynamicColor,
                colorTheme = colorTheme,
                timerAnimation = timerAnimation
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
        updateQuickCache(mode = mode)
    }

    suspend fun setAmoledBlack(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[Keys.AMOLED_BLACK] = enabled
        }
        updateQuickCache(amoled = enabled)
    }

    private fun updateQuickCache(mode: ThemeMode? = null, amoled: Boolean? = null) {
        try {
            val prefs = context.getSharedPreferences("theme_quick_cache", Context.MODE_PRIVATE)
            prefs.edit().apply {
                mode?.let { putString("theme_mode", it.name) }
                amoled?.let { putBoolean("amoled_black", it) }
                apply()
            }
        } catch (_: Exception) {}
    }

    suspend fun setUseDynamicColor(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[Keys.USE_DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setColorTheme(colorTheme: AppColorTheme) {
        context.themeDataStore.edit { prefs ->
            prefs[Keys.COLOR_THEME] = colorTheme.name
            prefs[Keys.USE_DYNAMIC_COLOR] = false
        }
    }

    suspend fun setTimerAnimation(animationStyle: TimerAnimationStyle) {
        context.themeDataStore.edit { prefs ->
            prefs[Keys.TIMER_ANIMATION] = animationStyle.name
        }
    }
}
