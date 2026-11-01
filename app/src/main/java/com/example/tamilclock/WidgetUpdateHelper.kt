package com.example.tamilclock

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
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

    private fun applyClockStyle(views: RemoteViews, context: Context) {
        val clockStyle = ThemeManager.getClockStyle(context)
        if (clockStyle == ThemeManager.CLOCK_DIGITAL) {
            views.setViewVisibility(R.id.analog_clock, View.GONE)
            views.setViewVisibility(R.id.digital_clock, View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.analog_clock, View.VISIBLE)
            views.setViewVisibility(R.id.digital_clock, View.GONE)
        }
    }

    fun update2x1(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val cal = Calendar.getInstance()
        val queryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        val gregDate = SimpleDateFormat("dd: MM: yyyy", Locale.ENGLISH).format(cal.time)

        val dbHelper = TamilDatabaseHelper(context)
        val data = dbHelper.getDayDetails(queryDate)

        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_2x1)
            applyClockStyle(views, context)
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
            val views = RemoteViews(context.packageName, R.layout.widget_4x1)
            applyClockStyle(views, context)
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
            val views = RemoteViews(context.packageName, R.layout.widget_4x2)
            applyClockStyle(views, context)
            views.setOnClickPendingIntent(R.id.widget_root, getPendingIntent(context))

            // Row 1: dd: mm: yyyy + week day in english
            views.setTextViewText(R.id.tv_widget_row1, gregWeekday)

            // Row 2: Tamil month + tamil date + paksham
            val pakshaStr = if (data.paksham.isNotEmpty()) " • ${data.paksham}" else ""
            views.setTextViewText(R.id.tv_widget_row2, "${data.tamilMonth} ${data.tamilDay}$pakshaStr")

            // Row 3: Thithi + timing + nakshatra + timing
            val tTime = if (data.tithiTime.isNotEmpty()) " ${data.tithiTime}" else ""
            val nTime = if (data.nakshatraTime.isNotEmpty()) " ${data.nakshatraTime}" else ""
            views.setTextViewText(R.id.tv_widget_row3, "${data.tithi}$tTime • ${data.nakshatra}$nTime")

            // Row 4: Aspecius time ந.நே : + timing
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
            val views = RemoteViews(context.packageName, R.layout.widget_4x4)
            applyClockStyle(views, context)
            views.setOnClickPendingIntent(R.id.widget_root, getPendingIntent(context))

            // Inside dial date & weekday
            views.setTextViewText(R.id.tv_widget_dial_date, gregDate)
            views.setTextViewText(R.id.tv_widget_dial_weekday, weekday)

            // Bottom Row 1: Tamil month + tamil date + paksham + Thithi + timing
            val tTime = if (data.tithiTime.isNotEmpty()) " ${data.tithiTime}" else ""
            views.setTextViewText(
                R.id.tv_widget_4x4_row1,
                "${data.tamilMonth} ${data.tamilDay} • ${data.paksham} • ${data.tithi}$tTime"
            )

            // Bottom Row 2: nakshatra + timing + Aspecius time ந.நே : + timing
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
