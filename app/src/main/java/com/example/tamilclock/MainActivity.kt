package com.example.tamilclock

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var pagerAdapter: MainPagerAdapter
    private lateinit var dbHelper: TamilDatabaseHelper
    private val selectedCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
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
                loadDayData()
            },
            onNextDayClick = {
                selectedCalendar.add(Calendar.DAY_OF_MONTH, 1)
                loadDayData()
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

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) = Unit
            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
        })
    }

    private fun setupHeaderButtons() {
        val btnCalendar = findViewById<ImageButton>(R.id.btn_monthly_calendar)
        val btnSettings = findViewById<ImageButton>(R.id.btn_settings_customization)

        btnCalendar?.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

        btnSettings?.setOnClickListener {
            // Future settings dialog
        }
    }

    private fun updateWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(this)

        val digitalComponent = ComponentName(this, DigitalClockWidgetProvider::class.java)
        val digitalIds = appWidgetManager.getAppWidgetIds(digitalComponent)
        if (digitalIds.isNotEmpty()) {
            val intent = Intent(this, DigitalClockWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, digitalIds)
            }
            sendBroadcast(intent)
        }

        val classicComponent = ComponentName(this, ClassicClockWidgetProvider::class.java)
        val classicIds = appWidgetManager.getAppWidgetIds(classicComponent)
        if (classicIds.isNotEmpty()) {
            val intent = Intent(this, ClassicClockWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, classicIds)
            }
            sendBroadcast(intent)
        }
    }
}
