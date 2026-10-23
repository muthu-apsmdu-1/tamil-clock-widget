package com.example.tamilclock

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class MainPagerAdapter(
    private val onPrevDayClick: () -> Unit,
    private val onNextDayClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_PANCHANGAM = 0
        private const val TYPE_RASI_PALAN = 1
        private const val TYPE_HORAI = 2
    }

    private val tabTitles = listOf("பஞ்சாங்கம்", "ராசிபலன்", "ஹோரை")
    private var panchangamData: TamilDateInfo? = null
    private var gregorianDateText: String = ""
    private var dayOfWeek: Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

    fun updateData(dateText: String, data: TamilDateInfo?, calendar: Calendar) {
        this.gregorianDateText = dateText
        this.panchangamData = data
        this.dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_PANCHANGAM
            1 -> TYPE_RASI_PALAN
            else -> TYPE_HORAI
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_PANCHANGAM -> {
                val view = inflater.inflate(R.layout.fragment_panchangam, parent, false)
                PanchangamViewHolder(view)
            }
            TYPE_HORAI -> {
                val view = inflater.inflate(R.layout.fragment_horai, parent, false)
                HoraiViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
                PlaceholderViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PanchangamViewHolder -> {
                holder.bind(gregorianDateText, panchangamData, onPrevDayClick, onNextDayClick)
            }
            is HoraiViewHolder -> {
                holder.bind(dayOfWeek)
            }
            is PlaceholderViewHolder -> {
                holder.textView.text = "${tabTitles[position]} பக்கம் விரைவில்..."
                holder.textView.textSize = 18f
                holder.textView.setPadding(32, 64, 32, 32)
                holder.textView.setTextColor(0xFF2C1A14.toInt())
            }
        }
    }

    override fun getItemCount(): Int = tabTitles.size

    class PanchangamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val btnPrev: ImageButton = itemView.findViewById(R.id.btn_prev_day)
        private val btnNext: ImageButton = itemView.findViewById(R.id.btn_next_day)
        private val tvGregorian: TextView = itemView.findViewById(R.id.tv_gregorian_header)
        private val tvWeekday: TextView = itemView.findViewById(R.id.tv_weekday_header)
        private val tvTamilDate: TextView = itemView.findViewById(R.id.tv_tamil_date_full)

        private val tvSunrise: TextView = itemView.findViewById(R.id.tv_sunrise_val)
        private val tvSunset: TextView = itemView.findViewById(R.id.tv_sunset_val)
        private val tvMoonrise: TextView = itemView.findViewById(R.id.tv_moonrise_val)
        private val tvMoonset: TextView = itemView.findViewById(R.id.tv_moonset_val)

        private val tvTithi: TextView = itemView.findViewById(R.id.tv_tithi_val)
        private val tvNakshatra: TextView = itemView.findViewById(R.id.tv_nakshatra_val)
        private val tvPaksham: TextView = itemView.findViewById(R.id.tv_paksham_val)

        private val tvNallaNeram: TextView = itemView.findViewById(R.id.tv_nalla_neram_val)
        private val tvGouri: TextView = itemView.findViewById(R.id.tv_gouri_val)
        private val tvRahu: TextView = itemView.findViewById(R.id.tv_rahu_val)
        private val tvYamagandam: TextView = itemView.findViewById(R.id.tv_yamagandam_val)

        fun bind(gregDate: String, data: TamilDateInfo?, onPrev: () -> Unit, onNext: () -> Unit) {
            btnPrev.setOnClickListener { onPrev() }
            btnNext.setOnClickListener { onNext() }

            tvGregorian.text = gregDate

            if (data != null) {
                tvWeekday.text = data.dayName
                tvTamilDate.text = "${data.tamilYear} வருடம் - ${data.tamilMonth} ${data.tamilDay}"

                tvSunrise.text = data.sunrise.ifEmpty { "06:08 AM" }
                tvSunset.text = data.sunset.ifEmpty { "05:58 PM" }
                tvMoonrise.text = data.moonrise.ifEmpty { "04:20 PM" }
                tvMoonset.text = data.moonset.ifEmpty { "04:55 AM" }

                tvTithi.text = data.tithi
                tvNakshatra.text = data.nakshatra
                tvPaksham.text = data.paksham

                tvNallaNeram.text = "${data.nallaNeramMorning} / ${data.nallaNeramEvening}"
                tvGouri.text = "${data.gouriNallaMorning} / ${data.gouriNallaEvening}"
                tvRahu.text = data.rahuKalam
                tvYamagandam.text = data.yamaGandam
            }
        }
    }

    class HoraiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayContainer: LinearLayout = itemView.findViewById(R.id.layout_day_horai_container)
        private val nightContainer: LinearLayout = itemView.findViewById(R.id.layout_night_horai_container)

        // Chaldean planetary order
        private val planets = listOf("சூரியன்", "சுக்கிரன்", "புதன்", "சந்திரன்", "சனி", "குரு", "செவ்வாய்")

        // First daytime Horai ruler based on day of week (Calendar.SUNDAY = 1)
        private val dayStartPlanetIndex = mapOf(
            Calendar.SUNDAY to 0,    // சூரியன்
            Calendar.MONDAY to 3,    // சந்திரன்
            Calendar.TUESDAY to 6,   // செவ்வாய்
            Calendar.WEDNESDAY to 2, // புதன்
            Calendar.THURSDAY to 5,  // குரு
            Calendar.FRIDAY to 1,    // சுக்கிரன்
            Calendar.SATURDAY to 4   // சனி
        )

        private val dayTimes = listOf(
            "06:00 AM - 07:00 AM", "07:00 AM - 08:00 AM", "08:00 AM - 09:00 AM",
            "09:00 AM - 10:00 AM", "10:00 AM - 11:00 AM", "11:00 AM - 12:00 PM",
            "12:00 PM - 01:00 PM", "01:00 PM - 02:00 PM", "02:00 PM - 03:00 PM",
            "03:00 PM - 04:00 PM", "04:00 PM - 05:00 PM", "05:00 PM - 06:00 PM"
        )

        private val nightTimes = listOf(
            "06:00 PM - 07:00 PM", "07:00 PM - 08:00 PM", "08:00 PM - 09:00 PM",
            "09:00 PM - 10:00 PM", "10:00 PM - 11:00 PM", "11:00 PM - 12:00 AM",
            "12:00 AM - 01:00 AM", "01:00 AM - 02:00 AM", "02:00 AM - 03:00 AM",
            "03:00 AM - 04:00 AM", "04:00 AM - 05:00 AM", "05:00 AM - 06:00 AM"
        )

        fun bind(dayOfWeek: Int) {
            dayContainer.removeAllViews()
            nightContainer.removeAllViews()

            val startIndex = dayStartPlanetIndex[dayOfWeek] ?: 0
            val inflater = LayoutInflater.from(itemView.context)

            // Daytime 12 Horais
            for (i in 0 until 12) {
                val planet = planets[(startIndex + i) % 7]
                addHoraiRow(inflater, dayContainer, dayTimes[i], planet)
            }

            // Nighttime 12 Horais continues consecutively from hour 12
            for (i in 0 until 12) {
                val planet = planets[(startIndex + 12 + i) % 7]
                addHoraiRow(inflater, nightContainer, nightTimes[i], planet)
            }
        }

        private fun addHoraiRow(inflater: LayoutInflater, container: LinearLayout, time: String, planet: String) {
            val row = inflater.inflate(R.layout.item_horai_row, container, false)
            val tvTime = row.findViewById<TextView>(R.id.tv_horai_time)
            val tvPlanet = row.findViewById<TextView>(R.id.tv_horai_planet)
            val tvNature = row.findViewById<TextView>(R.id.tv_horai_nature)

            tvTime.text = time
            tvPlanet.text = planet

            when (planet) {
                "குரு", "சுக்கிரன்", "புதன்", "சந்திரன்" -> {
                    tvNature.text = "சுபம்"
                    tvNature.setTextColor(Color.parseColor("#1B5E20"))
                    tvNature.setBackgroundColor(Color.parseColor("#E2F3E7"))
                }
                "சூரியன்" -> {
                    tvNature.text = "மத்திமம்"
                    tvNature.setTextColor(Color.parseColor("#F57F17"))
                    tvNature.setBackgroundColor(Color.parseColor("#FFF8E1"))
                }
                else -> {
                    tvNature.text = "தவிர்க்க"
                    tvNature.setTextColor(Color.parseColor("#B71C1C"))
                    tvNature.setBackgroundColor(Color.parseColor("#FFEBEE"))
                }
            }
            container.addView(row)
        }
    }

    class PlaceholderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(android.R.id.text1)
    }
}
