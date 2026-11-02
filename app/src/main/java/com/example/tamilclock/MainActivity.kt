package com.example.tamilclock

import android.app.DatePickerDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var pagerAdapter: MainPagerAdapter
    private lateinit var calendarMonthAdapter: CalendarMonthAdapter
    private lateinit var dbHelper: TamilDatabaseHelper
    private lateinit var viewPager: ViewPager2

    private val selectedCalendar: Calendar = Calendar.getInstance()
    private val displayedMonthCalendar: Calendar = Calendar.getInstance()

    private lateinit var tvSheetMonthYear: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = TamilDatabaseHelper(this)

        setupTabsAndViewPager()
        setupHybridBottomSheet()
        setupBackNavigation()
        loadDayData()
        updateWidgets()
    }

    // Back button navigation: Sub-tabs return to Home, sheet dismisses first
    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                } else if (viewPager.currentItem != 0) {
                    viewPager.currentItem = 0
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun setupTabsAndViewPager() {
        viewPager = findViewById(R.id.view_pager_main)

        pagerAdapter = MainPagerAdapter(
            dbHelper = dbHelper,
            onPrevDayClick = {
                selectedCalendar.add(Calendar.DAY_OF_MONTH, -1)
                displayedMonthCalendar.time = selectedCalendar.time
                loadDayData()
                refreshMonthCalendarGrid()
                updateWidgets()
            },
            onNextDayClick = {
                selectedCalendar.add(Calendar.DAY_OF_MONTH, 1)
                displayedMonthCalendar.time = selectedCalendar.time
                loadDayData()
                refreshMonthCalendarGrid()
                updateWidgets()
            },
            onDatePickerClick = {
                val datePicker = DatePickerDialog(
                    this,
                    { _, year, month, dayOfMonth ->
                        selectedCalendar.set(year, month, dayOfMonth)
                        displayedMonthCalendar.time = selectedCalendar.time
                        loadDayData()
                        refreshMonthCalendarGrid()
                        updateWidgets()
                    },
                    selectedCalendar.get(Calendar.YEAR),
                    selectedCalendar.get(Calendar.MONTH),
                    selectedCalendar.get(Calendar.DAY_OF_MONTH)
                )
                datePicker.show()
            },
            onTodayClick = {
                selectedCalendar.time = Calendar.getInstance().time
                displayedMonthCalendar.time = selectedCalendar.time
                loadDayData()
                refreshMonthCalendarGrid()
                updateWidgets()
            },
            onMonthCalendarClick = {
                displayedMonthCalendar.time = selectedCalendar.time
                refreshMonthCalendarGrid()
                calendarMonthAdapter.setClassicMode(true)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            },
            onHoraiClick = {
                viewPager.currentItem = 2
            },
            onSettingsClick = {
                showThemeCustomizationSheet()
            }
        )
        viewPager.adapter = pagerAdapter
    }

    private fun loadDayData() {
        val queryFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ta"))

        val dateKey = queryFormat.format(selectedCalendar.time)
        val formattedHeader = displayFormat.format(selectedCalendar.time)

        val dayData = dbHelper.getDayDetails(dateKey)
        pagerAdapter.updateData(formattedHeader, dayData, selectedCalendar)
    }

    private fun setupHybridBottomSheet() {
        val sheetView = findViewById<View>(R.id.bottom_sheet_calendar)
        bottomSheetBehavior = BottomSheetBehavior.from(sheetView)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        tvSheetMonthYear = findViewById(R.id.tv_sheet_month_year)
        val btnPrevMonth = findViewById<TextView>(R.id.btn_prev_month)
        val btnNextMonth = findViewById<TextView>(R.id.btn_next_month)

        val container = findViewById<FrameLayout>(R.id.container_calendar_content)
        val contentView = LayoutInflater.from(this).inflate(R.layout.view_calendar_sheet_content, container, true)

        val rvGrid = contentView.findViewById<RecyclerView>(R.id.rv_calendar_month_grid)
        rvGrid.layoutManager = GridLayoutManager(this, 7)

        calendarMonthAdapter = CalendarMonthAdapter { selectedDateKey ->
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            format.parse(selectedDateKey)?.let { parsedDate ->
                selectedCalendar.time = parsedDate
                displayedMonthCalendar.time = parsedDate
                loadDayData()
                refreshMonthCalendarGrid()
                updateWidgets()
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
        rvGrid.adapter = calendarMonthAdapter

        btnPrevMonth.setOnClickListener {
            displayedMonthCalendar.add(Calendar.MONTH, -1)
            refreshMonthCalendarGrid()
        }

        btnNextMonth.setOnClickListener {
            displayedMonthCalendar.add(Calendar.MONTH, 1)
            refreshMonthCalendarGrid()
        }
        refreshMonthCalendarGrid()
    }

    private fun refreshMonthCalendarGrid() {
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale("ta"))
        tvSheetMonthYear.text = monthYearFormat.format(displayedMonthCalendar.time)
        calendarMonthAdapter.setMonthData(displayedMonthCalendar, selectedCalendar, dbHelper)
    }

    // Settings Customization: Uses verified layout IDs, updates widgets, avoids unresolved references
    private fun showThemeCustomizationSheet() {
        val dialog = BottomSheetDialog(this)
        val sheetView = layoutInflater.inflate(R.layout.dialog_theme_customization, null)
        dialog.setContentView(sheetView)

        val btnAnalog = sheetView.findViewById<Button>(R.id.btn_style_analog)
        val btnDigital = sheetView.findViewById<Button>(R.id.btn_style_digital)

        // Points directly to existing R.id.check_theme_maroon without missing identifier errors
        val checkMaroon = sheetView.findViewById<TextView>(R.id.check_theme_maroon)
        val checkWhite = sheetView.findViewById<TextView>(R.id.check_theme_white)
        val checkYellow = sheetView.findViewById<TextView>(R.id.check_theme_yellow)
        val checkGreen = sheetView.findViewById<TextView>(R.id.check_theme_green)
        val checkCharcoal = sheetView.findViewById<TextView>(R.id.check_theme_charcoal)

        fun updateIndicators() {
            val curClock = ThemeManager.getClockStyle(this)
            if (curClock == ThemeManager.CLOCK_ANALOG) {
                btnAnalog?.text = "✓ முள் கடிகாரம்"
                btnDigital?.text = "எண் கடிகாரம்"
            } else {
                btnAnalog?.text = "முள் கடிகாரம்"
                btnDigital?.text = "✓ எண் கடிகாரம்"
            }

            val curTheme = ThemeManager.getSavedTheme(this)
            checkMaroon?.visibility = if (curTheme == ThemeManager.THEME_IVORY || curTheme == ThemeManager.THEME_MAROON) View.VISIBLE else View.GONE
            checkWhite?.visibility = if (curTheme == ThemeManager.THEME_WHITE) View.VISIBLE else View.GONE
            checkYellow?.visibility = if (curTheme == ThemeManager.THEME_YELLOW) View.VISIBLE else View.GONE
            checkGreen?.visibility = if (curTheme == ThemeManager.THEME_GREEN) View.VISIBLE else View.GONE
            checkCharcoal?.visibility = if (curTheme == ThemeManager.THEME_CHARCOAL) View.VISIBLE else View.GONE
        }
        updateIndicators()

        // 1. Clock Style Buttons
        btnAnalog?.setOnClickListener {
            ThemeManager.saveClockStyle(this, ThemeManager.CLOCK_ANALOG)
            updateWidgets()
            updateIndicators()
        }
        btnDigital?.setOnClickListener {
            ThemeManager.saveClockStyle(this, ThemeManager.CLOCK_DIGITAL)
            updateWidgets()
            updateIndicators()
        }

        // 2. Widget Opacity Slider
        val seekbarOpacity = sheetView.findViewById<SeekBar>(R.id.seekbar_widget_opacity)
        val tvOpacity = sheetView.findViewById<TextView>(R.id.tv_opacity_value)
        val currentOpacity = ThemeManager.getWidgetOpacity(this)

        seekbarOpacity?.progress = currentOpacity
        tvOpacity?.text = "$currentOpacity%"

        seekbarOpacity?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvOpacity?.text = "$progress%"
                if (fromUser) {
                    ThemeManager.saveWidgetOpacity(this@MainActivity, progress)
                    updateWidgets()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    ThemeManager.saveWidgetOpacity(this@MainActivity, it.progress)
                    updateWidgets()
                }
            }
        })

        // 3. Theme Selections: Updates settings & triggers immediate widget updates
        sheetView.findViewById<View>(R.id.option_theme_maroon)?.setOnClickListener {
            ThemeManager.saveTheme(this, ThemeManager.THEME_IVORY)
            updateIndicators()
            updateWidgets()
        }
        sheetView.findViewById<View>(R.id.option_theme_white)?.setOnClickListener {
            ThemeManager.saveTheme(this, ThemeManager.THEME_WHITE)
            updateIndicators()
            updateWidgets()
        }
        sheetView.findViewById<View>(R.id.option_theme_yellow)?.setOnClickListener {
            ThemeManager.saveTheme(this, ThemeManager.THEME_YELLOW)
            updateIndicators()
            updateWidgets()
        }
        sheetView.findViewById<View>(R.id.option_theme_green)?.setOnClickListener {
            ThemeManager.saveTheme(this, ThemeManager.THEME_GREEN)
            updateIndicators()
            updateWidgets()
        }
        sheetView.findViewById<View>(R.id.option_theme_charcoal)?.setOnClickListener {
            ThemeManager.saveTheme(this, ThemeManager.THEME_CHARCOAL)
            updateIndicators()
            updateWidgets()
        }

        dialog.show()
    }

    private fun updateWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(this)

        val c2x1 = ComponentName(this, WidgetProvider2x1::class.java)
        val ids2x1 = appWidgetManager.getAppWidgetIds(c2x1)
        if (ids2x1.isNotEmpty()) {
            WidgetUpdateHelper.update2x1(this, appWidgetManager, ids2x1)
            sendWidgetBroadcast(WidgetProvider2x1::class.java, ids2x1)
        }

        val c4x1 = ComponentName(this, WidgetProvider4x1::class.java)
        val ids4x1 = appWidgetManager.getAppWidgetIds(c4x1)
        if (ids4x1.isNotEmpty()) {
            WidgetUpdateHelper.update4x1(this, appWidgetManager, ids4x1)
            sendWidgetBroadcast(WidgetProvider4x1::class.java, ids4x1)
        }

        val c4x2 = ComponentName(this, WidgetProvider4x2::class.java)
        val ids4x2 = appWidgetManager.getAppWidgetIds(c4x2)
        if (ids4x2.isNotEmpty()) {
            WidgetUpdateHelper.update4x2(this, appWidgetManager, ids4x2)
            sendWidgetBroadcast(WidgetProvider4x2::class.java, ids4x2)
        }

        val c4x4 = ComponentName(this, WidgetProvider4x4::class.java)
        val ids4x4 = appWidgetManager.getAppWidgetIds(c4x4)
        if (ids4x4.isNotEmpty()) {
            WidgetUpdateHelper.update4x4(this, appWidgetManager, ids4x4)
            sendWidgetBroadcast(WidgetProvider4x4::class.java, ids4x4)
        }
    }

    private fun sendWidgetBroadcast(providerClass: Class<*>, ids: IntArray) {
        val intent = Intent(this, providerClass).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        sendBroadcast(intent)
    }
}
