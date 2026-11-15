
package com.example.tamilclock

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object WidgetUpdateHelper {

    private const val TAG = "WidgetUpdateHelper"

    private val NEON_GREEN = Color.parseColor("#39FF14")
    private val TEAL_NEON = Color.parseColor("#00F5D4")

    // -------------------------------------------------------------
    // COMMON HELPERS
    // -------------------------------------------------------------

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun RemoteViews.findId(
        context: Context,
        idName: String
    ): Int {
        return context.resources.getIdentifier(
            idName,
            "id",
            context.packageName
        )
    }

    private fun RemoteViews.safeSetText(
        context: Context,
        idName: String,
        text: CharSequence
    ) {
        val id = findId(context, idName)
        if (id != 0) setTextViewText(id, text)
    }

    private fun RemoteViews.safeSetTextColor(
        context: Context,
        idName: String,
        color: Int
    ) {
        val id = findId(context, idName)
        if (id != 0) setTextColor(id, color)
    }

    private fun RemoteViews.safeSetVisibility(
        context: Context,
        idName: String,
        visibility: Int
    ) {
        val id = findId(context, idName)
        if (id != 0) setViewVisibility(id, visibility)
    }

    private fun RemoteViews.safeSetImageBitmap(
        context: Context,
        idName: String,
        bitmap: Bitmap
    ) {
        val id = findId(context, idName)
        if (id != 0) setImageViewBitmap(id, bitmap)
    }

    // -------------------------------------------------------------
    // WIDGET COLOURS
    // -------------------------------------------------------------

    data class WidgetColorPalette(
        val isLightWidget: Boolean,
        val primaryText: Int,
        val secondaryText: Int,
        val accentColor: Int
    )

    private fun resolveWidgetColors(
        themeConfig: ThemeManager.ThemeConfig
    ): WidgetColorPalette {

        val isWhiteCanvas =
            themeConfig.preset == ThemeManager.Preset.PORCELAIN_BLUE ||
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
    // BACKGROUND BITMAP
    // -------------------------------------------------------------

    @JvmOverloads
    fun createWidgetBackgroundBitmap(
        context: Context,
        widthPx: Int,
        heightPx: Int,
        themeConfig: ThemeManager.ThemeConfig =
            ThemeManager.getActiveTheme(context)
    ): Bitmap {

        val w = widthPx.coerceAtLeast(10)
        val h = heightPx.coerceAtLeast(10)

        val bitmap = Bitmap.createBitmap(
            w,
            h,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        val rect = RectF(
            4f,
            4f,
            (w - 4).toFloat(),
            (h - 4).toFloat()
        )

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = themeConfig.resolvedBackgroundColor
            style = Paint.Style.FILL
        }

        canvas.drawRoundRect(rect, 32f, 32f, bgPaint)

        if (themeConfig.isBorderEnabled) {

            val borderAlpha = (
                (themeConfig.opacityPercent / 100f)
                    .coerceAtLeast(0.4f) * 255
                ).toInt().coerceIn(0, 255)

            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = themeConfig.accentColor
                alpha = borderAlpha
                style = Paint.Style.STROKE
                strokeWidth = 3.5f
            }

            canvas.drawRoundRect(
                rect,
                32f,
                32f,
                borderPaint
            )
        }

        return bitmap
    }

    // -------------------------------------------------------------
    // AMOLED NEON PILL
    // -------------------------------------------------------------

    private fun createAmoledPillBitmap(
        widthPx: Int,
        heightPx: Int
    ): Bitmap {

        val w = widthPx.coerceAtLeast(10)
        val h = heightPx.coerceAtLeast(10)

        val bitmap = Bitmap.createBitmap(
            w,
            h,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        val rect = RectF(
            4f,
            4f,
            (w - 4).toFloat(),
            (h - 4).toFloat()
        )

        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }

        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4439FF14")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        canvas.drawRoundRect(rect, 24f, 24f, fill)
        canvas.drawRoundRect(rect, 24f, 24f, stroke)

        return bitmap
    }

    // -------------------------------------------------------------
    // CLOCK FACE
    // -------------------------------------------------------------

    private fun applyClockFaceToViews(
        context: Context,
        views: RemoteViews,
        colors: WidgetColorPalette,
        cal: Calendar,
        clockBoxW: Int,
        clockBoxH: Int
    ) {

        val clockFace = ThemeManager.getClockFace(context)

        val analogId = views.findId(
            context,
            "analog_clock_view"
        )

        /*
         * Reset all clock layers first.
         * This prevents an old clock mode from leaving
         * unwanted views visible after a settings change.
         */

        views.safeSetVisibility(
            context, "analog_clock_view", View.GONE
        )

        views.safeSetVisibility(
            context, "iv_hybrid_dial_bg", View.GONE
        )

        views.safeSetVisibility(
            context, "tc_hybrid_top_digital", View.GONE
        )

        views.safeSetVisibility(
            context, "iv_digital_clock_bg", View.GONE
        )

        views.safeSetVisibility(
            context, "container_digital_clock", View.GONE
        )

        views.safeSetVisibility(
            context, "tv_digital_clock", View.VISIBLE
        )

        views.safeSetVisibility(
            context, "tv_digital_clock_ampm", View.VISIBLE
        )

        when (clockFace) {

            // -----------------------------------------------------
            // DIGITAL AMOLED NEON
            // -----------------------------------------------------

            ThemeManager.CLOCK_DIGITAL_AMOLED_NEON -> {

                val pill = createAmoledPillBitmap(
                    clockBoxW,
                    clockBoxH
                )

                views.safeSetImageBitmap(
                    context,
                    "iv_digital_clock_bg",
                    pill
                )

                views.safeSetVisibility(
                    context,
                    "iv_digital_clock_bg",
                    View.VISIBLE
                )

                views.safeSetVisibility(
                    context,
                    "container_digital_clock",
                    View.VISIBLE
                )

                views.safeSetTextColor(
                    context,
                    "tv_digital_clock",
                    NEON_GREEN
                )

                views.safeSetTextColor(
                    context,
                    "tv_digital_clock_ampm",
                    NEON_GREEN
                )
            }

            // -----------------------------------------------------
            // CLASSIC ANALOG
            // -----------------------------------------------------

            ThemeManager.CLOCK_ANALOG_CLASSIC -> {

                views.safeSetVisibility(
                    context,
                    "analog_clock_view",
                    View.VISIBLE
                )

                if (
                    analogId != 0 &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ) {
                    views.setIcon(
                        analogId,
                        "setDial",
                        Icon.createWithResource(
                            context,
                            R.drawable.classic_clock_dial
                        )
                    )

                    views.setIcon(
                        analogId,
                        "setHourHand",
                        Icon.createWithResource(
                            context,
                            R.drawable.classic_clock_hour
                        )
                    )

                    views.setIcon(
                        analogId,
                        "setMinuteHand",
                        Icon.createWithResource(
                            context,
                            R.drawable.classic_clock_minute
                        )
                    )

                    views.setIcon(
                        analogId,
                        "setSecondHand",
                        null
                    )
                }
            }

            // -----------------------------------------------------
            // AMOLED WHITE ANALOG
            // -----------------------------------------------------

            ThemeManager.CLOCK_ANALOG_AMOLED_WHITE -> {

                val dialBitmap =
                    ClockFaceRenderer.createAmoledWhiteDialBitmap(360)

                if (
                    analogId != 0 &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ) {

                    views.safeSetVisibility(
                        context,
                        "analog_clock_view",
                        View.VISIBLE
                    )

                    views.setIcon(
                        analogId,
                        "setDial",
                        Icon.createWithBitmap(dialBitmap)
                    )

                    views.setIcon(
                        analogId,
                        "setHourHand",
                        Icon.createWithResource(
                            context,
                            R.drawable.clock_hand_hour_capsule
                        )
                    )

                    views.setIcon(
                        analogId,
                        "setMinuteHand",
                        Icon.createWithResource(
                            context,
                            R.drawable.clock_hand_minute_capsule
                        )
                    )

                    views.setIcon(
                        analogId,
                        "setSecondHand",
                        Icon.createWithResource(
                            context,
                            R.drawable.clock_hand_second_red
                        )
                    )

                } else {

                    // Older Android: bitmap dial only.
                    // Hide native AnalogClock to avoid overlap.

                    views.safeSetVisibility(
                        context,
                        "analog_clock_view",
                        View.GONE
                    )

                    views.safeSetImageBitmap(
                        context,
                        "iv_hybrid_dial_bg",
                        dialBitmap
                    )

                    views.safeSetVisibility(
                        context,
                        "iv_hybrid_dial_bg",
                        View.VISIBLE
                    )
                }
            }

            // -----------------------------------------------------
            // AMOLED HYBRID ANALOG + DIGITAL
            // -----------------------------------------------------

            ThemeManager.CLOCK_ANALOG_AMOLED_HYBRID -> {

                val hybridDialBitmap =
                    ClockFaceRenderer.createAmoledHybridDialBitmap(
                        context,
                        cal,
                        380
                    )

                views.safeSetImageBitmap(
                    context,
                    "iv_hybrid_dial_bg",
                    hybridDialBitmap
                )

                views.safeSetVisibility(
                    context,
                    "iv_hybrid_dial_bg",
                    View.VISIBLE
                )

                views.safeSetTextColor(
                    context,
                    "tc_hybrid_top_digital",
                    TEAL_NEON
                )

                views.safeSetVisibility(
                    context,
                    "tc_hybrid_top_digital",
                    View.VISIBLE
                )

                if (
                    analogId != 0 &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ) {

                    /*
                     * API 31+:
                     * Transparent dial lets the bitmap below show.
                     * Native analog hands are rendered above it.
                     */

                    val transparentDial = Bitmap.createBitmap(
                        10,
                        10,
                        Bitmap.Config.ARGB_8888
                    )

                    views.safeSetVisibility(
                        context,
                        "analog_clock_view",
                        View.VISIBLE
                    )

                    views.setIcon(
                        analogId,
                        "setDial",
                        Icon.createWithBitmap(transparentDial)
                    )

                    views.setIcon(
                        analogId,
                        "setHourHand",
                        Icon.createWithResource(
                            context,
                            R.drawable.clock_hand_hour_capsule
                        )
                    )

                    views.setIcon(
                        analogId,
                        "setMinuteHand",
                        Icon.createWithResource(
                            context,
                            R.drawable.clock_hand_minute_capsule
                        )
                    )

                    views.setIcon(
                        analogId,
                        "setSecondHand",
                        Icon.createWithResource(
                            context,
                            R.drawable.clock_hand_second_red
                        )
                    )

                } else {

                    /*
                     * Older Android:
                     * Show bitmap dial and live digital time.
                     * Native AnalogClock is hidden.
                     */

                    views.safeSetVisibility(
                        context,
                        "analog_clock_view",
                        View.GONE
                    )
                }
            }

            // -----------------------------------------------------
            // CLASSIC DIGITAL (DEFAULT)
            // -----------------------------------------------------

            else -> {

                views.safeSetVisibility(
                    context,
                    "container_digital_clock",
                    View.VISIBLE
                )

                views.safeSetTextColor(
                    context,
                    "tv_digital_clock",
                    colors.primaryText
                )

                views.safeSetTextColor(
                    context,
                    "tv_digital_clock_ampm",
                    colors.primaryText
                )
            }
        }
    }

    // -------------------------------------------------------------
    // PAKSHAM CONVERSION
    // -------------------------------------------------------------

    fun convertPakshamName(rawPaksham: String): String {
        return when {
            rawPaksham.contains("சுக்ல") ||
                    rawPaksham.contains("வளர்பிறை") -> "வளர்"

            rawPaksham.contains("கிருஷ்ண") ||
                    rawPaksham.contains("தேய்பிறை") -> "தேய்"

            rawPaksham.isBlank() -> "---"

            else -> rawPaksham.trim()
        }
    }

    // -------------------------------------------------------------
    // PANCHANG DATA MODEL
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

    // -------------------------------------------------------------
    // DATABASE OPENING
    // -------------------------------------------------------------

    private fun openDatabaseSafely(
        context: Context,
        dbName: String
    ): SQLiteDatabase? {

        val dbFile = context.getDatabasePath(dbName)

        if (dbFile.exists()) {
            return SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
        }

        val altFile = File(
            File(context.applicationInfo.dataDir, "databases"),
            dbName
        )

        if (altFile.exists()) {
            return SQLiteDatabase.openDatabase(
                altFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
        }

        return null
    }

    // -------------------------------------------------------------
    // WEEKDAY FALLBACK FROM REQUESTED DATE
    // -------------------------------------------------------------

    private fun getWeekdayFromDate(queryDate: String): String {

        return try {

            val format = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.ENGLISH
            )

            val date = format.parse(queryDate)
                ?: return ""

            val cal = Calendar.getInstance().apply {
                time = date
            }

            when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "ஞாயிறு"
                Calendar.MONDAY -> "திங்கள்"
                Calendar.TUESDAY -> "செவ்வாய்"
                Calendar.WEDNESDAY -> "புதன்"
                Calendar.THURSDAY -> "வியாழன்"
                Calendar.FRIDAY -> "வெள்ளி"
                Calendar.SATURDAY -> "சனி"
                else -> ""
            }

        } catch (e: Exception) {
            Log.w(TAG, "Unable to calculate weekday", e)
            ""
        }
    }

    // -------------------------------------------------------------
    // LOAD UNIFIED WIDGET DATA
    // -------------------------------------------------------------

    fun loadUnifiedWidgetData(
        context: Context,
        queryDate: String
    ): WidgetPanchangData {

        val result = WidgetPanchangData()

        // ---------------------------------------------------------
        // MAIN PANCHANG DATABASE
        // ---------------------------------------------------------

        try {

            openDatabaseSafely(
                context,
                "panchang_clcon.db"
            )?.use { db ->

                db.rawQuery(
                    """
                    SELECT
                        tamil_year,
                        tamil_month,
                        tamil_date,
                        weekday_ta,
                        paksha,
                        tithi_name,
                        tithi_time,
                        nakshatra_name,
                        nakshatra_time,
                        amritadi_yoga,
                        chandrashtamam_rasi,
                        festivals_events
                    FROM panchangam_daily
                    WHERE gregorian_date = ?
                    LIMIT 1
                    """.trimIndent(),
                    arrayOf(queryDate)
                ).use { cursor ->

                    if (cursor.moveToFirst()) {

                        result.tamilYear =
                            cursor.getString(0) ?: "பராபவ"

                        result.tamilMonth =
                            cursor.getString(1) ?: ""

                        result.tamilDate =
                            cursor.getInt(2)

                        result.weekdayTa =
                            cursor.getString(3) ?: ""

                        result.paksham =
                            cursor.getString(4) ?: ""

                        result.thithi =
                            cursor.getString(5) ?: ""

                        result.thithiTime =
                            cursor.getString(6) ?: ""

                        result.nakshatra =
                            cursor.getString(7) ?: ""

                        result.nakshatraTime =
                            cursor.getString(8) ?: ""

                        result.yogam =
                            cursor.getString(9) ?: ""

                        result.chandrashtamam =
                            cursor.getString(10) ?: ""

                        result.festival =
                            cursor.getString(11) ?: ""
                    }
                }
            }

        } catch (e: Exception) {
            Log.w(TAG, "Panchang database read failed", e)
        }

        if (result.weekdayTa.isBlank()) {
            result.weekdayTa = getWeekdayFromDate(queryDate)
        }

        // ---------------------------------------------------------
        // AUSPICIOUS PERIODS DATABASE
        // ---------------------------------------------------------

        try {

            openDatabaseSafely(
                context,
                "auspicious_periods.db"
            )?.use { db ->

                db.rawQuery(
                    """
                    SELECT
                        nalla_neram_morning_start,
                        nalla_neram_morning_end,
                        nalla_neram_evening_start,
                        nalla_neram_evening_end,
                        gowri_day_start,
                        gowri_day_end,
                        gowri_night_start,
                        gowri_night_end,
                        raghu_start,
                        raghu_end,
                        emagandam_start,
                        emagandam_end,
                        kuligai_start,
                        kuligai_end
                    FROM weekday_timings_summary
                    WHERE weekday_ta = ?
                    LIMIT 1
                    """.trimIndent(),
                    arrayOf(result.weekdayTa)
                ).use { cursor ->

                    if (cursor.moveToFirst()) {

                        fun range(startIndex: Int, endIndex: Int): String {
                            val start = cursor.getString(startIndex) ?: ""
                            val end = cursor.getString(endIndex) ?: ""

                            return if (
                                start.isNotBlank() &&
                                end.isNotBlank()
                            ) {
                                "$start-$end"
                            } else {
                                ""
                            }
                        }

                        val morningNalla = range(0, 1)
                        val eveningNalla = range(2, 3)

                        result.nallaNeram = listOf(
                            morningNalla,
                            eveningNalla
                        ).filter { it.isNotBlank() }
                            .joinToString(", ")

                        val dayGowri = range(4, 5)
                        val nightGowri = range(6, 7)

                        result.gowriNallaNeram = listOf(
                            dayGowri,
                            nightGowri
                        ).filter { it.isNotBlank() }
                            .joinToString(", ")

                        result.rahuKalam = range(8, 9)
                        result.emakandam = range(10, 11)
                        result.kuligai = range(12, 13)
                    }
                }
            }

        } catch (e: Exception) {
            Log.w(TAG, "Auspicious periods database read failed", e)
        }

        // ---------------------------------------------------------
        // SUNRISE / SUNSET DATABASE
        // ---------------------------------------------------------

        try {

            openDatabaseSafely(
                context,
                "sun_moon_timings.db"
            )?.use { db ->

                db.rawQuery(
                    """
                    SELECT sunrise, sunset
                    FROM sun_moon_timings
                    WHERE date_gregorian = ?
                    LIMIT 1
                    """.trimIndent(),
                    arrayOf(queryDate)
                ).use { cursor ->

                    if (cursor.moveToFirst()) {
                        result.sunrise =
                            cursor.getString(0) ?: ""

                        result.sunset =
                            cursor.getString(1) ?: ""
                    }
                }
            }

        } catch (e: Exception) {
            Log.w(TAG, "Sun/moon timings database read failed", e)
        }

        return result
    }

    // -------------------------------------------------------------
    // RESOLVE SELECTED PANCHANG ELEMENT
    // -------------------------------------------------------------

    private fun resolveElement(
        key: String,
        data: WidgetPanchangData
    ): Pair<String, String> {

        return when (key) {

            ThemeManager.ELEM_PAKSHAM -> {
                Pair(
                    "பக்ஷம்",
                    convertPakshamName(data.paksham)
                )
            }

            ThemeManager.ELEM_THITHI -> {

                val name = data.thithi.trim()
                val time = data.thithiTime.trim()

                val value = when {
                    name.isNotBlank() && time.isNotBlank() ->
                        "$name ($time)"

                    name.isNotBlank() -> name

                    else -> "—"
                }

                Pair("திதி", value)
            }

            ThemeManager.ELEM_NAKSHATRA -> {

                val name = data.nakshatra.trim()
                val time = data.nakshatraTime.trim()

                val value = when {
                    name.isNotBlank() && time.isNotBlank() ->
                        "$name ($time)"

                    name.isNotBlank() -> name

                    else -> "—"
                }

                Pair("நட்சத்திரம்", value)
            }

            ThemeManager.ELEM_AMIRTHATHI_YOGA ->
                Pair("யோகம்", data.yogam.ifBlank { "—" })

            ThemeManager.ELEM_NALLA_NERAM ->
                Pair("நல்ல நேரம்", data.nallaNeram.ifBlank { "—" })

            ThemeManager.ELEM_GOWRI_NALLA_NERAM ->
                Pair("கௌரி", data.gowriNallaNeram.ifBlank { "—" })

            ThemeManager.ELEM_RAHU_KALAM ->
                Pair("ராகு காலம்", data.rahuKalam.ifBlank { "—" })

            ThemeManager.ELEM_YEMAKANDAM ->
                Pair("எமகண்டம்", data.emakandam.ifBlank { "—" })

            ThemeManager.ELEM_KULIGAI ->
                Pair("குளிகை", data.kuligai.ifBlank { "—" })

            ThemeManager.ELEM_SUNRISE_SET -> {

                val rise = data.sunrise.trim()
                val set = data.sunset.trim()

                val value = when {
                    rise.isNotBlank() && set.isNotBlank() ->
                        "உதயம்: $rise | மறைவு: $set"

                    rise.isNotBlank() ->
                        "உதயம்: $rise"

                    set.isNotBlank() ->
                        "மறைவு: $set"

                    else -> "—"
                }

                Pair("சூரியன்", value)
            }

            ThemeManager.ELEM_CHANDRASTAMA ->
                Pair(
                    "சந்திராஷ்டமம்",
                    data.chandrashtamam.ifBlank { "இல்லை" }
                )

            ThemeManager.ELEM_FESTIVAL ->
                Pair("சிறப்பு", data.festival.ifBlank { "—" })

            else -> Pair("", "")
        }
    }

    // -------------------------------------------------------------
    // COMMON WIDGET UPDATE ENGINE
    // -------------------------------------------------------------

    private fun updateWidget(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray?,
        providerClass: Class<out AppWidgetProvider>,
        layoutName: String,
        backgroundWidth: Int,
        backgroundHeight: Int,
        clockWidth: Int,
        clockHeight: Int,
        configure: (
            RemoteViews,
            WidgetPanchangData,
            Calendar,
            WidgetColorPalette
        ) -> Unit
    ) {

        val layoutId = context.resources.getIdentifier(
            layoutName,
            "layout",
            context.packageName
        )

        if (layoutId == 0) {
            Log.e(TAG, "Widget layout not found: $layoutName")
            return
        }

        val views = RemoteViews(
            context.packageName,
            layoutId
        )

        val cal = Calendar.getInstance()

        val queryDate = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.ENGLISH
        ).format(cal.time)

        val data = loadUnifiedWidgetData(
            context,
            queryDate
        )

        val themeConfig = ThemeManager.getActiveTheme(context)

        val colors = resolveWidgetColors(themeConfig)

        val background = createWidgetBackgroundBitmap(
            context,
            backgroundWidth,
            backgroundHeight,
            themeConfig
        )

        views.safeSetImageBitmap(
            context,
            "widget_bg_image",
            background
        )

        applyClockFaceToViews(
            context,
            views,
            colors,
            cal,
            clockWidth,
            clockHeight
        )

        val rootId = views.findId(
            context,
            "widget_root"
        )

        if (rootId != 0) {
            views.setOnClickPendingIntent(
                rootId,
                getPendingIntent(context)
            )
        }

        configure(
            views,
            data,
            cal,
            colors
        )

        /*
         * Use the supplied IDs when called from onUpdate().
         * Otherwise, obtain IDs belonging to this exact provider.
         */

        val targetIds = if (
            appWidgetIds != null && appWidgetIds.isNotEmpty()
        ) {
            appWidgetIds
        } else {
            manager.getAppWidgetIds(
                ComponentName(context, providerClass)
            )
        }

        if (targetIds.isNotEmpty()) {
            manager.updateAppWidget(
                targetIds,
                views
            )
        }
    }

    // -------------------------------------------------------------
    // 2x1 CAPSULE
    // -------------------------------------------------------------

    @JvmOverloads
    fun update2x1(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray? = null
    ) {

        updateWidget(
            context = context,
            manager = manager,
            appWidgetIds = appWidgetIds,
            providerClass = WidgetProvider2x1::class.java,
            layoutName = "widget_layout_2x1",
            backgroundWidth = 320,
            backgroundHeight = 160,
            clockWidth = 145,
            clockHeight = 145
        ) { views, data, cal, colors ->

            val dateFormat =
                ThemeManager.getWidgetDateFormat(context)

            views.safeSetText(
                context,
                "tv_widget_greg_date",
                ThemeManager.formatDateByPattern(
                    cal,
                    dateFormat
                )
            )

            views.safeSetTextColor(
                context,
                "tv_widget_greg_date",
                colors.primaryText
            )

            val month =
                data.tamilMonth.ifBlank { "---" }

            val day =
                if (data.tamilDate > 0) {
                    data.tamilDate.toString()
                } else {
                    "---"
                }

            views.safeSetText(
                context,
                "tv_widget_tamil_month_day",
                "$month $day"
            )

            views.safeSetTextColor(
                context,
                "tv_widget_tamil_month_day",
                colors.accentColor
            )
        }
    }

    // -------------------------------------------------------------
    // 4x1 RIBBON
    // -------------------------------------------------------------

    @JvmOverloads
    fun update4x1(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray? = null
    ) {

        updateWidget(
            context = context,
            manager = manager,
            appWidgetIds = appWidgetIds,
            providerClass = WidgetProvider4x1::class.java,
            layoutName = "widget_layout_4x1",
            backgroundWidth = 580,
            backgroundHeight = 140,
            clockWidth = 130,
            clockHeight = 130
        ) { views, data, cal, colors ->

            val dateFormat =
                ThemeManager.getWidgetDateFormat(context)

            views.safeSetText(
                context,
                "tv_widget_greg_date",
                ThemeManager.formatDateByPattern(
                    cal,
                    dateFormat
                )
            )

            views.safeSetTextColor(
                context,
                "tv_widget_greg_date",
                colors.primaryText
            )

            views.safeSetText(
                context,
                "tv_widget_weekday",
                data.weekdayTa.ifBlank { "---" }
            )

            views.safeSetTextColor(
                context,
                "tv_widget_weekday",
                colors.accentColor
            )

            val month =
                data.tamilMonth.ifBlank { "---" }

            val day =
                if (data.tamilDate > 0) {
                    data.tamilDate.toString()
                } else {
                    "---"
                }

            views.safeSetText(
                context,
                "tv_widget_tamil_date",
                "$month $day"
            )

            views.safeSetTextColor(
                context,
                "tv_widget_tamil_date",
                colors.accentColor
            )

            val paksham =
                convertPakshamName(data.paksham)

            val thithi =
                data.thithi.ifBlank { "---" }

            views.safeSetText(
                context,
                "tv_widget_paksham_thithi",
                "$paksham • $thithi"
            )

            views.safeSetTextColor(
                context,
                "tv_widget_paksham_thithi",
                colors.secondaryText
            )
        }
    }

    // -------------------------------------------------------------
    // 4x2 PANORAMIC
    // -------------------------------------------------------------

    @JvmOverloads
    fun update4x2(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray? = null
    ) {

        updateWidget(
            context = context,
            manager = manager,
            appWidgetIds = appWidgetIds,
            providerClass = WidgetProvider4x2::class.java,
            layoutName = "widget_layout_4x2",
            backgroundWidth = 580,
            backgroundHeight = 280,
            clockWidth = 260,
            clockHeight = 260
        ) { views, data, cal, colors ->

            val dateFormat =
                ThemeManager.getWidgetDateFormat(context)

            views.safeSetText(
                context,
                "tv_widget_greg_date",
                ThemeManager.formatDateByPattern(
                    cal,
                    dateFormat
                )
            )

            views.safeSetTextColor(
                context,
                "tv_widget_greg_date",
                colors.primaryText
            )

            views.safeSetText(
                context,
                "tv_widget_weekday",
                data.weekdayTa.ifBlank { "---" }
            )

            views.safeSetTextColor(
                context,
                "tv_widget_weekday",
                colors.accentColor
            )

            val month =
                data.tamilMonth.ifBlank { "---" }

            val day =
                if (data.tamilDate > 0) {
                    data.tamilDate.toString()
                } else {
                    "---"
                }

            views.safeSetText(
                context,
                "tv_widget_tamil_date",
                "$month $day"
            )

            views.safeSetTextColor(
                context,
                "tv_widget_tamil_date",
                colors.accentColor
            )

            val selectedKeys =
                ThemeManager.getElements4x2(context).toList()

            val elements = listOf(
                resolveElement(
                    selectedKeys.getOrNull(0)
                        ?: ThemeManager.ELEM_PAKSHAM,
                    data
                ),
                resolveElement(
                    selectedKeys.getOrNull(1)
                        ?: ThemeManager.ELEM_THITHI,
                    data
                ),
                resolveElement(
                    selectedKeys.getOrNull(2)
                        ?: ThemeManager.ELEM_NAKSHATRA,
                    data
                )
            )

            elements.forEachIndexed { index, element ->

                val number = index + 1

                views.safeSetText(
                    context,
                    "tv_widget_elem${number}_label",
                    "${element.first} :"
                )

                views.safeSetTextColor(
                    context,
                    "tv_widget_elem${number}_label",
                    colors.accentColor
                )

                views.safeSetText(
                    context,
                    "tv_widget_elem${number}_val",
                    element.second
                )

                views.safeSetTextColor(
                    context,
                    "tv_widget_elem${number}_val",
                    colors.primaryText
                )
            }
        }
    }

    // -------------------------------------------------------------
    // 4x4 HERITAGE
    // -------------------------------------------------------------

    @JvmOverloads
    fun update4x4(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray? = null
    ) {

        updateWidget(
            context = context,
            manager = manager,
            appWidgetIds = appWidgetIds,
            providerClass = WidgetProvider4x4::class.java,
            layoutName = "widget_layout_4x4",
            backgroundWidth = 580,
            backgroundHeight = 580,
            clockWidth = 320,
            clockHeight = 320
        ) { views, data, cal, colors ->

            val dateFormat =
                ThemeManager.getWidgetDateFormat(context)

            views.safeSetText(
                context,
                "tv_widget_greg_date",
                ThemeManager.formatDateByPattern(
                    cal,
                    dateFormat
                )
            )

            views.safeSetTextColor(
                context,
                "tv_widget_greg_date",
                colors.primaryText
            )

            views.safeSetTextColor(
                context,
                "tv_widget_fixed_sep1",
                colors.accentColor
            )

            views.safeSetText(
                context,
                "tv_widget_weekday",
                data.weekdayTa.ifBlank { "---" }
            )

            views.safeSetTextColor(
                context,
                "tv_widget_weekday",
                colors.accentColor
            )

            views.safeSetTextColor(
                context,
                "tv_widget_fixed_sep2",
                colors.accentColor
            )

            val tamilDate =
                if (data.tamilDate > 0) {
                    data.tamilDate.toString()
                } else {
                    "---"
                }

            val tamilYear =
                data.tamilYear.ifBlank { "பராபவ" }

            val tamilMonth =
                data.tamilMonth.ifBlank { "---" }

            views.safeSetText(
                context,
                "tv_widget_tamil_full",
                "$tamilYear • $tamilMonth $tamilDate"
            )

            views.safeSetTextColor(
                context,
                "tv_widget_tamil_full",
                colors.accentColor
            )

            val selectedKeys =
                ThemeManager.getElements4x4(context).toList()

            val elements = listOf(
                resolveElement(
                    selectedKeys.getOrNull(0)
                        ?: ThemeManager.ELEM_PAKSHAM,
                    data
                ),
                resolveElement(
                    selectedKeys.getOrNull(1)
                        ?: ThemeManager.ELEM_THITHI,
                    data
                ),
                resolveElement(
                    selectedKeys.getOrNull(2)
                        ?: ThemeManager.ELEM_NAKSHATRA,
                    data
                ),
                resolveElement(
                    selectedKeys.getOrNull(3)
                        ?: ThemeManager.ELEM_NALLA_NERAM,
                    data
                ),
                resolveElement(
                    selectedKeys.getOrNull(4)
                        ?: ThemeManager.ELEM_GOWRI_NALLA_NERAM,
                    data
                )
            )

            val row2Text =
                "${elements[0].first}: ${elements[0].second}" +
                "  |  ${elements[1].first}: ${elements[1].second}"

            val row3Text =
                "${elements[2].first}: ${elements[2].second}" +
                "  |  ${elements[3].first}: ${elements[3].second}"

            val row4Text =
                "${elements[4].first}: ${elements[4].second}"

            views.safeSetText(
                context,
                "tv_widget_row2_paragraph",
                row2Text
            )

            views.safeSetTextColor(
                context,
                "tv_widget_row2_paragraph",
                colors.primaryText
            )

            views.safeSetText(
                context,
                "tv_widget_row3_paragraph",
                row3Text
            )

            views.safeSetTextColor(
                context,
                "tv_widget_row3_paragraph",
                colors.primaryText
            )

            views.safeSetText(
                context,
                "tv_widget_row4_paragraph",
                row4Text
            )

            views.safeSetTextColor(
                context,
                "tv_widget_row4_paragraph",
                colors.primaryText
            )
        }
    }

    // -------------------------------------------------------------
    // UPDATE ALL WIDGETS
    // -------------------------------------------------------------

    fun updateAllWidgets(context: Context) {

        val manager = AppWidgetManager.getInstance(context)

        update2x1(context, manager)
        update4x1(context, manager)
        update4x2(context, manager)
        update4x4(context, manager)
    }
}
