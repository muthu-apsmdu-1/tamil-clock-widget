package com.example.tamilclock

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var currentAlpha = 80
    private var currentTextColor = Color.parseColor("#FFEB3B")
    private var currentBgColor = Color.parseColor("#000000")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvPanchangam = findViewById<TextView>(R.id.tv_panchangam_details)
        val alphaSeekBar = findViewById<SeekBar>(R.id.seek_alpha)
        val btnSave = findViewById<Button>(R.id.btn_save_widget)

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dbHelper = TamilDatabaseHelper(this)
        val info = dbHelper.getDayDetails(todayStr)

        if (info != null) {
            tvPanchangam.text = """
                தேதி: ${info.dayName} - ${info.tamilMonth} ${info.tamilDay}
                திதி: ${info.tithi}
                நட்சத்திரம்: ${info.nakshatra}
                நல்ல நேரம்: ${info.nallaNeram}
                ராகு காலம்: ${info.rahuKalam}
                எமகண்டம்: ${info.yamaGandam}
            """.trimIndent()
        } else {
            tvPanchangam.text = "இன்றைய பஞ்சாங்க விவரங்கள் கிடைக்கவில்லை"
        }

        alphaSeekBar.progress = currentAlpha
        alphaSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentAlpha = progress
            }
            override fun onStartTrackingTouch(bar: SeekBar?) {}
            override fun onStopTrackingTouch(bar: SeekBar?) {}
        })

        btnSave.setOnClickListener {
            val prefs = getSharedPreferences("WidgetPreferences", Context.MODE_PRIVATE)
            prefs.edit()
                .putInt("text_color", currentTextColor)
                .putInt("bg_color", currentBgColor)
                .putInt("bg_alpha", currentAlpha)
                .apply()

            TamilClockWidgetProvider.updateAllWidgets(this)
            finish()
        }
    }
}
