package com.example.tamilclock

import android.app.DatePickerDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.viewpager2.widget.ViewPager2
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var pagerAdapter: MainPagerAdapter
    private lateinit var dbHelper: TamilDatabaseHelper
    private lateinit var viewPager: ViewPager2

    val selectedCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = TamilDatabaseHelper(this)

        setupTabsAndViewPager()
        setupTopNavigationToolbar()
        setupBackNavigation()
        loadDayData()
        updateWidgets()
    }

    override fun onResume() {
        super.onResume()
        applyToolbarTheme()
        loadDayData()
    }

    private fun applyToolbarTheme() {
        val palette = ThemeManager.getThemePalette(this)

        // 1. Root Activity Background
        findViewById<View?>(R.id.root_main_layout)?.setBackgroundColor(palette.pageBackground)

        // 2. Pinned Top Banner (Logo + App Title)
        findViewById<CardView?>(R.id.card_top_banner)?.setCardBackgroundColor(palette.primaryHeader)
        findViewById<TextView?>(R.id.tv_app_title_banner)?.setTextColor(palette.accentGold)

        // 3. 7-Tool Navigation Toolbar
        findViewById<LinearLayout?>(R.id.layout_nav_toolbar)?.setBackgroundColor(palette.secondaryHeader)

        val navButtons = listOf<TextView?>(
            findViewById(R.id.btn_action_prev),
            findViewById(R.id.btn_action_menu),
            findViewById(R.id.btn_action_datepicker),
            findViewById(R.id.btn_action_today),
            findViewById(R.id.btn_action_horai),
            findViewById(R.id.btn_action_settings),
            findViewById(R.id.btn_action_next)
        )
        for (btn in navButtons) {
            btn?.setTextColor(palette.accentGold)
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // If not on Home (Index 2), return to Home first; else exit
                if (viewPager.currentItem != MainPagerAdapter.PAGE_PANCHANGAM) {
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

        // Default screen on launch: Home Page (Index 2)
        viewPager.setCurrentItem(MainPagerAdapter.PAGE_PANCHANGAM, false)
    }

    private fun setupTopNavigationToolbar() {
        // Buttons in exact sequence:
        // 1. Prev (<<)
        // 2. About App (☰) -> Index 0
        // 3. Monthly Calendar (🗓️) -> Index 1
        // 4. Home / Day Panchang (🏠) -> Index 2
        // 5. Gowri & Horai (☸) -> Index 3
        // 6. Settings (⚙) -> Index 4
        // 7. Next (>>)
        val btnPrev = findViewById<TextView?>(R.id.btn_action_prev)
        val btnMenu = findViewById<TextView?>(R.id.btn_action_menu)
        val btnCalendar = findViewById<TextView?>(R.id.btn_action_datepicker)
        val btnHome = findViewById<TextView?>(R.id.btn_action_today)
        val btnGowriHorai = findViewById<TextView?>(R.id.btn_action_horai)
        val btnSettings = findViewById<TextView?>(R.id.btn_action_settings)
        val btnNext = findViewById<TextView?>(R.id.btn_action_next)

        // 1. Previous Day
        btnPrev?.setOnClickListener {
            selectedCalendar.add(Calendar.DAY_OF_MONTH, -1)
            loadDayData()
            updateWidgets()
        }

        // 2. About App Page (☰) -> Index 0
        btnMenu?.setOnClickListener {
            viewPager.setCurrentItem(MainPagerAdapter.PAGE_ABOUT, true)
        }

        // 3. Monthly Calendar (🗓️) -> Index 1
        btnCalendar?.setOnClickListener {
            viewPager.setCurrentItem(MainPagerAdapter.PAGE_MONTHLY_CALENDAR, true)
        }

        // Long press on 🗓️ allows direct native date picking
        btnCalendar?.setOnLongClickListener {
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedCalendar.set(year, month, dayOfMonth)
                    loadDayData()
                    updateWidgets()
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
            true
        }

        // 4. Home Page (🏠) -> Index 2 (Day Panchang)
        btnHome?.setOnClickListener {
            selectedCalendar.time = Calendar.getInstance().time
            loadDayData()
            updateWidgets()
            viewPager.setCurrentItem(MainPagerAdapter.PAGE_PANCHANGAM, true)
        }

        // 5. Gowri & Horai Integrated Page (☸) -> Index 3
        btnGowriHorai?.setOnClickListener {
            viewPager.setCurrentItem(MainPagerAdapter.PAGE_GOWRI_HORAI, true)
        }

        // 6. Settings Page (⚙) -> Index 4
        btnSettings?.setOnClickListener {
            viewPager.setCurrentItem(MainPagerAdapter.PAGE_SETTINGS, true)
        }

        // 7. Next Day
        btnNext?.setOnClickListener {
            selectedCalendar.add(Calendar.DAY_OF_MONTH, 1)
            loadDayData()
            updateWidgets()
        }
    }

    fun loadDayData() {
        pagerAdapter.updateSelectedDate(selectedCalendar)
    }

    fun updateSelectedDate(cal: Calendar) {
        selectedCalendar.time = cal.time
        loadDayData()
        updateWidgets()
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

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}
