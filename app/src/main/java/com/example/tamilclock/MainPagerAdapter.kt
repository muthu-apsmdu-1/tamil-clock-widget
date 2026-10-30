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
import com.google.android.material.card.MaterialCardView
import java.util.Calendar

data class RasiItem(
    val name: String,
    val keyword: String
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

    // ==================== 1. PANCHANGAM VIEW HOLDER ====================
    class PanchangamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Navigation Buttons
        private val btnPrev: ImageButton = itemView.findViewById(R.id.btn_prev_day)
        private val btnNext: ImageButton = itemView.findViewById(R.id.btn_next_day)

        // 1. Top Date Header Card
        private val tvHeaderLine1: TextView = itemView.findViewById(R.id.tv_gregorian_header)
        private val tvHeaderLine2: TextView = itemView.findViewById(R.id.tv_tamil_date_full)

        // 2. Festival / Event Badge Card
        private val cardFestivalBadge: View? = itemView.findViewById(R.id.card_festival_badge)
        private val tvFestivalText: TextView? = itemView.findViewById(R.id.tv_festival_badge_text)

        // 3. Sun & Moon Timings Strip
        private val tvSunrise: TextView = itemView.findViewById(R.id.tv_sunrise_val)
        private val tvSunset: TextView = itemView.findViewById(R.id.tv_sunset_val)
        private val tvMoonrise: TextView = itemView.findViewById(R.id.tv_moonrise_val)
        private val tvMoonset: TextView = itemView.findViewById(R.id.tv_moonset_val)

        // 4. Panchangam Core 5 Elements
        private val tvPaksham: TextView? = itemView.findViewById(R.id.tv_paksham_val)
        private val tvTithi: TextView? = itemView.findViewById(R.id.tv_tithi_val)
        private val tvNakshatra: TextView = itemView.findViewById(R.id.tv_nakshatra_val)
        private val tvYoga: TextView? = itemView.findViewById(R.id.tv_yoga_val)
        private val tvKarana: TextView? = itemView.findViewById(R.id.tv_karana_val)

        // 5. Auspicious Timings Card (சுப நேரங்கள்)
        private val tvNallaNeram: TextView = itemView.findViewById(R.id.tv_nalla_neram_val)
        private val tvGouri: TextView = itemView.findViewById(R.id.tv_gouri_val)

        // 6. Inauspicious Timings Card (அசுப நேரங்கள்)
        private val tvRahu: TextView = itemView.findViewById(R.id.tv_rahu_val)
        private val tvYamagandam: TextView = itemView.findViewById(R.id.tv_yamagandam_val)
        private val tvKuligai: TextView? = itemView.findViewById(R.id.tv_kuligai_val)
        private val tvTyajyam: TextView? = itemView.findViewById(R.id.tv_tyajyam_val)

        fun bind(gregDate: String, data: TamilDateInfo?, onPrev: () -> Unit, onNext: () -> Unit) {
            btnPrev.setOnClickListener { onPrev() }
            btnNext.setOnClickListener { onNext() }

            if (data != null) {
                // 1. TOP DATE HEADER CARD
                val parts = data.gregorianDate.split("-")
                val formattedGregorian = if (parts.size == 3) {
                    "${parts[2]} - ${parts[1]} - ${parts[0]} ${data.dayNameTa}"
                } else {
                    "$gregDate ${data.dayNameTa}"
                }
                tvHeaderLine1.text = formattedGregorian
                tvHeaderLine2.text = "${data.tamilYear} - ${data.tamilMonth} ${data.tamilDay}"

                // 2. FESTIVAL / EVENT BADGE (Visible only if present)
                if (data.festivalsEvents.isNotBlank()) {
                    cardFestivalBadge?.visibility = View.VISIBLE
                    tvFestivalText?.visibility = View.VISIBLE
                    tvFestivalText?.text = "🎉 ${data.festivalsEvents}"
                } else {
                    cardFestivalBadge?.visibility = View.GONE
                    tvFestivalText?.visibility = View.GONE
                }

                // 3. SUN & MOON TIMINGS
                tvSunrise.text = "↑  ${data.sunrise.ifEmpty { "--:--" }}"
                tvSunset.text = "↓  ${data.sunset.ifEmpty { "--:--" }}"
                tvMoonrise.text = "↑  ${data.moonrise.ifEmpty { "--:--" }}"
                tvMoonset.text = "↓  ${data.moonset.ifEmpty { "--:--" }}"

                // 4. PANCHANGAM CORE 5 ELEMENTS (Specified Order)
                tvPaksham?.text = "பக்ஷம்: ${data.paksham.ifEmpty { "--" }}"

                val tithiTimeSuffix = if (data.tithiTime.isNotBlank()) " (${data.tithiTime} வரை)" else ""
                tvTithi?.text = "திதி: ${data.tithi}$tithiTimeSuffix"

                val nakshatraTimeSuffix = if (data.nakshatraTime.isNotBlank()) " (${data.nakshatraTime} வரை)" else ""
                tvNakshatra.text = "நட்சத்திரம்: ${data.nakshatra}$nakshatraTimeSuffix"

                val yogaTimeSuffix = if (data.yogaTime.isNotBlank()) " (${data.yogaTime} வரை)" else ""
                tvYoga?.text = "யோகம்: ${data.yogaName}$yogaTimeSuffix"

                val karanaTimeSuffix = if (data.karanaTime.isNotBlank()) " (${data.karanaTime} வரை)" else ""
                tvKarana?.text = "கரணம்: ${data.karanaName}$karanaTimeSuffix"

                // 5. AUSPICIOUS TIMINGS (சுப நேரங்கள்)
                tvNallaNeram.text = "நல்ல நேரம்: ${data.nallaNeram.ifEmpty { "--" }}"
                tvGouri.text = "கௌரி நல்ல நேரம்: ${data.gouriNallaNeram.ifEmpty { "--" }}"

                // 6. INAUSPICIOUS TIMINGS (அசுப நேரங்கள்)
                tvRahu.text = "இராகு காலம்: ${data.rahuKalam.ifEmpty { "--" }}"
                tvYamagandam.text = "எமகண்டம்: ${data.yamaGandam.ifEmpty { "--" }}"
                tvKuligai?.text = "குளிகை: ${data.kuligai.ifEmpty { "--" }}"

                if (data.tyajyamTime.isNotBlank() && data.tyajyamTime != "0") {
                    tvTyajyam?.visibility = View.VISIBLE
                    tvTyajyam?.text = "தியாஜ்யம்: ${data.tyajyamTime}"
                } else {
                    tvTyajyam?.visibility = View.GONE
                }
            } else {
                tvHeaderLine1.text = gregDate
                tvHeaderLine2.text = "தகவல் இல்லை"
                cardFestivalBadge?.visibility = View.GONE
                tvSunrise.text = "↑  --:--"
                tvSunset.text = "↓  --:--"
                tvMoonrise.text = "↑  --:--"
                tvMoonset.text = "↓  --:--"
                tvPaksham?.text = "பக்ஷம்: --"
                tvTithi?.text = "திதி: --"
                tvNakshatra.text = "நட்சத்திரம்: --"
                tvYoga?.text = "யோகம்: --"
                tvKarana?.text = "கரணம்: --"
                tvNallaNeram.text = "நல்ல நேரம்: --"
                tvGouri.text = "கௌரி நல்ல நேரம்: --"
                tvRahu.text = "இராகு காலம்: --"
                tvYamagandam.text = "எமகண்டம்: --"
                tvKuligai?.text = "குளிகை: --"
                tvTyajyam?.visibility = View.GONE
            }
        }
    }

    // ==================== 2. RASI PALAN VIEW HOLDER ====================
    class RasiPalanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rvGrid: RecyclerView = itemView.findViewById(R.id.rv_rasi_grid)

        private val rasiList = listOf(
            RasiItem("மேஷம்", "பெருமை"),
            RasiItem("ரிஷபம்", "பக்தி"),
            RasiItem("மிதுனம்", "உழைப்பு"),
            RasiItem("கடகம்", "புகழ்"),
            RasiItem("சிம்மம்", "வரவு"),
            RasiItem("கன்னி", "போட்டி"),
            RasiItem("துலாம்", "இன்பம்"),
            RasiItem("விருச்சிகம்", "நலம்"),
            RasiItem("தனுசு", "ஆதரவு"),
            RasiItem("மகரம்", "வெற்றி"),
            RasiItem("கும்பம்", "மேன்மை"),
            RasiItem("மீனம்", "அமைதி")
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
            val tvPrediction: TextView = view.findViewById(R.id.tv_rasi_prediction)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardVH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_rasi_card, parent, false)
            return CardVH(view)
        }

        override fun onBindViewHolder(holder: CardVH, position: Int) {
            val item = list[position]
            holder.tvName.text = item.name
            holder.tvPrediction.text = item.keyword
        }

        override fun getItemCount(): Int = list.size
    }

    // ==================== 3. HORAI VIEW HOLDER ====================
    class HoraiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayContainer: LinearLayout = itemView.findViewById(R.id.layout_day_horai_container)
        private val nightContainer: LinearLayout = itemView.findViewById(R.id.layout_night_horai_container)
        private val btnDay: TextView? = itemView.findViewById(R.id.btn_toggle_day_horai)
        private val btnNight: TextView? = itemView.findViewById(R.id.btn_toggle_night_horai)
        private val tvSectionTitle: TextView? = itemView.findViewById(R.id.tv_horai_section_title)
        private val tvSectionSubtitle: TextView? = itemView.findViewById(R.id.tv_horai_section_subtitle)

        private val planets = listOf("சூரியன்", "சுக்கிரன்", "புதன்", "சந்திரன்", "சனி", "குரு", "செவ்வாய்")
        private val planetBadges = mapOf(
            "சூரியன்" to "சூ", "சுக்கிரன்" to "சு", "புதன்" to "பு",
            "சந்திரன்" to "சந்", "சனி" to "ச", "குரு" to "குரு", "செவ்வாய்" to "செ"
        )

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

            btnDay?.setOnClickListener {
                dayContainer.visibility = View.VISIBLE
                nightContainer.visibility = View.GONE
                tvSectionTitle?.text = "பகல் ஹோரை நேரங்கள்"
                tvSectionSubtitle?.text = "(காலை 06:00 - மாலை 06:00)"
                btnDay.setBackgroundResource(R.drawable.bg_header_btn_ripple)
                btnNight?.background = null
            }

            btnNight?.setOnClickListener {
                dayContainer.visibility = View.GONE
                nightContainer.visibility = View.VISIBLE
                tvSectionTitle?.text = "இரவு ஹோரை நேரங்கள்"
                tvSectionSubtitle?.text = "(மாலை 06:00 - மறுநாள் காலை 06:00)"
                btnNight.setBackgroundResource(R.drawable.bg_header_btn_ripple)
                btnDay?.background = null
            }

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
            val card = row.findViewById<MaterialCardView>(R.id.card_horai_row)
            val tvBadge = row.findViewById<TextView>(R.id.tv_horai_badge_letter)
            val tvPlanet = row.findViewById<TextView>(R.id.tv_horai_planet)
            val tvTime = row.findViewById<TextView>(R.id.tv_horai_time)
            val tvNature = row.findViewById<TextView>(R.id.tv_horai_nature)

            tvBadge?.text = planetBadges[planet] ?: planet.take(2)
            tvPlanet?.text = "$planet •"
            tvTime?.text = time

            when (planet) {
                "குரு", "சுக்கிரன்", "புதன்", "சந்திரன்" -> {
                    tvNature?.text = "Good"
                    card?.setCardBackgroundColor(Color.parseColor("#D7F0DC"))
                    card?.strokeColor = Color.parseColor("#A3D9AC")
                    tvBadge?.setTextColor(Color.parseColor("#1B5E20"))
                }
                "சூரியன்" -> {
                    tvNature?.text = "Medium"
                    card?.setCardBackgroundColor(Color.parseColor("#FCEECB"))
                    card?.strokeColor = Color.parseColor("#F5D78E")
                    tvBadge?.setTextColor(Color.parseColor("#E65100"))
                }
                else -> {
                    tvNature?.text = "Avoid"
                    card?.setCardBackgroundColor(Color.parseColor("#E5E5EA"))
                    card?.strokeColor = Color.parseColor("#D1D1D6")
                    tvBadge?.setTextColor(Color.parseColor("#48484A"))
                }
            }
            container.addView(row)
        }
    }
}
