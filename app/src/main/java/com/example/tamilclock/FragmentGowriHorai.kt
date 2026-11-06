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
import androidx.fragment.app.Fragment
import java.util.Calendar
import java.util.Locale

class FragmentGowriHorai : Fragment() {

    companion object {
        private const val REFRESH_INTERVAL = 30_000L
        private const val ARG_IS_HORAI = "arg_is_horai"

        fun newInstance(initialTabIsHorai: Boolean): FragmentGowriHorai {
            val fragment = FragmentGowriHorai()
            fragment.arguments = Bundle().apply {
                putBoolean(ARG_IS_HORAI, initialTabIsHorai)
            }
            return fragment
        }
    }

    private lateinit var dbHelper: TamilDatabaseHelper
    private var rootLayout: LinearLayout? = null

    private lateinit var layoutDateStrip: LinearLayout
    private lateinit var tvHeaderStripDate: TextView

    private lateinit var tabBtnGowri: TextView
    private lateinit var tabBtnHorai: TextView

    private lateinit var layoutGowriTable: View
    private lateinit var layoutHoraiTable: View

    private lateinit var tvGowriDayHeader: TextView
    private lateinit var tvGowriNightHeader: TextView
    private lateinit var tvHoraiDayHeader: TextView
    private lateinit var tvHoraiNightHeader: TextView

    private lateinit var containerGowriDay: LinearLayout
    private lateinit var containerGowriNight: LinearLayout
    private lateinit var containerHoraiDay: LinearLayout
    private lateinit var containerHoraiNight: LinearLayout

    private var isGowriSelected = true
    private var isSelectedDateToday = true
    private var selectedCalendar: Calendar = Calendar.getInstance()

    private val refreshHandler = Handler(Looper.getMainLooper())

    data class SlotItem(
        val timeRange: String,
        val name: String,
        val startMin: Int,
        val endMin: Int,
        val isAuspicious: Boolean
    )

    private val gowriTimeRanges = listOf(
        Pair("06:00 - 07:30", Pair(360, 450)),
        Pair("07:30 - 09:00", Pair(450, 540)),
        Pair("09:00 - 10:30", Pair(540, 630)),
        Pair("10:30 - 12:00", Pair(630, 720)),
        Pair("12:00 - 01:30", Pair(720, 810)),
        Pair("01:30 - 03:00", Pair(810, 900)),
        Pair("03:00 - 04:30", Pair(900, 990)),
        Pair("04:30 - 06:00", Pair(990, 1080))
    )

    private val horaiTimeRanges = listOf(
        Pair("06:00 - 07:00", Pair(360, 420)),
        Pair("07:00 - 08:00", Pair(420, 480)),
        Pair("08:00 - 09:00", Pair(480, 540)),
        Pair("09:00 - 10:00", Pair(540, 600)),
        Pair("10:00 - 11:00", Pair(600, 660)),
        Pair("11:00 - 12:00", Pair(660, 720)),
        Pair("12:00 - 01:00", Pair(720, 780)),
        Pair("01:00 - 02:00", Pair(780, 840)),
        Pair("02:00 - 03:00", Pair(840, 900)),
        Pair("03:00 - 04:00", Pair(900, 960)),
        Pair("04:00 - 05:00", Pair(960, 1020)),
        Pair("05:00 - 06:00", Pair(1020, 1080))
    )

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

        layoutDateStrip = view.findViewById(R.id.layout_dynamic_date_strip)
        tvHeaderStripDate = view.findViewById(R.id.tv_header_strip_date)

        tabBtnGowri = view.findViewById(R.id.tab_btn_gowri)
        tabBtnHorai = view.findViewById(R.id.tab_btn_horai)

        layoutGowriTable = view.findViewById(R.id.layout_gowri_table)
        layoutHoraiTable = view.findViewById(R.id.layout_horai_table)

        tvGowriDayHeader = view.findViewById(R.id.tv_gowri_day_header)
        tvGowriNightHeader = view.findViewById(R.id.tv_gowri_night_header)
        tvHoraiDayHeader = view.findViewById(R.id.tv_horai_day_header)
        tvHoraiNightHeader = view.findViewById(R.id.tv_horai_night_header)

        containerGowriDay = view.findViewById(R.id.container_gowri_day_slots)
        containerGowriNight = view.findViewById(R.id.container_gowri_night_slots)
        containerHoraiDay = view.findViewById(R.id.container_horai_day_slots)
        containerHoraiNight = view.findViewById(R.id.container_horai_night_slots)

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

