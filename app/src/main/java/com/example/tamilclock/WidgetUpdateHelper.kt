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

    const val PREFS_NAME = "tamil_clock_widget_prefs"
    const val KEY_ANALOG_CLOCK = "pref_widget_analog_clock"
    const val KEY_4X4_SHOW_NAKSHATRA = "pref_4x4_show_nakshatra"
    const val KEY_4X4_SHOW_NALLA_NERAM = "pref_4x4_show_nalla_neram"
    const val KEY_4X4_SHOW_RAHU = "pref_4x4_show_rahu"
    const val KEY_4X4_SHOW_YAMAGANDAM = "pref_4x4_show_yamagandam"

    fun update2x1(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val (data, gregDate) = getDayInfo(context)
        val isAnalog = isAnalogClockPreferred(context)

        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout_2x1)
            attachClickPendingIntent(context, views, R.id.widget_2x1_root)

            setClockVisibility(views, R.id.clock_analog_2x1, R.id.clock_digital_2x1, isAnalog)
            views.setTextViewText(R.id.tv_2x1_greg_date, gregDate)

            if (data != null) {
                views.setTextViewText(R.id.tv_2x1_tamil_date, "${data.tamilMonth} ${data.tamilDay} ${getDayOfWeekTamil()}")
            }
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    fun update4x1(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val (data, gregDate) = getDayInfo(context)
        val isAnalog = isAnalogClockPreferred(context)

        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout_4x1)
            attachClickPendingIntent(context, views, R.id.widget_4x1_root)

            setClockVisibility(views, R.id.clock_analog_4x1, R.id.clock_digital_4x1, isAnalog)

            if (data != null) {
                views.setTextViewText(R.id.tv_4x1_dates_line, "$gregDate | ${data.tamilMonth} ${data.tamilDay}")
                views.setTextViewText(R.id.tv_4x1_tithi_line, "${getDayOfWeekTamil()} - ${data.tithi}")
            }
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    fun update4x2(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val (data, gregDate) = getDayInfo(context)
        val isAnalog = isAnalogClockPreferred(context)

        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout_4x2)
            attachClickPendingIntent(context, views, R.id.widget_4x2_root)

            setClockVisibility(views, R.id.clock_analog_4x2, R.id.clock_digital_4x2, isAnalog)
            views.setTextViewText(R.id.tv_4x2_greg_date, gregDate)

            if (data != null) {
                views.setTextViewText(R.id.tv_4x2_tamil_date, "${data.tamilMonth} ${data.tamilDay} ${getDayOfWeekTamil()}")
                views.setTextViewText(R.id.tv_4x2_tithi, data.tithi)
                views.setTextViewText(R.id.tv_4x2_nakshatra, "★ ${data.nakshatra} ${data.paksham}")
            }
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    fun update4x4(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val (data, _) = getDayInfo(context)
        val isAnalog = isAnalogClockPreferred(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val showNakshatra = prefs.getBoolean(KEY_4X4_SHOW_NAKSHATRA, true)
        val showNallaNeram = prefs.getBoolean(KEY_4X4_SHOW_NALLA_NERAM, true)
        val showRahu = prefs.getBoolean(KEY_4X4_SHOW_RAHU, true)
        val showYamagandam = prefs.getBoolean(KEY_4X4_SHOW_YAMAGANDAM, false)

        val fullGregFormat = SimpleDateFormat("dd - MM - yyyy | EEEE", Locale("ta"))
        val fullGregDate = fullGregFormat.format(Calendar.getInstance().time)

        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_classic_4x4)
            attachClickPendingIntent(context, views, R.id.widget_4x4_root)

            setClockVisibility(views, R.id.clock_analog_4x4, R.id.clock_digital_4x4, isAnalog)
            views.setTextViewText(R.id.tv_4x4_greg_date, fullGregDate)

            if (data != null) {
                views.setTextViewText(R.id.tv_4x4_tamil_date, "${data.tamilYear} வருடம் - ${data.tamilMonth} ${data.tamilDay}")
                views.setTextViewText(R.id.tv_4x4_tithi, data.tithi)

                views.setViewVisibility(R.id.tv_4x4_nakshatra, if (showNakshatra) View.VISIBLE else View.GONE)
                views.setTextViewText(R.id.tv_4x4_nakshatra, "★ நட்சத்திரம்: ${data.nakshatra} ${data.paksham}")

                views.setViewVisibility(R.id.tv_4x4_nalla_neram, if (showNallaNeram) View.VISIBLE else View.GONE)
                val nallaNeram = listOf(data.nallaNeramMorning, data.nallaNeramEvening).filter { it.isNotEmpty() }.joinToString(" | ")
                views.setTextViewText(R.id.tv_4x4_nalla_neram, "☉ ந.நேரம்: ${nallaNeram.ifEmpty { "காலை 10.45 - 11.45" }}")

                views.setViewVisibility(R.id.tv_4x4_rahu, if (showRahu) View.VISIBLE else View.GONE)
                views.setTextViewText(R.id.tv_4x4_rahu, "⊘ இராகு: ${data.rahuKalam}")

                views.setViewVisibility(R.id.tv_4x4_yamagandam, if (showYamagandam) View.VISIBLE else View.GONE)
                views.setTextViewText(R.id.tv_4x4_yamagandam, "☠ எமகண்டம்: ${data.yamaGandam}")
            }
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    private fun getDayInfo(context: Context): Pair<DayInfo?, String> {
        val dbHelper = TamilDatabaseHelper(context)
        val today = Calendar.getInstance()
        val queryFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val gregFormat = SimpleDateFormat("dd - MM - yyyy", Locale.getDefault())
        val data = dbHelper.getDayDetails(queryFormat.format(today.time))
        return Pair(data, gregFormat.format(today.time))
    }

    private fun getDayOfWeekTamil(): String {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "ஞாயிறு"
            Calendar.MONDAY -> "திங்கள்"
            Calendar.TUESDAY -> "செவ்வாய்"
            Calendar.WEDNESDAY -> "புதன்"
            Calendar.THURSDAY -> "வியாழன்"
            Calendar.FRIDAY -> "வெள்ளி"
            Calendar.SATURDAY -> "சனி"
            else -> ""
        }
    }

    private fun setClockVisibility(views: RemoteViews, analogId: Int, digitalId: Int, isAnalog: Boolean) {
        if (isAnalog) {
            views.setViewVisibility(analogId, View.VISIBLE)
            views.setViewVisibility(digitalId, View.GONE)
        } else {
            views.setViewVisibility(analogId, View.GONE)
            views.setViewVisibility(digitalId, View.VISIBLE)
        }
    }

    private fun attachClickPendingIntent(context: Context, views: RemoteViews, targetId: Int) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(targetId, pendingIntent)
    }

    private fun isAnalogClockPreferred(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ANALOG_CLOCK, true)
    }
}
