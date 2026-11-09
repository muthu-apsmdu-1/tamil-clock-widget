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
import androidx.core.content.ContextCompat
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
            // Dark Canvases (Black or Maroon)
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
    // Overload handles both callers with & without themeConfig
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

        // 1. Draw Background Fill (Canvas Base + Alpha)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = themeConfig.resolvedBackgroundColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)

        // 2. Draw Accent Outer Border if enabled
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

    // -------------------------------------------------------------
    // DIGITAL CLOCK FORMATTER: BIG TIME + HALF-SIZE AM/PM
    // -------------------------------------------------------------
    fun formatDigitalTimeWithHalfAmPm(cal: Calendar): CharSequence {
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
    // ASTROLOGICAL ELEMENT RESOLVER
private fun resolveElement(key: String, data: PanchangData): Pair<String, String> {
    return when (key) {
        ThemeManager.ELEM_PAKSHAM -> 
            Pair("பக்ஷம்", data.paksham.ifBlank { "—" })

        ThemeManager.ELEM_THITHI -> 
            Pair("திதி", if (data.thithiTime.isNotBlank()) "${data.thithi} (${data.thithiTime})" else data.thithi.ifBlank { "—" })

        ThemeManager.ELEM_NAKSHATRA -> 
            Pair("நட்சத்திரம்", if (data.nakshatraTime.isNotBlank()) "${data.nakshatra} (${data.nakshatraTime})" else data.nakshatra.ifBlank { "—" })

        ThemeManager.ELEM_AMIRTHATHI_YOGA -> 
            Pair("யோகம்", data.yogam.ifBlank { "—" })

        ThemeManager.ELEM_NALLA_NERAM -> 
            Pair("நல்ல நேரம்", data.nallaNeram.ifBlank { "—" })

        ThemeManager.ELEM_GOWRI_NALLA_NERAM -> 
            Pair("கௌரி நல்ல நேரம்", data.gowriNallaNeram.ifBlank { "—" })

        ThemeManager.ELEM_RAHU_KALAM -> 
            Pair("ராகு காலம்", data.rahuKalam.ifBlank { "—" })

        ThemeManager.ELEM_YEMAKANDAM -> 
            Pair("எமகண்டம்", data.emakandam.ifBlank { "—" })

        ThemeManager.ELEM_KULIGAI -> 
            Pair("குளிகை", data.kuligai.ifBlank { "—" })

        ThemeManager.ELEM_SUNRISE_SET -> 
            Pair("சூரியன்", "உதயம்: ${data.sunrise} | மறைவு: ${data.sunset}")

        ThemeManager.ELEM_CHANDRASTAMA -> 
            Pair("சந்திராஷ்டமம்", data.chandrashtamam.ifBlank { "இல்லை" })

        ThemeManager.ELEM_FESTIVAL -> 
            Pair("சிறப்பு", data.festival.ifBlank { "—" })

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

        val db = TamilDatabaseHelper(context)
        val data = db.getDayDetails(queryDate) ?: TamilDatabaseHelper.DayPanchangDetails()
        db.close()

        val themeConfig = ThemeManager.getActiveTheme(context)
        val bgBitmap = createWidgetBackgroundBitmap(context, 320, 160, themeConfig)
        views.safeSetImageBitmap(context, "widget_bg_image", bgBitmap)

        val colors = resolveWidgetColors(context, themeConfig)

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

        // Right Half: Row 1 - Gregorian Date
        val dateFormat = ThemeManager.getDateFormat4x2(context)
        val gregDateStr = formatEnglishDate(cal, dateFormat)
        views.safeSetText(context, "tv_widget_greg_date", gregDateStr)
        views.safeSetTextColor(context, "tv_widget_greg_date", colors.primaryText)

        // Right Half: Row 2 - Tamil Month & Date
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

        val db = TamilDatabaseHelper(context)
        val data = db.getDayDetails(queryDate) ?: TamilDatabaseHelper.DayPanchangDetails()
        db.close()

        val themeConfig = ThemeManager.getActiveTheme(context)
        val bgBitmap = createWidgetBackgroundBitmap(context, 580, 140, themeConfig)
        views.safeSetImageBitmap(context, "widget_bg_image", bgBitmap)

        val colors = resolveWidgetColors(context, themeConfig)

        // Clock
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

        // Gregorian Date + Weekday
        val dateFormat = ThemeManager.getDateFormat4x2(context)
        val gregDateStr = formatEnglishDate(cal, dateFormat)
        views.safeSetText(context, "tv_widget_greg_date", gregDateStr)
        views.safeSetTextColor(context, "tv_widget_greg_date", colors.primaryText)

        val weekdayStr = if (data.weekdayTa.isNotBlank()) data.weekdayTa else "---"
        views.safeSetText(context, "tv_widget_weekday", weekdayStr)
        views.safeSetTextColor(context, "tv_widget_weekday", colors.accentColor)

        // Tamil Date + Paksham & Thithi
        val tMonth = if (data.tamilMonth.isNotBlank()) data.tamilMonth else "---"
        val tDay = if (data.tamilDate > 0) data.tamilDate.toString() else "---"
        views.safeSetText(context, "tv_widget_tamil_date", "$tMonth $tDay")
        views.safeSetTextColor(context, "tv_widget_tamil_date", colors.accentColor)

        val convertedPaksham = convertPakshamName(data.paksha)
        val thithiStr = if (data.tithiName.isNotBlank()) data.tithiName else "---"
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

        val db = TamilDatabaseHelper(context)
        val data = db.getDayDetails(queryDate) ?: TamilDatabaseHelper.DayPanchangDetails()
        db.close()

        val themeConfig = ThemeManager.getActiveTheme(context)
        val bgBitmap = createWidgetBackgroundBitmap(context, 580, 280, themeConfig)
        views.safeSetImageBitmap(context, "widget_bg_image", bgBitmap)

        val colors = resolveWidgetColors(context, themeConfig)

        // Clock
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

        // Header Rows
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

        // Astrological Elements Rows
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

        val db = TamilDatabaseHelper(context)
        val data = db.getDayDetails(queryDate) ?: TamilDatabaseHelper.DayPanchangDetails()
        db.close()

        val themeConfig = ThemeManager.getActiveTheme(context)
        val bgBitmap = createWidgetBackgroundBitmap(context, 580, 580, themeConfig)
        views.safeSetImageBitmap(context, "widget_bg_image", bgBitmap)

        val colors = resolveWidgetColors(context, themeConfig)

        // Clock
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

        // Header Row
        val dateFormat = ThemeManager.getDateFormat4x4(context)
        views.safeSetText(context, "tv_widget_greg_date", formatEnglishDate(cal, dateFormat))
        views.safeSetTextColor(context, "tv_widget_greg_date", colors.primaryText)

        views.safeSetText(context, "tv_widget_weekday", data.weekdayTa)
        views.safeSetTextColor(context, "tv_widget_weekday", colors.accentColor)

        val tDateStr = if (data.tamilDate > 0) data.tamilDate.toString() else "---"
        views.safeSetText(context, "tv_widget_tamil_full", "${data.tamilYear} • ${data.tamilMonth} $tDateStr")
        views.safeSetTextColor(context, "tv_widget_tamil_full", colors.accentColor)

        // Astrological Elements Paragraph Rows
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
