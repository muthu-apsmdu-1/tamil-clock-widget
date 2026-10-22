package com.example.tamilclock

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        updateWidgets()
    }

    private fun updateWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(this)

        // Broadcast to Digital Widget
        val digitalComponent = ComponentName(this, DigitalClockWidgetProvider::class.java)
        val digitalIds = appWidgetManager.getAppWidgetIds(digitalComponent)
        if (digitalIds.isNotEmpty()) {
            val intent = Intent(this, DigitalClockWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, digitalIds)
            }
            sendBroadcast(intent)
        }

        // Broadcast to Classic Analog Widget
        val classicComponent = ComponentName(this, ClassicClockWidgetProvider::class.java)
        val classicIds = appWidgetManager.getAppWidgetIds(classicComponent)
        if (classicIds.isNotEmpty()) {
            val intent = Intent(this, ClassicClockWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, classicIds)
            }
            sendBroadcast(intent)
        }
    }
}
