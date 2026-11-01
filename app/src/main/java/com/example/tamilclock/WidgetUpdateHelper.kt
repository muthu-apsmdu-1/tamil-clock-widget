package com.example.tamilclock

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
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

    // 12. சரியான ColorFilter மற்றும் Clock Style அப்ளை செய்யும் முறை
    private fun applyStyleAndBackground(views: RemoteViews, context: Context) {
        val clockStyle = ThemeManager.getClockStyle(context)
        if (clockStyle == ThemeManager.CLOCK_DIGITAL) {
            views.setViewVisibility(R.id.analog_clock, View.GONE)
            views.setViewVisibility(R.id.digital_clock, View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.analog_clock, View.VISIBLE)
            views.setViewVisibility(R.id.digital_clock, View.GONE)
        }

        // விட்ஜெட் வெளிப்படைத்தன்மை நிறம் (Opacity Color Filter)
        val opacity = ThemeManager.getWidgetOpacity(context)
        val alpha = (opacity * 255 / 100).coerceIn(0, 255)
        val tintColor = Color.argb(alpha, 0x1E, 0x1E, 0x24)

        views.setInt(R.id.widget_root, "setColorFilter", tintColor)
        views.setInt(R.id.widget_root, "setImageAlpha", alpha)
    }

    fun update2x1(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val cal = Calendar.getInstance()
        val queryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        val gregDate = SimpleDateFormat("dd - MM - yy", Locale.ENGLISH).format(cal.time)

        val dbHelper = TamilDatabaseHelper(context)
        val data = dbHelper.getDayDetails(queryDate)

        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout_2x1)
            applyStyleAndBackground(views, context)
            views.setOnClickPendingIntent(R.id.widget_root, getPendingIntent(context))

            views.setTextViewText(R.id.tv_widget_greg_date, gregDate)
            views.setTextViewText(R.id.tv_widget_tamil_date, "${data.tamilMonth} ${data.tamilDay}")

            appWidgetManager.updateAppWidget(id, views)
        }
    }

    fun update4x1(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val cal = Calendar.getInstance()
        val queryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        val gregWeekday = SimpleDateFormat("dd: MM: yyyy EEEE", Locale.ENGLISH).format(cal.time)

        val dbHelper = TamilDatabaseHelper(context)
        val data = dbHelper.getDayDetails(queryDate)

        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout_4x1)
            applyStyleAndBackground(views, context)
            views.setOnClickPendingIntent(R.id.widget_root, getPendingIntent(context))

            views.setTextViewText(R.id.tv_widget_greg_date_weekday, gregWeekday)
            val tithiStr = if (data.tithi.isNotEmpty()) " • ${data.tithi}" else ""
            views.setTextViewText(R.id.tv_widget_tamil_month_date_tithi, "${data.tamilMonth} ${data.tamilDay}$tithiStr")

            appWidgetManager.updateAppWidget(id, views)
        }
    }

    fun update4x2(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val cal = Calendar.getInstance()
        val queryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        val gregWeekday = SimpleDateFormat("dd: MM: yyyy EEEE", Locale.ENGLISH).format(cal.time)

        val dbHelper = TamilDatabaseHelper(context)
        val data = dbHelper.getDayDetails(queryDate)

        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout_4x2)
            applyStyleAndBackground(views, context)
            views.setOnClickPendingIntent(R.id.widget_root, getPendingIntent(context))

            views.setTextViewText(R.id.tv_widget_row1, gregWeekday)
            val pakshaStr = if (data.paksham.isNotEmpty()) " • ${data.paksham}" else ""
            views.setTextViewText(R.id.tv_widget_row2, "${data.tamilMonth} ${data.tamilDay}$pakshaStr")

            val tTime = if (data.tithiTime.isNotEmpty()) " ${data.tithiTime}" else ""
            val nTime = if (data.nakshatraTime.isNotEmpty()) " ${data.nakshatraTime}" else ""
            views.setTextViewText(R.id.tv_widget_row3, "${data.tithi}$tTime • ${data.nakshatra}$nTime")

            val nn = data.nallaNeram.ifEmpty { "${data.nallaNeramMorning} / ${data.nallaNeramEvening}" }
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
        val data = dbHelper.getDayDetails(queryDate)

        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout_4x4)
            applyStyleAndBackground(views, context)
            views.setOnClickPendingIntent(R.id.widget_root, getPendingIntent(context))

            views.setTextViewText(R.id.tv_widget_dial_date, gregDate)
            views.setTextViewText(R.id.tv_widget_dial_weekday, weekday)

            val tTime = if (data.tithiTime.isNotEmpty()) " ${data.tithiTime}" else ""
            views.setTextViewText(
                R.id.tv_widget_4x4_row1,
                "${data.tamilMonth} ${data.tamilDay} • ${data.paksham} • ${data.tithi}$tTime"
            )

            val nTime = if (data.nakshatraTime.isNotEmpty()) " ${data.nakshatraTime}" else ""
            val nn = data.nallaNeram.ifEmpty { data.nallaNeramMorning }
            views.setTextViewText(
                R.id.tv_widget_4x4_row2,
                "${data.nakshatra}$nTime | ந.நே : $nn"
            )

            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
