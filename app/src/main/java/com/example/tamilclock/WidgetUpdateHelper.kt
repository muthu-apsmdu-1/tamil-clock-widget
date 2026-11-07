package com.example.tamilclock

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.RemoteViews
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
        if (resId != 0) {
            setTextViewText(resId, text)
        }
    }

    private fun RemoteViews.safeSetTextColor(context: Context, idName: String, color: Int) {
        val resId = context.resources.getIdentifier(idName, "id", context.packageName)
        if (resId != 0) {
            setTextColor(resId, color)
        }
    }

    private fun RemoteViews.safeSetVisibility(context: Context, idName: String, visibility: Int) {
        val resId = context.resources.getIdentifier(idName, "id", context.packageName)
        if (resId != 0) {
            setViewVisibility(resId, visibility)
        }
    }

    private fun RemoteViews.safeSetImageBitmap(context: Context, idName: String, bitmap: Bitmap) {
        val resId = context.resources.getIdentifier(idName, "id", context.packageName)
        if (resId != 0) {
            setImageViewBitmap(resId, bitmap)
        }
    }

    private fun RemoteViews.safeSetImageResource(context: Context, idName: String, resDrawableId: Int) {
        val resId = context.resources.getIdentifier(idName, "id", context.packageName)
        if (resId != 0) {
            setImageViewResource(resId, resDrawableId)
        }
    }

    // -------------------------------------------------------------
    // COLOR RESOLUTION FOR WIDGETS
    // -------------------------------------------------------------
    data class WidgetColorPalette(
        val isLightWidget: Boolean,
        val primaryText: Int,
        val secondaryText: Int,
        val accentGold: Int
    )

    private fun resolveWidgetColors(context: Context): WidgetColorPalette {
        val hex = ThemeManager.getWidgetBackgroundColor(context)
        val isLight = hex.equals(ThemeManager.WIDGET_COLOR_OFF_WHITE, ignoreCase = true)

        return if (isLight) {
            WidgetColorPalette(
                isLightWidget = true,
                primaryText = Color.parseColor("#081E7D"),     // Dark Blue
                secondaryText = Color.parseColor("#D81B60"),   // Pink
                accentGold = Color.parseColor("#B26A00")       // Dark Sandal Gold
            )
        } else {
            // Dark themes: pure white for content, gold for headers/accents
            WidgetColorPalette(
                isLightWidget = false,
                primaryText = Color.WHITE,
                secondaryText = Color.WHITE,
                accentGold = Color.parseColor("#FFDF00")
            )
        }
    }

    // -------------------------------------------------------------
    // DYNAMIC BACKGROUND BITMAP GENERATOR (COLOR + OPACITY)
    // -------------------------------------------------------------
    fun createWidgetBackgroundBitmap(context: Context, widthPx: Int, heightPx: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx.coerceAtLeast(10), heightPx.coerceAtLeast(10), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val hexColor = ThemeManager.getWidgetBackgroundColor(context)
        val baseColor = try {
            Color.parseColor(hexColor)
        } catch (_: Exception) {
            Color.parseColor(ThemeManager.WIDGET_COLOR_VELVET_MAROON)
        }

        val opacityPercent = ThemeManager.getWidgetOpacity(context)
        val alpha = (opacityPercent * 255 / 100).coerceIn(0, 255)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = baseColor
            this.alpha = alpha
            style = Paint.Style.FILL
        }
        val cornerRadius = 32f
        val rect = RectF(4f, 4f, (widthPx - 4).toFloat(), (heightPx - 4).toFloat())
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (hexColor.equals(ThemeManager.WIDGET_COLOR_OFF_WHITE, ignoreCase = true)) {
                Color.parseColor("#BDBDBD")
            } else {
                Color.parseColor("#FFDF00")
            }
            this.alpha = alpha.coerceAtLeast(80)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)

        return bitmap
    }

    // -------------------------------------------------------------
    // DIGITAL CLOCK FORMATTER: BIG TIME + HALF-SIZE AM/PM
    // -------------------------------------------------------------
    private fun formatDigitalTimeWithHalfAmPm(cal: Calendar): CharSequence {
        val timePart = SimpleDateFormat("h:mm", Locale.ENGLISH).format(cal.time)
        val amPmPart = SimpleDateFormat(" a", Locale.ENGLISH).format(cal.time)

        val fullText = "$timePart$amPmPart"
        val spannable = SpannableString(fullText)

        val startAmPm = fullText.length - amPmPart.length
        spannable.setSpan(
            RelativeSizeSpan(0.5f),
            startAmPm,
            fullText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    // -------------------------------------------------------------
    // DATE FORMATTER (06 - 11 - 2026 vs 06 Nov 26)
    // -------------------------------------------------------------
    private fun formatEnglishDate(cal: Calendar, formatType: Int): String {
        return if (formatType == ThemeManager.DATE_FORMAT_TEXT) {
            SimpleDateFormat("dd MMM yy", Locale.ENGLISH).format(cal.time)
        } else {
            SimpleDateFormat("dd - MM - yyyy", Locale.ENGLISH).format(cal.time)
        }
    }

    // -------------------------------------------------------------
    // PAKSHAM NAME CONVERTER (ALL WIDGETS)
    // சுக்ல பக்ஷம் -> "வளர்", கிருஷ்ண பக்ஷம் -> "தேய்"
    // -------------------------------------------------------------
    fun convertPakshamName(rawPaksham: String): String {
        return when {
            rawPaksham.contains("சுக்ல") || rawPaksham.contains("வளர்பிறை") -> "வளர்"
            rawPaksham.contains("கிருஷ்ண") || rawPaksham.contains("தேய்பிறை") -> "தேய்"
            rawPaksham.isBlank() -> "---"
            else -> rawPaksham.trim()
        }
    }

    // -------------------------------------------------------------
    // ASTROLOGICAL ELEMENT RESOLVER (TITLE + VALUE WITH "---")
    // -------------------------------------------------------------
    private fun resolveElement(elemKey: String, details: TamilDatabaseHelper.DayPanchangDetails): Pair<String, String> {
        val fallback = "---"
        return when (elemKey) {
            ThemeManager.ELEM_PAKSHAM -> Pair("பக்ஷம்", convertPakshamName(details.paksha))
            ThemeManager.ELEM_THITHI -> Pair("திதி", if (details.tithiName.isNotBlank()) details.tithiName else fallback)
            ThemeManager.ELEM_NAKSHATRA -> Pair("நட்சத்திரம்", if (details.nakshatraName.isNotBlank()) details.nakshatraName else fallback)
            ThemeManager.ELEM_KARNAM -> Pair("கரணம்", if (details.karanaName.isNotBlank()) details.karanaName else fallback)
            ThemeManager.ELEM_THYAJAM -> Pair("தியாகம்", if (details.tyajyamTime.isNotBlank()) details.tyajyamTime else fallback)
            ThemeManager.ELEM_NALLA_NERAM -> Pair("நல்ல நேரம்", if (details.nallaNeramMorning.isNotBlank()) details.nallaNeramMorning else fallback)
            ThemeManager.ELEM_GOWRI_NALLA_NERAM -> Pair("கௌரி", if (details.gowriMorning.isNotBlank()) details.gowriMorning else fallback)
            ThemeManager.ELEM_SOOLAM -> Pair("சூலம்", if (details.soolam.isNotBlank()) details.soolam else fallback)
            ThemeManager.ELEM_SUNRISE_SET -> Pair("உதயம்/மறைவு", "${details.sunrise} / ${details.sunset}")
            else -> Pair(fallback, fallback)
        }
    }

    // -------------------------------------------------------------
    // 1. UPDATE 2x1 WIDGET (Clock Half + Gregorian / Tamil Rows Half)
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

        val db = TamilDatabaseHelper(context)
        val data = db.getDayDetails(queryDate) ?: TamilDatabaseHelper.DayPanchangDetails()
        db.close()

        val bgBitmap = createWidgetBackgroundBitmap(context, 320, 160)
        views.safeSetImageBitmap(context, "widget_bg_image", bgBitmap)

        val colors = resolveWidgetColors(context)

        // Left Half: Clock Style
        val clockFace = ThemeManager.getClockFace(context)
        if (clockFace == ThemeManager.CLOCK_ANALOG) {
            views.safeSetVisibility(context, "analog_clock_view", View.VISIBLE)
            views.safeSetVisibility(context, "tv_digital_clock", View.GONE)
        } else {
            views.safeSetVisibility(context, "analog_clock_view", View.GONE)
            views.safeSetVisibility(context, "tv_digital_clock", View.VISIBLE)
            views.safeSetText(context, "tv_digital_clock", formatDigitalTimeWithHalfAmPm(cal))
            views.safeSetTextColor(context, "tv_digital_clock", colors.primaryText)
        }

        // Right Half: Row 1 - Gregorian Date (Optional type via Settings)
        val dateFormat = ThemeManager.getDateFormat4x2(context)
        val gregDateStr = formatEnglishDate(cal, dateFormat)
        views.safeSetText(context, "tv_widget_greg_date", gregDateStr)
        views.safeSetTextColor(context, "tv_widget_greg_date", colors.primaryText)

        // Right Half: Row 2 - Tamil Month & Date
        val tMonth = if (data.tamilMonth.isNotBlank()) data.tamilMonth else "---"
        val tDay = if (data.tamilDate > 0) data.tamilDate.toString() else "---"
        views.safeSetText(context, "tv_widget_tamil_month_day", "$tMonth $tDay")
        views.safeSetTextColor(context, "tv_widget_tamil_month_day", colors.secondaryText)

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
    // 2. UPDATE 4x1 WIDGET (1/4 Clock + 3/4 Balance Area)
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

        val db = TamilDatabaseHelper(context)
        val data = db.getDayDetails(queryDate) ?: TamilDatabaseHelper.DayPanchangDetails()
        db.close()

        val bgBitmap = createWidgetBackgroundBitmap(context, 580, 140)
        views.safeSetImageBitmap(context, "widget_bg_image", bgBitmap)

        val colors = resolveWidgetColors(context)

        // Left 1/4: Clock
        val clockFace = ThemeManager.getClockFace(context)
        if (clockFace == ThemeManager.CLOCK_ANALOG) {
            views.safeSetVisibility(context, "analog_clock_view", View.VISIBLE)
            views.safeSetVisibility(context, "tv_digital_clock", View.GONE)
        } else {
            views.safeSetVisibility(context, "analog_clock_view", View.GONE)
            views.safeSetVisibility(context, "tv_digital_clock", View.VISIBLE)
            views.safeSetText(context, "tv_digital_clock", formatDigitalTimeWithHalfAmPm(cal))
            views.safeSetTextColor(context, "tv_digital_clock", colors.primaryText)
        }

        // Right 3/4 - Row 1: Gregorian Date + Tamil Weekday
        val dateFormat = ThemeManager.getDateFormat4x2(context)
        val gregDateStr = formatEnglishDate(cal, dateFormat)
        views.safeSetText(context, "tv_widget_greg_date", gregDateStr)
        views.safeSetTextColor(context, "tv_widget_greg_date", colors.primaryText)

        val weekdayStr = if (data.weekdayTa.isNotBlank()) data.weekdayTa else "---"
        views.safeSetText(context, "tv_widget_weekday", weekdayStr)
        views.safeSetTextColor(context, "tv_widget_weekday", colors.accentGold)

        // Right 3/4 - Row 2: Tamil Month & Date + Converted Paksham & Thithi
        val tMonth = if (data.tamilMonth.isNotBlank()) data.tamilMonth else "---"
        val tDay = if (data.tamilDate > 0) data.tamilDate.toString() else "---"
        views.safeSetText(context, "tv_widget_tamil_date", "$tMonth $tDay")
        views.safeSetTextColor(context, "tv_widget_tamil_date", colors.secondaryText)

        val convertedPaksham = convertPakshamName(data.paksha)
        val thithiStr = if (data.tithiName.isNotBlank()) data.tithiName else "---"
        views.safeSetText(context, "tv_widget_paksham_thithi", "$convertedPaksham • $thithiStr")
        views.safeSetTextColor(context, "tv_widget_paksham_thithi", colors.primaryText)

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
        // -------------------------------------------------------------
    // 3. UPDATE 4x2 WIDGET (2x2 Clock Left + 2x2 Elements Area Right)
    // Background Color & Opacity: Driven strictly by Settings/ThemeManager
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

        val db = TamilDatabaseHelper(context)
        val data = db.getDayDetails(queryDate) ?: TamilDatabaseHelper.DayPanchangDetails()
        db.close()

        // 1. Dynamic Background Bitmap generated with Settings color & opacity
        val bgBitmap = createWidgetBackgroundBitmap(context, 580, 280)
        views.safeSetImageBitmap(context, "widget_bg_image", bgBitmap)

        val colors = resolveWidgetColors(context)

        // 2. 2x2 Left Side: Clock (Classic Analog vs Digital with 0.5x AM/PM)
        val clockFace = ThemeManager.getClockFace(context)
        if (clockFace == ThemeManager.CLOCK_ANALOG) {
            views.safeSetVisibility(context, "analog_clock_view", View.VISIBLE)
            views.safeSetVisibility(context, "tv_digital_clock", View.GONE)
        } else {
            views.safeSetVisibility(context, "analog_clock_view", View.GONE)
            views.safeSetVisibility(context, "tv_digital_clock", View.VISIBLE)
            views.safeSetText(context, "tv_digital_clock", formatDigitalTimeWithHalfAmPm(cal))
            views.safeSetTextColor(context, "tv_digital_clock", colors.primaryText)
        }

        // 3. 2x2 Right Side - Row 1: Gregorian Date (Settings choice) + Tamil Weekday
        val dateFormat = ThemeManager.getDateFormat4x2(context)
        val gregDateStr = formatEnglishDate(cal, dateFormat)
        views.safeSetText(context, "tv_widget_greg_date", gregDateStr)
        views.safeSetTextColor(context, "tv_widget_greg_date", colors.primaryText)

        val weekdayStr = if (data.weekdayTa.isNotBlank()) data.weekdayTa else "---"
        views.safeSetText(context, "tv_widget_weekday", weekdayStr)
        views.safeSetTextColor(context, "tv_widget_weekday", colors.accentGold)

        // 4. Row 2: Tamil Month & Date
        val tMonth = if (data.tamilMonth.isNotBlank()) data.tamilMonth else "---"
        val tDay = if (data.tamilDate > 0) data.tamilDate.toString() else "---"
        views.safeSetText(context, "tv_widget_tamil_date", "$tMonth $tDay")
        views.safeSetTextColor(context, "tv_widget_tamil_date", colors.secondaryText)

        // 5. Rows 3, 4, 5: 3 Options Selectable via Settings
        val selectedKeys = ThemeManager.getElements4x2(context).toList()
        val elem1 = resolveElement(selectedKeys.getOrNull(0) ?: ThemeManager.ELEM_PAKSHAM, data)
        val elem2 = resolveElement(selectedKeys.getOrNull(1) ?: ThemeManager.ELEM_THITHI, data)
        val elem3 = resolveElement(selectedKeys.getOrNull(2) ?: ThemeManager.ELEM_NAKSHATRA, data)

        // Row 3
        views.safeSetText(context, "tv_widget_elem1_label", "${elem1.first} :")
        views.safeSetTextColor(context, "tv_widget_elem1_label", colors.accentGold)
        views.safeSetText(context, "tv_widget_elem1_val", elem1.second)
        views.safeSetTextColor(context, "tv_widget_elem1_val", colors.primaryText)

        // Row 4
        views.safeSetText(context, "tv_widget_elem2_label", "${elem2.first} :")
        views.safeSetTextColor(context, "tv_widget_elem2_label", colors.accentGold)
        views.safeSetText(context, "tv_widget_elem2_val", elem2.second)
        views.safeSetTextColor(context, "tv_widget_elem2_val", colors.primaryText)

        // Row 5
        views.safeSetText(context, "tv_widget_elem3_label", "${elem3.first} :")
        views.safeSetTextColor(context, "tv_widget_elem3_label", colors.accentGold)
        views.safeSetText(context, "tv_widget_elem3_val", elem3.second)
        views.safeSetTextColor(context, "tv_widget_elem3_val", colors.primaryText)

        // Open App on Click
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

        val db = TamilDatabaseHelper(context)
        val data = db.getDayDetails(queryDate) ?: TamilDatabaseHelper.DayPanchangDetails()
        db.close()

        val bgBitmap = createWidgetBackgroundBitmap(context, 580, 580)
        views.safeSetImageBitmap(context, "widget_bg_image", bgBitmap)

        val colors = resolveWidgetColors(context)

        val clockFace = ThemeManager.getClockFace(context)
        if (clockFace == ThemeManager.CLOCK_ANALOG) {
            views.safeSetVisibility(context, "analog_clock_view", View.VISIBLE)
            views.safeSetVisibility(context, "digital_clock_view", View.GONE)
        } else {
            views.safeSetVisibility(context, "analog_clock_view", View.GONE)
            views.safeSetVisibility(context, "digital_clock_view", View.VISIBLE)
            views.safeSetText(context, "tv_digital_clock", formatDigitalTimeWithHalfAmPm(cal))
            views.safeSetTextColor(context, "tv_digital_clock", colors.primaryText)
        }

        val dateFormat = ThemeManager.getDateFormat4x4(context)
        views.safeSetText(context, "tv_widget_eng_date", formatEnglishDate(cal, dateFormat))
        views.safeSetTextColor(context, "tv_widget_eng_date", colors.primaryText)

        views.safeSetText(context, "tv_widget_weekday", data.weekdayTa)
        views.safeSetTextColor(context, "tv_widget_weekday", colors.accentGold)

        val tDateStr = if (data.tamilDate > 0) data.tamilDate.toString() else "---"
        views.safeSetText(context, "tv_widget_tamil_full", "${data.tamilYear} • ${data.tamilMonth} $tDateStr")
        views.safeSetTextColor(context, "tv_widget_tamil_full", colors.secondaryText)

        val selectedKeys = ThemeManager.getElements4x4(context).toList()
        val elem1 = resolveElement(selectedKeys.getOrNull(0) ?: ThemeManager.ELEM_PAKSHAM, data)
        val elem2 = resolveElement(selectedKeys.getOrNull(1) ?: ThemeManager.ELEM_THITHI, data)
        val elem3 = resolveElement(selectedKeys.getOrNull(2) ?: ThemeManager.ELEM_NAKSHATRA, data)
        val elem4 = resolveElement(selectedKeys.getOrNull(3) ?: ThemeManager.ELEM_NALLA_NERAM, data)
        val elem5 = resolveElement(selectedKeys.getOrNull(4) ?: ThemeManager.ELEM_GOWRI_NALLA_NERAM, data)

        views.safeSetText(context, "tv_widget_elem1_label", elem1.first)
        views.safeSetTextColor(context, "tv_widget_elem1_label", colors.accentGold)
        views.safeSetText(context, "tv_widget_elem1_val", elem1.second)
        views.safeSetTextColor(context, "tv_widget_elem1_val", colors.secondaryText)

        views.safeSetText(context, "tv_widget_elem2_label", elem2.first)
        views.safeSetTextColor(context, "tv_widget_elem2_label", colors.accentGold)
        views.safeSetText(context, "tv_widget_elem2_val", elem2.second)
        views.safeSetTextColor(context, "tv_widget_elem2_val", colors.secondaryText)

        views.safeSetText(context, "tv_widget_elem3_label", elem3.first)
        views.safeSetTextColor(context, "tv_widget_elem3_label", colors.accentGold)
        views.safeSetText(context, "tv_widget_elem3_val", elem3.second)
        views.safeSetTextColor(context, "tv_widget_elem3_val", colors.secondaryText)

        views.safeSetText(context, "tv_widget_elem4_label", elem4.first)
        views.safeSetTextColor(context, "tv_widget_elem4_label", colors.accentGold)
        views.safeSetText(context, "tv_widget_elem4_val", elem4.second)
        views.safeSetTextColor(context, "tv_widget_elem4_val", colors.secondaryText)

        views.safeSetText(context, "tv_widget_elem5_label", elem5.first)
        views.safeSetTextColor(context, "tv_widget_elem5_label", colors.accentGold)
        views.safeSetText(context, "tv_widget_elem5_val", elem5.second)
        views.safeSetTextColor(context, "tv_widget_elem5_val", colors.secondaryText)

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
