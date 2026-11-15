package com.example.tamilclock

import android.app.Activity
import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ThemeManager {

    private const val PREFS_NAME = "tamil_clock_prefs"

    // -------------------------------------------------------------
    // LEGACY THEME CONSTANTS
    // -------------------------------------------------------------
    const val THEME_IVORY = 0
    const val THEME_MAROON = 0
    const val THEME_WHITE = 1
    const val THEME_YELLOW = 2
    const val THEME_GREEN = 3
    const val THEME_CHARCOAL = 4

    // -------------------------------------------------------------
    // ITEM 1: APP BACKGROUND COLORS
    // -------------------------------------------------------------
    const val APP_COLOR_ANTIQUE_SANDAL = "#E8D5B5"
    const val APP_COLOR_BRIGHT_LEMON = "#FFEF00"
    const val APP_COLOR_MIDNIGHT_VIOLET = "#24113D"
    const val APP_COLOR_OFF_WHITE = "#FAFAFA"
    const val APP_COLOR_AMOLED_BLACK = "#000000"

    // -------------------------------------------------------------
    // ITEM 2: WIDGET BACKGROUND COLORS (LEGACY)
    // -------------------------------------------------------------
    const val WIDGET_COLOR_VELVET_MAROON = "#65001F"
    const val WIDGET_COLOR_ROYAL_NAVY = "#142B50"
    const val WIDGET_COLOR_MIDNIGHT_VIOLET = "#24113D"
    const val WIDGET_COLOR_OFF_WHITE = "#FAFAFA"
    const val WIDGET_COLOR_AMOLED_BLACK = "#000000"

    // -------------------------------------------------------------
    // CLOCK TYPES & 5 SUB-STYLES
    // -------------------------------------------------------------
    const val CLOCK_TYPE_DIGITAL = 0
    const val CLOCK_TYPE_ANALOG = 1

    const val CLOCK_DIGITAL_CLASSIC = 0       // Digital A: Transparent bg + White/Theme numerical
    const val CLOCK_DIGITAL_AMOLED_NEON = 1   // Digital B: AMOLED Black bg + Neon numerical
    const val CLOCK_ANALOG_CLASSIC = 2        // Analog A: Transparent classic dial + White numerical
    const val CLOCK_ANALOG_AMOLED_WHITE = 3   // Analog B: AMOLED Black dial + White numerical + Seconds hand
    const val CLOCK_ANALOG_AMOLED_HYBRID = 4  // Analog C: AMOLED Black dial + Teal triangles + Seconds + Complications

    // Legacy Aliases
    const val CLOCK_DIGITAL = CLOCK_DIGITAL_CLASSIC
    const val CLOCK_ANALOG = CLOCK_ANALOG_CLASSIC
    const val CLOCK_CLASSIC_VINTAGE = CLOCK_ANALOG_CLASSIC

    // -------------------------------------------------------------
    // SELECTABLE DATE VIEW FORMATS
    // -------------------------------------------------------------
    const val DATE_FORMAT_DASH = 0   // 14 - 11 - 2026 (dd - MM - yyyy)
    const val DATE_FORMAT_TEXT = 1   // 14 Nov 2026    (dd MMM yyyy)
    const val DATE_FORMAT_SLASH = 2  // 14/11/2026     (dd/MM/yyyy)
    const val DATE_FORMAT_NUMERIC = DATE_FORMAT_DASH

    // -------------------------------------------------------------
    // ASTROLOGICAL ELEMENTS KEYS
    // -------------------------------------------------------------
    const val ELEM_PAKSHAM = "paksham"
    const val ELEM_THITHI = "thithi"
    const val ELEM_NAKSHATRA = "nakshatra"
    const val ELEM_AMIRTHATHI_YOGA = "amirthathi_yoga"
    const val ELEM_NALLA_NERAM = "nalla_neram"
    const val ELEM_GOWRI_NALLA_NERAM = "gowri_nalla_neram"
    const val ELEM_RAHU_KALAM = "rahu_kalam"
    const val ELEM_YEMAKANDAM = "yemakandam"
    const val ELEM_KULIGAI = "kuligai"
    const val ELEM_SUNRISE_SET = "sunrise_sunset"
    const val ELEM_CHANDRASTAMA = "chandrastama"
    const val ELEM_FESTIVAL = "festival"

    val DEFAULT_ELEMENTS_4X2 = setOf(ELEM_PAKSHAM, ELEM_THITHI, ELEM_NAKSHATRA)
    val DEFAULT_ELEMENTS_4X4 = setOf(
        ELEM_PAKSHAM,
        ELEM_THITHI,
        ELEM_NAKSHATRA,
        ELEM_AMIRTHATHI_YOGA,
        ELEM_NALLA_NERAM
    )

    // -------------------------------------------------------------
    // PREFERENCE KEYS
    // -------------------------------------------------------------
    private const val KEY_THEME = "saved_theme"
    private const val KEY_APP_BG_COLOR = "app_bg_color"
    private const val KEY_WIDGET_BG_COLOR = "widget_bg_color"
    private const val KEY_WIDGET_OPACITY = "widget_opacity"
    private const val KEY_CLOCK_STYLE = "clock_style"
    private const val KEY_CLOCK_FACE = "clock_face"
    private const val KEY_WIDGET_DATE_FORMAT = "widget_date_format"
    private const val KEY_DATE_FORMAT_4X2 = "date_format_4x2"
    private const val KEY_DATE_FORMAT_4X4 = "date_format_4x4"
    private const val KEY_ELEMENTS_4X2 = "elements_4x2"
    private const val KEY_ELEMENTS_4X4 = "elements_4x4"

    private const val KEY_PRESET_ID = "selected_preset_id"
    private const val KEY_BORDER_ENABLED = "outer_border_enabled"
    const val SHADOW_THRESHOLD_PERCENT = 30

    enum class Preset(
        val id: String,
        val displayName: String,
        @ColorRes val bgResId: Int,
        @ColorRes val accentResId: Int
    ) {
        MIDNIGHT_GOLD("midnight_gold", "Midnight Gold", R.color.widget_canvas_black, R.color.widget_accent_gold),
        HERITAGE_MAROON("heritage_maroon", "Heritage Maroon", R.color.widget_canvas_maroon, R.color.widget_accent_gold),
        CLASSIC_MONO("classic_mono", "Classic Mono", R.color.widget_canvas_black, R.color.widget_accent_white),
        NEON_NIGHT("neon_night", "Neon Night", R.color.widget_canvas_black, R.color.widget_accent_neon_green),
        PORCELAIN_BLUE("porcelain_blue", "Porcelain Blue", R.color.widget_canvas_white, R.color.widget_accent_royal_blue),
        PORCELAIN_RED("porcelain_red", "Porcelain Red", R.color.widget_canvas_white, R.color.widget_accent_crimson);

        companion object {
            fun fromId(id: String?): Preset = entries.firstOrNull { it.id == id } ?: MIDNIGHT_GOLD
        }
    }

    data class ThemeConfig(
        val preset: Preset,
        val baseBackgroundColor: Int,
        val resolvedBackgroundColor: Int,
        val accentColor: Int,
        val opacityPercent: Int,
        val isBorderEnabled: Boolean,
        val requiresLegibilityShadow: Boolean
    )

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

    fun savePreset(context: Context, preset: Preset) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_PRESET_ID, preset.id).apply()
    }

    fun getSelectedPreset(context: Context): Preset {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return Preset.fromId(prefs.getString(KEY_PRESET_ID, Preset.MIDNIGHT_GOLD.id))
    }

    fun isBorderEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_BORDER_ENABLED, true)
    }

    fun saveBorderEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_BORDER_ENABLED, enabled).apply()
    }

    fun getActiveTheme(context: Context): ThemeConfig {
        val preset = getSelectedPreset(context)
        val opacity = getWidgetOpacity(context)
        val borderEnabled = isBorderEnabled(context)

        val rawBgColor = ContextCompat.getColor(context, preset.bgResId)
        val accentColor = ContextCompat.getColor(context, preset.accentResId)

        val alpha = (opacity * 255) / 100
        val resolvedBg = Color.argb(
            alpha,
            Color.red(rawBgColor),
            Color.green(rawBgColor),
            Color.blue(rawBgColor)
        )

        return ThemeConfig(
            preset = preset,
            baseBackgroundColor = rawBgColor,
            resolvedBackgroundColor = resolvedBg,
            accentColor = accentColor,
            opacityPercent = opacity,
            isBorderEnabled = borderEnabled,
            requiresLegibilityShadow = opacity < SHADOW_THRESHOLD_PERCENT
        )
    }

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
            .edit().putInt(KEY_THEME, themeIndex).apply()
    }

    fun getSavedTheme(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_THEME, THEME_IVORY)
    }

    fun getAppBackgroundColor(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_APP_BG_COLOR, APP_COLOR_ANTIQUE_SANDAL) ?: APP_COLOR_ANTIQUE_SANDAL
    }

    fun saveAppBackgroundColor(context: Context, colorHex: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_APP_BG_COLOR, colorHex).apply()
    }

    fun getWidgetBackgroundColor(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_WIDGET_BG_COLOR, WIDGET_COLOR_VELVET_MAROON) ?: WIDGET_COLOR_VELVET_MAROON
    }

    fun saveWidgetBackgroundColor(context: Context, colorHex: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_WIDGET_BG_COLOR, colorHex).apply()
    }

    fun getWidgetOpacity(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_WIDGET_OPACITY, 90).coerceIn(0, 100)
    }

    fun saveWidgetOpacity(context: Context, opacity: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_WIDGET_OPACITY, opacity.coerceIn(0, 100)).apply()
    }

    // -------------------------------------------------------------
    // CLOCK FACE GETTERS & SETTERS (5 STYLES)
    // -------------------------------------------------------------
    fun getClockFace(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_CLOCK_FACE, prefs.getInt(KEY_CLOCK_STYLE, CLOCK_DIGITAL_CLASSIC))
    }

    fun saveClockFace(context: Context, face: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_CLOCK_FACE, face)
            .putInt(KEY_CLOCK_STYLE, face)
            .apply()
    }

    fun isAnalogClockSelected(context: Context): Boolean {
        val face = getClockFace(context)
        return face == CLOCK_ANALOG_CLASSIC ||
               face == CLOCK_ANALOG_AMOLED_WHITE ||
               face == CLOCK_ANALOG_AMOLED_HYBRID
    }

    fun getClockStyle(context: Context): Int = getClockFace(context)
    fun saveClockStyle(context: Context, style: Int) = saveClockFace(context, style)

    // -------------------------------------------------------------
    // UNIFIED DATE FORMAT GETTERS & SETTERS (3 FORMATS)
    // -------------------------------------------------------------
    fun getWidgetDateFormat(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_WIDGET_DATE_FORMAT, prefs.getInt(KEY_DATE_FORMAT_4X2, DATE_FORMAT_DASH))
    }

    fun saveWidgetDateFormat(context: Context, format: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_WIDGET_DATE_FORMAT, format)
            .putInt(KEY_DATE_FORMAT_4X2, format)
            .putInt(KEY_DATE_FORMAT_4X4, format)
            .apply()
    }

    fun formatDateByPattern(cal: Calendar, formatType: Int): String {
        return when (formatType) {
            DATE_FORMAT_TEXT -> SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(cal.time)
            DATE_FORMAT_SLASH -> SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(cal.time)
            else -> SimpleDateFormat("dd - MM - yyyy", Locale.ENGLISH).format(cal.time)
        }
    }

    fun getDateFormat4x2(context: Context): Int = getWidgetDateFormat(context)
    fun saveDateFormat4x2(context: Context, format: Int) = saveWidgetDateFormat(context, format)
    fun getDateFormat4x4(context: Context): Int = getWidgetDateFormat(context)
    fun saveDateFormat4x4(context: Context, format: Int) = saveWidgetDateFormat(context, format)

    // 4x2 & 4x4 Custom elements
    fun getElements4x2(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_ELEMENTS_4X2, DEFAULT_ELEMENTS_4X2) ?: DEFAULT_ELEMENTS_4X2
    }

    fun saveElements4x2(context: Context, elements: Set<String>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putStringSet(KEY_ELEMENTS_4X2, elements).apply()
    }

    fun getElements4x4(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_ELEMENTS_4X4, DEFAULT_ELEMENTS_4X4) ?: DEFAULT_ELEMENTS_4X4
    }

    fun saveElements4x4(context: Context, elements: Set<String>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putStringSet(KEY_ELEMENTS_4X4, elements).apply()
    }

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
            else -> AppThemePalette(
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
