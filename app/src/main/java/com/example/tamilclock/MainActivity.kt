package com.example.tamilclock

import android.app.DatePickerDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
        setupTopNavigationToolbar()
        setupHybridBottomSheet()
        setupBackNavigation()
        loadDayData()
        updateWidgets()
    }

    // Back button navigation: Sheet dismisses first, any sub-page returns to Home (Index 2), then exits app
    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                } else if (viewPager.currentItem != MainPagerAdapter.PAGE_PANCHANGAM) {
                    viewPager.setCurrentItem(MainPagerAdapter.PAGE_PANCHANGAM, true)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun setupTabsAndViewPager() {
        viewPager = findViewById(R.id.view_pager_main)
        pagerAdapter = MainPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        // Launch directly onto Home Screen: Day Panchang (Index 2)
        viewPager.setCurrentItem(MainPagerAdapter.PAGE_PANCHANGAM, false)
    }

    private fun setupTopNavigationToolbar() {
        // 7-Button Navigation Bar in activity_main.xml
        val btnPrev = findViewById<TextView>(R.id.btn_action_prev)
        val btnDatePicker = findViewById<TextView>(R.id.btn_action_datepicker)
        val btnMenu = findViewById<TextView>(R.id.btn_action_menu)
        val btnToday = findViewById<TextView>(R.id.btn_action_today)
        val btnHorai = findViewById<TextView>(R.id.btn_action_horai)
        val btnSettings = findViewById<TextView>(R.id.btn_action_settings)
        val btnNext = findViewById<TextView>(R.id.btn_action_next)

        btnPrev?.setOnClickListener {
            selectedCalendar.add(Calendar.DAY_OF_MONTH, -1)
            displayedMonthCalendar.time = selectedCalendar.time
            loadDayData()
            refreshMonthCalendarGrid()
            updateWidgets()
        }

        btnNext?.setOnClickListener {
            selectedCalendar.add(Calendar.DAY_OF_MONTH, 1)
            displayedMonthCalendar.time = selectedCalendar.time
            loadDayData()
            refreshMonthCalendarGrid()
            updateWidgets()
        }

        btnToday?.setOnClickListener {
            selectedCalendar.time = Calendar.getInstance().time
            displayedMonthCalendar.time = selectedCalendar.time
            loadDayData()
            refreshMonthCalendarGrid()
            updateWidgets()
            viewPager.setCurrentItem(MainPagerAdapter.PAGE_PANCHANGAM, true)
        }

        btnDatePicker?.setOnClickListener {
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
        }

        btnMenu?.setOnClickListener {
            // Index 0: About App / Demo / Help
            viewPager.setCurrentItem(MainPagerAdapter.PAGE_ABOUT, true)
        }

        btnHorai?.setOnClickListener {
            // Index 4: Horai Page
            viewPager.setCurrentItem(MainPagerAdapter.PAGE_HORAI, true)
        }

        btnSettings?.setOnClickListener {
            // Index 5: Settings Page
            viewPager.setCurrentItem(MainPagerAdapter.PAGE_SETTINGS, true)
        }
    }

    private fun loadDayData() {
        pagerAdapter.updateSelectedDate(selectedCalendar)
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

        btnPrevMonth?.setOnClickListener {
            displayedMonthCalendar.add(Calendar.MONTH, -1)
            refreshMonthCalendarGrid()
        }

        btnNextMonth?.setOnClickListener {
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

    fun openMonthlyCalendarSheet() {
        displayedMonthCalendar.time = selectedCalendar.time
        refreshMonthCalendarGrid()
        calendarMonthAdapter.setClassicMode(true)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
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
