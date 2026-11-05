package com.example.tamilclock

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object WidgetUpdateHelper {

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

    /**
     * Creates a rounded rectangle bitmap with user-selected background color,
     * exact opacity, and a crisp gold border. 100% safe for Android RemoteViews.
     */
    private fun createWidgetBackgroundBitmap(context: Context, width: Int, height: Int): Bitmap {
        val w = if (width > 0) width else 400
        val h = if (height > 0) height else 200

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Fetch user theme color
        val baseColor = when (ThemeManager.getSavedTheme(context)) {
            ThemeManager.THEME_WHITE -> Color.parseColor("#FFFFFF")
            ThemeManager.THEME_YELLOW -> Color.parseColor("#FFF9E6")
            ThemeManager.THEME_GREEN -> Color.parseColor("#1B4D3E")
            ThemeManager.THEME_CHARCOAL -> Color.parseColor("#1E1E24")
            else -> Color.parseColor("#78000C") // Default Maroon
        }

        // 2. Calculate Alpha
        val opacity = ThemeManager.getWidgetOpacity(context)
        val alpha = (opacity * 255 / 100).coerceIn(0, 255)
        val finalFillColor = Color.argb(alpha, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))

        val strokeWidth = 6f
        val cornerRadius = 36f
        val rect = RectF(strokeWidth / 2, strokeWidth / 2, w - strokeWidth / 2, h - strokeWidth / 2)

        // Draw Fill
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = finalFillColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, fillPaint)

        // Draw Gold Border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFDF00")
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)

        return bitmap
    }

    private fun applyStyleAndBackground(views: RemoteViews, context: Context, width: Int, height: Int) {
        val clockStyle = ThemeManager.getClockStyle(context)
        if (clockStyle == ThemeManager.CLOCK_DIGITAL) {
            views.setViewVisibility(R.id.analog_clock, View.GONE)
            views.setViewVisibility(R.id.digital_clock, View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.analog_clock, View.VISIBLE)
            views.setViewVisibility(R.id.digital_clock, View.GONE)
        }

        // Apply dynamically generated background bitmap safely
        val bgBitmap = createWidgetBackgroundBitmap(context, width, height)
        views.setImageViewBitmap(R.id.widget_bg_image, bgBitmap)
    }

    fun update2x1(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val cal = Calendar.getInstance()
        val queryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        val gregDate = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(cal.time)

        val dbHelper = TamilDatabaseHelper(context)
        val data = dbHelper.getDayDetails(queryDate) ?: TamilDatabaseHelper.DayPanchangDetails()

        val weekdayTa = data.dayNameTa.ifEmpty { "திங்கள்" }
        val tamilDateText = if (data.tamilMonth.isNotEmpty() && data.tamilDay > 0) {
            "$weekdayTa - ${data.tamilMonth} ${data.tamilDay}"
        } else {
            weekdayTa
        }

        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout_2x1)
            applyStyleAndBackground(views, context, 500, 240)
            views.setOnClickPendingIntent(R.id.widget_root, getPendingIntent(context))

            views.setTextViewText(R.id.tv_widget_greg_date, gregDate)
            views.setTextViewText(R.id.tv_widget_tamil_date, tamilDateText)

            appWidgetManager.updateAppWidget(id, views)
        }
    }

    fun update4x1(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val cal = Calendar.getInstance()
        val queryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        val gregWeekday = SimpleDateFormat("dd: MM: yyyy EEEE", Locale.ENGLISH).format(cal.time)

        val dbHelper = TamilDatabaseHelper(context)
        val data = dbHelper.getDayDetails(queryDate) ?: TamilDatabaseHelper.DayPanchangDetails()

        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout_4x1)
            applyStyleAndBackground(views, context, 900, 240)
            views.setOnClickPendingIntent(R.id.widget_root, getPendingIntent(context))

            views.setTextViewText(R.id.tv_widget_greg_date_weekday, gregWeekday)
            val tithiStr = if (data.tithi.isNotEmpty()) " • ${data.tithi}" else ""
            val monthDateStr = if (data.tamilMonth.isNotEmpty() && data.tamilDay > 0) {
                "${data.tamilMonth} ${data.tamilDay}$tithiStr"
            } else {
                data.tithi
            }
            views.setTextViewText(R.id.tv_widget_tamil_month_date_tithi, monthDateStr)

            appWidgetManager.updateAppWidget(id, views)
        }
    }

    fun update4x2(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val cal = Calendar.getInstance()
        val queryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        val gregWeekday = SimpleDateFormat("dd: MM: yyyy EEEE", Locale.ENGLISH).format(cal.time)

        val dbHelper = TamilDatabaseHelper(context)
        val data = dbHelper.getDayDetails(queryDate) ?: TamilDatabaseHelper.DayPanchangDetails()

        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout_4x2)
            applyStyleAndBackground(views, context, 900, 450)
            views.setOnClickPendingIntent(R.id.widget_root, getPendingIntent(context))

            views.setTextViewText(R.id.tv_widget_row1, gregWeekday)
            val pakshaStr = if (data.paksham.isNotEmpty()) " • ${data.paksham}" else ""
            views.setTextViewText(R.id.tv_widget_row2, "${data.tamilMonth} ${data.tamilDay}$pakshaStr")

            val tTime = if (data.tithiTime.isNotEmpty()) " ${data.tithiTime}" else ""
            val nTime = if (data.nakshatraTime.isNotEmpty()) " ${data.nakshatraTime}" else ""
            views.setTextViewText(R.id.tv_widget_row3, "${data.tithi}$tTime • ${data.nakshatra}$nTime")

            val nnMorning = data.nallaNeramMorning
            val nnEvening = data.nallaNeramEvening
            val nn = when {
                nnMorning.isNotBlank() && nnEvening.isNotBlank() -> "$nnMorning / $nnEvening"
                nnMorning.isNotBlank() -> nnMorning
                nnEvening.isNotBlank() -> nnEvening
                else -> ""
            }
            views.setTextViewText(R.id.tv_widget_row4, "ந.நே : $nn")

            appWidgetManager.updateAppWidget(id, views)
        }
    }

    fun update4x4(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val cal = Calendar.getInstance()
        val queryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        val gregDate = SimpleDateFormat("dd: MM: yyyy", Locale.ENGLISH).format(cal.time)
        val weekday = SimpleDateFormat("EEEE", Locale.ENGLISH).format(cal.time)

        val dbHelper = TamilDatabaseHelper(context)
        val data = dbHelper.getDayDetails(queryDate) ?: TamilDatabaseHelper.DayPanchangDetails()

        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout_4x4)
            applyStyleAndBackground(views, context, 900, 900)
            views.setOnClickPendingIntent(R.id.widget_root, getPendingIntent(context))

            views.setTextViewText(R.id.tv_widget_dial_date, gregDate)
            views.setTextViewText(R.id.tv_widget_dial_weekday, weekday)

            val tTime = if (data.tithiTime.isNotEmpty()) " ${data.tithiTime}" else ""
            views.setTextViewText(
                R.id.tv_widget_4x4_row1,
                "${data.tamilMonth} ${data.tamilDay} • ${data.paksham} • ${data.tithi}$tTime"
            )

            val nTime = if (data.nakshatraTime.isNotEmpty()) " ${data.nakshatraTime}" else ""
            val nn = data.nallaNeramMorning.ifEmpty { data.nallaNeramEvening }
            views.setTextViewText(
                R.id.tv_widget_4x4_row2,
                "${data.nakshatra}$nTime | ந.நே : $nn"
            )

            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
