
package com.example.tamilclock

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class WidgetProvider4x1 : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        WidgetUpdateHelper.update4x1(
            context,
            appWidgetManager,
            appWidgetIds
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {

            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_USER_PRESENT,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {

                val appWidgetManager =
                    AppWidgetManager.getInstance(context)

                val thisWidget =
                    ComponentName(context, WidgetProvider4x1::class.java)

                val appWidgetIds =
                    appWidgetManager.getAppWidgetIds(thisWidget)

                if (appWidgetIds.isNotEmpty()) {
                    WidgetUpdateHelper.update4x1(
                        context,
                        appWidgetManager,
                        appWidgetIds
                    )
                }
            }
        }
    }
}
