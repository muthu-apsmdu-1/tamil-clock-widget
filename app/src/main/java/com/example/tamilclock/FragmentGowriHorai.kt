package com.example.tamilclock

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FragmentGowriHorai : Fragment() {

    companion object {
        private const val REFRESH_INTERVAL = 30_000L
        private const val ARG_IS_HORAI = "arg_is_horai"

        private val AUSPICIOUS_GOWRI = setOf("அமிர்தம்", "லாபம்", "தனம்", "சுகம்", "உத்தி", "சுபம்")
        private val AUSPICIOUS_HORAI = setOf("குரு", "சுக்கிரன்", "புதன்", "சந்திரன்")

        fun newInstance(initialTabIsHorai: Boolean): FragmentGowriHorai {
            return FragmentGowriHorai().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_IS_HORAI, initialTabIsHorai)
                }
            }
        }
    }

    private lateinit var dbHelper: TamilDatabaseHelper
    private var rootLayout: LinearLayout? = null

    // Date Strip & Live Clock
    private lateinit var layoutDateStrip: LinearLayout
    private lateinit var tvHeaderStripDate: TextView
    private lateinit var tvHeaderLiveClock: TextView

    // Sub-Tabs
    private lateinit var tabBtnGowri: TextView
    private lateinit var tabBtnHorai: TextView

    // 3-Column Table Header
    private lateinit var layoutTableHeader: LinearLayout
    private lateinit var tvColHeaderTime: TextView
    private lateinit var colHeaderDay: LinearLayout
    private lateinit var tvColHeaderDayTitle: TextView
    private lateinit var tvColHeaderDaySub: TextView
    private lateinit var colHeaderNight: LinearLayout
    private lateinit var tvColHeaderNightTitle: TextView
    private lateinit var tvColHeaderNightSub: TextView

    // Scroll & Container
    private var scrollGowriHorai: NestedScrollView? = null
    private lateinit var containerTableRows: LinearLayout

    // Bottom Legend Footer Strip
    private lateinit var layoutLegendFooter: LinearLayout
    private lateinit var tvLegendBadge: TextView
    private lateinit var tvLegendText: TextView

    private var isGowriSelected = true
    private var isSelectedDateToday = true
    private var selectedCalendar: Calendar = Calendar.getInstance()

    private val refreshHandler = Handler(Looper.getMainLooper())

    private val timeTickRunnable = object : Runnable {
        override fun run() {
            if (!isAdded || view == null) return
            updateLiveClockIfToday()
            highlightCurrentSlotIfToday()
            refreshHandler.postDelayed(this, REFRESH_INTERVAL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dbHelper = TamilDatabaseHelper(requireContext())
        return inflater.inflate(R.layout.fragment_gowri_horai, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootLayout = view.findViewById(R.id.root_gowri_horai)

        arguments?.let {
            val startAsHorai = it.getBoolean(ARG_IS_HORAI, false)
            isGowriSelected = !startAsHorai
        }

        // Top Summary Header Views
        layoutDateStrip = view.findViewById(R.id.layout_dynamic_date_strip)
        tvHeaderStripDate = view.findViewById(R.id.tv_header_strip_date)
        tvHeaderLiveClock = view.findViewById(R.id.tv_header_live_clock)

        // Sub Tabs
        tabBtnGowri = view.findViewById(R.id.tab_btn_gowri)
        tabBtnHorai = view.findViewById(R.id.tab_btn_horai)

        // 3-Column Header
        layoutTableHeader = view.findViewById(R.id.layout_table_header)
        tvColHeaderTime = view.findViewById(R.id.tv_col_header_time)
        colHeaderDay = view.findViewById(R.id.col_header_day)
        tvColHeaderDayTitle = view.findViewById(R.id.tv_col_header_day_title)
        tvColHeaderDaySub = view.findViewById(R.id.tv_col_header_day_sub)
        colHeaderNight = view.findViewById(R.id.col_header_night)
        tvColHeaderNightTitle = view.findViewById(R.id.tv_col_header_night_title)
        tvColHeaderNightSub = view.findViewById(R.id.tv_col_header_night_sub)

        // Scroll & Rows Container
        scrollGowriHorai = view.findViewById(R.id.scroll_gowri_horai)
        containerTableRows = view.findViewById(R.id.container_table_rows)

        // Bottom Legend Footer
        layoutLegendFooter = view.findViewById(R.id.layout_legend_footer)
        tvLegendBadge = view.findViewById(R.id.tv_legend_badge)
        tvLegendText = view.findViewById(R.id.tv_legend_text)

        tabBtnGowri.setOnClickListener { switchTab(true) }
        tabBtnHorai.setOnClickListener { switchTab(false) }

        applyTheme()
        switchTab(isGowriSelected)
        updateDate(selectedCalendar)
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
        renderCurrentData()
        updateLiveClockIfToday()
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

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }

    fun updateDate(cal: Calendar) {
        selectedCalendar = cal.clone() as Calendar
        isSelectedDateToday = isSameDate(selectedCalendar, Calendar.getInstance())

        if (!isAdded || view == null) return
        renderCurrentData()
        updateLiveClockIfToday()
    }

    private fun updateLiveClockIfToday() {
        if (!isSelectedDateToday) {
            tvHeaderLiveClock.visibility = View.GONE
            return
        }
        tvHeaderLiveClock.visibility = View.VISIBLE
        val sdf = SimpleDateFormat("hh : mm a", Locale.ENGLISH)
        tvHeaderLiveClock.text = sdf.format(Calendar.getInstance().time)
    }

    private fun renderCurrentData() {
        val dateKey = String.format(
            Locale.ENGLISH,
            "%04d-%02d-%02d",
            selectedCalendar.get(Calendar.YEAR),
            selectedCalendar.get(Calendar.MONTH) + 1,
            selectedCalendar.get(Calendar.DAY_OF_MONTH)
        )
        val dayDetails = dbHelper.getDayDetails(dateKey)

        val day = selectedCalendar.get(Calendar.DAY_OF_MONTH)
        val month = selectedCalendar.get(Calendar.MONTH) + 1
        val year = selectedCalendar.get(Calendar.YEAR) % 100
        val weekday = dayDetails?.weekdayTa ?: getTamilDayName(selectedCalendar.get(Calendar.DAY_OF_WEEK))
        val tamilMonth = dayDetails?.tamilMonth ?: "---"
        val tamilDay = if ((dayDetails?.tamilDate ?: 0) > 0) dayDetails!!.tamilDate.toString() else "---"

        tvHeaderStripDate.text = "$day - $month - $year, $tamilMonth $tamilDay, $weekday"

        renderTableData(weekday)
        highlightCurrentSlotIfToday()
    }

    private fun isDarkThemeActive(): Boolean {
        val context = context ?: return false
        val currentBg = ThemeManager.getAppBackgroundColor(context)
        return currentBg == ThemeManager.APP_COLOR_AMOLED_BLACK || 
               currentBg == ThemeManager.APP_COLOR_MIDNIGHT_VIOLET
    }

    private fun applyTheme() {
        val context = context ?: return
        val palette = ThemeManager.getThemePalette(context)
        val isDark = isDarkThemeActive()

        rootLayout?.setBackgroundColor(palette.pageBackground)

        layoutDateStrip.setBackgroundColor(palette.primaryHeader)
        tvHeaderStripDate.setTextColor(palette.accentGold)

        tvHeaderLiveClock.setBackgroundColor(palette.cardBackground)
        tvHeaderLiveClock.setTextColor(if (isDark) Color.WHITE else palette.primaryHeader)

        layoutTableHeader.setBackgroundColor(palette.primaryHeader)
        tvColHeaderTime.setTextColor(Color.WHITE)
        tvColHeaderDayTitle.setTextColor(Color.WHITE)
        tvColHeaderDaySub.setTextColor(palette.accentGold)
        tvColHeaderNightTitle.setTextColor(Color.WHITE)
        tvColHeaderNightSub.setTextColor(palette.accentGold)

        tvLegendBadge.setBackgroundColor(palette.primaryHeader)
        tvLegendBadge.setTextColor(palette.accentGold)
        tvLegendText.setTextColor(if (isDark) Color.WHITE else palette.primaryText)

        styleTabs(palette)
    }

    private fun switchTab(showGowri: Boolean) {
        isGowriSelected = showGowri
        val context = context ?: return
        val palette = ThemeManager.getThemePalette(context)
        styleTabs(palette)
        renderCurrentData()
    }

    private fun styleTabs(palette: ThemeManager.AppThemePalette) {
        if (isGowriSelected) {
            tabBtnGowri.setBackgroundColor(palette.primaryHeader)
            tabBtnGowri.setTextColor(palette.accentGold)

            tabBtnHorai.setBackgroundColor(palette.emptyCellBackground)
            tabBtnHorai.setTextColor(palette.secondaryText)
        } else {
            tabBtnHorai.setBackgroundColor(palette.primaryHeader)
            tabBtnHorai.setTextColor(palette.accentGold)

            tabBtnGowri.setBackgroundColor(palette.emptyCellBackground)
            tabBtnGowri.setTextColor(palette.secondaryText)
        }
    }

    private fun renderTableData(weekdayTa: String) {
        containerTableRows.removeAllViews()

        val slotData = if (isGowriSelected) {
            dbHelper.getGowriTableData(weekdayTa)
        } else {
            dbHelper.getHoraiTableData(weekdayTa)
        }

        slotData.forEachIndexed { index, item ->
            val isDayGood = if (isGowriSelected) {
                AUSPICIOUS_GOWRI.contains(item.dayValue.trim())
            } else {
                AUSPICIOUS_HORAI.contains(item.dayValue.trim())
            }

            val isNightGood = if (isGowriSelected) {
                AUSPICIOUS_GOWRI.contains(item.nightValue.trim())
            } else {
                AUSPICIOUS_HORAI.contains(item.nightValue.trim())
            }

            containerTableRows.addView(
                create3ColumnRowView(
                    item = item,
                    isDayGood = isDayGood,
                    isNightGood = isNightGood,
                    isAlternate = index % 2 == 1
                )
            )
        }
    }

    private fun create3ColumnRowView(
        item: TamilDatabaseHelper.TableSlotData,
        isDayGood: Boolean,
        isNightGood: Boolean,
        isAlternate: Boolean
    ): View {
        val context = requireContext()
        val palette = ThemeManager.getThemePalette(context)
        val density = resources.displayMetrics.density
        val isDark = isDarkThemeActive()

        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            weightSum = 10f
            setBackgroundColor(if (isAlternate) palette.cardBackground else palette.pageBackground)
            tag = item
        }

        // Col 1: Time Range (wrap_content height with symmetric padding)
        val tvTime = TextView(context).apply {
            id = R.id.tv_col_header_time
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3.2f)
            text = item.timeLabel
            textSize = 12.5f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(4, (10 * density).toInt(), 4, (10 * density).toInt())
            setTextColor(if (isDark) Color.WHITE else palette.primaryHeader)
        }

        val divider1 = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT)
            setBackgroundColor(palette.emptyCellBackground)
        }

        // Col 2: Day Slot Value (wrap_content height with symmetric padding)
        val dayColor = if (isDayGood) Color.parseColor("#008A20") else Color.parseColor("#D31212")
        val tvDay = TextView(context).apply {
            id = R.id.col_header_day
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3.4f)
            text = item.dayValue
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(4, (10 * density).toInt(), 4, (10 * density).toInt())
            setTextColor(dayColor)
        }

        val divider2 = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT)
            setBackgroundColor(palette.emptyCellBackground)
        }

        // Col 3: Night Slot Value (wrap_content height with symmetric padding)
        val nightColor = if (isNightGood) Color.parseColor("#008A20") else Color.parseColor("#D31212")
        val tvNight = TextView(context).apply {
            id = R.id.col_header_night
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3.4f)
            text = item.nightValue
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(4, (10 * density).toInt(), 4, (10 * density).toInt())
            setTextColor(nightColor)
        }

        row.addView(tvTime)
        row.addView(divider1)
        row.addView(tvDay)
        row.addView(divider2)
        row.addView(tvNight)

        return row
    }

    private fun highlightCurrentSlotIfToday() {
        if (!isAdded || view == null) return

        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val palette = ThemeManager.getThemePalette(requireContext())
        val isDayTime = currentMinutes in 360..1079 // 06:00 AM to 05:59 PM

        for (i in 0 until containerTableRows.childCount) {
            val row = containerTableRows.getChildAt(i) as? LinearLayout ?: continue
            val item = row.tag as? TamilDatabaseHelper.TableSlotData ?: continue

            val tvDayCell = row.findViewById<TextView>(R.id.col_header_day)
            val tvNightCell = row.findViewById<TextView>(R.id.col_header_night)

            val nightStart = (item.startMin + 720) % 1440
            val nightEnd = if (item.endMin + 720 == 1440) 1440 else (item.endMin + 720) % 1440

            val isDayActive = currentMinutes in item.startMin until item.endMin
            val isNightActive = if (nightEnd > nightStart) {
                currentMinutes in nightStart until nightEnd
            } else {
                currentMinutes >= nightStart || currentMinutes < nightEnd
            }

            if (isSelectedDateToday && isDayTime && isDayActive) {
                tvDayCell?.setBackgroundColor(palette.sundayHighlight)
            } else {
                tvDayCell?.setBackgroundColor(Color.TRANSPARENT)
            }

            if (isSelectedDateToday && !isDayTime && isNightActive) {
                tvNightCell?.setBackgroundColor(palette.sundayHighlight)
            } else {
                tvNightCell?.setBackgroundColor(Color.TRANSPARENT)
            }
        }
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
            else -> "---"
        }
    }

    private fun isSameDate(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
