package com.example.tamilclock

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Icon
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.format.DateFormat
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object WidgetUpdateHelper {

    private const val TAG = "WidgetUpdateHelper"

    private val NEON_GREEN = Color.parseColor("#39FF14")
    private val TEAL_NEON = Color.parseColor("#00F5D4")

    private val SLOT_AUSPICIOUS_DARK = Color.parseColor("#69F0AE")
    private val SLOT_AUSPICIOUS_LIGHT = Color.parseColor("#1B5E20")
    private val SLOT_CAUTION_DARK = Color.parseColor("#FF5252")
    private val SLOT_CAUTION_LIGHT = Color.parseColor("#B71C1C")

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

    private fun cleanDbString(raw: String?): String {
        val trimmed = raw?.trim().orEmpty()
        return if (trimmed.isEmpty() || trimmed == "---") "" else trimmed
    }

    private fun cleanFestivalText(raw: String?): String {
        if (raw.isNullOrBlank() || raw.trim() == "---") return ""
        return raw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() && it != "---" }
            .joinToString(", ")
    }

    private fun joinNonBlankParts(parts: List<String?>, separator: String = ", "): String {
        return parts
            .map { cleanDbString(it) }
            .filter { it.isNotEmpty() }
            .joinToString(separator)
    }

    private fun compactTimingRange(raw: String): String {
        return raw
            .replace(" - ", "-")
            .replace(" , ", ",")
            .replace(", ", ",")
            .trim()
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
        val is24Hr = DateFormat.is24HourFormat(context)
        val amPmVisibility = if (is24Hr) View.GONE else View.VISIBLE

        val density = context.resources.displayMetrics.density
        val minBoxPx = minOf(clockBoxW, clockBoxH).coerceAtLeast(96)
        val targetDialPx = ((150f * density).toInt())
            .coerceIn(96, minBoxPx)

        val analogId = views.findId(
            context,
            "analog_clock_view"
        )

        views.safeSetVisibility(context, "analog_clock_view", View.GONE)
        views.safeSetVisibility(context, "iv_hybrid_dial_bg", View.GONE)
        views.safeSetVisibility(context, "tc_hybrid_top_digital", View.GONE)
        views.safeSetVisibility(context, "iv_digital_clock_bg", View.GONE)
        views.safeSetVisibility(context, "container_digital_clock", View.GONE)
        views.safeSetVisibility(context, "tv_digital_clock", View.VISIBLE)
        views.safeSetVisibility(context, "tv_digital_clock_ampm", amPmVisibility)

        when (clockFace) {

            ThemeManager.CLOCK_DIGITAL_AMOLED_NEON -> {
                val pill = createAmoledPillBitmap(targetDialPx, targetDialPx)
                views.safeSetImageBitmap(context, "iv_digital_clock_bg", pill)
                views.safeSetVisibility(context, "iv_digital_clock_bg", View.VISIBLE)
                views.safeSetVisibility(context, "container_digital_clock", View.VISIBLE)
                views.safeSetVisibility(context, "tv_digital_clock_ampm", amPmVisibility)
                views.safeSetTextColor(context, "tv_digital_clock", NEON_GREEN)
                views.safeSetTextColor(context, "tv_digital_clock_ampm", NEON_GREEN)
            }

            ThemeManager.CLOCK_ANALOG_CLASSIC -> {
                views.safeSetVisibility(context, "analog_clock_view", View.VISIBLE)
                if (analogId != 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    views.setIcon(analogId, "setDial", Icon.createWithResource(context, R.drawable.classic_clock_dial))
                    views.setIcon(analogId, "setHourHand", Icon.createWithResource(context, R.drawable.classic_clock_hour))
                    views.setIcon(analogId, "setMinuteHand", Icon.createWithResource(context, R.drawable.classic_clock_minute))
                    views.setIcon(analogId, "setSecondHand", null)
                }
            }

            ThemeManager.CLOCK_ANALOG_AMOLED_WHITE -> {
                val dialBitmap = ClockFaceRenderer.createAmoledWhiteDialBitmap(context, targetDialPx)
                views.safeSetImageBitmap(context, "iv_hybrid_dial_bg", dialBitmap)
                views.safeSetVisibility(context, "iv_hybrid_dial_bg", View.VISIBLE)

                if (analogId != 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val transparentDial = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
                    views.safeSetVisibility(context, "analog_clock_view", View.VISIBLE)
                    views.setIcon(analogId, "setDial", Icon.createWithBitmap(transparentDial))
                    views.setIcon(analogId, "setHourHand", Icon.createWithResource(context, R.drawable.clock_hand_hour_capsule))
                    views.setIcon(analogId, "setMinuteHand", Icon.createWithResource(context, R.drawable.clock_hand_minute_capsule))
                    views.setIcon(analogId, "setSecondHand", Icon.createWithResource(context, R.drawable.clock_hand_second_red))
                } else {
                    views.safeSetVisibility(context, "analog_clock_view", View.GONE)
                }
            }

            ThemeManager.CLOCK_ANALOG_AMOLED_HYBRID -> {
                val hybridDialBitmap = ClockFaceRenderer.createAmoledHybridDialBitmap(context, cal, targetDialPx)
                views.safeSetImageBitmap(context, "iv_hybrid_dial_bg", hybridDialBitmap)
                views.safeSetVisibility(context, "iv_hybrid_dial_bg", View.VISIBLE)

                val showHybridSubClock = minBoxPx >= 180
                views.safeSetTextColor(context, "tc_hybrid_top_digital", TEAL_NEON)
                views.safeSetVisibility(
                    context,
                    "tc_hybrid_top_digital",
                    if (showHybridSubClock) View.VISIBLE else View.GONE
                )

                if (analogId != 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val transparentDial = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
                    views.safeSetVisibility(context, "analog_clock_view", View.VISIBLE)
                    views.setIcon(analogId, "setDial", Icon.createWithBitmap(transparentDial))
                    views.setIcon(analogId, "setHourHand", Icon.createWithResource(context, R.drawable.clock_hand_hour_capsule))
                    views.setIcon(analogId, "setMinuteHand", Icon.createWithResource(context, R.drawable.clock_hand_minute_capsule))
                    views.setIcon(analogId, "setSecondHand", Icon.createWithResource(context, R.drawable.clock_hand_second_red))
                } else {
                    views.safeSetVisibility(context, "analog_clock_view", View.GONE)
                }
            }

            else -> {
                views.safeSetVisibility(context, "container_digital_clock", View.VISIBLE)
                views.safeSetVisibility(context, "tv_digital_clock_ampm", amPmVisibility)
                views.safeSetTextColor(context, "tv_digital_clock", colors.primaryText)
                views.safeSetTextColor(context, "tv_digital_clock_ampm", colors.primaryText)
            }
        }
    }

    // -------------------------------------------------------------
    // PAKSHAM CONVERSION
    // -------------------------------------------------------------

    fun convertPakshamName(rawPaksham: String): String {
        val cleaned = cleanDbString(rawPaksham)
        return when {
            cleaned.contains("சுக்ல") ||
                    cleaned.contains("வளர்பிறை") -> "வளர்"

            cleaned.contains("கிருஷ்ண") ||
                    cleaned.contains("தேய்பிறை") -> "தேய்"

            cleaned.isEmpty() -> "---"

            else -> cleaned
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
        var amirthathiYogam: String = "",
        var nithyaYogam: String = "",
        var karnam: String = "",
        var thyajam: String = "",
        var soolam: String = "",
        var parikaram: String = "",
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
    // WEEKDAY FALLBACK FROM REQUESTED DATE
    // -------------------------------------------------------------

    private fun getWeekdayFromDate(queryDate: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val date = format.parse(queryDate) ?: return ""
            val cal = Calendar.getInstance().apply { time = date }
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
    // LOAD UNIFIED WIDGET DATA USING SHARED HELPER
    // -------------------------------------------------------------

    private fun loadUnifiedWidgetData(
        dbHelper: TamilDatabaseHelper,
        queryDate: String
    ): WidgetPanchangData {
        val result = WidgetPanchangData()

        try {
            val details = dbHelper.getDayDetails(queryDate)
            if (details != null) {
                result.tamilYear = cleanDbString(details.tamilYear).ifEmpty { "பராபவ" }
                result.tamilMonth = cleanDbString(details.tamilMonth)
                result.tamilDate = details.tamilDate
                result.weekdayTa = cleanDbString(details.weekdayTa)
                result.paksham = cleanDbString(details.paksha)
                result.thithi = cleanDbString(details.tithiName)
                result.thithiTime = cleanDbString(details.tithiTime)
                result.nakshatra = cleanDbString(details.nakshatraName)
                result.nakshatraTime = cleanDbString(details.nakshatraTime)
                result.amirthathiYogam = cleanDbString(details.amritadiYoga)
                result.nithyaYogam = cleanDbString(details.yogaName)
                result.karnam = cleanDbString(details.karanaName)
                result.thyajam = cleanDbString(details.tyajyamTime)
                result.soolam = cleanDbString(details.soolam)
                result.parikaram = cleanDbString(details.parikaram)
                result.chandrashtamam = cleanDbString(details.chandrashtamamRasi)
                result.festival = cleanFestivalText(details.festivalsEvents)

                result.nallaNeram = joinNonBlankParts(
                    listOf(details.nallaNeramMorning, details.nallaNeramEvening)
                )
                result.gowriNallaNeram = joinNonBlankParts(
                    listOf(details.gowriMorning, details.gowriNight)
                )
                result.rahuKalam = cleanDbString(details.rahuKaalam)
                result.emakandam = cleanDbString(details.yamagandam)
                result.kuligai = cleanDbString(details.kuligai)
                result.sunrise = cleanDbString(details.sunrise)
                result.sunset = cleanDbString(details.sunset)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Database read via TamilDatabaseHelper failed", e)
        }

        if (result.weekdayTa.isBlank()) {
            result.weekdayTa = getWeekdayFromDate(queryDate)
        }

        return result
    }

    fun loadUnifiedWidgetData(
        context: Context,
        queryDate: String
    ): WidgetPanchangData {
        val dbHelper = TamilDatabaseHelper(context)
        return try {
            loadUnifiedWidgetData(dbHelper, queryDate)
        } finally {
            dbHelper.close()
        }
    }

    // -------------------------------------------------------------
    // HORAI & GOWRI SLOT RESOLUTION (ALIGNED WITH FragmentGowriHorai)
    // -------------------------------------------------------------

    private data class ResolvedLiveSlot(
        val currentTimeLabel: String,
        val currentName: String,
        val currentColor: Int,
        val nextTimeLabel: String,
        val nextName: String
    )

    private fun extractEndTimeLabel(timeRange: String): String {
        val parts = timeRange
            .split("-", "–", "—")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        return if (parts.size >= 2) parts.last() else timeRange.trim()
    }

    private fun resolveLiveSlotFromSlots(
        daySlots: List<TamilDatabaseHelper.SolarSlotItem>,
        nightSlots: List<TamilDatabaseHelper.SolarSlotItem>,
        cal: Calendar,
        isDark: Boolean,
        fallbackColor: Int
    ): ResolvedLiveSlot {
        val allSlots = daySlots + nightSlots
        if (allSlots.isEmpty()) {
            return ResolvedLiveSlot("--:--", "---", fallbackColor, "--:--", "---")
        }

        val rawNow = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val riseToday = daySlots.firstOrNull()?.startEpochMins ?: 360
        val activeMinute = if (rawNow < riseToday) rawNow + 1440 else rawNow

        val activeIdx = allSlots.indexOfFirst {
            activeMinute in it.startEpochMins until it.endEpochMins
        }.coerceAtLeast(0)

        val currentSlot = allSlots[activeIdx]
        val nextSlot = allSlots.getOrElse(activeIdx + 1) { allSlots.first() }

        val slotColor = if (currentSlot.isAuspicious) {
            if (isDark) SLOT_AUSPICIOUS_DARK else SLOT_AUSPICIOUS_LIGHT
        } else {
            if (isDark) SLOT_CAUTION_DARK else SLOT_CAUTION_LIGHT
        }

        return ResolvedLiveSlot(
            currentTimeLabel = extractEndTimeLabel(currentSlot.timeRange),
            currentName = currentSlot.valueName.trim().ifEmpty { "---" },
            currentColor = slotColor,
            nextTimeLabel = extractEndTimeLabel(nextSlot.timeRange),
            nextName = nextSlot.valueName.trim().ifEmpty { "---" }
        )
    }

    // Single-row format:
    // Timing (small) + next Horai name (medium) + ☸️ + next gowri name (medium) + Timing (small)
    private fun buildSingleRowNextHoraiGowri(
        leftTime: String,
        nextHoraiName: String,
        nextGowriName: String,
        rightTime: String
    ): SpannableString {
        val tLeft = leftTime.trim()
        val hName = nextHoraiName.trim()
        val symbol = "☸️"
        val gName = nextGowriName.trim()
        val tRight = rightTime.trim()

        val fullText = "$tLeft  $hName  $symbol  $gName  $tRight"
        val spannable = SpannableString(fullText)

        if (tLeft.isNotEmpty()) {
            spannable.setSpan(
                RelativeSizeSpan(0.85f),
                0,
                tLeft.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        if (tRight.isNotEmpty()) {
            val rStart = fullText.lastIndexOf(tRight)
            if (rStart >= 0) {
                spannable.setSpan(
                    RelativeSizeSpan(0.85f),
                    rStart,
                    rStart + tRight.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        if (hName.isNotEmpty()) {
            val hStart = fullText.indexOf(hName, tLeft.length)
            if (hStart >= 0) {
                spannable.setSpan(
                    RelativeSizeSpan(1.15f),
                    hStart,
                    hStart + hName.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        if (gName.isNotEmpty()) {
            val gStart = fullText.indexOf(gName)
            if (gStart >= 0) {
                spannable.setSpan(
                    RelativeSizeSpan(1.15f),
                    gStart,
                    gStart + gName.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        return spannable
    }

    // -------------------------------------------------------------
    // FORMAT 10 SINGLE-ROW OPTIONS FOR 4x4 WIDGET
    // -------------------------------------------------------------

    private fun formatOptionRow(
        optionKey: String,
        data: WidgetPanchangData
    ): String {
        return when (optionKey) {
            ThemeManager.OPT_PAKSHAM_THITHI_NAKSHATRA -> {
                val pak = convertPakshamName(data.paksham)
                val thi = data.thithi.ifBlank { "---" }
                val nak = data.nakshatra.ifBlank { "---" }
                "🌘 $pak  🌙 $thi  ⭐ $nak"
            }

            ThemeManager.OPT_NALLA_NERAM -> {
                "🌸 ${data.nallaNeram.ifBlank { "---" }}"
            }

            ThemeManager.OPT_GOWRI_NALLA_NERAM -> {
                "🪔 ${data.gowriNallaNeram.ifBlank { "---" }}"
            }

            ThemeManager.OPT_AMIRTHATHI_NITHYA_YOGAM -> {
                val amirthathi = data.amirthathiYogam.ifBlank { "---" }
                val nithya = data.nithyaYogam.ifBlank { "---" }
                "⚙️ $amirthathi, 💫 $nithya"
            }

            ThemeManager.OPT_RAHU_YEMAKANDAM -> {
                val rahu = data.rahuKalam.ifBlank { "---" }
                val yema = data.emakandam.ifBlank { "---" }
                "⚠️ $rahu, ⚠️ $yema"
            }

            ThemeManager.OPT_KULIGAI_THYAJAM -> {
                val kul = data.kuligai.ifBlank { "---" }
                val thy = data.thyajam.ifBlank { "---" }
                "⏳ $kul  ⚠️ $thy"
            }

            ThemeManager.OPT_KARNAM_SOOLAM_PARIKARAM -> {
                val kar = data.karnam.ifBlank { "---" }
                val soolamCombined = joinNonBlankParts(
                    listOf(data.soolam, data.parikaram),
                    separator = " / "
                ).ifEmpty { "---" }
                "🧭 $kar, 🚩 $soolamCombined"
            }

            ThemeManager.OPT_SUN_TIMINGS -> {
                val rise = data.sunrise.ifBlank { "--:--" }
                val set = data.sunset.ifBlank { "--:--" }
                "🔆 ↑ $rise - $set ↓"
            }

            ThemeManager.OPT_CHANDRASTAMA -> {
                val chandra = data.chandrashtamam.ifBlank { "இல்லை" }
                "⚠️ சந்திராஷ்டமம் - $chandra"
            }

            ThemeManager.OPT_FESTIVAL -> {
                val fest = data.festival.ifBlank { "சிறப்பு இல்லை" }
                "🎉 $fest"
            }

            else -> {
                val pak = convertPakshamName(data.paksham)
                val thi = data.thithi.ifBlank { "---" }
                val nak = data.nakshatra.ifBlank { "---" }
                "🌘 $pak  🌙 $thi  ⭐ $nak"
            }
        }
    }

    // -------------------------------------------------------------
    // FORMAT COMPACT SYMBOL TEXT FOR 4x2 WIDGET (NO LABELS)
    // -------------------------------------------------------------

    private fun format4x2SymbolText(
        key: String,
        data: WidgetPanchangData
    ): String {
        return when (key) {
            ThemeManager.ELEM_PAKSHAM ->
                "🌘 ${convertPakshamName(data.paksham)}"

            ThemeManager.ELEM_THITHI -> {
                val name = data.thithi.trim()
                if (name.isNotEmpty()) "🌙 $name" else "🌙 —"
            }

            ThemeManager.ELEM_NAKSHATRA -> {
                val name = data.nakshatra.trim()
                if (name.isNotEmpty()) "⭐ $name" else "⭐ —"
            }

            ThemeManager.ELEM_AMIRTHATHI_YOGA -> {
                val yoga = data.amirthathiYogam.trim().ifEmpty { data.nithyaYogam.trim() }
                if (yoga.isNotEmpty()) "⚙️ $yoga" else "⚙️ —"
            }

            ThemeManager.ELEM_NALLA_NERAM -> {
                val compact = compactTimingRange(data.nallaNeram)
                if (compact.isNotEmpty()) "🌸 $compact" else "🌸 —"
            }

            ThemeManager.ELEM_GOWRI_NALLA_NERAM -> {
                val compact = compactTimingRange(data.gowriNallaNeram)
                if (compact.isNotEmpty()) "🪔 $compact" else "🪔 —"
            }

            ThemeManager.ELEM_RAHU_KALAM -> {
                val compact = compactTimingRange(data.rahuKalam)
                if (compact.isNotEmpty()) "⚠️ $compact" else "⚠️ —"
            }

            ThemeManager.ELEM_YEMAKANDAM -> {
                val compact = compactTimingRange(data.emakandam)
                if (compact.isNotEmpty()) "⚠️ $compact" else "⚠️ —"
            }

            ThemeManager.ELEM_KULIGAI -> {
                val compact = compactTimingRange(data.kuligai)
                if (compact.isNotEmpty()) "⏳ $compact" else "⏳ —"
            }

            ThemeManager.ELEM_SUNRISE_SET -> {
                val rise = data.sunrise.trim().ifEmpty { "--:--" }
                val set = data.sunset.trim().ifEmpty { "--:--" }
                "🔆 ↑$rise-$set↓"
            }

            ThemeManager.ELEM_CHANDRASTAMA -> {
                val chandra = data.chandrashtamam.trim().ifEmpty { "இல்லை" }
                "⚠️ $chandra"
            }

            ThemeManager.ELEM_FESTIVAL -> {
                val fest = data.festival.trim().ifEmpty { "சிறப்பு இல்லை" }
                "🎉 $fest"
            }

            else -> "—"
        }
    }

    // -------------------------------------------------------------
    // COMMON WIDGET UPDATE ENGINE (SOLAR-SYNCED)
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
            TamilDatabaseHelper,
            SolarDayHelper,
            SolarDayHelper.SolarTamilDateResult,
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

        val targetIds = if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
            appWidgetIds
        } else {
            manager.getAppWidgetIds(ComponentName(context, providerClass))
        }

        if (targetIds.isEmpty()) {
            return
        }

        val views = RemoteViews(context.packageName, layoutId)
        val cal = Calendar.getInstance()
        val themeConfig = ThemeManager.getActiveTheme(context)
        val colors = resolveWidgetColors(themeConfig)

        val background = createWidgetBackgroundBitmap(
            context,
            backgroundWidth,
            backgroundHeight,
            themeConfig
        )
        views.safeSetImageBitmap(context, "widget_bg_image", background)

        applyClockFaceToViews(
            context,
            views,
            colors,
            cal,
            clockWidth,
            clockHeight
        )

        val rootId = views.findId(context, "widget_root")
        if (rootId != 0) {
            views.setOnClickPendingIntent(rootId, getPendingIntent(context))
        }

        val dbHelper = TamilDatabaseHelper(context)
        val solarHelper = SolarDayHelper(context)
        try {
            val solarDate = solarHelper.resolveTamilDate(dbHelper, cal)
            val data = loadUnifiedWidgetData(dbHelper, solarDate.effectiveGregorianDate)
            configure(views, dbHelper, solarHelper, solarDate, data, cal, colors)
        } finally {
            dbHelper.close()
        }

        manager.updateAppWidget(targetIds, views)
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
        ) { views, _, _, solarDate, data, cal, colors ->
            val dateFormat = ThemeManager.getWidgetDateFormat(context)
            views.safeSetText(context, "tv_widget_greg_date", ThemeManager.formatDateByPattern(cal, dateFormat))
            views.safeSetTextColor(context, "tv_widget_greg_date", colors.primaryText)

            val month = solarDate.tamilMonth.ifBlank { data.tamilMonth.ifBlank { "---" } }
            val day = if (solarDate.tamilDate > 0) {
                solarDate.tamilDate.toString()
            } else if (data.tamilDate > 0) {
                data.tamilDate.toString()
            } else {
                "---"
            }
            views.safeSetText(context, "tv_widget_tamil_month_day", "$month $day")
            views.safeSetTextColor(context, "tv_widget_tamil_month_day", colors.accentColor)
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
        ) { views, _, _, solarDate, data, cal, colors ->
            val dateFormat = ThemeManager.getWidgetDateFormat(context)
            views.safeSetText(context, "tv_widget_greg_date", ThemeManager.formatDateByPattern(cal, dateFormat))
            views.safeSetTextColor(context, "tv_widget_greg_date", colors.primaryText)

            val weekday = solarDate.weekdayTa.ifBlank { data.weekdayTa.ifBlank { "---" } }
            views.safeSetText(context, "tv_widget_weekday", weekday)
            views.safeSetTextColor(context, "tv_widget_weekday", colors.accentColor)

            val month = solarDate.tamilMonth.ifBlank { data.tamilMonth.ifBlank { "---" } }
            val day = if (solarDate.tamilDate > 0) {
                solarDate.tamilDate.toString()
            } else if (data.tamilDate > 0) {
                data.tamilDate.toString()
            } else {
                "---"
            }
            views.safeSetText(context, "tv_widget_tamil_date", "$month $day")
            views.safeSetTextColor(context, "tv_widget_tamil_date", colors.accentColor)

            val paksham = convertPakshamName(data.paksham)
            val thithi = data.thithi.ifBlank { "---" }
            views.safeSetText(context, "tv_widget_paksham_thithi", "$paksham • $thithi")
            views.safeSetTextColor(context, "tv_widget_paksham_thithi", colors.secondaryText)
        }
    }

    // -------------------------------------------------------------
    // 4x2 PANORAMIC (SYMBOL-BASED ELEMENTS, NO LABELS)
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
        ) { views, _, _, solarDate, data, cal, colors ->
            val dateFormat = ThemeManager.getWidgetDateFormat(context)
            views.safeSetText(context, "tv_widget_greg_date", ThemeManager.formatDateByPattern(cal, dateFormat))
            views.safeSetTextColor(context, "tv_widget_greg_date", colors.primaryText)

            val weekday = solarDate.weekdayTa.ifBlank { data.weekdayTa.ifBlank { "---" } }
            views.safeSetText(context, "tv_widget_weekday", weekday)
            views.safeSetTextColor(context, "tv_widget_weekday", colors.accentColor)

            val month = solarDate.tamilMonth.ifBlank { data.tamilMonth.ifBlank { "---" } }
            val day = if (solarDate.tamilDate > 0) {
                solarDate.tamilDate.toString()
            } else if (data.tamilDate > 0) {
                data.tamilDate.toString()
            } else {
                "---"
            }
            views.safeSetText(context, "tv_widget_tamil_date", "$month $day")
            views.safeSetTextColor(context, "tv_widget_tamil_date", colors.accentColor)

            val selectedKeys = ThemeManager.getElements4x2(context).toList()
            val elementKeys = listOf(
                selectedKeys.getOrNull(0) ?: ThemeManager.ELEM_PAKSHAM,
                selectedKeys.getOrNull(1) ?: ThemeManager.ELEM_THITHI,
                selectedKeys.getOrNull(2) ?: ThemeManager.ELEM_NAKSHATRA
            )

            elementKeys.forEachIndexed { index, key ->
                val number = index + 1
                val symbolLine = format4x2SymbolText(key, data)
                views.safeSetVisibility(context, "tv_widget_elem${number}_label", View.GONE)
                views.safeSetText(context, "tv_widget_elem${number}_val", symbolLine)
                views.safeSetTextColor(context, "tv_widget_elem${number}_val", colors.primaryText)
            }
        }
    }

    // -------------------------------------------------------------
    // 4x4 HERITAGE (UPPER 60% FRAME + LOWER 40% FRAME)
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
            clockWidth = 240,
            clockHeight = 240
        ) { views, dbHelper, _, solarDate, data, cal, colors ->

            val liveEnabled = ThemeManager.isLiveHoraiGowriEnabled(context)

            if (liveEnabled) {
                val sdfDb = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                val todayKey = solarDate.effectiveGregorianDate.ifBlank { sdfDb.format(cal.time) }
                val tomorrowCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
                val tomorrowKey = sdfDb.format(tomorrowCal.time)
                val weekdayTa = solarDate.weekdayTa.ifBlank {
                    data.weekdayTa.ifBlank { getWeekdayFromDate(todayKey) }
                }

                val horaiSchedule = dbHelper.getDynamicHoraiData(todayKey, tomorrowKey, weekdayTa)
                val gowriSchedule = dbHelper.getDynamicGowriData(todayKey, tomorrowKey, weekdayTa)
                val isDark = !colors.isLightWidget

                val horaiLive = resolveLiveSlotFromSlots(
                    daySlots = horaiSchedule.daySlots,
                    nightSlots = horaiSchedule.nightSlots,
                    cal = cal,
                    isDark = isDark,
                    fallbackColor = colors.primaryText
                )
                val gowriLive = resolveLiveSlotFromSlots(
                    daySlots = gowriSchedule.daySlots,
                    nightSlots = gowriSchedule.nightSlots,
                    cal = cal,
                    isDark = isDark,
                    fallbackColor = colors.accentColor
                )

                // Left side: small timing + big live Horai name (green/red by isAuspicious)
                views.safeSetVisibility(context, "tv_widget_live_horai_time_left", View.VISIBLE)
                views.safeSetText(context, "tv_widget_live_horai_time_left", horaiLive.currentTimeLabel)
                views.safeSetTextColor(context, "tv_widget_live_horai_time_left", colors.secondaryText)

                views.safeSetVisibility(context, "tv_widget_live_horai_left", View.VISIBLE)
                views.safeSetText(context, "tv_widget_live_horai_left", horaiLive.currentName)
                views.safeSetTextColor(context, "tv_widget_live_horai_left", horaiLive.currentColor)

                // Right side: small timing + big live Gowri name (green/red by isAuspicious)
                views.safeSetVisibility(context, "tv_widget_live_gowri_time_right", View.VISIBLE)
                views.safeSetText(context, "tv_widget_live_gowri_time_right", gowriLive.currentTimeLabel)
                views.safeSetTextColor(context, "tv_widget_live_gowri_time_right", colors.secondaryText)

                views.safeSetVisibility(context, "tv_widget_live_gowri_right", View.VISIBLE)
                views.safeSetText(context, "tv_widget_live_gowri_right", gowriLive.currentName)
                views.safeSetTextColor(context, "tv_widget_live_gowri_right", gowriLive.currentColor)

                // Single row: Timing (small) + next Horai name (medium) + ☸️ + next Gowri name (medium) + Timing (small)
                views.safeSetVisibility(context, "tv_widget_horai_gowri_next", View.VISIBLE)
                views.safeSetText(
                    context,
                    "tv_widget_horai_gowri_next",
                    buildSingleRowNextHoraiGowri(
                        leftTime = horaiLive.nextTimeLabel,
                        nextHoraiName = horaiLive.nextName,
                        nextGowriName = gowriLive.nextName,
                        rightTime = gowriLive.nextTimeLabel
                    )
                )
                views.safeSetTextColor(context, "tv_widget_horai_gowri_next", colors.primaryText)
            } else {
                views.safeSetVisibility(context, "tv_widget_live_horai_time_left", View.GONE)
                views.safeSetVisibility(context, "tv_widget_live_horai_left", View.GONE)
                views.safeSetVisibility(context, "tv_widget_live_gowri_time_right", View.GONE)
                views.safeSetVisibility(context, "tv_widget_live_gowri_right", View.GONE)
                views.safeSetVisibility(context, "tv_widget_horai_gowri_next", View.GONE)
            }

            // Gregorian date (formatted via settings) + Effective solar Tamil Month/Date + Tamil Weekday
            val dateFormat = ThemeManager.getWidgetDateFormat(context)
            val gregDateText = ThemeManager.formatDateByPattern(cal, dateFormat)
            val tamilMonth = solarDate.tamilMonth.ifBlank { data.tamilMonth.ifBlank { "---" } }
            val tamilDay = if (solarDate.tamilDate > 0) {
                solarDate.tamilDate.toString()
            } else if (data.tamilDate > 0) {
                data.tamilDate.toString()
            } else {
                "---"
            }
            val weekday = solarDate.weekdayTa.ifBlank { data.weekdayTa.ifBlank { "---" } }
            val dateRowText = "$gregDateText  •  $tamilMonth $tamilDay  •  $weekday"

            // Up to 3 of the 10 single-row options chosen by user
            val chosen3 = ThemeManager.getSelectedOptions4x4(context)
                .distinct()
                .take(3)

            val optionLines = chosen3.map { key -> formatOptionRow(key, data) }

            // Bottom-align [Tamil Month + Date] followed by [Selected Options] across the 4 rows
            val bottomSequence = listOf(dateRowText) + optionLines
            val totalRows = 4
            val leadingEmptyCount = (totalRows - bottomSequence.size).coerceAtLeast(0)

            val rowViewIds = listOf(
                "tv_widget_row1_fixed",
                "tv_widget_row2_opt1",
                "tv_widget_row3_opt2",
                "tv_widget_row4_opt3"
            )

            rowViewIds.forEachIndexed { rowIdx, viewIdName ->
                val seqIdx = rowIdx - leadingEmptyCount
                if (seqIdx >= 0 && seqIdx < bottomSequence.size) {
                    val textForRow = bottomSequence[seqIdx]
                    val isDateRow = seqIdx == 0
                    views.safeSetVisibility(context, viewIdName, View.VISIBLE)
                    views.safeSetText(context, viewIdName, textForRow)
                    views.safeSetTextColor(
                        context,
                        viewIdName,
                        if (isDateRow) colors.accentColor else colors.primaryText
                    )
                } else {
                    views.safeSetText(context, viewIdName, "")
                    views.safeSetVisibility(context, viewIdName, View.GONE)
                }
            }
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
