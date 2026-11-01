package com.example.tamilclock

import android.content.Context
import android.content.SharedPreferences

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
}
