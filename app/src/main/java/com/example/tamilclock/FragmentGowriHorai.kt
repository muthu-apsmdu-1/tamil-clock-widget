package com.example.tamilclock

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Calendar
import java.util.Locale

class FragmentGowriHorai : Fragment() {

    private lateinit var tabBtnGowri: TextView
    private lateinit var tabBtnHorai: TextView
    private lateinit var layoutGowriTable: View
    private lateinit var layoutHoraiTable: View
    private lateinit var tvHeaderStripDate: TextView

    private lateinit var containerGowriDay: LinearLayout
    private lateinit var containerGowriNight: LinearLayout
    private lateinit var containerHoraiDay: LinearLayout
    private lateinit var containerHoraiNight: LinearLayout

    private var isGowriSelected = true
    private var isSelectedDateToday = true
    private var selectedCalendar: Calendar = Calendar.getInstance()

    private val refreshHandler = Handler(Looper.getMainLooper())
    private val timeTickRunnable = object : Runnable {
        override fun run() {
            highlightCurrentSlotIfToday()
            refreshHandler.postDelayed(this, 30_000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gowri_horai, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabBtnGowri = view.findViewById(R.id.tab_btn_gowri)
        tabBtnHorai = view.findViewById(R.id.tab_btn_horai)
        layoutGowriTable = view.findViewById(R.id.layout_gowri_table)
        layoutHoraiTable = view.findViewById(R.id.layout_horai_table)
        tvHeaderStripDate = view.findViewById(R.id.tv_header_strip_date)

        containerGowriDay = view.findViewById(R.id.container_gowri_day_slots)
        containerGowriNight = view.findViewById(R.id.container_gowri_night_slots)
        containerHoraiDay = view.findViewById(R.id.container_horai_day_slots)
        containerHoraiNight = view.findViewById(R.id.container_horai_night_slots)

        tabBtnGowri.setOnClickListener { switchTab(showGowri = true) }
        tabBtnHorai.setOnClickListener { switchTab(showGowri = false) }

        updateDate(Calendar.getInstance())
    }

    private fun switchTab(showGowri: Boolean) {
        isGowriSelected = showGowri
        if (showGowri) {
            layoutGowriTable.visibility = View.VISIBLE
            layoutHoraiTable.visibility = View.GONE
            tabBtnGowri.setBackgroundResource(R.drawable.bg_tab_selected)
            tabBtnGowri.setTextColor(Color.WHITE)
            tabBtnHorai.setBackgroundResource(R.drawable.bg_tab_unselected)
            tabBtnHorai.setTextColor(Color.parseColor("#9E9E9E"))
        } else {
            layoutGowriTable.visibility = View.GONE
            layoutHoraiTable.visibility = View.VISIBLE
            tabBtnHorai.setBackgroundResource(R.drawable.bg_tab_selected)
            tabBtnHorai.setTextColor(Color.WHITE)
            tabBtnGowri.setBackgroundResource(R.drawable.bg_tab_unselected)
            tabBtnGowri.setTextColor(Color.parseColor("#9E9E9E"))
        }
        highlightCurrentSlotIfToday()
    }

    fun updateDate(cal: Calendar) {
        selectedCalendar = cal.clone() as Calendar
        val today = Calendar.getInstance()
        isSelectedDateToday = (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))

        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR) % 100
        val dayName = getTamilDayName(cal.get(Calendar.DAY_OF_WEEK))

        tvHeaderStripDate.text = String.format(Locale.getDefault(), "%d - %d - %02d, %s", day, month, year, dayName)

        if (isSelectedDateToday) {
            tvHeaderStripDate.textSize = 15f
            tvHeaderStripDate.setTextColor(Color.parseColor("#081E7D"))
        } else {
            tvHeaderStripDate.textSize = 13f
            tvHeaderStripDate.setTextColor(Color.parseColor("#B71C1C"))
        }

        renderTablesFromSqlite(cal)
        highlightCurrentSlotIfToday()
    }

