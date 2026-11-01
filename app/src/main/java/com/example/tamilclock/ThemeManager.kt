package com.example.tamilclock

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color

object ThemeManager {
    const val THEME_WHITE = 0
    const val THEME_YELLOW = 1
    const val THEME_GREEN = 2
    const val THEME_CHARCOAL = 3

    const val CLOCK_ANALOG = 0
    const val CLOCK_DIGITAL = 1

    private const val PREFS_NAME = "tamil_clock_prefs"
    private const val KEY_THEME = "selected_theme"
    private const val KEY_CLOCK_STYLE = "clock_style"
    private const val KEY_WIDGET_OPACITY = "widget_opacity"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveTheme(context: Context, themeId: Int) {
        getPrefs(context).edit().putInt(KEY_THEME, themeId).apply()
    }

    fun getSavedTheme(context: Context): Int {
        return getPrefs(context).getInt(KEY_THEME, THEME_CHARCOAL)
    }

    fun applyTheme(context: Context) {
        // Theme application logic
    }

    fun saveClockStyle(context: Context, clockStyle: Int) {
        getPrefs(context).edit().putInt(KEY_CLOCK_STYLE, clockStyle).apply()
    }

    fun getClockStyle(context: Context): Int {
        return getPrefs(context).getInt(KEY_CLOCK_STYLE, CLOCK_ANALOG)
    }

    // Opacity: 0 (0% - transparent) to 100 (100% - solid)
    fun saveWidgetOpacity(context: Context, opacityPercent: Int) {
        getPrefs(context).edit().putInt(KEY_WIDGET_OPACITY, opacityPercent).apply()
    }

    fun getWidgetOpacity(context: Context): Int {
        return getPrefs(context).getInt(KEY_WIDGET_OPACITY, 85) // Default 85% opacity
    }

    // Calculates the ARGB color integer for the widget background based on opacity
    fun getWidgetBackgroundColor(context: Context): Int {
        val opacity = getWidgetOpacity(context)
        val alpha = (opacity * 255 / 100).coerceIn(0, 255)
        // Base dark slate color (#1E1E24) with dynamic alpha
        return Color.argb(alpha, 0x1E, 0x1E, 0x24)
    }
}
