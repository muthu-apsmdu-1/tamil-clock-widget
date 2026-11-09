package com.example.tamilclock

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.*
import android.view.View
import android.widget.RemoteViews
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object WidgetUpdateHelper {

    // -------------------------------------------------------------
    // PENDING INTENT BUILDER
    // -------------------------------------------------------------
    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // -------------------------------------------------------------
    // SAFE VIEW BINDING HELPERS
    // -------------------------------------------------------------
    private fun RemoteViews.safeSetText(context: Context, idName: String, text: CharSequence) {
        val resId = context.resources.getIdentifier(idName, "id", context.packageName)
        if (resId != 0) setTextViewText(resId, text)
    }

    private fun RemoteViews.safeSetTextColor(context: Context, idName: String, color: Int) {
        val resId = context.resources.getIdentifier(idName, "id", context.packageName)
        if (resId != 0) setTextColor(resId, color)
    }

    private fun RemoteViews.safeSetVisibility(context: Context, idName: String, visibility: Int) {
        val resId = context.resources.getIdentifier(idName, "id", context.packageName)
        if (resId != 0) setViewVisibility(resId, visibility)
    }

    private fun RemoteViews.safeSetImageBitmap(context: Context, idName: String, bitmap: Bitmap) {
        val resId = context.resources.getIdentifier(idName, "id", context.packageName)
        if (resId != 0) setImageViewBitmap(resId, bitmap)
    }

    // -------------------------------------------------------------
    // COLOR RESOLUTION FOR WIDGETS
    // -------------------------------------------------------------
    data class WidgetColorPalette(
        val isLightWidget: Boolean,
        val primaryText: Int,
        val secondaryText: Int,
        val accentColor: Int
    )

    private fun resolveWidgetColors(context: Context, themeConfig: ThemeManager.ThemeConfig): WidgetColorPalette {
        val isWhiteCanvas = themeConfig.preset == ThemeManager.Preset.PORCELAIN_BLUE ||
                themeConfig.preset == ThemeManager.Preset.PORCELAIN_RED

        return if (isWhiteCanvas) {
            WidgetColorPalette(
                isLightWidget = true,
                primaryText = themeConfig.accentColor,
                secondaryText = Color.parseColor("#424242"),
                accentColor = themeConfig.accentColor
            )
        } else {
            WidgetColorPalette(
                isLightWidget = false,
                primaryText = Color.WHITE,
                secondaryText = Color.parseColor("#E0E0E0"),
                accentColor = themeConfig.accentColor
            )
        }
    }

    // -------------------------------------------------------------
    // DYNAMIC BACKGROUND BITMAP GENERATOR (COLOR + OPACITY + BORDER)
    // -------------------------------------------------------------
    @JvmOverloads
    fun createWidgetBackgroundBitmap(
        context: Context,
        widthPx: Int,
        heightPx: Int,
        themeConfig: ThemeManager.ThemeConfig = ThemeManager.getActiveTheme(context)
    ): Bitmap {
        val w = widthPx.coerceAtLeast(10)
        val h = heightPx.coerceAtLeast(10)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val cornerRadius = 32f
        val rect = RectF(4f, 4f, (w - 4).toFloat(), (h - 4).toFloat())

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = themeConfig.resolvedBackgroundColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)

        if (themeConfig.isBorderEnabled) {
            val borderAlpha = ((themeConfig.opacityPercent / 100f).coerceAtLeast(0.4f) * 255).toInt()
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = themeConfig.accentColor
                alpha = borderAlpha
                style = Paint.Style.STROKE
                strokeWidth = 3.5f
            }
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)
        }

        return bitmap
    }

    private fun formatEnglishDate(cal: Calendar, formatType: Int): String {
        return if (formatType == ThemeManager.DATE_FORMAT_TEXT) {
            SimpleDateFormat("dd MMM yy", Locale.ENGLISH).format(cal.time)
        } else {
            SimpleDateFormat("dd - MM - yyyy", Locale.ENGLISH).format(cal.time)
        }
    }

    fun convertPakshamName(rawPaksham: String): String {
        return when {
            rawPaksham.contains("சுக்ல") || rawPaksham.contains("வளர்பிறை") -> "வளர்"
            rawPaksham.contains("கிருஷ்ண") || rawPaksham.contains("தேய்பிறை") -> "தேய்"
            rawPaksham.isBlank() -> "---"
            else -> rawPaksham.trim()
        }
    }

    // -------------------------------------------------------------
    // UNIFIED PANCHANG DATA HOLDER FOR WIDGETS
    // -------------------------------------------------------------
    data class WidgetPanchangData(
        var tamilYear: String = "பராபவ",
        var tamilMonth: String = "",
        var tamilDate: Int = 0,
        var weekdayTa: String = "",
        var paksham: String = "",
        var thithi: String = "",
        var thithiTime: String = "",
        var nakshatra: String = "",
        var nakshatraTime: String = "",
        var yogam: String = "",
        var nallaNeram: String = "",
        var gowriNallaNeram: String = "",
        var rahuKalam: String = "",
        var emakandam: String = "",
        var kuligai: String = "",
        var sunrise: String = "",
        var sunset: String = "",
        var chandrashtamam: String = "",
        var festival: String = ""
    )

    // Helper to open existing copied SQLite databases safely
    private fun openDatabaseSafely(context: Context, dbName: String): SQLiteDatabase? {
        val dbFile = context.getDatabasePath(dbName)
        if (dbFile.exists()) {
            return SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        }
        val databasesDir = File(context.applicationInfo.dataDir, "databases")
        val altFile = File(databasesDir, dbName)
        if (altFile.exists()) {
            return SQLiteDatabase.openDatabase(altFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        }
        return null
    }

    // Queries panchang_clcon.db, auspicious_periods.db, and sun_moon_timings.db directly
    fun loadUnifiedWidgetData(context: Context, queryDate: String): WidgetPanchangData {
        val result = WidgetPanchangData()

        // 1. Fetch from panchang_clcon.db
        try {
            openDatabaseSafely(context, "panchang_clcon.db")?.use { db ->
                val cursor = db.rawQuery(
                    "SELECT tamil_year, tamil_month, tamil_date, weekday_ta, paksha, tithi_name, tithi_time, nakshatra_name, nakshatra_time, amritadi_yoga, chandrashtamam_rasi, festivals_events FROM panchangam_daily WHERE gregorian_date = ? LIMIT 1",
                    arrayOf(queryDate)
                )
                if (cursor.moveToFirst()) {
                    result.tamilYear = cursor.getString(0) ?: "பராபவ"
                    result.tamilMonth = cursor.getString(1) ?: ""
                    result.tamilDate = cursor.getInt(2)
                    result.weekdayTa = cursor.getString(3) ?: ""
                    result.paksham = cursor.getString(4) ?: ""
                    result.thithi = cursor.getString(5) ?: ""
                    result.thithiTime = cursor.getString(6) ?: ""
                    result.nakshatra = cursor.getString(7) ?: ""
                    result.nakshatraTime = cursor.getString(8) ?: ""
                    result.yogam = cursor.getString(9) ?: ""
                    result.chandrashtamam = cursor.getString(10) ?: ""
                    result.festival = cursor.getString(11) ?: ""
                }
                cursor.close()
            }
        } catch (_: Exception) {}

        // Fallback for weekday if panchang record wasn't found
        if (result.weekdayTa.isBlank()) {
            val cal = Calendar.getInstance()
            result.weekdayTa = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "ஞாயிறு"
                Calendar.MONDAY -> "திங்கள்"
                Calendar.TUESDAY -> "செவ்வாய்"
                Calendar.WEDNESDAY -> "புதன்"
                Calendar.THURSDAY -> "வியாழன்"
                Calendar.FRIDAY -> "வெள்ளி"
                Calendar.SATURDAY -> "சனி"
                else -> ""
            }
        }

        // 2. Fetch Auspicious Timings from auspicious_periods.db (Table: weekday_timings_summary)
        try {
            openDatabaseSafely(context, "auspicious_periods.db")?.use { db ->
                val cursor = db.rawQuery(
                    "SELECT nalla_neram_morning_start, nalla_neram_morning_end, nalla_neram_evening_start, nalla_neram_evening_end, gowri_day_start, gowri_day_end, gowri_night_start, gowri_night_end, raghu_start, raghu_end, emagandam_start, emagandam_end, kuligai_start, kuligai_end FROM weekday_timings_summary WHERE weekday_ta = ? LIMIT 1",
                    arrayOf(result.weekdayTa)
                )
                if (cursor.moveToFirst()) {
                    val nStartM = cursor.getString(0) ?: ""
                    val nEndM = cursor.getString(1) ?: ""
                    val nStartE = cursor.getString(2) ?: ""
                    val nEndE = cursor.getString(3) ?: ""
                    val mornStr = if (nStartM.isNotBlank() && nEndM.isNotBlank()) "$nStartM-$nEndM" else ""
                    val eveStr = if (nStartE.isNotBlank() && nEndE.isNotBlank()) "$nStartE-$nEndE" else ""
                    result.nallaNeram = when {
                        mornStr.isNotBlank() && eveStr.isNotBlank() -> "$mornStr, $eveStr"
                        mornStr.isNotBlank() -> mornStr
                        else -> eveStr
                    }

                    val gStartD = cursor.getString(4) ?: ""
                    val gEndD = cursor.getString(5) ?: ""
                    val gStartN = cursor.getString(6) ?: ""
                    val gEndN = cursor.getString(7) ?: ""
                    val gDay = if (gStartD.isNotBlank() && gEndD.isNotBlank()) "$gStartD-$gEndD" else ""
                    val gNight = if (gStartN.isNotBlank() && gEndN.isNotBlank()) "$gStartN-$gEndN" else ""
                    result.gowriNallaNeram = when {
                        gDay.isNotBlank() && gNight.isNotBlank() -> "$gDay, $gNight"
                        gDay.isNotBlank() -> gDay
                        else -> gNight
                    }

                    val rStart = cursor.getString(8) ?: ""
                    val rEnd = cursor.getString(9) ?: ""
                    result.rahuKalam = if (rStart.isNotBlank() && rEnd.isNotBlank()) "$rStart - $rEnd" else ""

                    val eStart = cursor.getString(10) ?: ""
                    val eEnd = cursor.getString(11) ?: ""
                    result.emakandam = if (eStart.isNotBlank() && eEnd.isNotBlank()) "$eStart - $eEnd" else ""

                    val kStart = cursor.getString(12) ?: ""
                    val kEnd = cursor.getString(13) ?: ""
                    result.kuligai = if (kStart.isNotBlank() && kEnd.isNotBlank()) "$kStart - $kEnd" else ""
                }
                cursor.close()
            }
        } catch (_: Exception) {}

        // 3. Fetch Sun Timings from sun_moon_timings.db (Table: sun_moon_timings)
        try {
            openDatabaseSafely(context, "sun_moon_timings.db")?.use { db ->
                val cursor = db.rawQuery(
                    "SELECT sunrise, sunset FROM sun_moon_timings WHERE date_gregorian = ? LIMIT 1",
                    arrayOf(queryDate)
                )
                if (cursor.moveToFirst()) {
                    result.sunrise = cursor.getString(0) ?: ""
                    result.sunset = cursor.getString(1) ?: ""
                }
                cursor.close()
            }
        } catch (_: Exception) {}

        return result
    }

    // -------------------------------------------------------------
    // ASTROLOGICAL ELEMENT RESOLVER (12 PRIORITIZED ELEMENTS)
    // -------------------------------------------------------------
    private fun resolveElement(key: String, data: WidgetPanchangData): Pair<String, String> {
        return when (key) {
            ThemeManager.ELEM_PAKSHAM -> {
                val converted = convertPakshamName(data.paksham)
                Pair("பக்ஷம்", if (converted.isNotBlank()) converted else "—")
            }

            ThemeManager.ELEM_THITHI -> {
                val tName = data.thithi.trim()
                val tTime = data.thithiTime.trim()
                val text = when {
                    tName.isNotBlank() && tTime.isNotBlank() -> "$tName ($tTime)"
                    tName.isNotBlank() -> tName
                    else -> "—"
                }
                Pair("திதி", text)
            }

            ThemeManager.ELEM_NAKSHATRA -> {
                val nName = data.nakshatra.trim()
                val nTime = data.nakshatraTime.trim()
                val text = when {
                    nName.isNotBlank() && nTime.isNotBlank() -> "$nName ($nTime)"
                    nName.isNotBlank() -> nName
                    else -> "—"
                }
                Pair("நட்சத்திரம்", text)
            }

            ThemeManager.ELEM_AMIRTHATHI_YOGA -> {
                Pair("யோகம்", data.yogam.ifBlank { "—" })
            }

            ThemeManager.ELEM_NALLA_NERAM -> {
                Pair("நல்ல நேரம்", data.nallaNeram.ifBlank { "—" })
            }

            ThemeManager.ELEM_GOWRI_NALLA_NERAM -> {
                Pair("கௌரி", data.gowriNallaNeram.ifBlank { "—" })
            }

            ThemeManager.ELEM_RAHU_KALAM -> {
                Pair("ராகு காலம்", data.rahuKalam.ifBlank { "—" })
            }

            ThemeManager.ELEM_YEMAKANDAM -> {
                Pair("எமகண்டம்", data.emakandam.ifBlank { "—" })
            }

            ThemeManager.ELEM_KULIGAI -> {
                Pair("குளிகை", data.kuligai.ifBlank { "—" })
            }

            ThemeManager.ELEM_SUNRISE_SET -> {
                val rise = data.sunrise.trim()
                val set = data.sunset.trim()
                val text = when {
                    rise.isNotBlank() && set.isNotBlank() -> "உதயம்: $rise | மறைவு: $set"
                    rise.isNotBlank() -> "உதயம்: $rise"
                    else -> "—"
                }
                Pair("சூரியன்", text)
            }

            ThemeManager.ELEM_CHANDRASTAMA -> {
                Pair("சந்திராஷ்டமம்", data.chandrashtamam.ifBlank { "இல்லை" })
            }

            ThemeManager.ELEM_FESTIVAL -> {
                Pair("சிறப்பு", data.festival.ifBlank { "—" })
            }

            else -> Pair("", "")
        }
    }

    // -------------------------------------------------------------
    // 1. UPDATE 2x1 WIDGET
    // -------------------------------------------------------------
    @JvmOverloads
    fun update2x1(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray? = null
    ) {
        val layoutId = context.resources.getIdentifier("widget_layout_2x1", "layout", context.packageName)
        if (layoutId == 0) return
        val views = RemoteViews(context.packageName, layoutId)

        val cal = Calendar.getInstance()
        val queryDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(cal.time)
        val data = loadUnifiedWidgetData(context, queryDate)

        val themeConfig = ThemeManager.getActiveTheme(context)
        val bgBitmap = createWidgetBackgroundBitmap(context, 320, 160, themeConfig)
        views.safeSetImageBitmap(context, "widget_bg_image", bgBitmap)

        val colors = resolveWidgetColors(context, themeConfig)

        val clockFace = ThemeManager.getClockFace(context)
        if (clockFace == ThemeManager.CLOCK_ANALOG) {
            views.safeSetVisibility(context, "analog_clock_view", View.VISIBLE)
            views.safeSetVisibility(context, "container_digital_clock", View.GONE)
            views.safeSetVisibility(context, "tv_digital_clock", View.GONE)
        } else {
            views.safeSetVisibility(context, "analog_clock_view", View.GONE)
            views.safeSetVisibility(context, "container_digital_clock", View.VISIBLE)
            views.safeSetVisibility(context, "tv_digital_clock", View.VISIBLE)
            views.safeSetTextColor(context, "tv_digital_clock", colors.primaryText)
            views.safeSetTextColor(context, "tv_digital_clock_ampm", colors.primaryText)
        }

        val dateFormat = ThemeManager.getDateFormat4x2(context)
        val gregDateStr = formatEnglishDate(cal, dateFormat)
        views.safeSetText(context, "tv_widget_greg_date", gregDateStr)
        views.safeSetTextColor(context, "tv_widget_greg_date", colors.primaryText)

        val tMonth = if (data.tamilMonth.isNotBlank()) data.tamilMonth else "---"
        val tDay = if (data.tamilDate > 0) data.tamilDate.toString() else "---"
        views.safeSetText(context, "tv_widget_tamil_month_day", "$tMonth $tDay")
        views.safeSetTextColor(context, "tv_widget_tamil_month_day", colors.accentColor)

        val rootId = context.resources.getIdentifier("widget_root", "id", context.packageName)
        if (rootId != 0) {
            views.setOnClickPendingIntent(rootId, getPendingIntent(context))
        }

        val targetIds = if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
            appWidgetIds
        } else {
            manager.getAppWidgetIds(ComponentName(context, WidgetProvider2x1::class.java))
        }

        if (targetIds != null && targetIds.isNotEmpty()) {
            manager.updateAppWidget(targetIds, views)
        }
    }

    // -------------------------------------------------------------
    // 2. UPDATE 4x1 WIDGET
    // -------------------------------------------------------------
    @JvmOverloads
    fun update4x1(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray? = null
    ) {
        val layoutId = context.resources.getIdentifier("widget_layout_4x1", "layout", context.packageName)
        if (layoutId == 0) return
        val views = RemoteViews(context.packageName, layoutId)

        val cal = Calendar.getInstance()
        val queryDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(cal.time)
        val data = loadUnifiedWidgetData(context, queryDate)

        val themeConfig = ThemeManager.getActiveTheme(context)
        val bgBitmap = createWidgetBackgroundBitmap(context, 580, 140, themeConfig)
        views.safeSetImageBitmap(context, "widget_bg_image", bgBitmap)

        val colors = resolveWidgetColors(context, themeConfig)

        val clockFace = ThemeManager.getClockFace(context)
        if (clockFace == ThemeManager.CLOCK_ANALOG) {
            views.safeSetVisibility(context, "analog_clock_view", View.VISIBLE)
            views.safeSetVisibility(context, "container_digital_clock", View.GONE)
            views.safeSetVisibility(context, "tv_digital_clock", View.GONE)
        } else {
            views.safeSetVisibility(context, "analog_clock_view", View.GONE)
            views.safeSetVisibility(context, "container_digital_clock", View.VISIBLE)
            views.safeSetVisibility(context, "tv_digital_clock", View.VISIBLE)
            views.safeSetTextColor(context, "tv_digital_clock", colors.primaryText)
            views.safeSetTextColor(context, "tv_digital_clock_ampm", colors.primaryText)
        }

        val dateFormat = ThemeManager.getDateFormat4x2(context)
        val gregDateStr = formatEnglishDate(cal, dateFormat)
        views.safeSetText(context, "tv_widget_greg_date", gregDateStr)
        views.safeSetTextColor(context, "tv_widget_greg_date", colors.primaryText)

        val weekdayStr = if (data.weekdayTa.isNotBlank()) data.weekdayTa else "---"
        views.safeSetText(context, "tv_widget_weekday", weekdayStr)
        views.safeSetTextColor(context, "tv_widget_weekday", colors.accentColor)

        val tMonth = if (data.tamilMonth.isNotBlank()) data.tamilMonth else "---"
        val tDay = if (data.tamilDate > 0) data.tamilDate.toString() else "---"
        views.safeSetText(context, "tv_widget_tamil_date", "$tMonth $tDay")
        views.safeSetTextColor(context, "tv_widget_tamil_date", colors.accentColor)

        val convertedPaksham = convertPakshamName(data.paksham)
        val thithiStr = if (data.thithi.isNotBlank()) data.thithi else "---"
        views.safeSetText(context, "tv_widget_paksham_thithi", "$convertedPaksham • $thithiStr")
        views.safeSetTextColor(context, "tv_widget_paksham_thithi", colors.secondaryText)

        val rootId = context.resources.getIdentifier("widget_root", "id", context.packageName)
        if (rootId != 0) {
            views.setOnClickPendingIntent(rootId, getPendingIntent(context))
        }

        val targetIds = if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
            appWidgetIds
        } else {
            manager.getAppWidgetIds(ComponentName(context, WidgetProvider4x1::class.java))
        }

        if (targetIds != null && targetIds.isNotEmpty()) {
            manager.updateAppWidget(targetIds, views)
        }
    }

    // -------------------------------------------------------------
    // 3. UPDATE 4x2 WIDGET
    // -------------------------------------------------------------
    @JvmOverloads
    fun update4x2(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray? = null
    ) {
        val layoutId = context.resources.getIdentifier("widget_layout_4x2", "layout", context.packageName)
        if (layoutId == 0) return
        val views = RemoteViews(context.packageName, layoutId)

        val cal = Calendar.getInstance()
        val queryDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(cal.time)
        val data = loadUnifiedWidgetData(context, queryDate)

        val themeConfig = ThemeManager.getActiveTheme(context)
        val bgBitmap = createWidgetBackgroundBitmap(context, 580, 280, themeConfig)
        views.safeSetImageBitmap(context, "widget_bg_image", bgBitmap)

        val colors = resolveWidgetColors(context, themeConfig)

        val clockFace = ThemeManager.getClockFace(context)
        if (clockFace == ThemeManager.CLOCK_ANALOG) {
            views.safeSetVisibility(context, "analog_clock_view", View.VISIBLE)
            views.safeSetVisibility(context, "container_digital_clock", View.GONE)
            views.safeSetVisibility(context, "tv_digital_clock", View.GONE)
        } else {
            views.safeSetVisibility(context, "analog_clock_view", View.GONE)
            views.safeSetVisibility(context, "container_digital_clock", View.VISIBLE)
            views.safeSetVisibility(context, "tv_digital_clock", View.VISIBLE)
            views.safeSetTextColor(context, "tv_digital_clock", colors.primaryText)
            views.safeSetTextColor(context, "tv_digital_clock_ampm", colors.primaryText)
        }

        val dateFormat = ThemeManager.getDateFormat4x2(context)
        val gregDateStr = formatEnglishDate(cal, dateFormat)
        views.safeSetText(context, "tv_widget_greg_date", gregDateStr)
        views.safeSetTextColor(context, "tv_widget_greg_date", colors.primaryText)

        val weekdayStr = if (data.weekdayTa.isNotBlank()) data.weekdayTa else "---"
        views.safeSetText(context, "tv_widget_weekday", weekdayStr)
        views.safeSetTextColor(context, "tv_widget_weekday", colors.accentColor)

        val tMonth = if (data.tamilMonth.isNotBlank()) data.tamilMonth else "---"
        val tDay = if (data.tamilDate > 0) data.tamilDate.toString() else "---"
        views.safeSetText(context, "tv_widget_tamil_date", "$tMonth $tDay")
        views.safeSetTextColor(context, "tv_widget_tamil_date", colors.accentColor)

        val selectedKeys = ThemeManager.getElements4x2(context).toList()
        val elem1 = resolveElement(selectedKeys.getOrNull(0) ?: ThemeManager.ELEM_PAKSHAM, data)
        val elem2 = resolveElement(selectedKeys.getOrNull(1) ?: ThemeManager.ELEM_THITHI, data)
        val elem3 = resolveElement(selectedKeys.getOrNull(2) ?: ThemeManager.ELEM_NAKSHATRA, data)

        views.safeSetText(context, "tv_widget_elem1_label", "${elem1.first} :")
        views.safeSetTextColor(context, "tv_widget_elem1_label", colors.accentColor)
        views.safeSetText(context, "tv_widget_elem1_val", elem1.second)
        views.safeSetTextColor(context, "tv_widget_elem1_val", colors.primaryText)

        views.safeSetText(context, "tv_widget_elem2_label", "${elem2.first} :")
        views.safeSetTextColor(context, "tv_widget_elem2_label", colors.accentColor)
        views.safeSetText(context, "tv_widget_elem2_val", elem2.second)
        views.safeSetTextColor(context, "tv_widget_elem2_val", colors.primaryText)

        views.safeSetText(context, "tv_widget_elem3_label", "${elem3.first} :")
        views.safeSetTextColor(context, "tv_widget_elem3_label", colors.accentColor)
        views.safeSetText(context, "tv_widget_elem3_val", elem3.second)
        views.safeSetTextColor(context, "tv_widget_elem3_val", colors.primaryText)

        val rootId = context.resources.getIdentifier("widget_root", "id", context.packageName)
        if (rootId != 0) {
            views.setOnClickPendingIntent(rootId, getPendingIntent(context))
        }

        val targetIds = if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
            appWidgetIds
        } else {
            manager.getAppWidgetIds(ComponentName(context, WidgetProvider4x2::class.java))
        }

        if (targetIds != null && targetIds.isNotEmpty()) {
            manager.updateAppWidget(targetIds, views)
        }
    }

    // -------------------------------------------------------------
    // 4. UPDATE 4x4 WIDGET
    // -------------------------------------------------------------
    @JvmOverloads
    fun update4x4(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray? = null
    ) {
        val layoutId = context.resources.getIdentifier("widget_layout_4x4", "layout", context.packageName)
        if (layoutId == 0) return
        val views = RemoteViews(context.packageName, layoutId)

        val cal = Calendar.getInstance()
        val queryDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(cal.time)
        val data = loadUnifiedWidgetData(context, queryDate)

        val themeConfig = ThemeManager.getActiveTheme(context)
        val bgBitmap = createWidgetBackgroundBitmap(context, 580, 580, themeConfig)
        views.safeSetImageBitmap(context, "widget_bg_image", bgBitmap)

        val colors = resolveWidgetColors(context, themeConfig)

        val clockFace = ThemeManager.getClockFace(context)
        if (clockFace == ThemeManager.CLOCK_ANALOG) {
            views.safeSetVisibility(context, "analog_clock_view", View.VISIBLE)
            views.safeSetVisibility(context, "container_digital_clock", View.GONE)
            views.safeSetVisibility(context, "tv_digital_clock", View.GONE)
        } else {
            views.safeSetVisibility(context, "analog_clock_view", View.GONE)
            views.safeSetVisibility(context, "container_digital_clock", View.VISIBLE)
            views.safeSetVisibility(context, "tv_digital_clock", View.VISIBLE)
            views.safeSetTextColor(context, "tv_digital_clock", colors.primaryText)
            views.safeSetTextColor(context, "tv_digital_clock_ampm", colors.primaryText)
        }

        val dateFormat = ThemeManager.getDateFormat4x4(context)
        views.safeSetText(context, "tv_widget_greg_date", formatEnglishDate(cal, dateFormat))
        views.safeSetTextColor(context, "tv_widget_greg_date", colors.primaryText)

        views.safeSetTextColor(context, "tv_widget_fixed_sep1", colors.accentColor)
        views.safeSetText(context, "tv_widget_weekday", data.weekdayTa)
        views.safeSetTextColor(context, "tv_widget_weekday", colors.accentColor)

        views.safeSetTextColor(context, "tv_widget_fixed_sep2", colors.accentColor)
        val tDateStr = if (data.tamilDate > 0) data.tamilDate.toString() else "---"
        views.safeSetText(context, "tv_widget_tamil_full", "${data.tamilYear} • ${data.tamilMonth} $tDateStr")
        views.safeSetTextColor(context, "tv_widget_tamil_full", colors.accentColor)

        val selectedKeys = ThemeManager.getElements4x4(context).toList()
        val elem1 = resolveElement(selectedKeys.getOrNull(0) ?: ThemeManager.ELEM_PAKSHAM, data)
        val elem2 = resolveElement(selectedKeys.getOrNull(1) ?: ThemeManager.ELEM_THITHI, data)
        val elem3 = resolveElement(selectedKeys.getOrNull(2) ?: ThemeManager.ELEM_NAKSHATRA, data)
        val elem4 = resolveElement(selectedKeys.getOrNull(3) ?: ThemeManager.ELEM_NALLA_NERAM, data)
        val elem5 = resolveElement(selectedKeys.getOrNull(4) ?: ThemeManager.ELEM_GOWRI_NALLA_NERAM, data)

        val row2Text = "${elem1.first}: ${elem1.second}  |  ${elem2.first}: ${elem2.second}"
        views.safeSetText(context, "tv_widget_row2_paragraph", row2Text)
        views.safeSetTextColor(context, "tv_widget_row2_paragraph", colors.primaryText)

        val row3Text = "${elem3.first}: ${elem3.second}  |  ${elem4.first}: ${elem4.second}"
        views.safeSetText(context, "tv_widget_row3_paragraph", row3Text)
        views.safeSetTextColor(context, "tv_widget_row3_paragraph", colors.primaryText)

        val row4Text = "${elem5.first}: ${elem5.second}"
        views.safeSetText(context, "tv_widget_row4_paragraph", row4Text)
        views.safeSetTextColor(context, "tv_widget_row4_paragraph", colors.primaryText)

        val rootId = context.resources.getIdentifier("widget_root", "id", context.packageName)
        if (rootId != 0) {
            views.setOnClickPendingIntent(rootId, getPendingIntent(context))
        }

        val targetIds = if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
            appWidgetIds
        } else {
            manager.getAppWidgetIds(ComponentName(context, WidgetProvider4x4::class.java))
        }

        if (targetIds != null && targetIds.isNotEmpty()) {
            manager.updateAppWidget(targetIds, views)
        }
    }

    // -------------------------------------------------------------
    // 5. UPDATE ALL WIDGETS
    // -------------------------------------------------------------
    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        update2x1(context, appWidgetManager)
        update4x1(context, appWidgetManager)
        update4x2(context, appWidgetManager)
        update4x4(context, appWidgetManager)
    }
}