    private fun getDb(dbName: String): SQLiteDatabase {
        val dbFile = requireContext().getDatabasePath(dbName)
        if (!dbFile.exists()) {
            dbFile.parentFile?.mkdirs()
            val inputStream: InputStream = requireContext().assets.open(dbName)
            val outputStream = FileOutputStream(dbFile)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
        }
        return SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
    }

    private fun renderTablesFromSqlite(cal: Calendar) {
        containerGowriDay.removeAllViews()
        containerGowriNight.removeAllViews()
        containerHoraiDay.removeAllViews()
        containerHoraiNight.removeAllViews()

        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
        val dayNameEng = when (dayOfWeek) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Sunday"
        }

        // 1. Query gowri.db
        try {
            val gowriDb = getDb("gowri.db")
            loadGowriData(gowriDb, dayNameEng, dayOfWeek)
            gowriDb.close()
        } catch (e: Exception) {
            e.printStackTrace()
            fallbackGowri(dayOfWeek)
        }

        // 2. Query horai.db
        try {
            val horaiDb = getDb("horai.db")
            loadHoraiData(horaiDb, dayNameEng, dayOfWeek)
            horaiDb.close()
        } catch (e: Exception) {
            e.printStackTrace()
            fallbackHorai(dayOfWeek)
        }
    }

    private fun loadGowriData(db: SQLiteDatabase, dayNameEng: String, dayOfWeek: Int) {
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM gowri WHERE day = ? OR day_id = ? ORDER BY id ASC",
            arrayOf(dayNameEng, dayOfWeek.toString())
        )
        if (cursor.moveToFirst()) {
            do {
                val time = cursor.getString(cursor.getColumnIndexOrThrow("time"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val periodType = cursor.getString(cursor.getColumnIndexOrThrow("type")) // "day" or "night"
                val isGood = isGowriGood(name)

                val (sMin, eMin) = parseTimeRangeToMinutes(time)
                val item = SlotItem(time, name, sMin, eMin, isGood)

                if (periodType.equals("night", ignoreCase = true)) {
                    containerGowriNight.addView(createSlotRowView(item))
                } else {
                    containerGowriDay.addView(createSlotRowView(item))
                }
            } while (cursor.moveToNext())
        } else {
            fallbackGowri(dayOfWeek)
        }
        cursor.close()
    }

    private fun loadHoraiData(db: SQLiteDatabase, dayNameEng: String, dayOfWeek: Int) {
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM horai WHERE day = ? OR day_id = ? ORDER BY id ASC",
            arrayOf(dayNameEng, dayOfWeek.toString())
        )
        if (cursor.moveToFirst()) {
            do {
                val time = cursor.getString(cursor.getColumnIndexOrThrow("time"))
                val planet = cursor.getString(cursor.getColumnIndexOrThrow("planet"))
                val periodType = cursor.getString(cursor.getColumnIndexOrThrow("type")) // "day" or "night"
                val isGood = isHoraiGood(planet)

                val (sMin, eMin) = parseTimeRangeToMinutes(time)
                val item = SlotItem(time, planet, sMin, eMin, isGood)

                if (periodType.equals("night", ignoreCase = true)) {
                    containerHoraiNight.addView(createSlotRowView(item))
                } else {
                    containerHoraiDay.addView(createSlotRowView(item))
                }
            } while (cursor.moveToNext())
        } else {
            fallbackHorai(dayOfWeek)
        }
        cursor.close()
    }

    private fun isGowriGood(name: String): Boolean {
        return name.contains("லாபம்") || name.contains("சுகம்") || 
               name.contains("அமிர்தம்") || name.contains("உத்தி") || name.contains("சுபம்")
    }

    private fun isHoraiGood(planet: String): Boolean {
        // Jupiter, Venus, Mercury, Moon are generally auspicious
        return planet.contains("குரு") || planet.contains("சுக்கிரன்") || 
               planet.contains("புதன்") || planet.contains("சந்திரன்")
    }

    private fun parseTimeRangeToMinutes(timeRange: String): Pair<Int, Int> {
        return try {
            val parts = timeRange.split("-")
            val sParts = parts[0].trim().split(":")
            val eParts = parts[1].trim().split(":")
            val startMin = sParts[0].toInt() * 60 + sParts[1].toInt()
            val endMin = eParts[0].toInt() * 60 + eParts[1].toInt()
            Pair(startMin, endMin)
        } catch (e: Exception) {
            Pair(0, 0)
        }
    }

    private fun createSlotRowView(item: SlotItem): View {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 4, 0, 4) }
            setBackgroundResource(R.drawable.bg_row4_chip)
            setPadding(12, 12, 12, 12)
            tag = item
        }

        val tvTime = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
            text = item.timeRange
            textSize = 12.5f
            setTextColor(Color.parseColor(if (item.isAuspicious) "#0D5C2E" else "#B71C1C"))
            textStyle = android.graphics.Typeface.BOLD
        }

        val tvName = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
            text = item.name
            textSize = 13f
            textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            setTextColor(Color.parseColor(if (item.isAuspicious) "#0D5C2E" else "#B71C1C"))
            textStyle = android.graphics.Typeface.BOLD
        }

        row.addView(tvTime)
        row.addView(tvName)
        return row
    }

    private fun highlightCurrentSlotIfToday() {
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val activeContainers = if (isGowriSelected) {
            listOf(containerGowriDay, containerGowriNight)
        } else {
            listOf(containerHoraiDay, containerHoraiNight)
        }

        for (container in activeContainers) {
            for (i in 0 until container.childCount) {
                val row = container.getChildAt(i)
                val item = row.tag as? SlotItem ?: continue

                if (isSelectedDateToday && currentMinutes in item.startMin until item.endMin) {
                    row.setBackgroundResource(R.drawable.bg_chandrashtamam_card) // Gold/Border highlight
                } else {
                    row.setBackgroundResource(R.drawable.bg_row4_chip) // Adaptive theme background
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshHandler.post(timeTickRunnable)
    }

    override fun onPause() {
        super.onPause()
        refreshHandler.removeCallbacks(timeTickRunnable)
    }

    private fun getTamilDayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
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

    // Fallbacks if table column names differ
    private fun fallbackGowri(dayOfWeek: Int) {
        val defaultDay = listOf(
            SlotItem("06:00 - 07:30", "லாபம்", 360, 450, true),
            SlotItem("07:30 - 09:00", "சுகம்", 450, 540, true),
            SlotItem("09:00 - 10:30", "அமிர்தம்", 540, 630, true),
            SlotItem("10:30 - 12:00", "உத்தி", 630, 720, true),
            SlotItem("12:00 - 13:30", "ரோகம்", 720, 810, false),
            SlotItem("13:30 - 15:00", "லாபம்", 810, 900, true),
            SlotItem("15:00 - 16:30", "சுகம்", 900, 990, true),
            SlotItem("16:30 - 18:00", "சோரம்", 990, 1080, false)
        )
        for (slot in defaultDay) containerGowriDay.addView(createSlotRowView(slot))
    }

    private fun fallbackHorai(dayOfWeek: Int) {
        val defaultHorai = listOf(
            SlotItem("06:00 - 07:00", "சூரியன்", 360, 420, true),
            SlotItem("07:00 - 08:00", "சுக்கிரன்", 420, 480, true),
            SlotItem("08:00 - 09:00", "புதன்", 480, 540, true),
            SlotItem("09:00 - 10:00", "சந்திரன்", 540, 600, true),
            SlotItem("10:00 - 11:00", "சனி", 600, 660, false),
            SlotItem("11:00 - 12:00", "குரு", 660, 720, true)
        )
        for (slot in defaultHorai) containerHoraiDay.addView(createSlotRowView(slot))
    }

    data class SlotItem(
        val timeRange: String,
        val name: String,
        val startMin: Int,
        val endMin: Int,
        val isAuspicious: Boolean
    )
}
