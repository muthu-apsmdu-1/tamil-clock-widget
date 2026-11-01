package com.example.tamilclock

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainPagerAdapter(
    private val onPrevDayClick: () -> Unit,
    private val onNextDayClick: () -> Unit,
    private val onDatePickerClick: () -> Unit = {},
    private val onTodayClick: () -> Unit = {},
    private val onMonthCalendarClick: () -> Unit = {},
    private val onHoraiClick: () -> Unit = {},
    private val onSettingsClick: () -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_PANCHANGAM = 0
        const val TYPE_GOWRI = 1
        const val TYPE_HORAI = 2
    }

    private var panchangamData: TamilDateInfo? = null
    private var gregorianDateText: String = ""
    private var calendar: Calendar = Calendar.getInstance()

    fun updateData(dateText: String, data: TamilDateInfo?, cal: Calendar) {
        this.gregorianDateText = dateText
        this.panchangamData = data
        this.calendar = cal
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_PANCHANGAM
            1 -> TYPE_GOWRI
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
            TYPE_GOWRI -> {
                val view = inflater.inflate(R.layout.fragment_rasi_palan, parent, false)
                GowriViewHolder(view)
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
                holder.bind(
                    gregorianDateText, 
                    panchangamData, 
                    calendar, 
                    onPrevDayClick, 
                    onNextDayClick, 
                    onDatePickerClick, 
                    onTodayClick,
                    onMonthCalendarClick,
                    onHoraiClick,
                    onSettingsClick
                )
            }
            is GowriViewHolder -> {
                val weekday = panchangamData?.dayNameTa?.ifEmpty { "ஞாயிறு" } ?: "ஞாயிறு"
                holder.bind(weekday)
            }
            is HoraiViewHolder -> {
                val weekday = panchangamData?.dayNameTa?.ifEmpty { "ஞாயிறு" } ?: "ஞாயிறு"
                holder.bind(weekday)
            }
        }
    }

    override fun getItemCount(): Int = 3

    // =========================================================================
    // 1. PANCHANGAM VIEW HOLDER
    // =========================================================================
    class PanchangamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val btnPrev: TextView = itemView.findViewById(R.id.btn_action_prev)
        private val btnNext: TextView = itemView.findViewById(R.id.btn_action_next)
        private val btnDatePicker: TextView = itemView.findViewById(R.id.btn_action_datepicker)
        private val btnToday: TextView = itemView.findViewById(R.id.btn_action_today)
        private val btnMenu: TextView = itemView.findViewById(R.id.btn_action_menu)
        private val btnHorai: TextView = itemView.findViewById(R.id.btn_action_horai)
        private val btnSettings: TextView = itemView.findViewById(R.id.btn_action_settings)

        private val tvBigGregorianDate: TextView = itemView.findViewById(R.id.tv_big_gregorian_date)
        private val tvBannerTamilYear: TextView = itemView.findViewById(R.id.tv_banner_tamil_year)
        private val tvBannerTamilMonth: TextView = itemView.findViewById(R.id.tv_banner_tamil_month)
        private val tvBannerTamilDate: TextView = itemView.findViewById(R.id.tv_banner_tamil_date)
        private val tvBannerTamilWeekday: TextView = itemView.findViewById(R.id.tv_banner_tamil_weekday)
        private val tvBannerEngMonth: TextView = itemView.findViewById(R.id.tv_banner_eng_month)
        private val tvBannerEngWeekday: TextView = itemView.findViewById(R.id.tv_banner_eng_weekday)

        private val layoutSimpleMode: LinearLayout = itemView.findViewById(R.id.layout_simple_mode)
        private val layoutExpandedMode: LinearLayout = itemView.findViewById(R.id.layout_expanded_mode)
        private val btnToggleExpand: ImageButton = itemView.findViewById(R.id.btn_toggle_expand)
        private val btnToggleCollapse: ImageButton = itemView.findViewById(R.id.btn_toggle_collapse)

        private val tvSimpleParagraph: TextView = itemView.findViewById(R.id.tv_simple_panchangam_paragraph)
        private val tvSimpleChandrashtamam: TextView = itemView.findViewById(R.id.tv_simple_chandrashtamam)
        private val tvSimpleNallaMorning: TextView = itemView.findViewById(R.id.tv_simple_nalla_neram_morning)
        private val tvSimpleNallaEvening: TextView = itemView.findViewById(R.id.tv_simple_nalla_neram_evening)
        private val tvSimpleGowriMorning: TextView = itemView.findViewById(R.id.tv_simple_gowri_morning)
        private val tvSimpleGowriEvening: TextView = itemView.findViewById(R.id.tv_simple_gowri_evening)
        private val tvSimpleRahu: TextView = itemView.findViewById(R.id.tv_simple_rahu)
        private val tvSimpleYamagandam: TextView = itemView.findViewById(R.id.tv_simple_yamagandam)
        private val tvSimpleKuligai: TextView = itemView.findViewById(R.id.tv_simple_kuligai)
        private val tvSimpleSoolam: TextView = itemView.findViewById(R.id.tv_simple_soolam)
        private val tvSimpleParikaram: TextView = itemView.findViewById(R.id.tv_simple_parikaram)

        private val tvExpSunTimings: TextView = itemView.findViewById(R.id.tv_exp_sun_timings)
        private val tvExpMoonTimings: TextView = itemView.findViewById(R.id.tv_exp_moon_timings)
        private val tvExpTithiName: TextView = itemView.findViewById(R.id.tv_exp_tithi_name)
        private val tvExpTithiTime: TextView = itemView.findViewById(R.id.tv_exp_tithi_time)
        private val tvExpNakshatraName: TextView = itemView.findViewById(R.id.tv_exp_nakshatra_name)
        private val tvExpNakshatraTime: TextView = itemView.findViewById(R.id.tv_exp_nakshatra_time)
        private val tvExpAmritadiYoga: TextView = itemView.findViewById(R.id.tv_exp_amritadi_yoga)
        private val tvExpChandrashtamam: TextView = itemView.findViewById(R.id.tv_exp_chandrashtamam)
        private val tvExpNallaNeram: TextView = itemView.findViewById(R.id.tv_exp_nalla_neram)
        private val tvExpGowriNeram: TextView = itemView.findViewById(R.id.tv_exp_gowri_neram)
        private val tvExpRahu: TextView = itemView.findViewById(R.id.tv_exp_rahu)
        private val tvExpYamagandam: TextView = itemView.findViewById(R.id.tv_exp_yamagandam)
        private val tvExpKuligai: TextView = itemView.findViewById(R.id.tv_exp_kuligai)
        private val tvExpNityaYoga: TextView = itemView.findViewById(R.id.tv_exp_nitya_yoga)
        private val tvExpKarana: TextView = itemView.findViewById(R.id.tv_exp_karana)
        private val tvExpTyajyam: TextView = itemView.findViewById(R.id.tv_exp_tyajyam)
        private val tvExpSoolam: TextView = itemView.findViewById(R.id.tv_exp_soolam)
        private val tvExpParikaram: TextView = itemView.findViewById(R.id.tv_exp_parikaram)
        private val tvExpFestivals: TextView = itemView.findViewById(R.id.tv_exp_festivals)

        private val tvRasiMesham: TextView = itemView.findViewById(R.id.tv_rasi_mesham)
        private val tvRasiKadagam: TextView = itemView.findViewById(R.id.tv_rasi_kadagam)
        private val tvRasiThulaam: TextView = itemView.findViewById(R.id.tv_rasi_thulaam)
        private val tvRasiMagaram: TextView = itemView.findViewById(R.id.tv_rasi_magaram)
        private val tvRasiRishabam: TextView = itemView.findViewById(R.id.tv_rasi_rishabam)
        private val tvRasiSimmam: TextView = itemView.findViewById(R.id.tv_rasi_simmam)
        private val tvRasiViruchigam: TextView = itemView.findViewById(R.id.tv_rasi_viruchigam)
        private val tvRasiKumbam: TextView = itemView.findViewById(R.id.tv_rasi_kumbam)
        private val tvRasiMithunam: TextView = itemView.findViewById(R.id.tv_rasi_mithunam)
        private val tvRasiKanni: TextView = itemView.findViewById(R.id.tv_rasi_kanni)
        private val tvRasiDhanusu: TextView = itemView.findViewById(R.id.tv_rasi_dhanusu)
        private val tvRasiMeenam: TextView = itemView.findViewById(R.id.tv_rasi_meenam)

        private var isExpanded = false

        fun bind(
            gregDate: String,
            data: TamilDateInfo?,
            cal: Calendar,
            onPrev: () -> Unit,
            onNext: () -> Unit,
            onDatePicker: () -> Unit,
            onToday: () -> Unit,
            onMonthCalendar: () -> Unit,
            onHorai: () -> Unit,
            onSettings: () -> Unit
        ) {
            btnPrev.setOnClickListener { onPrev() }
            btnNext.setOnClickListener { onNext() }
            btnDatePicker.setOnClickListener { onDatePicker() }
            btnToday.setOnClickListener { onToday() }
            btnMenu.setOnClickListener { onMonthCalendar() }
            btnHorai.setOnClickListener { onHorai() }
            btnSettings.setOnClickListener { onSettings() }

            btnToggleExpand.setOnClickListener {
                isExpanded = true
                applyModeState()
            }
            btnToggleCollapse.setOnClickListener {
                isExpanded = false
                applyModeState()
            }
            applyModeState()

            val day = cal.get(Calendar.DAY_OF_MONTH)
            val month = cal.get(Calendar.MONTH) + 1
            val year = cal.get(Calendar.YEAR)
            tvBigGregorianDate.text = "$day-$month-$year"

            val engMonthFormat = SimpleDateFormat("MMMM", Locale.ENGLISH)
            val engWeekdayFormat = SimpleDateFormat("EEEE", Locale.ENGLISH)
            tvBannerEngMonth.text = engMonthFormat.format(cal.time)
            tvBannerEngWeekday.text = engWeekdayFormat.format(cal.time)

            if (data != null) {
                tvBannerTamilYear.text = data.tamilYear
                tvBannerTamilMonth.text = data.tamilMonth
                tvBannerTamilDate.text = data.tamilDay.toString()
                tvBannerTamilWeekday.text = data.dayNameTa

                val nakshatraTimeStr = if (data.nakshatraTime.isNotBlank()) " - ${data.nakshatraTime} வரை" else ""
                val tithiTimeStr = if (data.tithiTime.isNotBlank()) " - ${data.tithiTime} வரை" else ""
                val amritaYogaStr = if (data.amritadiYoga.isNotBlank()) ", யோகம் : ${data.amritadiYoga}" else ""
                val tyajyamStr = if (data.tyajyamTime.isNotBlank() && data.tyajyamTime != "0") ", தியாஜ்யம் : ${data.tyajyamTime}" else ""
                val festivalStr = if (data.festivalsEvents.isNotBlank()) ". ${data.festivalsEvents}" else ""

                tvSimpleParagraph.text = buildString {
                    append("${data.nakshatra}$nakshatraTimeStr, ")
                    append("திதி : ${data.tithi}$tithiTimeStr")
                    append(amritaYogaStr)
                    append(tyajyamStr)
                    append(festivalStr)
                }

                tvSimpleChandrashtamam.text = "சந்திராஷ்டமம் : ${data.chandrashtamamRasi.ifEmpty { "--" }}"
                tvSimpleNallaMorning.text = "காலை : ${data.nallaNeramMorning.ifEmpty { "--" }}"
                tvSimpleNallaEvening.text = "மாலை : ${data.nallaNeramEvening.ifEmpty { "--" }}"
                tvSimpleGowriMorning.text = "காலை : ${data.gouriNallaMorning.ifEmpty { "--" }}"
                tvSimpleGowriEvening.text = "மாலை : ${data.gouriNallaEvening.ifEmpty { "--" }}"

                tvSimpleRahu.text = data.rahuKalam.ifEmpty { "--" }
                tvSimpleYamagandam.text = data.yamaGandam.ifEmpty { "--" }
                tvSimpleKuligai.text = data.kuligai.ifEmpty { "--" }
                tvSimpleSoolam.text = data.soolam.ifEmpty { "மேற்கு" }
                tvSimpleParikaram.text = data.parikaram.ifEmpty { "வெல்லம்" }

                tvExpSunTimings.text = "சூரிய உதயம்: ${data.sunrise.ifEmpty { "06:08" }} • மறைவு: ${data.sunset.ifEmpty { "17:55" }}"
                tvExpMoonTimings.text = "சந்திர உதயம்: ${data.moonrise.ifEmpty { "08:56 PM" }} • மறைவு: ${data.moonset.ifEmpty { "12:07" }}"
                tvExpTithiName.text = "திதி: ${data.paksham} ${data.tithi}"
                tvExpTithiTime.text = if (data.tithiTime.isNotBlank()) "${data.tithiTime} வரை" else "நாள் முழுவதும்"
                tvExpNakshatraName.text = "நட்சத்திரம்: ${data.nakshatra}"
                tvExpNakshatraTime.text = if (data.nakshatraTime.isNotBlank()) "${data.nakshatraTime} வரை" else "நாள் முழுவதும்"
                tvExpAmritadiYoga.text = "யோகம்: ${data.amritadiYoga.ifEmpty { "சித்த யோகம்" }}"
                tvExpChandrashtamam.text = "சந்திராஷ்டமம்: ${data.chandrashtamamRasi.ifEmpty { "--" }}"
                tvExpNallaNeram.text = "காலை: ${data.nallaNeramMorning} | மாலை: ${data.nallaNeramEvening}"
                tvExpGowriNeram.text = "காலை: ${data.gouriNallaMorning} | மாலை: ${data.gouriNallaEvening}"
                tvExpRahu.text = data.rahuKalam.ifEmpty { "--" }
                tvExpYamagandam.text = data.yamaGandam.ifEmpty { "--" }
                tvExpKuligai.text = data.kuligai.ifEmpty { "--" }
                tvExpNityaYoga.text = "நித்திய யோகம்: ${data.yogaName} (${data.yogaTime} வரை)"
                tvExpKarana.text = "கரணம்: ${data.karanaName} (${data.karanaTime} வரை)"
                tvExpTyajyam.text = data.tyajyamTime.ifEmpty { "--" }
                tvExpSoolam.text = "சூலம்: ${data.soolam.ifEmpty { "மேற்கு" }}"
                tvExpParikaram.text = "பரிகாரம்: ${data.parikaram.ifEmpty { "வெல்லம்" }}"
                tvExpFestivals.text = data.festivalsEvents.ifEmpty { "சிறப்பு நிகழ்வுகள் ஏதுமில்லை" }

                val rasi = data.rasiPalanMap
                tvRasiMesham.text = "மேஷம் - ${rasi["mesham"] ?: "நன்மை"}"
                tvRasiKadagam.text = "கடகம் - ${rasi["kadagam"] ?: "மகிழ்ச்சி"}"
                tvRasiThulaam.text = "துலாம் - ${rasi["thulaam"] ?: "உழைப்பு"}"
                tvRasiMagaram.text = "மகரம் - ${rasi["magaram"] ?: "மேன்மை"}"

                tvRasiRishabam.text = "ரிஷபம் - ${rasi["rishabam"] ?: "வெற்றி"}"
                tvRasiSimmam.text = "சிம்மம் - ${rasi["simmam"] ?: "செலவு"}"
                tvRasiViruchigam.text = "விருச்சி - ${rasi["viruchigam"] ?: "ஆதாயம்"}"
                tvRasiKumbam.text = "கும்பம் - ${rasi["kumbam"] ?: "தனம்"}"

                tvRasiMithunam.text = "மிதுனம் - ${rasi["mithunam"] ?: "வரவு"}"
                tvRasiKanni.text = "கன்னி - ${rasi["kanni"] ?: "லாபம்"}"
                tvRasiDhanusu.text = "தனுசு - ${rasi["dhanusu"] ?: "எச்சரிக்கை"}"
                tvRasiMeenam.text = "மீனம் - ${rasi["meenam"] ?: "புகழ்"}"
            }
        }

        private fun applyModeState() {
            if (isExpanded) {
                layoutSimpleMode.visibility = View.GONE
                layoutExpandedMode.visibility = View.VISIBLE
            } else {
                layoutSimpleMode.visibility = View.VISIBLE
                layoutExpandedMode.visibility = View.GONE
            }
        }
    }

    // =========================================================================
    // 2. GOWRI PANCHANGAM VIEW HOLDER (உத்தி, அமிர்தம், லாபம் table)
    // =========================================================================
    class GowriViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val containerDay: LinearLayout? = itemView.findViewById(R.id.container_day_gowri)
        private val containerNight: LinearLayout? = itemView.findViewById(R.id.container_night_gowri)

        fun bind(weekdayTa: String) {
            val daySlots = listOf(
                "காலை 06:00 - 07:30" to "உத்தி",
                "காலை 07:30 - 09:00" to "அமிர்தம்",
                "காலை 09:00 - 10:30" to "ரோகம்",
                "காலை 10:30 - 12:00" to "லாபம்",
                "மதியம் 12:00 - 01:30" to "தனம்",
                "மதியம் 01:30 - 03:00" to "விஷம்",
                "மாலை 03:00 - 04:30" to "சோரம்",
                "மாலை 04:30 - 06:00" to "சுபம்"
            )

            val nightSlots = listOf(
                "மாலை 06:00 - 07:30" to "சுபம்",
                "இரவு 07:30 - 09:00" to "அமிர்தம்",
                "இரவு 09:00 - 10:30" to "உத்தி",
                "இரவு 10:30 - 12:00" to "ரோகம்",
                "நள்ளிரவு 12:00 - 01:30" to "லாபம்",
                "நள்ளிரவு 01:30 - 03:00" to "தனம்",
                "விடியற்காலை 03:00 - 04:30" to "விஷம்",
                "விடியற்காலை 04:30 - 06:00" to "சோரம்"
            )

            containerDay?.removeAllViews()
            for ((time, name) in daySlots) {
                containerDay?.addView(createGowriRow(itemView, time, name))
            }

            containerNight?.removeAllViews()
            for ((time, name) in nightSlots) {
                containerNight?.addView(createGowriRow(itemView, time, name))
            }
        }

        private fun createGowriRow(parent: View, time: String, name: String): View {
            val row = LinearLayout(parent.context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(8, 8, 8, 8)
            }

            val tvTime = TextView(parent.context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.4f)
                text = time
                setTextColor(Color.parseColor("#333333"))
                textSize = 12.5f
                paint.isFakeBoldText = true
            }

            val isGood = name in listOf("அமிர்தம்", "சுபம்", "லாபம்", "தனம்", "உத்தி")
            val tvName = TextView(parent.context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = name
                gravity = android.view.Gravity.END
                setTextColor(if (isGood) Color.parseColor("#1B5E20") else Color.parseColor("#B71C1C"))
                textSize = 13f
                paint.isFakeBoldText = true
            }

            row.addView(tvTime)
            row.addView(tvName)
            return row
        }
    }

    // =========================================================================
    // 3. HORAI VIEW HOLDER
    // =========================================================================
    class HoraiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val container: LinearLayout? = itemView.findViewById(R.id.container_horai_list)
        private val tvHeader: TextView? = itemView.findViewById(R.id.tv_horai_day_header)

        fun bind(weekdayTa: String) {
            tvHeader?.text = "$weekdayTa ஹோரை நேரங்கள் (சூரிய உதயம் முதல்)"
            if (container != null) {
                container.removeAllViews()
                val horais = listOf(
                    "காலை 06:00 - 07:00" to "சூரியன்",
                    "காலை 07:00 - 08:00" to "சுக்கிரன்",
                    "காலை 08:00 - 09:00" to "புதன்",
                    "காலை 09:00 - 10:00" to "சந்திரன்",
                    "காலை 10:00 - 11:00" to "சனி",
                    "காலை 11:00 - 12:00" to "குரு",
                    "மதியம் 12:00 - 01:00" to "செவ்வாய்",
                    "மதியம் 01:00 - 02:00" to "சூரியன்",
                    "மதியம் 02:00 - 03:00" to "சுக்கிரன்",
                    "மாலை 03:00 - 04:00" to "புதன்",
                    "மாலை 04:00 - 05:00" to "சந்திரன்",
                    "மாலை 05:00 - 06:00" to "சனி"
                )
                for ((time, planet) in horais) {
                    val row = LinearLayout(itemView.context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(12, 10, 12, 10)
                    }
                    val tvTime = TextView(itemView.context).apply {
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.3f)
                        text = time
                        setTextColor(Color.parseColor("#E0E0E0"))
                        textSize = 13f
                    }
                    val tvPlanet = TextView(itemView.context).apply {
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        text = "$planet ஹோரை"
                        gravity = android.view.Gravity.END
                        setTextColor(Color.parseColor("#FFDF00"))
                        textSize = 13.5f
                        paint.isFakeBoldText = true
                    }
                    row.addView(tvTime)
                    row.addView(tvPlanet)
                    container.addView(row)
                }
            }
        }
    }
}
