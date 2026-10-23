package com.example.tamilclock

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {
    private const val PREF_NAME = "app_theme_prefs"
    private const val KEY_THEME_ID = "selected_theme_id"

    const val THEME_WHITE = 0
    const val THEME_YELLOW = 1
    const val THEME_GREEN = 2
    const val THEME_CHARCOAL = 3

    fun applyTheme(context: Context) {
        // Enforce strict independence from system dark/light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val themeId = getSavedTheme(context)
        context.setTheme(getThemeStyleRes(themeId))
    }

    fun saveTheme(context: Context, themeId: Int) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_THEME_ID, themeId)
            .apply()
    }

    fun getSavedTheme(context: Context): Int {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_THEME_ID, THEME_WHITE)
    }

    private fun getThemeStyleRes(themeId: Int): Int {
        return when (themeId) {
            THEME_YELLOW -> R.style.Theme_TamilCalendar_Yellow
            THEME_GREEN -> R.style.Theme_TamilCalendar_Green
            THEME_CHARCOAL -> R.style.Theme_TamilCalendar_Charcoal
            else -> R.style.Theme_TamilCalendar_White
        }
    }
}
