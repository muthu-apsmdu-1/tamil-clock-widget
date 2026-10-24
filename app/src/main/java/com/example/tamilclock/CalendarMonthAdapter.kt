package com.example.tamilclock

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class MonthDayModel(
    val dateKey: String,          // yyyy-MM-dd
    val dayNumber: Int,           // 1..31
    val isCurrentMonth: Boolean,
    val isSelected: Boolean,
    val isToday: Boolean,
    val tamilDate: String,        // e.g. "ஐப்பசி 7"
    val tithiBadge: String,       // e.g. "அமா", "பௌ"
    val extraNote: String         // e.g. "சுப நாள்", "கிருத்திகை"
)

class CalendarMonthAdapter(
    private val onDateSelected: (String) -> Unit
) : RecyclerView.Adapter<CalendarMonthAdapter.DayViewHolder>() {

    private val dayList = mutableListOf<MonthDayModel?>()
    private var isClassicMode: Boolean = false

    fun setClassicMode(enabled: Boolean) {
        if (this.isClassicMode != enabled) {
            this.isClassicMode = enabled
            notifyDataSetChanged()
        }
    }

    fun setMonthData(
        displayedCal: Calendar,
        selectedCal: Calendar,
        dbHelper: TamilDatabaseHelper
    ) {
        dayList.clear()
        val workCal = displayedCal.clone() as Calendar
        workCal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = workCal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday
        val daysInMonth = workCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Empty padding cells for days before the 1st
        val leadingBlanks = firstDayOfWeek - Calendar.SUNDAY
        for (i in 0 until leadingBlanks) {
            dayList.add(null)
        }

        val ymdFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayKey = ymdFormat.format(Calendar.getInstance().time)
        val selectedKey = ymdFormat.format(selectedCal.time)

        for (day in 1..daysInMonth) {
            workCal.set(Calendar.DAY_OF_MONTH, day)
            val dateKey = ymdFormat.format(workCal.time)
            val info = dbHelper.getDayDetails(dateKey)

            val tamilText = if (info != null) "${info.tamilMonth} ${info.tamilDay}" else ""
            val badge = when {
                info?.tithi?.contains("அமாவாசை") == true -> "அமா"
                info?.tithi?.contains("பௌர்ணமி") == true -> "பௌ"
                info?.nakshatra?.contains("கிருத்திகை") == true -> "கிரு"
                else -> ""
            }

            dayList.add(
                MonthDayModel(
                    dateKey = dateKey,
                    dayNumber = day,
                    isCurrentMonth = true,
                    isSelected = (dateKey == selectedKey),
                    isToday = (dateKey == todayKey),
                    tamilDate = tamilText,
                    tithiBadge = badge,
                    extraNote = if (badge.isNotEmpty()) badge else (info?.tithi ?: "")
                )
            )
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_month_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val model = dayList[position]
        holder.bind(model, isClassicMode, onDateSelected)
    }

    override fun getItemCount(): Int = dayList.size

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.card_calendar_day)
        
        // Simple View components
        private val layoutSimple: LinearLayout = itemView.findViewById(R.id.layout_mode_simple)
        private val tvSimpleDay: TextView = itemView.findViewById(R.id.tv_simple_day_num)
        private val dotIndicator: View = itemView.findViewById(R.id.dot_simple_indicator)

        // Classic View components
        private val layoutClassic: LinearLayout = itemView.findViewById(R.id.layout_mode_classic)
        private val tvClassicGregDay: TextView = itemView.findViewById(R.id.tv_classic_greg_day)
        private val tvClassicTamilDate: TextView = itemView.findViewById(R.id.tv_classic_tamil_date)
        private val tvClassicBadge: TextView = itemView.findViewById(R.id.tv_classic_festival_badge)
        private val tvClassicExtra: TextView = itemView.findViewById(R.id.tv_classic_extra_info)

        fun bind(
            model: MonthDayModel?,
            isClassic: Boolean,
            onSelect: (String) -> Unit
        ) {
            if (model == null) {
                itemView.visibility = View.INVISIBLE
                itemView.setOnClickListener(null)
                return
            }

            itemView.visibility = View.VISIBLE

            // Click listener works in BOTH simple and classic modes
            itemView.setOnClickListener {
                onSelect(model.dateKey)
            }

            if (isClassic) {
                layoutSimple.visibility = View.GONE
                layoutClassic.visibility = View.VISIBLE

                tvClassicGregDay.text = model.dayNumber.toString()
                tvClassicTamilDate.text = model.tamilDate

                if (model.tithiBadge.isNotEmpty()) {
                    tvClassicBadge.visibility = View.VISIBLE
                    tvClassicBadge.text = model.tithiBadge
                } else {
                    tvClassicBadge.visibility = View.GONE
                }

                tvClassicExtra.text = model.extraNote

                // Styling for selected and today's date
                when {
                    model.isSelected -> {
                        card.strokeColor = Color.parseColor("#5D0A14")
                        card.strokeWidth = 3
                        card.setCardBackgroundColor(Color.parseColor("#FFF3E0"))
                    }
                    model.isToday -> {
                        card.strokeColor = Color.parseColor("#C68B04")
                        card.strokeWidth = 2
                        card.setCardBackgroundColor(Color.parseColor("#FFFDF7"))
                    }
                    else -> {
                        card.strokeColor = Color.parseColor("#E5E5EA")
                        card.strokeWidth = 1
                        card.setCardBackgroundColor(Color.WHITE)
                    }
                }
            } else {
                layoutSimple.visibility = View.VISIBLE
                layoutClassic.visibility = View.GONE

                tvSimpleDay.text = model.dayNumber.toString()

                when {
                    model.isSelected -> {
                        tvSimpleDay.setBackgroundColor(Color.parseColor("#5D0A14"))
                        tvSimpleDay.setTextColor(Color.WHITE)
                        dotIndicator.visibility = View.INVISIBLE
                    }
                    model.isToday -> {
                        tvSimpleDay.background = null
                        tvSimpleDay.setTextColor(Color.parseColor("#C68B04"))
                        dotIndicator.visibility = View.VISIBLE
                    }
                    else -> {
                        tvSimpleDay.background = null
                        tvSimpleDay.setTextColor(Color.parseColor("#1C1C1E"))
                        dotIndicator.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }
}
