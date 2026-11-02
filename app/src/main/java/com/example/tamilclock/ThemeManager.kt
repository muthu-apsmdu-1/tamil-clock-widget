package com.example.tamilclock

import android.app.Activity
import android.content.Context
import android.graphics.Color

object ThemeManager {
    const val THEME_IVORY = 0      // Default
    const val THEME_WHITE = 1
    const val THEME_YELLOW = 2
    const val THEME_GREEN = 3
    const val THEME_CHARCOAL = 4

    const val CLOCK_ANALOG = 0
    const val CLOCK_DIGITAL = 1

    private const val PREFS_NAME = "tamil_clock_prefs"
    private const val KEY_THEME = "saved_theme"
    private const val KEY_OPACITY = "widget_opacity"
    private const val KEY_CLOCK_STYLE = "clock_style"

    fun applyTheme(activity: Activity) {
        val styleRes = when (getSavedTheme(activity)) {
            THEME_WHITE -> R.style.Theme_TamilClock_White
            THEME_YELLOW -> R.style.Theme_TamilClock_LightYellow
            THEME_GREEN -> R.style.Theme_TamilClock_LightGreen
            THEME_CHARCOAL -> R.style.Theme_TamilClock_Charcoal
            else -> R.style.Theme_TamilClock_IvoryCream
        }
        activity.setTheme(styleRes)
    }

    fun saveTheme(context: Context, themeIndex: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_THEME, themeIndex)
            .apply()
    }

    fun getSavedTheme(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_THEME, THEME_IVORY)
    }

    // Colors used for Home Screen Widgets
    fun getWidgetBaseColor(context: Context): Int {
        return when (getSavedTheme(context)) {
            THEME_WHITE -> Color.parseColor("#FFFFFF")
            THEME_YELLOW -> Color.parseColor("#FFF8DC")
            THEME_GREEN -> Color.parseColor("#EBF7EE")
            THEME_CHARCOAL -> Color.parseColor("#1E1E24")
            else -> Color.parseColor("#FFFDF2") // Ivory Cream Default
        }
    }

    fun saveWidgetOpacity(context: Context, opacity: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_OPACITY, opacity).apply()
    }

    fun getWidgetOpacity(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_OPACITY, 85)
    }

    fun saveClockStyle(context: Context, style: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_CLOCK_STYLE, style).apply()
    }

    fun getClockStyle(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_CLOCK_STYLE, CLOCK_DIGITAL)
    }
}
