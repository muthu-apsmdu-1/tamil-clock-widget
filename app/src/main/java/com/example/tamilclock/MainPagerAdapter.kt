package com.example.tamilclock

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.Calendar

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    companion object {
        // Requested 5-Page Order:
        // 0: About App
        // 1: Monthly Calendar (with date picker)
        // 2: Home Page (Panchangam Simple & Expanded)
        // 3: Gowri & Horai Integrated Page
        // 4: Settings Page
        const val PAGE_ABOUT = 0
        const val PAGE_MONTHLY_CALENDAR = 1
        const val PAGE_PANCHANGAM = 2
        const val PAGE_GOWRI_HORAI = 3
        const val PAGE_SETTINGS = 4

        // Backward-compatibility aliases for MainActivity
        const val PAGE_GOWRI = PAGE_GOWRI_HORAI
        const val PAGE_HORAI = PAGE_GOWRI_HORAI

        const val TOTAL_PAGES = 5
    }

    private val fragmentReferences = mutableMapOf<Int, Fragment>()
    private var currentCalendar: Calendar = Calendar.getInstance()

    override fun getItemCount(): Int = TOTAL_PAGES

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            PAGE_ABOUT -> FragmentAboutApp()
            PAGE_MONTHLY_CALENDAR -> FragmentMonthlyCalendar()
            PAGE_PANCHANGAM -> FragmentPanchangam()
            PAGE_GOWRI_HORAI -> FragmentGowriHorai()
            PAGE_SETTINGS -> FragmentSettings()
            else -> FragmentPanchangam()
        }
        fragmentReferences[position] = fragment
        syncDateToFragment(fragment, currentCalendar)
        return fragment
    }

    fun updateSelectedDate(calendar: Calendar) {
        currentCalendar = calendar.clone() as Calendar
        fragmentReferences.values.forEach { fragment ->
            syncDateToFragment(fragment, currentCalendar)
        }
    }

    private fun syncDateToFragment(fragment: Fragment, calendar: Calendar) {
        when (fragment) {
            is FragmentMonthlyCalendar -> fragment.updateDate(calendar)
            is FragmentPanchangam -> fragment.updateDate(calendar)
            is FragmentGowriHorai -> fragment.updateDate(calendar)
        }
    }
}
