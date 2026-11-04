package com.example.tamilclock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import java.util.Calendar

class FragmentMonthlyCalendar : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.view_calendar_sheet_content, container, false)
    }

    fun updateDate(cal: Calendar) {
        // Handled by CalendarMonthAdapter when wired
    }
}
