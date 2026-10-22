package com.example.tamilclock

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: TamilDatabaseHelper
    private var currentDate = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = TamilDatabaseHelper(this)

        updateUI()
        updateWidgets()
    }

    private fun updateUI() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = dateFormat.format(currentDate.time)
        val info = dbHelper.getDayDetails(dateString)

        val tvTamilDate = findViewById<TextView?>(R.id.tv_tamil_date)
        val tvNallaNeram = findViewById<TextView?>(R.id.tv_nalla_neram)

        if (info != null) {
            tvTamilDate?.text = "${info.tamilMonth} ${info.tamilDay}, ${info.tamilYear}"
            tvNallaNeram?.text = "${info.nallaNeramMorning} / ${info.nallaNeramEvening}"
        } else {
            tvTamilDate?.text = "ஐப்பசி 5"
            tvNallaNeram?.text = "10:45 AM - 11:45 AM"
        }
    }

    private fun updateWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(this)

        // Notify Digital Widget
        val digitalComponent = ComponentName(this, DigitalClockWidgetProvider::class.java)
        val digitalIds = appWidgetManager.getAppWidgetIds(digitalComponent)
        if (digitalIds.isNotEmpty()) {
            val intent = Intent(this, DigitalClockWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, digitalIds)
            }
            sendBroadcast(intent)
        }

        // Notify Classic Analog Widget
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
