package com.example.tamilclock

import android.app.Activity
import android.content.Context
import android.graphics.Color

object ThemeManager {

    private const val PREFS_NAME = "tamil_clock_prefs"

    // Keys
    private const val KEY_APP_BG_COLOR = "app_bg_color"
    private const val KEY_WIDGET_BG_COLOR = "widget_bg_color"
    private const val KEY_WIDGET_OPACITY = "widget_opacity"
    private const val KEY_CLOCK_FACE = "clock_face"
    private const val KEY_DATE_FORMAT_4X2 = "date_format_4x2"
    private const val KEY_DATE_FORMAT_4X4 = "date_format_4x4"
    private const val KEY_ELEMENTS_4X2 = "elements_4x2"
    private const val KEY_ELEMENTS_4X4 = "elements_4x4"

    // App Colors
    const val APP_COLOR_ANTIQUE_SANDAL = "#E8D5B5" // Default
    const val APP_COLOR_BRIGHT_LEMON = "#FFEF00"
    const val APP_COLOR_MIDNIGHT_VIOLET = "#24113D"
    const val APP_COLOR_OFF_WHITE = "#FAFAFA"
    const val APP_COLOR_AMOLED_BLACK = "#000000"

    // Widget Colors
    const val WIDGET_COLOR_VELVET_MAROON = "#65001F" // Default
    const val WIDGET_COLOR_ROYAL_NAVY = "#142B50"
    const val WIDGET_COLOR_MIDNIGHT_VIOLET = "#24113D"
    const val WIDGET_COLOR_OFF_WHITE = "#FAFAFA"
    const val WIDGET_COLOR_AMOLED_BLACK = "#000000"

    // Clock Face
    const val CLOCK_DIGITAL = 0
    const val CLOCK_CLASSIC_VINTAGE = 1

    // Date View Formats
    const val DATE_FORMAT_NUMERIC = 0 // 06 - 11 - 2026
    const val DATE_FORMAT_TEXT = 1    // 06 Nov 26

    // Astrological Custom Elements
    const val ELEM_PAKSHAM = "paksham"
    const val ELEM_THITHI = "thithi"
    const val ELEM_NAKSHATRA = "nakshatra"
    const val ELEM_KARNAM = "karnam"
    const val ELEM_THYAJAM = "thyajam"
    const val ELEM_NALLA_NERAM = "nalla_neram"
    const val ELEM_GOWRI_NALLA_NERAM = "gowri_nalla_neram"
    const val ELEM_SOOLAM = "soolam"
    const val ELEM_SUNRISE_SET = "sunrise_set"

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

