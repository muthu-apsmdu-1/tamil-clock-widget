package com.example.tamilclock

import android.app.DatePickerDialog
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
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FragmentMonthlyCalendar : Fragment() {

    private lateinit var dbHelper: TamilDatabaseHelper
    private val displayCalendar = Calendar.getInstance()

    private lateinit var tvMonthYearTitle: TextView
    private lateinit var tvTamilMonthYearHeader: TextView
    private lateinit var gridTable: TableLayout

    companion object {
        private const val TAG = "MonthlyCalendar"
        private const val DATE_COLUMNS = 5
        private const val ROW_COUNT = 7
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    // -------------------------------------------------------------
    // CALLED BY MainPagerAdapter.kt ON DATE CHANGE
    // -------------------------------------------------------------
    fun updateDate(cal: Calendar) {
        displayCalendar.time = cal.time
        if (isAdded && ::gridTable.isInitialized) {
            renderCalendar()
        }
    }

    // -------------------------------------------------------------
    // CREATE VIEW
    // -------------------------------------------------------------
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dbHelper = TamilDatabaseHelper(requireContext())

        val root = LinearLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#FBFBFB"))
            setPadding(dp(4), dp(8), dp(4), dp(4))
        }

        // Header Section
        val headerLayout = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }

        val btnPrev = ImageButton(requireContext()).apply {
            setImageResource(android.R.drawable.ic_media_previous)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.parseColor("#78000C"))
            setOnClickListener {
                displayCalendar.set(Calendar.DAY_OF_MONTH, 1)
                displayCalendar.add(Calendar.MONTH, -1)
                renderCalendar()
            }
        }

        val titleContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setOnClickListener { showDatePicker() }
        }

        tvMonthYearTitle = TextView(requireContext()).apply {
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#78000C"))
            gravity = Gravity.CENTER
        }

        tvTamilMonthYearHeader = TextView(requireContext()).apply {
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#00695C"))
            gravity = Gravity.CENTER
        }

        titleContainer.addView(tvMonthYearTitle)
        titleContainer.addView(tvTamilMonthYearHeader)

        val btnNext = ImageButton(requireContext()).apply {
            setImageResource(android.R.drawable.ic_media_next)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.parseColor("#78000C"))
            setOnClickListener {
                displayCalendar.set(Calendar.DAY_OF_MONTH, 1)
                displayCalendar.add(Calendar.MONTH, 1)
                renderCalendar()
            }
        }

        headerLayout.addView(btnPrev)
        headerLayout.addView(titleContainer)
        headerLayout.addView(btnNext)
        root.addView(headerLayout)

        // 5 Column x 7 Row Scrollable Grid Container
        val scrollContainer = ScrollView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            isFillViewport = true
        }

        gridTable = TableLayout(requireContext()).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            isStretchAllColumns = true
        }

        scrollContainer.addView(gridTable)
        root.addView(scrollContainer)

        renderCalendar()
        return root
    }

    // -------------------------------------------------------------
    // DATE PICKER DIALOG
    // -------------------------------------------------------------
    private fun showDatePicker() {
        val dpd = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                displayCalendar.set(Calendar.DAY_OF_MONTH, 1)
                displayCalendar.set(Calendar.YEAR, year)
                displayCalendar.set(Calendar.MONTH, month)
                val maxDay = displayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                displayCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth.coerceAtMost(maxDay))
                renderCalendar()
            },
            displayCalendar.get(Calendar.YEAR),
            displayCalendar.get(Calendar.MONTH),
            displayCalendar.get(Calendar.DAY_OF_MONTH)
        )
        dpd.show()
    }

    // -------------------------------------------------------------
    // MAIN CALENDAR RENDERING: 7 ROWS x (1 HEADER + 5 DATE COLS)
    // -------------------------------------------------------------
    private fun renderCalendar() {
        gridTable.removeAllViews()

        val year = displayCalendar.get(Calendar.YEAR)
        val month = displayCalendar.get(Calendar.MONTH)

        val engMonthName = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(displayCalendar.time)
        tvMonthYearTitle.text = engMonthName.uppercase(Locale.ENGLISH)
        tvTamilMonthYearHeader.text = getTamilMonthHeader(year, month + 1)

        val firstDayCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val daysInMonth = firstDayCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 7 Weekday Buckets: Sunday (0) to Saturday (6)
        val dayBuckets = Array(ROW_COUNT) { mutableListOf<CalendarDayCell>() }
        var prevTamilMonth = ""

        for (day in 1..daysInMonth) {
            val queryDate = String.format(Locale.ENGLISH, "%04d-%02d-%02d", year, month + 1, day)
            val details = dbHelper.getDayDetails(queryDate)

            var isTamilStart = false
            if (details != null && details.tamilMonth.isNotEmpty()) {
                if (details.tamilDay == 1 || (prevTamilMonth.isNotEmpty() && details.tamilMonth != prevTamilMonth)) {
                    isTamilStart = true
                }
                prevTamilMonth = details.tamilMonth
            }

            val tempCal = firstDayCal.clone() as Calendar
            tempCal.set(Calendar.DAY_OF_MONTH, day)
            val dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) // 1 = Sun, 7 = Sat
            val rowIndex = dayOfWeek - 1

            val cellData = CalendarDayCell(
                gregorianDay = day,
                gregorianDate = queryDate,
                tamilDay = details?.tamilDay ?: 0,
                tamilMonth = details?.tamilMonth ?: "",
                isEnglishMonthStart = (day == 1),
                isTamilMonthStart = isTamilStart,
                festivals = details?.festivalsEvents ?: details?.festivals ?: "",
                tithi = details?.tithiName ?: details?.tithi ?: "",
                paksha = details?.paksha ?: details?.paksham ?: "",
                isSunday = (rowIndex == 0),
                dayOfWeek = dayOfWeek
            )

            dayBuckets[rowIndex].add(cellData)
        }

        val weekdayHeaders = listOf(
            Pair("SUN", "ஞாயிறு"),
            Pair("MON", "திங்கள்"),
            Pair("TUE", "செவ்வாய்"),
            Pair("WED", "புதன்"),
            Pair("THU", "வியாழன்"),
            Pair("FRI", "வெள்ளி"),
            Pair("SAT", "சனி")
        )

        val minRowHeight = dp(62)

        for (r in 0 until ROW_COUNT) {
            val tableRow = TableRow(requireContext()).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
                minimumHeight = minRowHeight
            }

            // Col 0: Weekday Vertical Header Cell
            val headerCell = createWeekdayHeaderCell(
                weekdayHeaders[r].first,
                weekdayHeaders[r].second,
                r == 0
            )
            tableRow.addView(headerCell)

            // ---------------------------------------------------------
            // 5-OCCURRENCE SHIFT & GRID POSITIONING LOGIC
            // ---------------------------------------------------------
            val occurrences = dayBuckets[r]
            val slots = arrayOfNulls<CalendarDayCell>(DATE_COLUMNS)

            if (occurrences.size >= 5) {
                // When 5 dates occur on this day of the week (e.g. 3, 10, 17, 24, 31):
                // - 5th occurrence (31) moves to Column 1 (slots[0])
                // - Occurrences 1, 2, 3, 4 shift to Columns 2, 3, 4, 5 (slots[1..4])
                slots[0] = occurrences[4]
                slots[1] = occurrences[0]
                slots[2] = occurrences[1]
                slots[3] = occurrences[2]
                slots[4] = occurrences[3]
            } else {
                // 4 or fewer occurrences:
                // If the first date of this weekday is on or after Day 8,
                // it belongs to Week 2, so leave Column 1 (slots[0]) empty.
                val firstDay = occurrences.firstOrNull()?.gregorianDay ?: 1
                val startSlot = if (firstDay > 7) 1 else 0

                occurrences.forEachIndexed { index, cell ->
                    val target = startSlot + index
                    if (target < DATE_COLUMNS) {
                        slots[target] = cell
                    }
                }
            }

            // Render all 5 date columns
            for (c in 0 until DATE_COLUMNS) {
                tableRow.addView(createDateCellView(slots[c]))
            }

            gridTable.addView(tableRow)
        }
    }

    // -------------------------------------------------------------
    // WEEKDAY HEADER CELL (LEFT COLUMN)
    // -------------------------------------------------------------
    private fun createWeekdayHeaderCell(
        eng: String,
        tam: String,
        isSunday: Boolean
    ): View {
        return CardView(requireContext()).apply {
            layoutParams = TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                0.95f
            ).apply {
                setMargins(dp(1), dp(2), dp(1), dp(2))
            }

            radius = dp(5).toFloat()
            cardElevation = dp(2).toFloat()

            setCardBackgroundColor(
                if (isSunday) Color.parseColor("#FFEBEE") else Color.parseColor("#E0F7FA")
            )

            val container = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(2), dp(4), dp(2), dp(4))
            }

            val tvEng = TextView(requireContext()).apply {
                text = eng
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(if (isSunday) Color.parseColor("#D32F2F") else Color.parseColor("#0277BD"))
                gravity = Gravity.CENTER
                maxLines = 1
            }

            val tvTam = TextView(requireContext()).apply {
                text = tam
                textSize = 9.5f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(if (isSunday) Color.parseColor("#B71C1C") else Color.parseColor("#004D40"))
                gravity = Gravity.CENTER
                maxLines = 1
            }

            container.addView(tvEng)
            container.addView(tvTam)
            addView(container)
        }
    }

    // -------------------------------------------------------------
    // DATE CELL
    // -------------------------------------------------------------
    private fun createDateCellView(cell: CalendarDayCell?): View {
        val card = CardView(requireContext()).apply {
            layoutParams = TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            ).apply {
                setMargins(dp(1), dp(2), dp(1), dp(2))
            }

            radius = dp(5).toFloat()
            cardElevation = if (cell != null) dp(2).toFloat() else 0f

            setCardBackgroundColor(
                when {
                    cell == null -> Color.parseColor("#F5F5F5")
                    cell.isSunday -> Color.parseColor("#FFF8F8")
                    else -> Color.WHITE
                }
            )
        }

        // Tapping on empty cell triggers 2-second pop-up
        if (cell == null) {
            card.setOnClickListener { showEmptyCellToast() }
            return card
        }

        val contentLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(2), dp(2), dp(2), dp(2))
        }

        // 1. Festival / Tithi Badge
        val badge = getFestivalBadge(cell)
        val tvTopBadge = TextView(requireContext()).apply {
            text = badge
            textSize = 8f
            maxLines = 1
            setTextColor(Color.parseColor("#6A1B9A"))
            gravity = Gravity.CENTER
            visibility = if (badge.isNotEmpty()) View.VISIBLE else View.INVISIBLE
        }
        contentLayout.addView(tvTopBadge)

        // 2. English Date (Large, Blue / Red for Sunday)
        val tvEnglish = TextView(requireContext()).apply {
            text = cell.gregorianDay.toString()
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(if (cell.isSunday) Color.parseColor("#C62828") else Color.parseColor("#1565C0"))
            gravity = Gravity.CENTER
            maxLines = 1
        }
        contentLayout.addView(tvEnglish)

        // 3. Tamil Date & Month (Small, Pink/Magenta)
        val tvTamil = TextView(requireContext()).apply {
            textSize = 9.5f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#E91E63"))
            gravity = Gravity.CENTER
            maxLines = 2

            text = when {
                (cell.isEnglishMonthStart || cell.isTamilMonthStart) && cell.tamilMonth.isNotEmpty() -> {
                    "${cell.tamilMonth} ${cell.tamilDay}"
                }
                cell.tamilDay > 0 -> cell.tamilDay.toString()
                else -> ""
            }
        }
        contentLayout.addView(tvTamil)

        card.addView(contentLayout)

        card.setOnClickListener {
            navigateToHomeWithDate(cell.gregorianDate)
        }

        return card
    }

    // -------------------------------------------------------------
    // FESTIVAL BADGE IDENTIFIER
    // -------------------------------------------------------------
    private fun getFestivalBadge(cell: CalendarDayCell): String {
        val fest = cell.festivals
        val tithi = cell.tithi

        return when {
            fest.contains("மாட்டுப் பொங்கல்", true) -> "🐂 மாட்டுப் பொங்கல்"
            fest.contains("பொங்கல்", true) || fest.contains("Pongal", true) -> "🌾 பொங்கல்"
            fest.contains("தீபாவளி", true) -> "🪔 தீபாவளி"
            fest.contains("குடியரசு", true) || fest.contains("Republic", true) -> "🇮🇳 குடியரசு"
            fest.contains("ஆங்கில புத்தாண்டு", true) || (cell.gregorianDay == 1 && cell.isEnglishMonthStart) -> "✨ புத்தாண்டு"
            fest.contains("சுதந்திர", true) || fest.contains("Independence", true) -> "🇮🇳 சுதந்திர தினம்"
            fest.contains("விநாயகர்", true) -> "🐘 விநாயகர் சதுர்த்தி"
            fest.contains("கிருஷ்ண", true) || fest.contains("கோகுலாஷ்டமி", true) -> "🪈 ஜெயந்தி"
            fest.contains("சிவராத்திரி", true) -> "🔱 சிவராத்திரி"
            fest.contains("கார்த்திகை", true) -> "🪔 கார்த்திகை"
            tithi.contains("அமாவாசை", true) -> "🌑 அமாவாசை"
            tithi.contains("பௌர்ணமி", true) -> "🌕 பௌர்ணமி"
            tithi.contains("ஏகாதசி", true) || fest.contains("ஏகாதசி", true) -> "🕉️ ஏகாதசி"
            tithi.contains("பிரதோஷம்", true) || fest.contains("பிரதோஷம்", true) -> "🔱 பிரதோஷம்"
            tithi.contains("சங்கடஹர", true) || fest.contains("சங்கடஹர", true) -> "🐘 சதுர்த்தி"
            tithi.contains("சஷ்டி", true) || fest.contains("சஷ்டி", true) -> "⚡ சஷ்டி"
            fest.isNotBlank() -> fest.split(",", "/", " ").firstOrNull { it.isNotBlank() } ?: "⭐"
            else -> ""
        }
    }

    // -------------------------------------------------------------
    // EMPTY CELL MESSAGE (2 SECONDS)
    // -------------------------------------------------------------
    private fun showEmptyCellToast() {
        val toast = Toast.makeText(requireContext(), "please select correct date", Toast.LENGTH_SHORT)
        toast.show()
        Handler(Looper.getMainLooper()).postDelayed({ toast.cancel() }, 2000)
    }

    // -------------------------------------------------------------
    // TAMIL MONTH HEADER
    // -------------------------------------------------------------
    private fun getTamilMonthHeader(year: Int, month: Int): String {
        val midDate = String.format(Locale.ENGLISH, "%04d-%02d-15", year, month)
        val data = dbHelper.getDayDetails(midDate)
        return if (data != null && data.tamilMonth.isNotEmpty()) {
            "${data.tamilYear} • ${data.tamilMonth}"
        } else {
            ""
        }
    }

    // -------------------------------------------------------------
    // SAFE VIEW PAGER TRAVERSAL (NO STATIC R.id REFERENCES)
    // -------------------------------------------------------------
    private fun findViewPagerInView(view: View): ViewPager2? {
        if (view is ViewPager2) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val found = findViewPagerInView(view.getChildAt(i))
                if (found != null) return found
            }
        }
        return null
    }

    // -------------------------------------------------------------
    // NAVIGATE TO PANCHANGAM WITH SELECTED DATE
    // -------------------------------------------------------------
    private fun navigateToHomeWithDate(dateString: String) {
        val act = activity ?: return

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).apply { isLenient = false }
            val parsedDate = sdf.parse(dateString) ?: return

            val targetCal = Calendar.getInstance().apply {
                time = parsedDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // Update MainActivity date instance if available
            val methodNames = listOf("loadDayData", "updateSelectedDate", "setSelectedDate")
            val loadMethod = act.javaClass.methods.firstOrNull { method ->
                method.name in methodNames &&
                method.parameterTypes.size == 1 &&
                (method.parameterTypes[0] == Calendar::class.java || method.parameterTypes[0] == String::class.java)
            }

            if (loadMethod != null) {
                when (loadMethod.parameterTypes[0]) {
                    Calendar::class.java -> loadMethod.invoke(act, targetCal)
                    String::class.java -> loadMethod.invoke(act, dateString)
                }
            } else {
                Log.w(TAG, "No compatible MainActivity date-update method found")
            }

            // Locate ViewPager2 safely without static ID dependencies
            val rootDecor = act.window?.decorView
            val viewPager = if (rootDecor != null) findViewPagerInView(rootDecor) else null

            viewPager?.setCurrentItem(MainPagerAdapter.PAGE_PANCHANGAM, true)
        } catch (e: Exception) {
            Log.e(TAG, "Date navigation failed: $dateString", e)
        }
    }

    // -------------------------------------------------------------
    // CLEANUP & DATA MODEL
    // -------------------------------------------------------------
    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }

    data class CalendarDayCell(
        val gregorianDay: Int,
        val gregorianDate: String,
        val tamilDay: Int,
        val tamilMonth: String,
        val isEnglishMonthStart: Boolean,
        val isTamilMonthStart: Boolean,
        val festivals: String,
        val tithi: String,
        val paksha: String,
        val isSunday: Boolean,
        val dayOfWeek: Int
    )
}
