package com.example.tamilclock

import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Locale

class FragmentGowriHorai : Fragment() {

    companion object {
        private const val TAG = "FragmentGowriHorai"
        private const val REFRESH_INTERVAL = 30_000L

        private const val GOWRI_DB = "gowri.db"
        private const val HORAI_DB = "horai.db"

        private const val GOWRI_TABLE = "weekday_gowri"
        private const val HORAI_TABLE = "weekday_horai"

        private const val ARG_IS_HORAI = "arg_is_horai"

        fun newInstance(initialTabIsHorai: Boolean): FragmentGowriHorai {
            val fragment = FragmentGowriHorai()
            val args = Bundle().apply {
                putBoolean(ARG_IS_HORAI, initialTabIsHorai)
            }
            fragment.arguments = args
            return fragment
        }
    }

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

    // --------------------------------------------------
    // DATABASE SLOT DEFINITIONS
    // --------------------------------------------------

    private data class SlotDefinition(
        val column: String,
        val startMinute: Int,
        val durationMinutes: Int
    )

    // Gowri: 8 periods, each 90 minutes.
    // Day:   06:00 to 18:00
    // Night: 18:00 to 06:00
    private val gowriSlots = listOf(
        SlotDefinition("slot_6_730", 0, 90),
        SlotDefinition("slot_730_9", 90, 90),
        SlotDefinition("slot_9_1030", 180, 90),
        SlotDefinition("slot_1030_12", 270, 90),
        SlotDefinition("slot_12_130", 360, 90),
        SlotDefinition("slot_130_3", 450, 90),
        SlotDefinition("slot_3_430", 540, 90),
        SlotDefinition("slot_430_6", 630, 90)
    )

    // Horai: 12 periods, each 60 minutes.
    // Day:   06:00 to 18:00
    // Night: 18:00 to 06:00
    private val horaiSlots = listOf(
        SlotDefinition("slot_6_7", 0, 60),
        SlotDefinition("slot_7_8", 60, 60),
        SlotDefinition("slot_8_9", 120, 60),
        SlotDefinition("slot_9_10", 180, 60),
        SlotDefinition("slot_10_11", 240, 60),
        SlotDefinition("slot_11_12", 300, 60),
        SlotDefinition("slot_12_1", 360, 60),
        SlotDefinition("slot_1_2", 420, 60),
        SlotDefinition("slot_2_3", 480, 60),
        SlotDefinition("slot_3_4", 540, 60),
        SlotDefinition("slot_4_5", 600, 60),
        SlotDefinition("slot_5_6", 660, 60)
    )

    // --------------------------------------------------
    // DATA MODEL
    // --------------------------------------------------

    data class SlotItem(
        val timeRange: String,
        val name: String,
        val startMin: Int,
        val endMin: Int,
        val isAuspicious: Boolean
    )

    // --------------------------------------------------
    // CURRENT TIME REFRESH
    // --------------------------------------------------