    // --- App Background Color Getters/Setters ---
    fun getAppBackgroundColor(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_APP_BG_COLOR, APP_COLOR_ANTIQUE_SANDAL) ?: APP_COLOR_ANTIQUE_SANDAL
    }

    fun saveAppBackgroundColor(context: Context, colorHex: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_APP_BG_COLOR, colorHex).apply()
    }

    // --- Widget Background Color Getters/Setters ---
    fun getWidgetBackgroundColor(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_WIDGET_BG_COLOR, WIDGET_COLOR_VELVET_MAROON) ?: WIDGET_COLOR_VELVET_MAROON
    }

    fun saveWidgetBackgroundColor(context: Context, colorHex: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_WIDGET_BG_COLOR, colorHex).apply()
    }

    // --- Widget Opacity ---
    fun getWidgetOpacity(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_WIDGET_OPACITY, 90).coerceIn(0, 100)
    }

    fun saveWidgetOpacity(context: Context, opacity: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_WIDGET_OPACITY, opacity.coerceIn(0, 100)).apply()
    }

    // --- Clock Face ---
    fun getClockFace(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_CLOCK_FACE, CLOCK_DIGITAL)
    }

    fun saveClockFace(context: Context, face: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_CLOCK_FACE, face).apply()
    }

    // Backward compatibility for existing callers
    fun getSavedTheme(context: Context): Int = 0
    fun getClockStyle(context: Context): Int = getClockFace(context)
    fun saveClockStyle(context: Context, style: Int) = saveClockFace(context, style)
    fun getWidgetBaseColor(context: Context): Int = Color.parseColor(getWidgetBackgroundColor(context))

    // --- Date Format (4x2 & 4x4) ---
    fun getDateFormat4x2(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_DATE_FORMAT_4X2, DATE_FORMAT_NUMERIC)
    }

    fun saveDateFormat4x2(context: Context, format: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_DATE_FORMAT_4X2, format).apply()
    }

    fun getDateFormat4x4(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_DATE_FORMAT_4X4, DATE_FORMAT_NUMERIC)
    }

    fun saveDateFormat4x4(context: Context, format: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_DATE_FORMAT_4X4, format).apply()
    }

    // --- Custom Elements (4x2: 3 elements, 4x4: 5 elements) ---
    fun getElements4x2(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_ELEMENTS_4X2, setOf(ELEM_PAKSHAM, ELEM_THITHI, ELEM_NAKSHATRA))
            ?: setOf(ELEM_PAKSHAM, ELEM_THITHI, ELEM_NAKSHATRA)
    }

    fun saveElements4x2(context: Context, elements: Set<String>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putStringSet(KEY_ELEMENTS_4X2, elements).apply()
    }

    fun getElements4x4(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_ELEMENTS_4X4, setOf(ELEM_PAKSHAM, ELEM_THITHI, ELEM_NAKSHATRA, ELEM_NALLA_NERAM, ELEM_GOWRI_NALLA_NERAM))
            ?: setOf(ELEM_PAKSHAM, ELEM_THITHI, ELEM_NAKSHATRA, ELEM_NALLA_NERAM, ELEM_GOWRI_NALLA_NERAM)
    }

    fun saveElements4x4(context: Context, elements: Set<String>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putStringSet(KEY_ELEMENTS_4X4, elements).apply()
    }

    // Dynamic UI Palette Generator based on App Background Color
    fun getThemePalette(context: Context): AppThemePalette {
        val hex = getAppBackgroundColor(context)
        return when (hex) {
            APP_COLOR_BRIGHT_LEMON -> AppThemePalette(
                pageBackground = Color.parseColor("#FFEF00"),
                cardBackground = Color.parseColor("#FFFDE7"),
                primaryHeader = Color.parseColor("#827717"),
                secondaryHeader = Color.parseColor("#558B2F"),
                primaryText = Color.parseColor("#212121"),
                secondaryText = Color.parseColor("#424242"),
                accentGold = Color.parseColor("#F57F17"),
                emptyCellBackground = Color.parseColor("#FFF9C4"),
                sundayHighlight = Color.parseColor("#FFCDD2"),
                cardBorderColor = Color.parseColor("#FBC02D")
            )
            APP_COLOR_MIDNIGHT_VIOLET -> AppThemePalette(
                pageBackground = Color.parseColor("#24113D"),
                cardBackground = Color.parseColor("#341E52"),
                primaryHeader = Color.parseColor("#E1BEE7"),
                secondaryHeader = Color.parseColor("#CE93D8"),
                primaryText = Color.parseColor("#FFFFFF"),
                secondaryText = Color.parseColor("#E0E0E0"),
                accentGold = Color.parseColor("#FFDF00"),
                emptyCellBackground = Color.parseColor("#1B0B2E"),
                sundayHighlight = Color.parseColor("#4A1525"),
                cardBorderColor = Color.parseColor("#4A2875")
            )
            APP_COLOR_OFF_WHITE -> AppThemePalette(
                pageBackground = Color.parseColor("#FAFAFA"),
                cardBackground = Color.parseColor("#FFFFFF"),
                primaryHeader = Color.parseColor("#212121"),
                secondaryHeader = Color.parseColor("#616161"),
                primaryText = Color.parseColor("#102027"),
                secondaryText = Color.parseColor("#424242"),
                accentGold = Color.parseColor("#D4AF37"),
                emptyCellBackground = Color.parseColor("#EEEEEE"),
                sundayHighlight = Color.parseColor("#FFEBEE"),
                cardBorderColor = Color.parseColor("#E0E0E0")
            )
            APP_COLOR_AMOLED_BLACK -> AppThemePalette(
                pageBackground = Color.parseColor("#000000"),
                cardBackground = Color.parseColor("#121212"),
                primaryHeader = Color.parseColor("#FFDF00"),
                secondaryHeader = Color.parseColor("#B0BEC5"),
                primaryText = Color.parseColor("#FFFFFF"),
                secondaryText = Color.parseColor("#B0BEC5"),
                accentGold = Color.parseColor("#FFDF00"),
                emptyCellBackground = Color.parseColor("#1F1F1F"),
                sundayHighlight = Color.parseColor("#381418"),
                cardBorderColor = Color.parseColor("#2C2C2C")
            )
            else -> AppThemePalette( // Antique Sandal Default (#E8D5B5)
                pageBackground = Color.parseColor("#E8D5B5"),
                cardBackground = Color.parseColor("#F5EBE0"),
                primaryHeader = Color.parseColor("#4E3629"),
                secondaryHeader = Color.parseColor("#6D4C41"),
                primaryText = Color.parseColor("#2D2013"),
                secondaryText = Color.parseColor("#5D4037"),
                accentGold = Color.parseColor("#B26A00"),
                emptyCellBackground = Color.parseColor("#DBC49E"),
                sundayHighlight = Color.parseColor("#F0C2C2"),
                cardBorderColor = Color.parseColor("#D0BCA0")
            )
        }
    }
}
