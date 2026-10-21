package com.example.tamilclock

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
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

        val prefs = getSharedPreferences("WidgetPreferences", Context.MODE_PRIVATE)
        currentTextColor = prefs.getInt("text_color", Color.parseColor("#FFEB3B"))
        currentBgColor = prefs.getInt("bg_color", Color.parseColor("#000000"))
        currentAlpha = prefs.getInt("bg_alpha", 80)

        val tvPanchangam = findViewById<TextView>(R.id.tv_panchangam_details)
        val previewBox = findViewById<View>(R.id.preview_box)
        val previewClock = findViewById<TextView>(R.id.preview_clock)
        val previewDate = findViewById<TextView>(R.id.preview_date)

        val btnYellow = findViewById<Button>(R.id.btn_color_yellow)
        val btnWhite = findViewById<Button>(R.id.btn_color_white)
        val btnCyan = findViewById<Button>(R.id.btn_color_cyan)
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
            previewDate.text = "${info.dayName} - ${info.tamilMonth} ${info.tamilDay}"
        } else {
            tvPanchangam.text = "இன்றைய பஞ்சாங்க விவரங்கள் கிடைக்கவில்லை"
        }

        fun updatePreview() {
            val bg = Color.argb(
                currentAlpha,
                Color.red(currentBgColor),
                Color.green(currentBgColor),
                Color.blue(currentBgColor)
            )
            previewBox.setBackgroundColor(bg)
            previewClock.setTextColor(currentTextColor)
            previewDate.setTextColor(currentTextColor)
        }

        updatePreview()

        btnYellow.setOnClickListener {
            currentTextColor = Color.parseColor("#FFEB3B")
            updatePreview()
        }
        btnWhite.setOnClickListener {
            currentTextColor = Color.parseColor("#FFFFFF")
            updatePreview()
        }
        btnCyan.setOnClickListener {
            currentTextColor = Color.parseColor("#00E5FF")
            updatePreview()
        }

        alphaSeekBar.progress = currentAlpha
        alphaSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentAlpha = progress
                updatePreview()
            }
            override fun onStartTrackingTouch(bar: SeekBar?) {}
            override fun onStopTrackingTouch(bar: SeekBar?) {}
        })

        btnSave.setOnClickListener {
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
