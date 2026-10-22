package com.example.tamilclock

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

abstract class TamilClockWidgetBase(private val layoutResId: Int) : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            updateWidgetView(context, appWidgetManager, appWidgetId, options)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        updateWidgetView(context, appWidgetManager, appWidgetId, newOptions)
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    private fun updateWidgetView(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        options: Bundle
    ) {
        val views = RemoteViews(context.packageName, layoutResId)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dbHelper = TamilDatabaseHelper(context)
        val dateInfo = dbHelper.getDayDetails(todayStr)

        val cal = java.util.Calendar.getInstance()
        val gregFormat = SimpleDateFormat("dd - MM - yyyy", Locale.getDefault())
        views.setTextViewText(R.id.tv_gregorian_date, gregFormat.format(cal.time))

        if (dateInfo != null) {
            views.setTextViewText(R.id.tv_weekday_name, dateInfo.dayName)
            views.setTextViewText(R.id.tv_tamil_date, "${dateInfo.tamilMonth} ${dateInfo.tamilDay}")
            views.setTextViewText(R.id.tv_row4_chip1, "${dateInfo.tamilYear} வருடம்")
            views.setTextViewText(R.id.tv_row4_chip2, "${dateInfo.paksham} - ${dateInfo.tithi}")
        } else {
            views.setTextViewText(R.id.tv_weekday_name, SimpleDateFormat("EEEE", Locale("ta")).format(cal.time))
            views.setTextViewText(R.id.tv_tamil_date, "ஐப்பசி 5")
            views.setTextViewText(R.id.tv_row4_chip1, "பராபவ வருடம்")
            views.setTextViewText(R.id.tv_row4_chip2, "ஏகாதசி")
        }

        // Responsive height check: hide Row 4 when widget is compressed below 100dp
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110)
        if (minHeight < 100) {
            views.setViewVisibility(R.id.layout_row4_container, View.GONE)
        } else {
            views.setViewVisibility(R.id.layout_row4_container, View.VISIBLE)
        }

        // TOUCH RULE: ONLY tv_tamil_date launches the app. Clock & background do nothing.
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.tv_tamil_date, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}

class DigitalClockWidgetProvider : TamilClockWidgetBase(R.layout.widget_digital_clock)
class ClassicClockWidgetProvider : TamilClockWidgetBase(R.layout.widget_classic_clock)
