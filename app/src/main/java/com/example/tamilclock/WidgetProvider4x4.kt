package com.example.tamilclock

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class WidgetProvider4x4 : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        if (appWidgetIds.isNotEmpty()) {
            WidgetUpdateHelper.update4x4(
                context,
                appWidgetManager,
                appWidgetIds
            )
        }
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
                refreshAllWidgets(context)
            }
        }
    }

    private fun refreshAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, WidgetProvider4x4::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        onUpdate(context, appWidgetManager, appWidgetIds)
    }
}
