package com.example.tamilclock

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

data class RasiItem(
    val name: String,
    val lord: String,
    val prediction: String,
    val luckyNumber: String,
    val luckyColor: String
)

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
            TYPE_RASI_PALAN -> {
                val view = inflater.inflate(R.layout.fragment_rasi_palan, parent, false)
                RasiPalanViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.fragment_horai, parent, false)
                HoraiViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PanchangamViewHolder -> {
                holder.bind(gregorianDateText, panchangamData, onPrevDayClick, onNextDayClick)
            }
            is RasiPalanViewHolder -> {
                holder.bind()
            }
            is HoraiViewHolder -> {
                holder.bind(dayOfWeek)
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

    class RasiPalanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rvGrid: RecyclerView = itemView.findViewById(R.id.rv_rasi_grid)

        private val rasiList = listOf(
            RasiItem("மேஷம்", "செவ்வாய்", "தொழிலில் மேன்மை உண்டாகும். சுப முயற்சிகள் கைகூடும்.", "9", "சிவப்பு"),
            RasiItem("ரிஷபம்", "சுக்கிரன்", "பணவரவு திருப்திகரமாக இருக்கும். குடும்பத்தில் மகிழ்ச்சி கூடும்.", "6", "வெள்ளை"),
            RasiItem("மிதுனம்", "புதன்", "புதிய வாய்ப்புகள் தேடி வரும். நண்பர்களின் ஆதரவு கிடைக்கும்.", "5", "பச்சை"),
            RasiItem("கடகம்", "சந்திரன்", "மன அமைதியும் ஆரோக்கியமும் கூடும். பயணங்களால் அனுகூலம்.", "2", "வெள்ளை"),
            RasiItem("சிம்மம்", "சூரியன்", "எடுத்த காரியங்களில் வெற்றி கிட்டும். தலைமைப் பண்பு பளிச்சிடும்.", "1", "மஞ்சள்"),
            RasiItem("கன்னி", "புதன்", "திட்டமிட்ட காரியங்கள் சுமூகமாக முடியும். வரவு செலவில் கவனம் தேவை.", "5", "சாம்பல்"),
            RasiItem("துலாம்", "சுக்கிரன்", "தொழில் கூட்டாளிகளிடம் இணக்கம் கூடும். எதிர்பாராத தனலாபம்.", "6", "நீலம்"),
            RasiItem("விருச்சிகம்", "செவ்வாய்", "தைரியத்துடன் செயல்பட்டு தடைகளைத் தாண்டுவீர்கள். உடல்நலம் சீராகும்.", "9", "சிவப்பு"),
            RasiItem("தனுசு", "குரு", "பக்தி மார்க்கத்தில் நாட்டம் செல்லும். பெரியோரின் ஆசி கிட்டும்.", "3", "மஞ்சள்"),
            RasiItem("மகரம்", "சனி", "கடின உழைப்பிற்கு ஏற்ற பலன் கிடைக்கும். நிதானத்துடன் செயல்படவும்.", "8", "கருநீலம்"),
            RasiItem("கும்பம்", "சனி", "புதிய சிந்தனைகள் மேலோங்கும். சமூகத்தில் நற்பெயர் கூடும்.", "8", "நீலம்"),
            RasiItem("மீனம்", "குரு", "சுப காரிய பேச்சுவார்த்தைகள் முடிவுக்கு வரும். அமைதியான நாள்.", "3", "பொன்நிறம்")
        )

        fun bind() {
            rvGrid.layoutManager = GridLayoutManager(itemView.context, 2)
            rvGrid.adapter = RasiCardAdapter(rasiList)
        }
    }

    class RasiCardAdapter(private val list: List<RasiItem>) :
        RecyclerView.Adapter<RasiCardAdapter.CardVH>() {

        class CardVH(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tv_rasi_name)
            val tvLord: TextView = view.findViewById(R.id.tv_rasi_lord)
            val tvPrediction: TextView = view.findViewById(R.id.tv_rasi_prediction)
            val tvLuckyNum: TextView = view.findViewById(R.id.tv_rasi_lucky_num)
            val tvLuckyColor: TextView = view.findViewById(R.id.tv_rasi_lucky_color)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardVH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_rasi_card, parent, false)
            return CardVH(view)
        }

        override fun onBindViewHolder(holder: CardVH, position: Int) {
            val item = list[position]
            holder.tvName.text = item.name
            holder.tvLord.text = item.lord
            holder.tvPrediction.text = item.prediction
            holder.tvLuckyNum.text = "எண்: ${item.luckyNumber}"
            holder.tvLuckyColor.text = "நிறம்: ${item.luckyColor}"
        }

        override fun getItemCount(): Int = list.size
    }

    class HoraiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayContainer: LinearLayout = itemView.findViewById(R.id.layout_day_horai_container)
        private val nightContainer: LinearLayout = itemView.findViewById(R.id.layout_night_horai_container)

        private val planets = listOf("சூரியன்", "சுக்கிரன்", "புதன்", "சந்திரன்", "சனி", "குரு", "செவ்வாய்")

        private val dayStartPlanetIndex = mapOf(
            Calendar.SUNDAY to 0,
            Calendar.MONDAY to 3,
            Calendar.TUESDAY to 6,
            Calendar.WEDNESDAY to 2,
            Calendar.THURSDAY to 5,
            Calendar.FRIDAY to 1,
            Calendar.SATURDAY to 4
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

            for (i in 0 until 12) {
                val planet = planets[(startIndex + i) % 7]
                addHoraiRow(inflater, dayContainer, dayTimes[i], planet)
            }

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
}