    private val timeTickRunnable = object : Runnable {
        override fun run() {
            if (!isAdded || view == null) return

            highlightCurrentSlotIfToday()

            refreshHandler.postDelayed(this, REFRESH_INTERVAL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_gowri_horai,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Read initial configuration from arguments if provided
        arguments?.let {
            val startAsHorai = it.getBoolean(ARG_IS_HORAI, false)
            isGowriSelected = !startAsHorai
        }

        tabBtnGowri = view.findViewById(R.id.tab_btn_gowri)
        tabBtnHorai = view.findViewById(R.id.tab_btn_horai)

        layoutGowriTable = view.findViewById(R.id.layout_gowri_table)
        layoutHoraiTable = view.findViewById(R.id.layout_horai_table)

        tvHeaderStripDate = view.findViewById(R.id.tv_header_strip_date)

        containerGowriDay = view.findViewById(R.id.container_gowri_day_slots)
        containerGowriNight = view.findViewById(R.id.container_gowri_night_slots)

        containerHoraiDay = view.findViewById(R.id.container_horai_day_slots)
        containerHoraiNight = view.findViewById(R.id.container_horai_night_slots)

        tabBtnGowri.setOnClickListener { switchTab(true) }
        tabBtnHorai.setOnClickListener { switchTab(false) }

        switchTab(isGowriSelected)
        updateDate(selectedCalendar)
    }

    // --------------------------------------------------
    // TAB SWITCHING
    // --------------------------------------------------

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

    // --------------------------------------------------
    // DATE UPDATE
    // --------------------------------------------------

    fun updateDate(cal: Calendar) {
        selectedCalendar = cal.clone() as Calendar

        val today = Calendar.getInstance()
        isSelectedDateToday = isSameDate(selectedCalendar, today)

        if (!isAdded || view == null) return

        val day = selectedCalendar.get(Calendar.DAY_OF_MONTH)
        val month = selectedCalendar.get(Calendar.MONTH) + 1
        val year = selectedCalendar.get(Calendar.YEAR) % 100

        val dayName = getTamilDayName(selectedCalendar.get(Calendar.DAY_OF_WEEK))

        tvHeaderStripDate.text = String.format(
            Locale.getDefault(),
            "%d - %d - %02d, %s",
            day,
            month,
            year,
            dayName
        )

        if (isSelectedDateToday) {
            tvHeaderStripDate.textSize = 15f
            tvHeaderStripDate.setTextColor(Color.parseColor("#081E7D"))
        } else {
            tvHeaderStripDate.textSize = 13f
            tvHeaderStripDate.setTextColor(Color.parseColor("#B71C1C"))
        }

        renderTablesFromSqlite(selectedCalendar)
        highlightCurrentSlotIfToday()
    }

    // --------------------------------------------------
    // SAFE DATABASE COPY AND OPEN
    // --------------------------------------------------

    private fun getDb(dbName: String): SQLiteDatabase {
        val context = requireContext()
        val dbFile = context.getDatabasePath(dbName)

        if (!dbFile.exists()) {
            val parent = dbFile.parentFile
                ?: throw IllegalStateException("Database directory unavailable")

            if (!parent.exists() && !parent.mkdirs()) {
                throw IllegalStateException("Cannot create database directory")
            }

            val tempFile = File(parent, "$dbName.tmp")

            if (tempFile.exists() && !tempFile.delete()) {
                throw IllegalStateException("Cannot remove temporary database")
            }

            try {
                context.assets.open(dbName).use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                        output.fd.sync()
                    }
                }

                if (!tempFile.renameTo(dbFile)) {
                    throw IllegalStateException("Cannot install database file")
                }
            } catch (e: Exception) {
                tempFile.delete()
                throw e
            }
        }

        return SQLiteDatabase.openDatabase(
            dbFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY
        )
    }

    // --------------------------------------------------
    // DATABASE STRUCTURE VALIDATION
    // --------------------------------------------------

    private fun validateDatabase(
        db: SQLiteDatabase,
        tableName: String,
        requiredColumns: List<String>
    ) {
        val columns = mutableSetOf<String>()

        db.rawQuery("PRAGMA table_info($tableName)", null).use { cursor ->
            val index = cursor.getColumnIndexOrThrow("name")
            while (cursor.moveToNext()) {
                columns.add(cursor.getString(index))
            }
        }

        if (!columns.containsAll(requiredColumns)) {
            throw IllegalStateException("Invalid database structure: $tableName")
        }
    }

    // --------------------------------------------------
    // TAMIL WEEKDAY
    // --------------------------------------------------

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

    // --------------------------------------------------
    // RENDER BOTH DATABASES
    // --------------------------------------------------

