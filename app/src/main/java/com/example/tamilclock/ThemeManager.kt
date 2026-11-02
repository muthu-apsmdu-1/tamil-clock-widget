package com.example.tamilclock

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color

object ThemeManager {
    private const val PREF_NAME = "tamil_clock_prefs"
    private const val KEY_THEME = "selected_theme"
    private const val KEY_CLOCK_STYLE = "clock_style"
    private const val KEY_WIDGET_OPACITY = "widget_opacity"

    // Theme IDs
    const val THEME_MAROON = 0
    const val THEME_WHITE = 1
    const val THEME_YELLOW = 2
    const val THEME_GREEN = 3
    const val THEME_CHARCOAL = 4

    // Clock Styles
    const val CLOCK_ANALOG = 0
    const val CLOCK_DIGITAL = 1

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveTheme(context: Context, themeId: Int) {
        getPrefs(context).edit().putInt(KEY_THEME, themeId).apply()
    }

    fun getSavedTheme(context: Context): Int {
        return getPrefs(context).getInt(KEY_THEME, THEME_MAROON)
    }

    fun saveClockStyle(context: Context, style: Int) {
        getPrefs(context).edit().putInt(KEY_CLOCK_STYLE, style).apply()
    }

    fun getClockStyle(context: Context): Int {
        return getPrefs(context).getInt(KEY_CLOCK_STYLE, CLOCK_ANALOG)
    }

    fun saveWidgetOpacity(context: Context, opacity: Int) {
        getPrefs(context).edit().putInt(KEY_WIDGET_OPACITY, opacity.coerceIn(0, 100)).apply()
    }

    fun getWidgetOpacity(context: Context): Int {
        return getPrefs(context).getInt(KEY_WIDGET_OPACITY, 95)
    }

    /**
     * Resolves the RGB base color for dynamic widget rendering based on the active theme.
     */
    fun getWidgetBaseColor(context: Context): Int {
        return when (getSavedTheme(context)) {
            THEME_WHITE -> Color.parseColor("#FFFFFF")
            THEME_YELLOW -> Color.parseColor("#FFF9E6")
            THEME_GREEN -> Color.parseColor("#1B4D3E")
            THEME_CHARCOAL -> Color.parseColor("#1E1E24")
            else -> Color.parseColor("#78000C") // Heritage Maroon
        }
    }

    /**
     * Safe activity theme applicator without relying on undeclared XML styles.
     */
    fun applyTheme(activity: Activity) {
        // Keeps the primary system theme intact while widgets dynamically render their chosen colors
    }
}
