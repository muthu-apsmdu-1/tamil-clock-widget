package com.example.tamilclock

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
        private const val TYPE_PANCHANGAM = 0
        private const val TYPE_RASI_PALAN = 1
        private const val TYPE_HORAI = 2
    }

    private val tabTitles = listOf("பஞ்சாங்கம்", "ராசிபலன்", "ஹோரை")
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
            is RasiPalanViewHolder -> {
                holder.bind()
            }
            is HoraiViewHolder -> {
                holder.bind(calendar.get(Calendar.DAY_OF_WEEK))
            }
        }
    }

    override fun getItemCount(): Int = tabTitles.size

    // =========================================================================
    // 1. PANCHANGAM VIEW HOLDER (Simple & Expanded Views)
    // =========================================================================
    class PanchangamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Navigation & Action Buttons
        private val btnPrev: TextView = itemView.findViewById(R.id.btn_action_prev)
        private val btnNext: TextView = itemView.findViewById(R.id.btn_action_next)
        private val btnDatePicker: TextView = itemView.findViewById(R.id.btn_action_datepicker)
        private val btnToday: TextView = itemView.findViewById(R.id.btn_action_today)
        private val btnMenu: TextView = itemView.findViewById(R.id.btn_action_menu)
        private val btnHorai: TextView = itemView.findViewById(R.id.btn_action_horai)
        private val btnSettings: TextView = itemView.findViewById(R.id.btn_action_settings)

        // Top Large Date & 3-Column Card
        private val tvBigGregorianDate: TextView = itemView.findViewById(R.id.tv_big_gregorian_date)
        private val tvBannerTamilYear: TextView = itemView.findViewById(R.id.tv_banner_tamil_year)
        private val tvBannerTamilMonth: TextView = itemView.findViewById(R.id.tv_banner_tamil_month)
        private val tvBannerTamilDate: TextView = itemView.findViewById(R.id.tv_banner_tamil_date)
        private val tvBannerTamilWeekday: TextView = itemView.findViewById(R.id.tv_banner_tamil_weekday)
        private val tvBannerEngMonth: TextView = itemView.findViewById(R.id.tv_banner_eng_month)
        private val tvBannerEngWeekday: TextView = itemView.findViewById(R.id.tv_banner_eng_weekday)

        // Dual Mode Containers
        private val layoutSimpleMode: LinearLayout = itemView.findViewById(R.id.layout_simple_mode)
        private val layoutExpandedMode: LinearLayout = itemView.findViewById(R.id.layout_expanded_mode)
        private val btnToggleExpand: ImageButton = itemView.findViewById(R.id.btn_toggle_expand)
        private val btnToggleCollapse: ImageButton = itemView.findViewById(R.id.btn_toggle_collapse)

        // Simple Mode Views
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

        // Expanded Mode Views
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

        // Bottom 4x3 Daily Rasi Palan Table
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

            // Mode Toggle Listeners
            btnToggleExpand.setOnClickListener {
                isExpanded = true
                applyModeState()
            }
            btnToggleCollapse.setOnClickListener {
                isExpanded = false
                applyModeState()
            }
            applyModeState()

            // 1. Populate Big Gregorian Date Card
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val month = cal.get(Calendar.MONTH) + 1
            val year = cal.get(Calendar.YEAR)
            tvBigGregorianDate.text = "$day-$month-$year"

            // 2. Populate 3-Column Tamil / English Date Card
            val engMonthFormat = SimpleDateFormat("MMMM", Locale.ENGLISH)
            val engWeekdayFormat = SimpleDateFormat("EEEE", Locale.ENGLISH)
            tvBannerEngMonth.text = engMonthFormat.format(cal.time)
            tvBannerEngWeekday.text = engWeekdayFormat.format(cal.time)

            if (data != null) {
                tvBannerTamilYear.text = data.tamilYear
                tvBannerTamilMonth.text = data.tamilMonth
                tvBannerTamilDate.text = data.tamilDay.toString()
                tvBannerTamilWeekday.text = data.dayNameTa

                // --- 3. POPULATE SIMPLE MODE ---
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

                // --- 4. POPULATE EXPANDED MODE ---
                tvExpSunTimings.text = "சூரிய உதயம்: ${data.sunrise.ifEmpty { "06:08 AM" }} • மறைவு: ${data.sunset.ifEmpty { "05:55 PM" }}"
                tvExpMoonTimings.text = "சந்திர உதயம்: ${data.moonrise.ifEmpty { "08:56 PM" }} • மறைவு: ${data.moonset.ifEmpty { "09:06 AM" }}"
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

                // --- 5. POPULATE 4x3 DAILY RASI PALAN TABLE ---
                val rasi = data.rasiPalanMap
                tvRasiMesham.text = "மேஷம் - ${rasi["mesham"] ?: "நன்மை"}"
                tvRasiKadagam.text = "கடகம் - ${rasi["kadagam"] ?: "மகிழ்ச்சி"}"
                tvRasiThulaam.text = "துலாம் - ${rasi["thulaam"] ?: "முயற்சி"}"
                tvRasiMagaram.text = "மகரம் - ${rasi["magaram"] ?: "மேன்மை"}"

                tvRasiRishabam.text = "ரிஷபம் - ${rasi["rishabam"] ?: "வெற்றி"}"
                tvRasiSimmam.text = "சிம்மம் - ${rasi["simmam"] ?: "செலவு"}"
                tvRasiViruchigam.text = "விருச்சி - ${rasi["viruchigam"] ?: "வரவு"}"
                tvRasiKumbam.text = "கும்பம் - ${rasi["kumbam"] ?: "தனம்"}"

                tvRasiMithunam.text = "மிதுனம் - ${rasi["mithunam"] ?: "வரவு"}"
                tvRasiKanni.text = "கன்னி - ${rasi["kanni"] ?: "லாபம்"}"
                tvRasiDhanusu.text = "தனுசு - ${rasi["dhanusu"] ?: "சாந்தம்"}"
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
    // 2. RASI PALAN TAB VIEW HOLDER
    // =========================================================================
    class RasiPalanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {}
    }

    // =========================================================================
    // 3. HORAI TAB VIEW HOLDER
    // =========================================================================
    class HoraiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(dayOfWeek: Int) {}
    }
}