    private fun renderTablesFromSqlite(cal: Calendar) {
        clearAllContainers()

        val weekday = getTamilDayName(cal.get(Calendar.DAY_OF_WEEK))

        try {
            getDb(GOWRI_DB).use { db ->
                loadGowriData(db, weekday)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gowri database error", e)
            showDatabaseError(containerGowriDay, containerGowriNight)
        }

        try {
            getDb(HORAI_DB).use { db ->
                loadHoraiData(db, weekday)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Horai database error", e)
            showDatabaseError(containerHoraiDay, containerHoraiNight)
        }
    }

    // --------------------------------------------------
    // CLEAR CONTAINERS
    // --------------------------------------------------

    private fun clearAllContainers() {
        containerGowriDay.removeAllViews()
        containerGowriNight.removeAllViews()
        containerHoraiDay.removeAllViews()
        containerHoraiNight.removeAllViews()
    }

    // --------------------------------------------------
    // LOAD GOWRI DATABASE
    // --------------------------------------------------

    private fun loadGowriData(
        db: SQLiteDatabase,
        weekday: String
    ) {
        val requiredColumns = listOf(
            "weekday_ta",
            "kaalam"
        ) + gowriSlots.map { it.column }

        try {
            validateDatabase(db, GOWRI_TABLE, requiredColumns)

            val dayItems = mutableListOf<SlotItem>()
            val nightItems = mutableListOf<SlotItem>()

            loadGowriPeriod(db, weekday, "பகல்", dayItems, 6 * 60)
            loadGowriPeriod(db, weekday, "இரவு", nightItems, 18 * 60)

            displayItems(containerGowriDay, dayItems)
            displayItems(containerGowriNight, nightItems)

        } catch (e: Exception) {
            Log.e(TAG, "Gowri loading error", e)
            showDatabaseError(containerGowriDay, containerGowriNight)
        }
    }

    // --------------------------------------------------
    // LOAD INDIVIDUAL GOWRI PERIOD
    // --------------------------------------------------

    private fun loadGowriPeriod(
        db: SQLiteDatabase,
        weekday: String,
        kaalam: String,
        destination: MutableList<SlotItem>,
        baseMinute: Int
    ) {
        db.query(
            GOWRI_TABLE,
            null,
            "weekday_ta = ? AND kaalam = ?",
            arrayOf(weekday, kaalam),
            null,
            null,
            null
        ).use { cursor ->

            if (!cursor.moveToFirst()) {
                Log.w(TAG, "Gowri record not found for: $weekday, $kaalam")
                return
            }

            for (slot in gowriSlots) {
                val index = cursor.getColumnIndexOrThrow(slot.column)
                val name = cursor.getString(index)?.trim().orEmpty()

                if (name.isEmpty()) continue

                val start = baseMinute + slot.startMinute
                val end = start + slot.durationMinutes

                val sMin = start % 1440
                val eMin = if (end % 1440 == 0 && end > 0) 1440 else end % 1440

                destination.add(
                    SlotItem(
                        timeRange = formatTimeRange(start, end),
                        name = name,
                        startMin = sMin,
                        endMin = eMin,
                        isAuspicious = isGowriGood(name)
                    )
                )
            }
        }
    }

    // --------------------------------------------------
    // LOAD HORAI DATABASE
    // --------------------------------------------------

    private fun loadHoraiData(
        db: SQLiteDatabase,
        weekday: String
    ) {
        val requiredColumns = listOf(
            "weekday_ta",
            "kaalam"
        ) + horaiSlots.map { it.column }

        try {
            validateDatabase(db, HORAI_TABLE, requiredColumns)

            val dayItems = mutableListOf<SlotItem>()
            val nightItems = mutableListOf<SlotItem>()

            loadHoraiPeriod(db, weekday, "பகல்", dayItems, 6 * 60)
            loadHoraiPeriod(db, weekday, "இரவு", nightItems, 18 * 60)

            displayItems(containerHoraiDay, dayItems)
            displayItems(containerHoraiNight, nightItems)

        } catch (e: Exception) {
            Log.e(TAG, "Horai loading error", e)
            showDatabaseError(containerHoraiDay, containerHoraiNight)
        }
    }

    // --------------------------------------------------
    // LOAD INDIVIDUAL HORAI PERIOD
    // --------------------------------------------------

    private fun loadHoraiPeriod(
        db: SQLiteDatabase,
        weekday: String,
        kaalam: String,
        destination: MutableList<SlotItem>,
        baseMinute: Int
    ) {
        db.query(
            HORAI_TABLE,
            null,
            "weekday_ta = ? AND kaalam = ?",
            arrayOf(weekday, kaalam),
            null,
            null,
            null
        ).use { cursor ->

            if (!cursor.moveToFirst()) {
                Log.w(TAG, "Horai record not found for: $weekday, $kaalam")
                return
            }

            for (slot in horaiSlots) {
                val index = cursor.getColumnIndexOrThrow(slot.column)
                val planet = cursor.getString(index)?.trim().orEmpty()

                if (planet.isEmpty()) continue

                val start = baseMinute + slot.startMinute
                val end = start + slot.durationMinutes

                val sMin = start % 1440
                val eMin = if (end % 1440 == 0 && end > 0) 1440 else end % 1440

                destination.add(
                    SlotItem(
                        timeRange = formatTimeRange(start, end),
                        name = planet,
                        startMin = sMin,
                        endMin = eMin,
                        isAuspicious = isHoraiGood(planet)
                    )
                )
            }
        }
    }

    // --------------------------------------------------
    // TIME FORMATTING
    // --------------------------------------------------

    private fun formatTimeRange(
        startMinute: Int,
        endMinute: Int
    ): String {
        return "${formatClock(startMinute)} - ${formatClock(endMinute)}"
    }

    private fun formatClock(totalMinutes: Int): String {
        val minuteOfDay = totalMinutes % 1440
        val hour = minuteOfDay / 60
        val minute = minuteOfDay % 60

        return String.format(
            Locale.US,
            "%02d:%02d",
            hour,
            minute
        )
    }

    // --------------------------------------------------
    // DISPLAY DATABASE RECORDS
    // --------------------------------------------------

    private fun displayItems(
        container: LinearLayout,
        items: List<SlotItem>
    ) {
        container.removeAllViews()

        if (items.isEmpty()) {
            showEmptyPlaceholder(container)
            return
        }

        for (item in items) {
            container.addView(createSlotRowView(item))
        }
    }

    // --------------------------------------------------
    // EMPTY AND ERROR STATES
    // --------------------------------------------------

    private fun showEmptyPlaceholder(container: LinearLayout) {
        showPlaceholder(container, "---")
    }

    private fun showDatabaseError(
        dayContainer: LinearLayout,
        nightContainer: LinearLayout
    ) {
        showPlaceholder(dayContainer, "தரவு கிடைக்கவில்லை")
        showPlaceholder(nightContainer, "தரவு கிடைக்கவில்லை")
    }

    private fun showPlaceholder(
        container: LinearLayout,
        message: String
    ) {
        container.removeAllViews()

        val tvEmpty = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 4, 0, 4)
            }
            text = message
            gravity = Gravity.CENTER
            textSize = 14f
            setTextColor(Color.parseColor("#8E8E93"))
            setPadding(0, 16, 0, 16)
        }

