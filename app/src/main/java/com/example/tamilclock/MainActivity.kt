package com.example.tamilclock

import android.app.DatePickerDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
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
        loadDayData()
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
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

        // Launch directly on Home Screen: Day Panchang (Index 2)
        viewPager.setCurrentItem(MainPagerAdapter.PAGE_PANCHANGAM, false)
    }

    private fun setupTopNavigationToolbar() {
        // Top Toolbar Buttons: <<, 🗓️, ☰, 🏠, ☸, ⚙, >>
        val btnPrev = findViewById<TextView?>(R.id.btn_action_prev)
        val btnCalendar = findViewById<TextView?>(R.id.btn_action_datepicker) // 🗓️ Icon
        val btnMenu = findViewById<TextView?>(R.id.btn_action_menu)             // ☰ Icon
        val btnToday = findViewById<TextView?>(R.id.btn_action_today)           // 🏠 Icon
        val btnHorai = findViewById<TextView?>(R.id.btn_action_horai)           // ☸ Icon
        val btnSettings = findViewById<TextView?>(R.id.btn_action_settings)     // ⚙ Icon
        val btnNext = findViewById<TextView?>(R.id.btn_action_next)             // >> Icon

        // Previous Day
        btnPrev?.setOnClickListener {
            selectedCalendar.add(Calendar.DAY_OF_MONTH, -1)
            loadDayData()
            updateWidgets()
        }

        // Next Day
        btnNext?.setOnClickListener {
            selectedCalendar.add(Calendar.DAY_OF_MONTH, 1)
            loadDayData()
            updateWidgets()
        }

        // Home / Today Button (Index 2)
        btnToday?.setOnClickListener {
            selectedCalendar.time = Calendar.getInstance().time
            loadDayData()
            updateWidgets()
            viewPager.setCurrentItem(MainPagerAdapter.PAGE_PANCHANGAM, true)
        }

        // Monthly Calendar Button (Index 1)
        btnCalendar?.setOnClickListener {
            viewPager.setCurrentItem(MainPagerAdapter.PAGE_MONTHLY_CALENDAR, true)
        }

        // Long press on Calendar icon opens DatePickerDialog directly
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

        // Gowri Menu (Index 3)
        btnMenu?.setOnClickListener {
            viewPager.setCurrentItem(MainPagerAdapter.PAGE_GOWRI, true)
        }

        // Horai / Chakra Page (Index 4)
        btnHorai?.setOnClickListener {
            viewPager.setCurrentItem(MainPagerAdapter.PAGE_HORAI, true)
        }

        // Settings Page (Index 5)
        btnSettings?.setOnClickListener {
            viewPager.setCurrentItem(MainPagerAdapter.PAGE_SETTINGS, true)
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