        renderTables(weekday)
        highlightCurrentSlotIfToday()
    }

    private fun applyTheme() {
        val context = context ?: return
        val palette = ThemeManager.getThemePalette(context)

        rootLayout?.setBackgroundColor(palette.pageBackground)
        layoutDateStrip.setBackgroundColor(palette.cardBackground)
        tvHeaderStripDate.setTextColor(palette.primaryHeader)

        tvGowriDayHeader.setBackgroundColor(palette.primaryHeader)
        tvGowriDayHeader.setTextColor(palette.accentGold)

        tvGowriNightHeader.setBackgroundColor(palette.secondaryHeader)
        tvGowriNightHeader.setTextColor(palette.accentGold)

        tvHoraiDayHeader.setBackgroundColor(palette.primaryHeader)
        tvHoraiDayHeader.setTextColor(palette.accentGold)

        tvHoraiNightHeader.setBackgroundColor(palette.secondaryHeader)
        tvHoraiNightHeader.setTextColor(palette.accentGold)

        styleTabs(palette)
    }

    private fun switchTab(showGowri: Boolean) {
        isGowriSelected = showGowri
        layoutGowriTable.visibility = if (showGowri) View.VISIBLE else View.GONE
        layoutHoraiTable.visibility = if (showGowri) View.GONE else View.VISIBLE

        val context = context ?: return
        val palette = ThemeManager.getThemePalette(context)
        styleTabs(palette)
        highlightCurrentSlotIfToday()
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

    private fun renderTables(weekdayTa: String) {
        containerGowriDay.removeAllViews()
        containerGowriNight.removeAllViews()
        containerHoraiDay.removeAllViews()
        containerHoraiNight.removeAllViews()

        val gowriDay = dbHelper.getGowriDetails(weekdayTa, "பகல்")
        val gowriNight = dbHelper.getGowriDetails(weekdayTa, "இரவு")
        populateSlotContainer(containerGowriDay, gowriDay?.slots, gowriTimeRanges, isGowri = true, isNight = false)
        populateSlotContainer(containerGowriNight, gowriNight?.slots, gowriTimeRanges, isGowri = true, isNight = true)

        val horaiDay = dbHelper.getHoraiDetails(weekdayTa, "பகல்")
        val horaiNight = dbHelper.getHoraiDetails(weekdayTa, "இரவு")
        populateSlotContainer(containerHoraiDay, horaiDay?.slots, horaiTimeRanges, isGowri = false, isNight = false)
        populateSlotContainer(containerHoraiNight, horaiNight?.slots, horaiTimeRanges, isGowri = false, isNight = true)
    }

    private fun populateSlotContainer(
        container: LinearLayout,
        slots: List<String>?,
        rangeDefs: List<Pair<String, Pair<Int, Int>>>,
        isGowri: Boolean,
        isNight: Boolean
    ) {
        if (slots.isNullOrEmpty()) {
            val emptyTv = TextView(requireContext()).apply {
                text = "---"
                gravity = Gravity.CENTER
                textSize = 14f
                setTextColor(Color.parseColor("#9E9E9E"))
                setPadding(0, 16, 0, 16)
            }
            container.addView(emptyTv)
            return
        }

        val baseOffset = if (isNight) 720 else 0

        slots.forEachIndexed { index, nameValue ->
            val rangeDef = rangeDefs.getOrNull(index)
            val timeText = rangeDef?.first ?: "---"
            val sMin = ((rangeDef?.second?.first ?: 0) + baseOffset) % 1440
            val eMin = ((rangeDef?.second?.second ?: 0) + baseOffset) % 1440

            val isGood = if (isGowri) isGowriGood(nameValue) else isHoraiGood(nameValue)
            val item = SlotItem(
                timeRange = timeText,
                name = if (nameValue.isBlank()) "---" else nameValue,
                startMin = sMin,
                endMin = if (eMin == 0 && isNight) 1440 else eMin,
                isAuspicious = isGood
            )
            container.addView(createSlotRowView(item))
        }
    }

    private fun createSlotRowView(item: SlotItem): View {
        val palette = ThemeManager.getThemePalette(requireContext())

        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 3, 0, 3)
            }
            setBackgroundColor(palette.cardBackground)
            setPadding(14, 12, 14, 12)
            elevation = 1f
            tag = item
        }

        val textColor = if (item.isAuspicious) {
            Color.parseColor("#2E7D32")
        } else {
            Color.parseColor("#C62828")
        }

        val tvTime = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
            text = item.timeRange
            textSize = 13f
            setTextColor(textColor)
            typeface = Typeface.DEFAULT_BOLD
        }

        val tvName = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
            text = item.name
            textSize = 13.5f
            gravity = Gravity.END
            setTextColor(textColor)
            typeface = Typeface.DEFAULT_BOLD
        }

        row.addView(tvTime)
        row.addView(tvName)
        return row
    }

    private fun highlightCurrentSlotIfToday() {
        if (!isAdded || view == null) return

        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val palette = ThemeManager.getThemePalette(requireContext())

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
                    row.setBackgroundColor(palette.sundayHighlight)
                } else {
                    row.setBackgroundColor(palette.cardBackground)
                }
            }
        }
    }

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
