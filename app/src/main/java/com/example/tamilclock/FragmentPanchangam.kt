package com.example.tamilclock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import java.util.Calendar

class FragmentPanchangam : Fragment() {

    private var currentCalendar: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_panchangam, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        renderPanchangam(currentCalendar)
    }

    fun updateDate(cal: Calendar) {
        currentCalendar = cal.clone() as Calendar
        if (isAdded && view != null) {
            renderPanchangam(currentCalendar)
        }
    }

    private fun renderPanchangam(cal: Calendar) {
        // View binding and population logic can be handled here or through your existing helper
    }
}
