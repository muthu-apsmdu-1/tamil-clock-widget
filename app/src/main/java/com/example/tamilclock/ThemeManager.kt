package com.example.tamilclock

import android.app.Activity
import android.content.Context
import android.graphics.Color

object ThemeManager {

    // -------------------------------------------------------------
    // THEME CONSTANTS (INT PRESERVED)
    // -------------------------------------------------------------
    const val THEME_IVORY = 0      // Default
    const val THEME_MAROON = 0     // Backward compatibility alias
    const val THEME_WHITE = 1
    const val THEME_YELLOW = 2
    const val THEME_GREEN = 3
    const val THEME_CHARCOAL = 4

    // -------------------------------------------------------------
    // CLOCK STYLES (INT PRESERVED)
    // -------------------------------------------------------------
    const val CLOCK_ANALOG = 0
    const val CLOCK_DIGITAL = 1

    private const val PREFS_NAME = "tamil_clock_prefs"
    private const val KEY_THEME = "saved_theme"
    private const val KEY_OPACITY = "widget_opacity"
    private const val KEY_CLOCK_STYLE = "clock_style"

    // -------------------------------------------------------------
    // UNIFIED PALETTE MODEL
    // -------------------------------------------------------------
    data class AppThemePalette(
        val pageBackground: Int,
        val cardBackground: Int,
        val primaryHeader: Int,
        val secondaryHeader: Int,
        val primaryText: Int,
        val secondaryText: Int,
        val accentGold: Int,
        val emptyCellBackground: Int,
        val sundayHighlight: Int,
        val cardBorderColor: Int
    )

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

    fun saveWidgetOpacity(context: Context, opacity: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_OPACITY, opacity.coerceIn(0, 100))
            .apply()
    }

    fun getWidgetOpacity(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_OPACITY, 85).coerceIn(0, 100)
    }

    fun saveClockStyle(context: Context, style: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_CLOCK_STYLE, style)
            .apply()
    }

    fun getClockStyle(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_CLOCK_STYLE, CLOCK_DIGITAL)
    }

    fun getWidgetBaseColor(context: Context): Int {
        return when (getSavedTheme(context)) {
            THEME_WHITE -> Color.parseColor("#FFFFFF")
            THEME_YELLOW -> Color.parseColor("#FFF8DC")
            THEME_GREEN -> Color.parseColor("#EBF7EE")
            THEME_CHARCOAL -> Color.parseColor("#1E1E24")
            else -> Color.parseColor("#FFFDF2")
        }
    }

    /**
     * Complete color palette resolver for Fragments, Views, and Widgets
     */
    fun getThemePalette(context: Context): AppThemePalette {
        return when (getSavedTheme(context)) {
            THEME_WHITE -> AppThemePalette(
                pageBackground = Color.parseColor("#F4F6F8"),
                cardBackground = Color.parseColor("#FFFFFF"),
                primaryHeader = Color.parseColor("#263238"),
                secondaryHeader = Color.parseColor("#455A64"),
                primaryText = Color.parseColor("#102027"),
                secondaryText = Color.parseColor("#546E7A"),
                accentGold = Color.parseColor("#D4AF37"),
                emptyCellBackground = Color.parseColor("#ECEFF1"),
                sundayHighlight = Color.parseColor("#FFEBEE"),
                cardBorderColor = Color.parseColor("#CFD8DC")
            )
            THEME_YELLOW -> AppThemePalette(
                pageBackground = Color.parseColor("#FFFDF0"),
                cardBackground = Color.parseColor("#FFF9E6"),
                primaryHeader = Color.parseColor("#B26A00"),
                secondaryHeader = Color.parseColor("#795548"),
                primaryText = Color.parseColor("#3E2723"),
                secondaryText = Color.parseColor("#5D4037"),
                accentGold = Color.parseColor("#FFB300"),
                emptyCellBackground = Color.parseColor("#FFF3CD"),
                sundayHighlight = Color.parseColor("#FFE0B2"),
                cardBorderColor = Color.parseColor("#FFE082")
            )
            THEME_GREEN -> AppThemePalette(
                pageBackground = Color.parseColor("#EBF3EE"),
                cardBackground = Color.parseColor("#FFFFFF"),
                primaryHeader = Color.parseColor("#1B4D3E"),
                secondaryHeader = Color.parseColor("#2E7D32"),
                primaryText = Color.parseColor("#0A261E"),
                secondaryText = Color.parseColor("#33691E"),
                accentGold = Color.parseColor("#C5A059"),
                emptyCellBackground = Color.parseColor("#D7E5DC"),
                sundayHighlight = Color.parseColor("#FFEBEE"),
                cardBorderColor = Color.parseColor("#A5D6A7")
            )
            THEME_CHARCOAL -> AppThemePalette(
                pageBackground = Color.parseColor("#121216"),
                cardBackground = Color.parseColor("#1E1E24"),
                primaryHeader = Color.parseColor("#FFDF00"),
                secondaryHeader = Color.parseColor("#B0BEC5"),
                primaryText = Color.parseColor("#F5F5F5"),
                secondaryText = Color.parseColor("#B0BEC5"),
                accentGold = Color.parseColor("#FFDF00"),
                emptyCellBackground = Color.parseColor("#2A2A32"),
                sundayHighlight = Color.parseColor("#3A1A1E"),
                cardBorderColor = Color.parseColor("#373742")
            )
            else -> AppThemePalette( // Ivory / Maroon Default
                pageBackground = Color.parseColor("#FAF0F2"),
                cardBackground = Color.parseColor("#FFFFFF"),
                primaryHeader = Color.parseColor("#78000C"),
                secondaryHeader = Color.parseColor("#00695C"),
                primaryText = Color.parseColor("#212121"),
                secondaryText = Color.parseColor("#616161"),
                accentGold = Color.parseColor("#FFDF00"),
                emptyCellBackground = Color.parseColor("#EFE5E7"),
                sundayHighlight = Color.parseColor("#FFEBEE"),
                cardBorderColor = Color.parseColor("#FFCDD2")
            )
        }
    }
}