        container.addView(tvEmpty)
    }

    // --------------------------------------------------
    // AUSPICIOUS PERIOD CLASSIFICATION
    // --------------------------------------------------

    private fun isGowriGood(name: String): Boolean {
        return name.contains("லாபம்") ||
            name.contains("சுகம்") ||
            name.contains("அமிர்தம்") ||
            name.contains("உத்தி") ||
            name.contains("சுபம்") ||
            name.contains("தனம்")
    }

    private fun isHoraiGood(planet: String): Boolean {
        return planet.contains("குரு") ||
            planet.contains("சுக்கிரன்") ||
            planet.contains("புதன்") ||
            planet.contains("சந்திரன்")
    }

    // --------------------------------------------------
    // CREATE TIMETABLE ROW
    // --------------------------------------------------

    private fun createSlotRowView(item: SlotItem): View {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 4, 0, 4)
            }
            setBackgroundResource(R.drawable.bg_row4_chip)
            setPadding(12, 12, 12, 12)
            tag = item
        }

        val textColor = Color.parseColor(
            if (item.isAuspicious) {
                "#0D5C2E"
            } else {
                "#B71C1C"
            }
        )

        val tvTime = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.2f
            )
            text = item.timeRange
            textSize = 12.5f
            setTextColor(textColor)
            setTypeface(typeface, Typeface.BOLD)
        }

        val tvName = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            )
            text = item.name
            textSize = 13f
            textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            setTextColor(textColor)
            setTypeface(typeface, Typeface.BOLD)
        }

        row.addView(tvTime)
        row.addView(tvName)

        return row
    }

    // --------------------------------------------------
    // CURRENT SLOT HIGHLIGHTING
    // --------------------------------------------------

    private fun highlightCurrentSlotIfToday() {
        if (!isAdded || view == null) return

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

                val isActive = if (item.endMin > item.startMin) {
                    currentMinutes in item.startMin until item.endMin
                } else {
                    currentMinutes >= item.startMin || currentMinutes < item.endMin
                }

                if (isSelectedDateToday && isActive) {
                    row.setBackgroundResource(R.drawable.bg_chandrashtamam_card)
                } else {
                    row.setBackgroundResource(R.drawable.bg_row4_chip)
                }
            }
        }
    }

    // --------------------------------------------------
    // DATE COMPARISON
    // --------------------------------------------------

    private fun isSameDate(
        first: Calendar,
        second: Calendar
    ): Boolean {
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
            first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
    }

    // --------------------------------------------------
    // LIFECYCLE MANAGEMENT
    // --------------------------------------------------

    override fun onResume() {
        super.onResume()

        val now = Calendar.getInstance()
        if (isSelectedDateToday && !isSameDate(selectedCalendar, now)) {
            updateDate(now)
        }

        refreshHandler.removeCallbacks(timeTickRunnable)
        refreshHandler.post(timeTickRunnable)
    }

    override fun onPause() {
        refreshHandler.removeCallbacks(timeTickRunnable)
        super.onPause()
    }

    override fun onDestroyView() {
        refreshHandler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }
}
