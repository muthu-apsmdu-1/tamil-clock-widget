package com.example.tamilclock

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.Calendar

class MainPagerAdapter(
    activity: FragmentActivity
) : FragmentStateAdapter(activity) {

    companion object {
        const val PAGE_ABOUT = 0
        const val PAGE_MONTHLY_CALENDAR = 1
        const val PAGE_PANCHANGAM = 2
        const val PAGE_GOWRI = 3
        const val PAGE_HORAI = 4
        const val PAGE_SETTINGS = 5

        const val TOTAL_PAGES = 6
    }

    private var currentCalendar: Calendar = Calendar.getInstance()

    // Store references to notify active fragments on date change
    private val fragmentMap = mutableMapOf<Int, Fragment>()

    override fun getItemCount(): Int = TOTAL_PAGES

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            PAGE_ABOUT -> FragmentAboutApp()
            PAGE_MONTHLY_CALENDAR -> FragmentMonthlyCalendar()
            PAGE_PANCHANGAM -> FragmentPanchangam()
            PAGE_GOWRI -> FragmentGowriHorai.newInstance(initialTabIsHorai = false)
            PAGE_HORAI -> FragmentGowriHorai.newInstance(initialTabIsHorai = true)
            PAGE_SETTINGS -> FragmentSettings()
            else -> FragmentPanchangam()
        }
        fragmentMap[position] = fragment
        return fragment
    }

    fun updateSelectedDate(cal: Calendar) {
        currentCalendar = cal.clone() as Calendar

        // Update active fragments if instantiated
        (fragmentMap[PAGE_PANCHANGAM] as? FragmentPanchangam)?.updateDate(currentCalendar)
        (fragmentMap[PAGE_GOWRI] as? FragmentGowriHorai)?.updateDate(currentCalendar)
        (fragmentMap[PAGE_HORAI] as? FragmentGowriHorai)?.updateDate(currentCalendar)
        (fragmentMap[PAGE_MONTHLY_CALENDAR] as? FragmentMonthlyCalendar)?.updateDate(currentCalendar)
    }

    fun getSelectedDate(): Calendar = currentCalendar.clone() as Calendar
}
