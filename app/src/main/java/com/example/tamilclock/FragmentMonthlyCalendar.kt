package com.example.tamilclock

import android.app.DatePickerDialog
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

    private var rootLayout: LinearLayout? = null
    private var tvMonthYearTitle: TextView? = null
    private var tvTamilMonthYearHeader: TextView? = null
    private var btnPrev: ImageButton? = null
    private var btnNext: ImageButton? = null
    private var gridTable: TableLayout? = null

    companion object {
        private const val TAG = "MonthlyCalendar"
        private const val ROW_COUNT = 7
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    fun updateDate(cal: Calendar) {
        displayCalendar.time = cal.time
        if (isAdded && gridTable != null) {
            renderCalendar()
        }
    }

    // 1. LIFECYCLE FIX: Initialize dbHelper in onCreate (once per Fragment lifecycle)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = TamilDatabaseHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_monthly_calendar,
            container,
            false
        )

        rootLayout = view.findViewById(R.id.root_monthly_calendar)
        tvMonthYearTitle = view.findViewById(R.id.tv_month_year_title)
        tvTamilMonthYearHeader = view.findViewById(R.id.tv_tamil_month_year_header)
        btnPrev = view.findViewById(R.id.btn_prev_month)
        btnNext = view.findViewById(R.id.btn_next_month)
        gridTable = view.findViewById(R.id.table_calendar_grid)

        btnPrev?.setOnClickListener {
            displayCalendar.set(Calendar.DAY_OF_MONTH, 1)
            displayCalendar.add(Calendar.MONTH, -1)
            renderCalendar()
        }

        btnNext?.setOnClickListener {
            displayCalendar.set(Calendar.DAY_OF_MONTH, 1)
            displayCalendar.add(Calendar.MONTH, 1)
            renderCalendar()
        }

        view.findViewById<LinearLayout>(R.id.container_title)?.setOnClickListener {
            showDatePicker()
        }

        renderCalendar()
        return view
    }

    override fun onResume() {
        super.onResume()
        if (gridTable != null) {
            renderCalendar()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootLayout = null
        tvMonthYearTitle = null
        tvTamilMonthYearHeader = null
        btnPrev = null
        btnNext = null
        gridTable = null
    }

    // 2. LIFECYCLE FIX: Close dbHelper only when the Fragment itself is destroyed
    override fun onDestroy() {
        if (::dbHelper.isInitialized) {
            dbHelper.close()
        }
        super.onDestroy()
    }

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

    private fun renderCalendar() {
        val table = gridTable ?: return
        val root = rootLayout ?: return
        val context = context ?: return

        table.removeAllViews()

        val palette = ThemeManager.getThemePalette(context)

        root.setBackgroundColor(palette.pageBackground)
        tvMonthYearTitle?.setTextColor(palette.primaryHeader)
        tvTamilMonthYearHeader?.setTextColor(palette.secondaryHeader)
        btnPrev?.setColorFilter(palette.primaryHeader)
        btnNext?.setColorFilter(palette.primaryHeader)

        val year = displayCalendar.get(Calendar.YEAR)
        val month = displayCalendar.get(Calendar.MONTH)

        val engMonthName = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(displayCalendar.time)
        tvMonthYearTitle?.text = engMonthName.uppercase(Locale.ENGLISH)
        tvTamilMonthYearHeader?.text = getTamilMonthHeader(year, month + 1)

        val firstDayCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val daysInMonth = firstDayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = firstDayCal.get(Calendar.DAY_OF_WEEK)
        val firstDayOffset = firstDayOfWeek - 1
        val totalSlotsNeeded = firstDayOffset + daysInMonth
        val numCols = maxOf(5, (totalSlotsNeeded + 6) / 7)

        val gridMatrix = Array(ROW_COUNT) {
            arrayOfNulls<CalendarDayCell>(numCols)
        }

        var prevTamilMonth = ""

        for (day in 1..daysInMonth) {
            val queryDate = String.format(Locale.ENGLISH, "%04d-%02d-%02d", year, month + 1, day)
            val details = dbHelper.getDayDetails(queryDate)
            val tMonth = details?.tamilMonth ?: "---"
            val tDay = details?.tamilDate ?: 0

            if (day == 1) {
                prevTamilMonth = tMonth
            }

            val isNewMonthStart = (day == 1) || (
                tMonth.isNotBlank() &&
                tMonth != "---" &&
                tMonth != prevTamilMonth
            )

            if (tMonth.isNotBlank() && tMonth != "---") {
                prevTamilMonth = tMonth
            }

            val tempCal = firstDayCal.clone() as Calendar
            tempCal.set(Calendar.DAY_OF_MONTH, day)
            val dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
            val row = dayOfWeek - 1
            val col = ((day - 1) + firstDayOffset) / 7

            if (row in 0 until ROW_COUNT && col in 0 until numCols) {
                gridMatrix[row][col] = CalendarDayCell(
                    gregorianDay = day,
                    gregorianDate = queryDate,
                    tamilDay = tDay,
                    tamilMonth = tMonth,
                    isTamilMonthTransition = isNewMonthStart,
                    isSunday = (row == 0)
                )
            }
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

        val minRowHeight = dp(60)

        for (r in 0 until ROW_COUNT) {
            val tableRow = TableRow(context).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
                minimumHeight = minRowHeight
            }

            val headerCell = createWeekdayHeaderCell(
                weekdayHeaders[r].first,
                weekdayHeaders[r].second,
                r == 0,
                palette
            )
            tableRow.addView(headerCell)

            for (c in 0 until numCols) {
                tableRow.addView(
                    createDateCellView(
                        gridMatrix[r][c],
                        palette
                    )
                )
            }
            table.addView(tableRow)
        }
    }

    private fun createWeekdayHeaderCell(
        eng: String,
        tam: String,
        isSunday: Boolean,
        palette: ThemeManager.AppThemePalette
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
            cardElevation = dp(1).toFloat()
            setCardBackgroundColor(
                if (isSunday) palette.sundayHighlight
                else palette.cardBackground
            )

            val container = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(2), dp(4), dp(2), dp(4))
            }

            val tvEng = TextView(requireContext()).apply {
                text = eng
                textSize = 12.5f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(if (isSunday) palette.accentGold else palette.primaryHeader)
                gravity = Gravity.CENTER
                maxLines = 1
            }

            val tvTam = TextView(requireContext()).apply {
                text = tam
                textSize = 9.5f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(palette.secondaryHeader)
                gravity = Gravity.CENTER
                maxLines = 1
            }

            container.addView(tvEng)
            container.addView(tvTam)
            addView(container)
        }
    }

    private fun createDateCellView(
        cell: CalendarDayCell?,
        palette: ThemeManager.AppThemePalette
    ): View {
        val card = CardView(requireContext()).apply {
            layoutParams = TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            ).apply {
                setMargins(dp(1), dp(2), dp(1), dp(2))
            }

            radius = dp(5).toFloat()
            cardElevation = if (cell != null) 1.5f * resources.displayMetrics.density else 0f

            setCardBackgroundColor(
                when {
                    cell == null -> palette.emptyCellBackground
                    cell.isSunday -> palette.sundayHighlight
                    else -> palette.cardBackground
                }
            )
        }

        if (cell == null) {
            card.setOnClickListener { showEmptyCellToast() }
            return card
        }

        val frameCell = FrameLayout(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }

        val tvEnglish = TextView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
            text = cell.gregorianDay.toString()
            textSize = 21f
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 1
            includeFontPadding = false
            setTextColor(if (cell.isSunday) palette.accentGold else palette.primaryHeader)
        }
        frameCell.addView(tvEnglish)

        val tvTamil = TextView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.END
            )
            textSize = 9.5f
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 1
            includeFontPadding = false
            setTextColor(palette.secondaryHeader)

            text = when {
                cell.isTamilMonthTransition &&
                cell.tamilMonth.isNotEmpty() &&
                cell.tamilMonth != "---" -> {
                    "${cell.tamilMonth} ${cell.tamilDay}"
                }
                cell.tamilDay > 0 -> {
                    cell.tamilDay.toString()
                }
                else -> ""
            }
        }
        frameCell.addView(tvTamil)

        card.addView(frameCell)
        card.setOnClickListener {
            navigateToHomeWithDate(cell.gregorianDate)
        }
        return card
    }

    private fun showEmptyCellToast() {
        val toast = Toast.makeText(
            requireContext(),
            "please select correct date",
            Toast.LENGTH_SHORT
        )
        toast.show()
        Handler(Looper.getMainLooper()).postDelayed({ toast.cancel() }, 2000)
    }

    private fun getTamilMonthHeader(year: Int, month: Int): String {
        val midDate = String.format(Locale.ENGLISH, "%04d-%02d-15", year, month)
        val data = dbHelper.getDayDetails(midDate)
        return if (data != null && data.tamilMonth.isNotEmpty() && data.tamilMonth != "---") {
            "${data.tamilYear} • ${data.tamilMonth}"
        } else {
            "---"
        }
    }

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

    private fun navigateToHomeWithDate(dateString: String) {
        val act = activity ?: return
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).apply {
                isLenient = false
            }
            val parsedDate = sdf.parse(dateString) ?: return
            val targetCal = Calendar.getInstance().apply {
                time = parsedDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val methodNames = listOf("loadDayData", "updateSelectedDate", "setSelectedDate")
            val loadMethod = act.javaClass.methods.firstOrNull { method ->
                method.name in methodNames &&
                method.parameterTypes.size == 1 &&
                (
                    method.parameterTypes[0] == Calendar::class.java ||
                    method.parameterTypes[0] == String::class.java
                )
            }

            if (loadMethod != null) {
                when (loadMethod.parameterTypes[0]) {
                    Calendar::class.java -> loadMethod.invoke(act, targetCal)
                    String::class.java -> loadMethod.invoke(act, dateString)
                }
            } else {
                Log.w(TAG, "No compatible MainActivity date-update method found")
            }

            val rootDecor = act.window?.decorView
            val viewPager = if (rootDecor != null) findViewPagerInView(rootDecor) else null
            viewPager?.setCurrentItem(MainPagerAdapter.PAGE_PANCHANGAM, true)
        } catch (e: Exception) {
            Log.e(TAG, "Date navigation failed: $dateString", e)
        }
    }

    data class CalendarDayCell(
        val gregorianDay: Int,
        val gregorianDate: String,
        val tamilDay: Int,
        val tamilMonth: String,
        val isTamilMonthTransition: Boolean,
        val isSunday: Boolean
    )
}
