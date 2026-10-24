package com.example.tamilclock

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var pagerAdapter: MainPagerAdapter
    private lateinit var calendarMonthAdapter: CalendarMonthAdapter
    private lateinit var dbHelper: TamilDatabaseHelper

    private val selectedCalendar: Calendar = Calendar.getInstance()
    private val displayedMonthCalendar: Calendar = Calendar.getInstance()

    private lateinit var tvSheetMonthYear: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enforce the saved theme before inflating views
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = TamilDatabaseHelper(this)

        setupTabsAndViewPager()
        setupHybridBottomSheet()
        setupHeaderButtons()
        loadDayData()
        updateWidgets()
    }

    private fun setupTabsAndViewPager() {
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout_main)
        val viewPager = findViewById<ViewPager2>(R.id.view_pager_main)

        pagerAdapter = MainPagerAdapter(
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
            }
        )
        viewPager.adapter = pagerAdapter

        val tabTitles = listOf("பஞ்சாங்கம்", "ராசிபலன்", "ஹோரை")
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
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
        val btnPrevMonth = findViewById<ImageButton>(R.id.btn_prev_month)
        val btnNextMonth = findViewById<ImageButton>(R.id.btn_next_month)
        val container = findViewById<FrameLayout>(R.id.container_calendar_content)

        // Inflate view_calendar_sheet_content into bottom sheet container
        val contentView = LayoutInflater.from(this).inflate(
            R.layout.view_calendar_sheet_content,
            container,
            true
        )

        val rvGrid = contentView.findViewById<RecyclerView>(R.id.rv_calendar_month_grid)
        rvGrid.layoutManager = GridLayoutManager(this, 7)

        // Selective date jump works across both simple and classic view modes
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

        // Handles the pull-up full-screen transformation into Classic Wall Calendar
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        calendarMonthAdapter.setClassicMode(true)
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        calendarMonthAdapter.setClassicMode(false)
                    }
                    else -> Unit
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset > 0.5f) {
                    calendarMonthAdapter.setClassicMode(true)
                } else if (slideOffset < 0.3f) {
                    calendarMonthAdapter.setClassicMode(false)
                }
            }
        })

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

        calendarMonthAdapter.setMonthData(
            displayedMonthCalendar,
            selectedCalendar,
            dbHelper
        )
    }

    private fun setupHeaderButtons() {
        val btnCalendar = findViewById<ImageButton>(R.id.btn_monthly_calendar)
        val btnSettings = findViewById<ImageButton>(R.id.btn_settings_customization)

        btnCalendar?.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                displayedMonthCalendar.time = selectedCalendar.time
                refreshMonthCalendarGrid()
                calendarMonthAdapter.setClassicMode(false)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

        btnSettings?.setOnClickListener {
            showThemeCustomizationSheet()
        }
    }

    private fun showThemeCustomizationSheet() {
        val dialog = BottomSheetDialog(this)
        val sheetView = layoutInflater.inflate(R.layout.dialog_theme_customization, null)
        dialog.setContentView(sheetView)

        sheetView.findViewById<View>(R.id.option_theme_white)?.setOnClickListener {
            changeTheme(ThemeManager.THEME_WHITE, dialog)
        }
        sheetView.findViewById<View>(R.id.option_theme_yellow)?.setOnClickListener {
            changeTheme(ThemeManager.THEME_YELLOW, dialog)
        }
        sheetView.findViewById<View>(R.id.option_theme_green)?.setOnClickListener {
            changeTheme(ThemeManager.THEME_GREEN, dialog)
        }
        sheetView.findViewById<View>(R.id.option_theme_charcoal)?.setOnClickListener {
            changeTheme(ThemeManager.THEME_CHARCOAL, dialog)
        }

        dialog.show()
    }

    private fun changeTheme(themeId: Int, dialog: BottomSheetDialog) {
        ThemeManager.saveTheme(this, themeId)
        dialog.dismiss()
        recreate()
    }

    private fun updateWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(this)

        // 1. 2x1 Capsule Widget
        val c2x1 = ComponentName(this, WidgetProvider2x1::class.java)
        val ids2x1 = appWidgetManager.getAppWidgetIds(c2x1)
        if (ids2x1.isNotEmpty()) {
            WidgetUpdateHelper.update2x1(this, appWidgetManager, ids2x1)
            sendWidgetBroadcast(WidgetProvider2x1::class.java, ids2x1)
        }

        // 2. 4x1 Ribbon Widget
        val c4x1 = ComponentName(this, WidgetProvider4x1::class.java)
        val ids4x1 = appWidgetManager.getAppWidgetIds(c4x1)
        if (ids4x1.isNotEmpty()) {
            WidgetUpdateHelper.update4x1(this, appWidgetManager, ids4x1)
            sendWidgetBroadcast(WidgetProvider4x1::class.java, ids4x1)
        }

        // 3. 4x2 Panoramic Widget
        val c4x2 = ComponentName(this, WidgetProvider4x2::class.java)
        val ids4x2 = appWidgetManager.getAppWidgetIds(c4x2)
        if (ids4x2.isNotEmpty()) {
            WidgetUpdateHelper.update4x2(this, appWidgetManager, ids4x2)
            sendWidgetBroadcast(WidgetProvider4x2::class.java, ids4x2)
        }

        // 4. 4x4 Heritage Wall Widget
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
