package com.example.tamilclock

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FragmentGowriHorai : Fragment() {

    private lateinit var dbHelper: TamilDatabaseHelper
    private var currentCalendar: Calendar = Calendar.getInstance()
    private var isHoraiSelected: Boolean = true

    // Tracks the system date when rendered to detect midnight rollover automatically
    private var lastTrackedSystemYear: Int = -1
    private var lastTrackedSystemDayOfYear: Int = -1

    private var vh: GowriHoraiViewHolder? = null

    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            if (!isAdded || vh == null) return
            checkMidnightRolloverAndRefresh()
            refreshHandler.postDelayed(this, 15_000L)
        }
    }

    companion object {
        private const val KEY_IS_HORAI = "key_is_horai_tab"
        private const val ARG_IS_HORAI = "arg_is_horai"
        private val NEON_GREEN = Color.parseColor("#39FF14")
        private val INACTIVE_GREY = Color.parseColor("#B0B0B0")

        fun newInstance(initialTabIsHorai: Boolean): FragmentGowriHorai {
            return FragmentGowriHorai().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_IS_HORAI, initialTabIsHorai)
                }
            }
        }
    }

    private class GowriHoraiViewHolder(view: View) {
        val cardHeader: CardView = view.findViewById(R.id.card_gh_header)
        val btnPrevDay: ImageButton = view.findViewById(R.id.btn_gh_prev_day)
        val btnNextDay: ImageButton = view.findViewById(R.id.btn_gh_next_day)
        val tvGregorianDate: TextView = view.findViewById(R.id.tv_gh_gregorian_date)
        val tvTamilDateDay: TextView = view.findViewById(R.id.tv_gh_tamil_date_day)
        val tvSunrise: TextView = view.findViewById(R.id.tv_gh_sunrise)
        val tvSunset: TextView = view.findViewById(R.id.tv_gh_sunset)
        val liveClockGroup: LinearLayout = view.findViewById(R.id.ll_gh_live_clock_group)
        val tcLiveClock: TextClock = view.findViewById(R.id.tc_gh_live_clock)

        val tabGowri: TextView = view.findViewById(R.id.tab_btn_gowri)
        val tabHorai: TextView = view.findViewById(R.id.tab_btn_horai)

        val cardDayTable: LinearLayout = view.findViewById(R.id.card_gh_day_table)
        val headerDayCols: LinearLayout = view.findViewById(R.id.header_gh_day_cols)
        val colDayPhase: TextView = view.findViewById(R.id.col_day_phase)
        val colDayTime: TextView = view.findViewById(R.id.col_day_time)
        val colDayType: TextView = view.findViewById(R.id.col_day_type)
        val containerDayRows: LinearLayout = view.findViewById(R.id.container_day_rows)

        val tvNightBanner: TextView = view.findViewById(R.id.tv_gh_night_banner)

        val cardNightTable: LinearLayout = view.findViewById(R.id.card_gh_night_table)
        val headerNightCols: LinearLayout = view.findViewById(R.id.header_gh_night_cols)
        val colNightPhase: TextView = view.findViewById(R.id.col_night_phase)
        val colNightTime: TextView = view.findViewById(R.id.col_night_time)
        val colNightType: TextView = view.findViewById(R.id.col_night_type)
        val containerNightRows: LinearLayout = view.findViewById(R.id.container_night_rows)

        val tvFooter: TextView = view.findViewById(R.id.tv_gh_next_sunrise_footer)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = TamilDatabaseHelper(requireContext())
        if (savedInstanceState != null) {
            isHoraiSelected = savedInstanceState.getBoolean(KEY_IS_HORAI, true)
        } else {
            arguments?.let {
                isHoraiSelected = it.getBoolean(ARG_IS_HORAI, true)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_IS_HORAI, isHoraiSelected)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_gowri_horai, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.selectedCalendar?.let {
            currentCalendar = it.clone() as Calendar
        }

        val holder = GowriHoraiViewHolder(view)
        vh = holder

        recordCurrentSystemDate()
        setupListeners(holder)
        renderAll()
    }

    override fun onResume() {
        super.onResume()
        if (vh != null) {
            checkMidnightRolloverAndRefresh(forceRender = true)
        }
        refreshHandler.removeCallbacks(refreshRunnable)
        refreshHandler.postDelayed(refreshRunnable, 15_000L)
    }

    override fun onPause() {
        refreshHandler.removeCallbacks(refreshRunnable)
        super.onPause()
    }

    override fun onDestroyView() {
        refreshHandler.removeCallbacksAndMessages(null)
        vh = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        if (::dbHelper.isInitialized) {
            dbHelper.close()
        }
        super.onDestroy()
    }

    fun updateDate(cal: Calendar) {
        currentCalendar = cal.clone() as Calendar
        recordCurrentSystemDate()
        if (isAdded && vh != null) {
            renderAll()
        }
    }

    private fun recordCurrentSystemDate() {
        val now = Calendar.getInstance()
        lastTrackedSystemYear = now.get(Calendar.YEAR)
        lastTrackedSystemDayOfYear = now.get(Calendar.DAY_OF_YEAR)
    }

    private fun checkMidnightRolloverAndRefresh(forceRender: Boolean = false) {
        val now = Calendar.getInstance()
        val nowYear = now.get(Calendar.YEAR)
        val nowDayOfYear = now.get(Calendar.DAY_OF_YEAR)

        val dateRolledOver = (lastTrackedSystemYear != -1) &&
            (nowYear != lastTrackedSystemYear || nowDayOfYear != lastTrackedSystemDayOfYear)

        if (dateRolledOver) {
            // Check if the user was viewing the previous "Today"
            val wasViewingPreviousToday =
                currentCalendar.get(Calendar.YEAR) == lastTrackedSystemYear &&
                currentCalendar.get(Calendar.DAY_OF_YEAR) == lastTrackedSystemDayOfYear

            recordCurrentSystemDate()

            if (wasViewingPreviousToday) {
                currentCalendar = now.clone() as Calendar
                (activity as? MainActivity)?.syncSelectedDate(currentCalendar.clone() as Calendar)
            }
            renderAll()
        } else if (forceRender || isToday(currentCalendar)) {
            renderAll()
        }
    }

    private fun setupListeners(h: GowriHoraiViewHolder) {
        h.btnPrevDay.setOnClickListener {
            currentCalendar.add(Calendar.DAY_OF_MONTH, -1)
            renderAll()
            (activity as? MainActivity)?.syncSelectedDate(currentCalendar.clone() as Calendar)
        }

        h.btnNextDay.setOnClickListener {
            currentCalendar.add(Calendar.DAY_OF_MONTH, 1)
            renderAll()
            (activity as? MainActivity)?.syncSelectedDate(currentCalendar.clone() as Calendar)
        }

        h.tabGowri.setOnClickListener {
            if (isHoraiSelected) {
                isHoraiSelected = false
                renderAll()
            }
        }

        h.tabHorai.setOnClickListener {
            if (!isHoraiSelected) {
                isHoraiSelected = true
                renderAll()
            }
        }
    }

    private fun isToday(cal: Calendar): Boolean {
        val today = Calendar.getInstance()
        return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
               cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    private fun renderAll() {
        val rootView = view ?: return
        val context = context ?: return
        val h = vh ?: return
        val density = resources.displayMetrics.density
        val palette = ThemeManager.getThemePalette(context)
        val isDark = ColorUtils.calculateLuminance(palette.pageBackground) < 0.35

        rootView.setBackgroundColor(palette.pageBackground)

        // 1. Header Today (Fluorescent Neon Green + Live Clock) vs Non-Today (Grey, Clock Hidden)
        val viewingToday = isToday(currentCalendar)
        if (viewingToday) {
            h.tvGregorianDate.setTextColor(NEON_GREEN)
            h.liveClockGroup.visibility = View.VISIBLE
            h.tcLiveClock.setTextColor(NEON_GREEN)
        } else {
            h.tvGregorianDate.setTextColor(INACTIVE_GREY)
            h.liveClockGroup.visibility = View.GONE
        }

        // 2. Dual Pill Selector Tabs
        val activeTabBg = GradientDrawable().apply {
            cornerRadius = 18f * density
            setColor(Color.parseColor("#0D5C2E"))
            setStroke((1.5f * density).toInt(), palette.accentGold)
        }
        val inactiveTabBg = GradientDrawable().apply {
            cornerRadius = 18f * density
            setColor(palette.cardBackground)
            setStroke((1f * density).toInt(), palette.secondaryHeader)
        }

        if (isHoraiSelected) {
            h.tabHorai.background = activeTabBg
            h.tabHorai.setTextColor(palette.accentGold)
            h.tabGowri.background = inactiveTabBg
            h.tabGowri.setTextColor(if (isDark) Color.WHITE else palette.primaryHeader)
            h.colDayType.text = "ஹோரை"
            h.colNightType.text = "ஹோரை"
            h.tvNightBanner.text = "இரவு ஹோரை"
        } else {
            h.tabGowri.background = activeTabBg
            h.tabGowri.setTextColor(palette.accentGold)
            h.tabHorai.background = inactiveTabBg
            h.tabHorai.setTextColor(if (isDark) Color.WHITE else palette.primaryHeader)
            h.colDayType.text = "கௌரி"
            h.colNightType.text = "கௌரி"
            h.tvNightBanner.text = "இரவு கௌரி"
        }

        // 3. Table Headers & Night Banner
        h.cardDayTable.setBackgroundColor(palette.cardBackground)
        h.cardNightTable.setBackgroundColor(palette.cardBackground)
        h.headerDayCols.setBackgroundColor(palette.secondaryHeader)
        h.headerNightCols.setBackgroundColor(palette.secondaryHeader)

        val colHdrText = palette.accentGold
        h.colDayPhase.setTextColor(colHdrText)
        h.colDayTime.setTextColor(colHdrText)
        h.colDayType.setTextColor(colHdrText)
        h.colNightPhase.setTextColor(colHdrText)
        h.colNightTime.setTextColor(colHdrText)
        h.colNightType.setTextColor(colHdrText)

        val nightBannerBg = GradientDrawable().apply {
            cornerRadius = 16f * density
            setColor(Color.parseColor("#081E7D"))
            setStroke((1.5f * density).toInt(), palette.accentGold)
        }
        h.tvNightBanner.background = nightBannerBg
        h.tvNightBanner.setTextColor(palette.accentGold)

        // 4. Query Dates & Populate Data
        val sdfDb = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val sdfDisplay = SimpleDateFormat("dd - MM - yyyy", Locale.ENGLISH)

        val todayKey = sdfDb.format(currentCalendar.time)
        h.tvGregorianDate.text = sdfDisplay.format(currentCalendar.time)

        val tomorrowCal = (currentCalendar.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }
        val tomorrowKey = sdfDb.format(tomorrowCal.time)

        val todayDetails = dbHelper.getDayDetails(todayKey)
        val tomorrowDetails = dbHelper.getDayDetails(tomorrowKey)

        val tamilMonth = todayDetails?.tamilMonth ?: "---"
        val tamilDate = if ((todayDetails?.tamilDate ?: 0) > 0) todayDetails!!.tamilDate.toString() else "---"
        val weekdayTa = todayDetails?.weekdayTa ?: getTamilDayName(currentCalendar.get(Calendar.DAY_OF_WEEK))
        h.tvTamilDateDay.text = "$tamilMonth $tamilDate, $weekdayTa"

        val dynamicData = if (isHoraiSelected) {
            dbHelper.getDynamicHoraiData(todayKey, tomorrowKey, weekdayTa)
        } else {
            dbHelper.getDynamicGowriData(todayKey, tomorrowKey, weekdayTa)
        }

        h.tvSunrise.text = "☀️ ↑ ${dynamicData.sunriseToday}"
        h.tvSunset.text = "↓ ${dynamicData.sunsetToday}"

        val nowCal = Calendar.getInstance()
        val currentMinsFromMidnight = if (viewingToday) {
            val rawNow = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)
            val riseToday = dynamicData.daySlots.firstOrNull()?.startEpochMins ?: 360
            if (rawNow < riseToday) rawNow + 1440 else rawNow
        } else {
            -1
        }

        populateTableRows(
            container = h.containerDayRows,
            slots = dynamicData.daySlots,
            isDark = isDark,
            activeMinute = currentMinsFromMidnight
        )

        populateTableRows(
            container = h.containerNightRows,
            slots = dynamicData.nightSlots,
            isDark = isDark,
            activeMinute = currentMinsFromMidnight
        )

        // 5. Bottom Next-Day Sunrise Pill
        val tomorrowWeekday = tomorrowDetails?.weekdayTa ?: getTamilDayName(tomorrowCal.get(Calendar.DAY_OF_WEEK))
        val tomorrowDisplay = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(tomorrowCal.time)
        val footerBg = GradientDrawable().apply {
            cornerRadius = 16f * density
            setColor(Color.parseColor("#5C0011"))
            setStroke((1.5f * density).toInt(), palette.accentGold)
        }
        h.tvFooter.background = footerBg
        h.tvFooter.setTextColor(palette.accentGold)
        h.tvFooter.text = "$tomorrowDisplay - $tomorrowWeekday - சூரிய உதயம்: காலை ${dynamicData.sunriseTomorrow}"
    }

    private fun populateTableRows(
        container: LinearLayout,
        slots: List<TamilDatabaseHelper.SolarSlotItem>,
        isDark: Boolean,
        activeMinute: Int
    ) {
        container.removeAllViews()
        val ctx = context ?: return
        val density = resources.displayMetrics.density

        val auspiciousCol = if (isDark) Color.parseColor("#69F0AE") else Color.parseColor("#1B5E20")
        val cautionCol = if (isDark) Color.parseColor("#FF5252") else Color.parseColor("#B71C1C")

        slots.forEachIndexed { index, item ->
            val isCurrentSlot = activeMinute in item.startEpochMins until item.endEpochMins
            val rowTextCol = if (item.isAuspicious) auspiciousCol else cautionCol

            val rowLayout = LinearLayout(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (34 * density).toInt()
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                weightSum = 3f

                val rowBg = GradientDrawable().apply {
                    setStroke((0.6f * density).toInt(), Color.parseColor("#44888888"))
                    when {
                        isCurrentSlot -> setColor(if (isDark) Color.parseColor("#1B4D2E") else Color.parseColor("#C8E6C9"))
                        index % 2 == 1 -> setColor(if (isDark) Color.parseColor("#14FFFFFF") else Color.parseColor("#0D000000"))
                        else -> setColor(Color.TRANSPARENT)
                    }
                }
                background = rowBg
            }

            val tvPhase = TextView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.85f)
                gravity = Gravity.CENTER
                text = item.phaseLabel
                textSize = 12.5f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(rowTextCol)
            }

            val tvTime = TextView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.15f)
                gravity = Gravity.CENTER
                text = item.timeRange
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(rowTextCol)
            }

            val tvValue = TextView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
                gravity = Gravity.CENTER
                text = item.valueName
                textSize = 13.5f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(rowTextCol)
            }

            rowLayout.addView(tvPhase)
            rowLayout.addView(tvTime)
            rowLayout.addView(tvValue)
            container.addView(rowLayout)
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
}
