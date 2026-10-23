package com.example.tamilclock

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class MonthDayItem(
    val dayNumber: Int,
    val dateKey: String,
    val tamilDayText: String,
    val isCurrentMonth: Boolean,
    val isSelected: Boolean
)

class CalendarMonthAdapter(
    private val onDaySelected: (String) -> Unit
) : RecyclerView.Adapter<CalendarMonthAdapter.DayViewHolder>() {

    private val dayList = mutableListOf<MonthDayItem>()

    fun setMonthData(
        monthCalendar: Calendar,
        selectedCalendar: Calendar,
        dbHelper: TamilDatabaseHelper
    ) {
        dayList.clear()

        val cal = monthCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // Sunday = 1, Saturday = 7
        val emptyLeadingSlots = firstDayOfWeek - 1
        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedKey = keyFormat.format(selectedCalendar.time)

        // Empty padding cells before 1st of month
        for (i in 0 until emptyLeadingSlots) {
            dayList.add(MonthDayItem(0, "", "", isCurrentMonth = false, isSelected = false))
        }

        // Active days of the month
        for (day in 1..maxDaysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val dateKey = keyFormat.format(cal.time)
            val dayInfo = dbHelper.getDayDetails(dateKey)
            val tamilDay = if (dayInfo != null && dayInfo.tamilDay > 0) "${dayInfo.tamilDay}" else ""

            dayList.add(
                MonthDayItem(
                    dayNumber = day,
                    dateKey = dateKey,
                    tamilDayText = tamilDay,
                    isCurrentMonth = true,
                    isSelected = (dateKey == selectedKey)
                )
            )
        }

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day_cell, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val item = dayList[position]
        holder.bind(item, onDaySelected)
    }

    override fun getItemCount(): Int = dayList.size

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rootLayout: View = itemView.findViewById(R.id.cell_root)
        private val tvGregorian: TextView = itemView.findViewById(R.id.tv_cell_gregorian)
        private val tvTamil: TextView = itemView.findViewById(R.id.tv_cell_tamil)

        fun bind(item: MonthDayItem, onSelected: (String) -> Unit) {
            if (!item.isCurrentMonth || item.dayNumber == 0) {
                rootLayout.visibility = View.INVISIBLE
                rootLayout.setOnClickListener(null)
                return
            }

            rootLayout.visibility = View.VISIBLE
            tvGregorian.text = item.dayNumber.toString()
            tvTamil.text = item.tamilDayText

            if (item.isSelected) {
                rootLayout.setBackgroundColor(Color.parseColor("#5D0A14"))
                tvGregorian.setTextColor(Color.WHITE)
                tvTamil.setTextColor(Color.parseColor("#FFD54F"))
            } else {
                rootLayout.setBackgroundResource(R.drawable.bg_row4_chip)
                tvGregorian.setTextColor(Color.WHITE)
                tvTamil.setTextColor(Color.parseColor("#D4AF37"))
            }

            rootLayout.setOnClickListener {
                if (item.dateKey.isNotEmpty()) {
                    onSelected(item.dateKey)
                }
            }
        }
    }
}
