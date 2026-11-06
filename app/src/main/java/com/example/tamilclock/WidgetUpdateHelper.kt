package com.example.tamilclock

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.*
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
    // SAFE VIEW BINDING HELPER
    // Prevents crash or compile failure if an ID is missing in XML
    // -------------------------------------------------------------
    private fun RemoteViews.safeSetText(context: Context, idName: String, text: CharSequence) {
        val resId = context.resources.getIdentifier(idName, "id", context.packageName)
        if (resId != 0) {
            setTextViewText(resId, text)
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
            color = if (hexColor == ThemeManager.WIDGET_COLOR_OFF_WHITE) Color.parseColor("#BDBDBD") else Color.parseColor("#FFDF00")
            this.alpha = alpha.coerceAtLeast(80)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)

        return bitmap
    }

    // -------------------------------------------------------------
    // DATE VIEW FORMATTER (06 - 11 - 2026 vs 06 Nov 26)
    // -------------------------------------------------------------
    private fun formatEnglishDate(cal: Calendar, formatType: Int): String {
        return if (formatType == ThemeManager.DATE_FORMAT_TEXT) {
            SimpleDateFormat("dd MMM yy", Locale.ENGLISH).format(cal.time)
        } else {
            SimpleDateFormat("dd - MM - yyyy", Locale.ENGLISH).format(cal.time)
        }
    }

    // -------------------------------------------------------------
    // ASTROLOGICAL ELEMENT RESOLVER (TITLE + VALUE WITH "---")
    // -------------------------------------------------------------
    private fun resolveElement(elemKey: String, details: TamilDatabaseHelper.DayPanchangDetails): Pair<String, String> {
        return when (elemKey) {
            ThemeManager.ELEM_PAKSHAM -> Pair("பக்ஷம்", details.paksha)
            ThemeManager.ELEM_THITHI -> Pair("திதி", details.tithiName)
            ThemeManager.ELEM_NAKSHATRA -> Pair("நட்சத்திரம்", details.nakshatraName)
            ThemeManager.ELEM_KARNAM -> Pair("கரணம்", details.karanaName)
            ThemeManager.ELEM_THYAJAM -> Pair("தியாகம்", details.tyajyamTime)
            ThemeManager.ELEM_NALLA_NERAM -> Pair("நல்ல நேரம்", details.nallaNeramMorning)
            ThemeManager.ELEM_GOWRI_NALLA_NERAM -> Pair("கௌரி", details.gowriMorning)
            ThemeManager.ELEM_SOOLAM -> Pair("சூலம்", details.soolam)
            ThemeManager.ELEM_SUNRISE_SET -> Pair("உதயம்/மறைவு", "${details.sunrise} / ${details.sunset}")
            else -> Pair("---", "---")
        }
    }

    // -------------------------------------------------------------
    // 1. UPDATE 2x1 WIDGET (Supports both 2-arg and 3-arg invocations)
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

        val bgBitmap = createWidgetBackgroundBitmap(context, 280, 140)
        views.safeSetImageBitmap(context, "widget_bg_image", bgBitmap)

        views.safeSetText(context, "tv_widget_greg_date", SimpleDateFormat("dd", Locale.ENGLISH).format(cal.time))
        views.safeSetText(context, "tv_widget_greg_month_year", SimpleDateFormat("MMM yyyy", Locale.ENGLISH).format(cal.time).uppercase(Locale.ENGLISH))
        views.safeSetText(context, "tv_widget_weekday", data.weekdayTa)
        views.safeSetText(context, "tv_widget_tamil_month_day", "${data.tamilMonth} ${data.tamilDateStr}")
        views.safeSetText(context, "tv_widget_tamil_date", "${data.tamilMonth} ${data.tamilDateStr}")

        val rootId = context.resources.getIdentifier("widget_root", "id", context.packageName)
        if (rootId != 0) {
            views.setOnClickPendingIntent(rootId, getPendingIntent(context))
        }

        val targetIds = if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
            appWidgetIds
        } else {
            manager.getAppWidgetIds(ComponentName(context, WidgetProvider2x1::class.java))
        }

        if (targetIds.isNotEmpty()) {
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

        val bgBitmap = createWidgetBackgroundBitmap(context, 580, 140)
        views.safeSetImageBitmap(context, "widget_bg_image", bgBitmap)

        views.safeSetText(context, "tv_widget_greg_date", SimpleDateFormat("dd", Locale.ENGLISH).format(cal.time))
        views.safeSetText(context, "tv_widget_weekday", data.weekdayTa)
        views.safeSetText(context, "tv_widget_tamil_month", data.tamilMonth)
        views.safeSetText(context, "tv_widget_tamil_day", data.tamilDateStr)
        views.safeSetText(context, "tv_widget_tithi", data.tithiName)
        views.safeSetText(context, "tv_widget_nalla_neram", data.nallaNeramMorning)

        val rootId = context.resources.getIdentifier("widget_root", "id", context.packageName)
        if (rootId != 0) {
            views.setOnClickPendingIntent(rootId, getPendingIntent(context))
        }

        val targetIds = if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
            appWidgetIds
        } else {
            manager.getAppWidgetIds(ComponentName(context, WidgetProvider4x1::class.java))
        }

        if (targetIds.isNotEmpty()) {
            manager.updateAppWidget(targetIds, views)
        }
    }

    // -------------------------------------------------------------
    // 3. UPDATE 4x2 WIDGET (DATE FORMAT + SELECTABLE 3 ELEMENTS)
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

        val bgBitmap = createWidgetBackgroundBitmap(context, 580, 280)
        views.safeSetImageBitmap(context, "widget_bg_image", bgBitmap)

        val dateFormat = ThemeManager.getDateFormat4x2(context)
        views.safeSetText(context, "tv_widget_eng_date", formatEnglishDate(cal, dateFormat))
        views.safeSetText(context, "tv_widget_weekday", data.weekdayTa)
        views.safeSetText(context, "tv_widget_tamil_date", "${data.tamilMonth} ${data.tamilDateStr}")

        val selectedKeys = ThemeManager.getElements4x2(context).toList()
        val elem1 = resolveElement(selectedKeys.getOrNull(0) ?: ThemeManager.ELEM_PAKSHAM, data)
        val elem2 = resolveElement(selectedKeys.getOrNull(1) ?: ThemeManager.ELEM_THITHI, data)
        val elem3 = resolveElement(selectedKeys.getOrNull(2) ?: ThemeManager.ELEM_NAKSHATRA, data)

        views.safeSetText(context, "tv_widget_elem1_label", elem1.first)
        views.safeSetText(context, "tv_widget_elem1_val", elem1.second)
        views.safeSetText(context, "tv_widget_elem2_label", elem2.first)
        views.safeSetText(context, "tv_widget_elem2_val", elem2.second)
        views.safeSetText(context, "tv_widget_elem3_label", elem3.first)
        views.safeSetText(context, "tv_widget_elem3_val", elem3.second)

        val rootId = context.resources.getIdentifier("widget_root", "id", context.packageName)
        if (rootId != 0) {
            views.setOnClickPendingIntent(rootId, getPendingIntent(context))
        }

        val targetIds = if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
            appWidgetIds
        } else {
            manager.getAppWidgetIds(ComponentName(context, WidgetProvider4x2::class.java))
        }

        if (targetIds.isNotEmpty()) {
            manager.updateAppWidget(targetIds, views)
        }
    }

    // -------------------------------------------------------------
    // 4. UPDATE 4x4 WIDGET (CLOCK FACE + DATE FORMAT + SELECTABLE 5 ELEMENTS)
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

        val clockFace = ThemeManager.getClockFace(context)
        if (clockFace == ThemeManager.CLOCK_CLASSIC_VINTAGE) {
            views.safeSetVisibility(context, "analog_clock_view", View.VISIBLE)
            views.safeSetVisibility(context, "digital_clock_view", View.GONE)
        } else {
            views.safeSetVisibility(context, "analog_clock_view", View.GONE)
            views.safeSetVisibility(context, "digital_clock_view", View.VISIBLE)
            views.safeSetText(context, "tv_digital_clock", SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(cal.time))
        }

        val dateFormat = ThemeManager.getDateFormat4x4(context)
        views.safeSetText(context, "tv_widget_eng_date", formatEnglishDate(cal, dateFormat))
        views.safeSetText(context, "tv_widget_weekday", data.weekdayTa)
        views.safeSetText(context, "tv_widget_tamil_full", "${data.tamilYear} • ${data.tamilMonth} ${data.tamilDateStr}")

        val selectedKeys = ThemeManager.getElements4x4(context).toList()
        val elem1 = resolveElement(selectedKeys.getOrNull(0) ?: ThemeManager.ELEM_PAKSHAM, data)
        val elem2 = resolveElement(selectedKeys.getOrNull(1) ?: ThemeManager.ELEM_THITHI, data)
        val elem3 = resolveElement(selectedKeys.getOrNull(2) ?: ThemeManager.ELEM_NAKSHATRA, data)
        val elem4 = resolveElement(selectedKeys.getOrNull(3) ?: ThemeManager.ELEM_NALLA_NERAM, data)
        val elem5 = resolveElement(selectedKeys.getOrNull(4) ?: ThemeManager.ELEM_GOWRI_NALLA_NERAM, data)

        views.safeSetText(context, "tv_widget_elem1_label", elem1.first)
        views.safeSetText(context, "tv_widget_elem1_val", elem1.second)
        views.safeSetText(context, "tv_widget_elem2_label", elem2.first)
        views.safeSetText(context, "tv_widget_elem2_val", elem2.second)
        views.safeSetText(context, "tv_widget_elem3_label", elem3.first)
        views.safeSetText(context, "tv_widget_elem3_val", elem3.second)
        views.safeSetText(context, "tv_widget_elem4_label", elem4.first)
        views.safeSetText(context, "tv_widget_elem4_val", elem4.second)
        views.safeSetText(context, "tv_widget_elem5_label", elem5.first)
        views.safeSetText(context, "tv_widget_elem5_val", elem5.second)

        val rootId = context.resources.getIdentifier("widget_root", "id", context.packageName)
        if (rootId != 0) {
            views.setOnClickPendingIntent(rootId, getPendingIntent(context))
        }

        val targetIds = if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
            appWidgetIds
        } else {
            manager.getAppWidgetIds(ComponentName(context, WidgetProvider4x4::class.java))
        }

        if (targetIds.isNotEmpty()) {
            manager.updateAppWidget(targetIds, views)
        }
    }

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        update2x1(context, appWidgetManager)
        update4x1(context, appWidgetManager)
        update4x2(context, appWidgetManager)
        update4x4(context, appWidgetManager)
    }
}
