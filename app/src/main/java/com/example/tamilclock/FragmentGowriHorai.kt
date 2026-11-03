
package com.example.tamilclock

import android.database.Cursor
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

    private var selectedCalendar: Calendar =
        Calendar.getInstance()

    private val refreshHandler =
        Handler(Looper.getMainLooper())

    private val timeTickRunnable = object : Runnable {
        override fun run() {
            if (!isAdded || view == null) return

            highlightCurrentSlotIfToday()

            refreshHandler.postDelayed(
                this,
                REFRESH_INTERVAL
            )
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

        tabBtnGowri =
            view.findViewById(R.id.tab_btn_gowri)

        tabBtnHorai =
            view.findViewById(R.id.tab_btn_horai)

        layoutGowriTable =
            view.findViewById(R.id.layout_gowri_table)

        layoutHoraiTable =
            view.findViewById(R.id.layout_horai_table)

        tvHeaderStripDate =
            view.findViewById(R.id.tv_header_strip_date)

        containerGowriDay =
            view.findViewById(R.id.container_gowri_day_slots)

        containerGowriNight =
            view.findViewById(R.id.container_gowri_night_slots)

        containerHoraiDay =
            view.findViewById(R.id.container_horai_day_slots)

        containerHoraiNight =
            view.findViewById(R.id.container_horai_night_slots)

        tabBtnGowri.setOnClickListener {
            switchTab(true)
        }

        tabBtnHorai.setOnClickListener {
            switchTab(false)
        }

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

            tabBtnGowri.setBackgroundResource(
                R.drawable.bg_tab_selected
            )

            tabBtnGowri.setTextColor(Color.WHITE)

            tabBtnHorai.setBackgroundResource(
                R.drawable.bg_tab_unselected
            )

            tabBtnHorai.setTextColor(
                Color.parseColor("#9E9E9E")
            )

        } else {

            layoutGowriTable.visibility = View.GONE
            layoutHoraiTable.visibility = View.VISIBLE

            tabBtnHorai.setBackgroundResource(
                R.drawable.bg_tab_selected
            )

            tabBtnHorai.setTextColor(Color.WHITE)

            tabBtnGowri.setBackgroundResource(
                R.drawable.bg_tab_unselected
            )

            tabBtnGowri.setTextColor(
                Color.parseColor("#9E9E9E")
            )
        }

        highlightCurrentSlotIfToday()
    }

    // --------------------------------------------------
    // DATE UPDATE
    // --------------------------------------------------

    fun updateDate(cal: Calendar) {

        if (!isAdded || view == null) return

        selectedCalendar = cal.clone() as Calendar

        val today = Calendar.getInstance()

        isSelectedDateToday =
            cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR) % 100

        val dayName =
            getTamilDayName(cal.get(Calendar.DAY_OF_WEEK))

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

            tvHeaderStripDate.setTextColor(
                Color.parseColor("#081E7D")
            )

        } else {

            tvHeaderStripDate.textSize = 13f

            tvHeaderStripDate.setTextColor(
                Color.parseColor("#B71C1C")
            )
        }

        renderTablesFromSqlite(cal)

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
                ?: throw IllegalStateException(
                    "Database directory unavailable"
                )

            if (!parent.exists() && !parent.mkdirs()) {
                throw IllegalStateException(
                    "Cannot create database directory"
                )
            }

            val tempFile = File(
                parent,
                "$dbName.tmp"
            )

            if (tempFile.exists()) {
                tempFile.delete()
            }

            try {

                context.assets.open(dbName).use { input ->

                    FileOutputStream(tempFile).use { output ->

                        input.copyTo(output)
                        output.fd.sync()
                    }
                }

                if (!tempFile.renameTo(dbFile)) {
                    throw IllegalStateException(
                        "Cannot install database file"
                    )
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
    // DATABASE COLUMN DETECTION
    // --------------------------------------------------

    private fun getColumnNames(
        db: SQLiteDatabase,
        tableName: String
    ): Set<String> {

        val columns = mutableSetOf<String>()

        db.rawQuery(
            "PRAGMA table_info($tableName)",
            null
        ).use { cursor ->

            val nameIndex =
                cursor.getColumnIndexOrThrow("name")

            while (cursor.moveToNext()) {
                columns.add(
                    cursor.getString(nameIndex)
                )
            }
        }

        return columns
    }

    // --------------------------------------------------
    // RENDER DATABASE TABLES
    // --------------------------------------------------

    private fun renderTablesFromSqlite(cal: Calendar) {

        clearAllContainers()

        val dayOfWeek =
            cal.get(Calendar.DAY_OF_WEEK)

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

        // GOWRI DATABASE

        try {

            getDb("gowri.db").use { db ->

                loadGowriData(
                    db,
                    dayNameEng,
                    dayOfWeek
                )
            }

        } catch (e: Exception) {

            Log.e(TAG, "Gowri database error", e)

            showDatabaseError(
                containerGowriDay,
                containerGowriNight
            )
        }

        // HORAI DATABASE

        try {

            getDb("horai.db").use { db ->

                loadHoraiData(
                    db,
                    dayNameEng,
                    dayOfWeek
                )
            }

        } catch (e: Exception) {

            Log.e(TAG, "Horai database error", e)

            showDatabaseError(
                containerHoraiDay,
                containerHoraiNight
            )
        }
    }

    private fun clearAllContainers() {

        containerGowriDay.removeAllViews()
        containerGowriNight.removeAllViews()

        containerHoraiDay.removeAllViews()
        containerHoraiNight.removeAllViews()
    }

    // --------------------------------------------------
    // WEEKDAY QUERY
    // --------------------------------------------------

    private fun getWeekdayQuery(
        db: SQLiteDatabase,
        table: String,
        dayName: String,
        dayId: Int
    ): Cursor {

        val columns = getColumnNames(db, table)

        val dayColumn = when {

            "day_id" in columns -> "day_id"

            "day" in columns -> "day"

            else -> throw IllegalStateException(
                "$table has no weekday column"
            )
        }

        val orderColumn =
            if ("id" in columns) "id" else "rowid"

        val requiredColumns = when (table) {

            "gowri" -> listOf(
                "time", "name", "type"
            )

            "horai" -> listOf(
                "time", "planet", "type"
            )

            else -> emptyList()
        }

        if (!columns.containsAll(requiredColumns)) {
            throw IllegalStateException(
                "Missing columns in $table"
            )
        }

        val selectionValue =
            if (dayColumn == "day_id") {
                dayId.toString()
            } else {
                dayName
            }

        return db.rawQuery(
            "SELECT * FROM $table " +
                "WHERE $dayColumn = ? " +
                "ORDER BY $orderColumn ASC",
            arrayOf(selectionValue)
        )
    }

    // --------------------------------------------------
    // LOAD GOWRI DATA
    // --------------------------------------------------

    private fun loadGowriData(
        db: SQLiteDatabase,
        dayNameEng: String,
        dayOfWeek: Int
    ) {

        val dayItems = mutableListOf<SlotItem>()
        val nightItems = mutableListOf<SlotItem>()

        try {

            getWeekdayQuery(
                db,
                "gowri",
                dayNameEng,
                dayOfWeek
            ).use { cursor ->

                val timeIndex =
                    cursor.getColumnIndexOrThrow("time")

                val nameIndex =
                    cursor.getColumnIndexOrThrow("name")

                val typeIndex =
                    cursor.getColumnIndexOrThrow("type")

                while (cursor.moveToNext()) {

                    val time =
                        cursor.getString(timeIndex)

                    val name =
                        cursor.getString(nameIndex)

                    val type =
                        cursor.getString(typeIndex)

                    val range =
                        parseTimeRangeToMinutes(time)
                            ?: continue

                    val item = SlotItem(
                        time,
                        name,
                        range.first,
                        range.second,
                        isGowriGood(name)
                    )

                    if (type.equals("night", true)) {
                        nightItems.add(item)
                    } else {
                        dayItems.add(item)
                    }
                }
            }

            displayItems(
                containerGowriDay,
                dayItems
            )

            displayItems(
                containerGowriNight,
                nightItems
            )

        } catch (e: Exception) {

            Log.e(TAG, "Gowri query error", e)

            showDatabaseError(
                containerGowriDay,
                containerGowriNight
            )
        }
    }

    // --------------------------------------------------
    // LOAD HORAI DATA
    // --------------------------------------------------

    private fun loadHoraiData(
        db: SQLiteDatabase,
        dayNameEng: String,
        dayOfWeek: Int
    ) {

        val dayItems = mutableListOf<SlotItem>()
        val nightItems = mutableListOf<SlotItem>()

        try {

            getWeekdayQuery(
                db,
                "horai",
                dayNameEng,
                dayOfWeek
            ).use { cursor ->

                val timeIndex =
                    cursor.getColumnIndexOrThrow("time")

                val planetIndex =
                    cursor.getColumnIndexOrThrow("planet")

                val typeIndex =
                    cursor.getColumnIndexOrThrow("type")

                while (cursor.moveToNext()) {

                    val time =
                        cursor.getString(timeIndex)

                    val planet =
                        cursor.getString(planetIndex)

                    val type =
                        cursor.getString(typeIndex)

                    val range =
                        parseTimeRangeToMinutes(time)
                            ?: continue

                    val item = SlotItem(
                        time,
                        planet,
                        range.first,
                        range.second,
                        isHoraiGood(planet)
                    )

                    if (type.equals("night", true)) {
                        nightItems.add(item)
                    } else {
                        dayItems.add(item)
                    }
                }
            }

            displayItems(
                containerHoraiDay,
                dayItems
            )

            displayItems(
                containerHoraiNight,
                nightItems
            )

        } catch (e: Exception) {

            Log.e(TAG, "Horai query error", e)

            showDatabaseError(
                containerHoraiDay,
                containerHoraiNight
            )
        }
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

            container.addView(
                createSlotRowView(item)
            )
        }
    }

    // --------------------------------------------------
    // EMPTY AND ERROR STATES
    // --------------------------------------------------

    private fun showEmptyPlaceholder(
        container: LinearLayout
    ) {

        showPlaceholder(
            container,
            "---"
        )
    }

    private fun showDatabaseError(
        dayContainer: LinearLayout,
        nightContainer: LinearLayout
    ) {

        showPlaceholder(
            dayContainer,
            "தரவு கிடைக்கவில்லை"
        )

        showPlaceholder(
            nightContainer,
            "தரவு கிடைக்கவில்லை"
        )
    }

    private fun showPlaceholder(
        container: LinearLayout,
        message: String
    ) {

        container.removeAllViews()

        val tvEmpty = TextView(
            requireContext()
        ).apply {

            layoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 4, 0, 4)
                }

            text = message

            gravity = Gravity.CENTER

            textSize = 14f

            setTextColor(
                Color.parseColor("#8E8E93")
            )

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
            name.contains("சுபம்")
    }

    private fun isHoraiGood(planet: String): Boolean {

        return planet.contains("குரு") ||
            planet.contains("சுக்கிரன்") ||
            planet.contains("புதன்") ||
            planet.contains("சந்திரன்")
    }

    // --------------------------------------------------
    // TIME VALIDATION
    // --------------------------------------------------

    private fun parseTimeRangeToMinutes(
        timeRange: String
    ): Pair<Int, Int>? {

        return try {

            val parts = timeRange.split("-")

            if (parts.size != 2) {
                return null
            }

            val timePattern =
                Regex("^([01]?[0-9]|2[0-3]):([0-5][0-9])$")

    override fun onResume() {
        super.onResume()

        val now = Calendar.getInstance()
        // If the user previously was on 'today', but resumed the app on the next calendar day:
        if (isSelectedDateToday && !isSameDate(selectedCalendar, now)) {
            updateDate(now)
        }

        refreshHandler.removeCallbacks(timeTickRunnable)
        refreshHandler.post(timeTickRunnable)
    }

                ?: return null

            if (start == end) {
                return null
            }

            Pair(start, end)

        } catch (e: Exception) {

            Log.w(
                TAG,
                "Invalid time range: $timeRange",
                e
            )

            null
        }
    }

    // --------------------------------------------------
    // CREATE TIMETABLE ROW
    // --------------------------------------------------

    private fun createSlotRowView(
        item: SlotItem
    ): View {

        val row = LinearLayout(
            requireContext()
        ).apply {

            orientation = LinearLayout.HORIZONTAL

            layoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 4, 0, 4)
                }

            setBackgroundResource(
                R.drawable.bg_row4_chip
            )

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

        val tvTime = TextView(
            requireContext()
        ).apply {

            layoutParams =
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.2f
                )

            text = item.timeRange

            textSize = 12.5f

            setTextColor(textColor)

            setTypeface(
                typeface,
                Typeface.BOLD
            )
        }

        val tvName = TextView(
            requireContext()
        ).apply {

            layoutParams =
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                )

            text = item.name

            textSize = 13f

            textAlignment =
                View.TEXT_ALIGNMENT_VIEW_END

            setTextColor(textColor)

            setTypeface(
                typeface,
                Typeface.BOLD
            )
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

        val currentMinutes =
            now.get(Calendar.HOUR_OF_DAY) * 60 +
            now.get(Calendar.MINUTE)

        val activeContainers =
            if (isGowriSelected) {

                listOf(
                    containerGowriDay,
                    containerGowriNight
                )

            } else {

                listOf(
                    containerHoraiDay,
                    containerHoraiNight
                )
            }

        for (container in activeContainers) {

            for (i in 0 until container.childCount) {

                val row = container.getChildAt(i)

                val item =
                    row.tag as? SlotItem ?: continue

                val isActive =
                    if (item.endMin > item.startMin) {

                        currentMinutes in
                            item.startMin until item.endMin

                    } else {

                        currentMinutes >= item.startMin ||
                            currentMinutes < item.endMin
                    }

                if (isSelectedDateToday && isActive) {

                    row.setBackgroundResource(
                        R.drawable.bg_chandrashtamam_card
                    )

                } else {

                    row.setBackgroundResource(
                        R.drawable.bg_row4_chip
                    )
                }
            }
        }
    }

    // --------------------------------------------------
    // TAMIL WEEKDAY
    // --------------------------------------------------

    private fun getTamilDayName(
        dayOfWeek: Int
    ): String {

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
    // LIFECYCLE MANAGEMENT
    // --------------------------------------------------

    override fun onResume() {

        super.onResume()

        // If the previously selected date was today,
        // refresh it in case the calendar day changed
        // while the application was paused.

        val now = Calendar.getInstance()

        if (isSameDate(
                selectedCalendar,
                now
            ) &&
            !isSameDate(
                selectedCalendar,
                Calendar.getInstance()
            )
        ) {
            updateDate(now)
        }

        refreshHandler.removeCallbacks(
            timeTickRunnable
        )

        refreshHandler.post(
            timeTickRunnable
        )
    }

    private fun isSameDate(
        first: Calendar,
        second: Calendar
    ): Boolean {

        return first.get(Calendar.YEAR) ==
            second.get(Calendar.YEAR) &&
            first.get(Calendar.DAY_OF_YEAR) ==
            second.get(Calendar.DAY_OF_YEAR)
    }

    override fun onPause() {

        refreshHandler.removeCallbacks(
            timeTickRunnable
        )

        super.onPause()
    }

    override fun onDestroyView() {

        refreshHandler.removeCallbacks(
            timeTickRunnable
        )

        super.onDestroyView()
    }

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
}
