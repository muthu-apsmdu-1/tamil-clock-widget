package com.example.tamilclock

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TamilClockWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED -> {
                updateAllWidgets(context)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, TamilClockWidgetProvider::class.java)
            )
            for (id in ids) {
                updateWidget(context, appWidgetManager, id)
            }
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_tamil_clock)
            val prefs = context.getSharedPreferences("WidgetPreferences", Context.MODE_PRIVATE)

            val textColor = prefs.getInt("text_color", Color.parseColor("#FFEB3B"))
            val bgColor = prefs.getInt("bg_color", Color.parseColor("#000000"))
            val bgAlpha = prefs.getInt("bg_alpha", 80)

            val computedBg = Color.argb(bgAlpha, Color.red(bgColor), Color.green(bgColor), Color.blue(bgColor))

            views.setInt(R.id.widget_root, "setBackgroundColor", computedBg)
            views.setTextColor(R.id.widget_text_clock, textColor)
            views.setTextColor(R.id.tv_gregorian_date, textColor)
            views.setTextColor(R.id.tv_tamil_date, textColor)

            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val displayDateStr = SimpleDateFormat("dd - MM - yyyy", Locale.getDefault()).format(Date())

            val dbHelper = TamilDatabaseHelper(context)
            val info = dbHelper.getDayDetails(todayStr)

            views.setTextViewText(R.id.tv_gregorian_date, displayDateStr)

            if (info != null) {
                views.setTextViewText(R.id.tv_tamil_date, "${info.dayName} - ${info.tamilMonth} ${info.tamilDay}")
            } else {
                views.setTextViewText(R.id.tv_tamil_date, "புதன் - ஐப்பசி 4")
            }

            val clickIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
