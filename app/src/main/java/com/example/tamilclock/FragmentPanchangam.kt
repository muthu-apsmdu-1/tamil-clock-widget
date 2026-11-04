package com.example.tamilclock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FragmentPanchangam : Fragment() {

    private lateinit var dbHelper: TamilDatabaseHelper
    private var currentCalendar: Calendar = Calendar.getInstance()

    // Top Capsules
    private lateinit var tvBigGregorianDate: TextView
    private lateinit var tvBannerTamilYear: TextView
    private lateinit var tvBannerTamilMonth: TextView
    private lateinit var tvBannerTamilDate: TextView
    private lateinit var tvBannerTamilWeekday: TextView
    private lateinit var tvBannerEngMonth: TextView
    private lateinit var tvBannerEngWeekday: TextView

    // Mode Containers & Toggle Buttons
    private lateinit var layoutSimpleMode: View
    private lateinit var layoutExpandedMode: View
    private lateinit var btnToggleExpand: ImageButton
    private lateinit var btnToggleCollapse: ImageButton

    // Simple Mode Views
    private lateinit var tvSimpleSummary: TextView
    private lateinit var tvSimpleNallaMorn: TextView
    private lateinit var tvSimpleNallaEve: TextView
    private lateinit var tvSimpleGowriMorn: TextView
    private lateinit var tvSimpleGowriEve: TextView
    private lateinit var tvSimpleRahu: TextView
    private lateinit var tvSimpleYamagandam: TextView
    private lateinit var tvSimpleKuligai: TextView
    private lateinit var tvSimpleSoolam: TextView
    private lateinit var tvSimpleParikaram: TextView
    private lateinit var tvSimpleChandrashtamam: TextView

    // Simple Mode Rasi Grid (12 Houses)
    private lateinit var tvRasiMesham: TextView
    private lateinit var tvRasiRishabam: TextView
    private lateinit var tvRasiMithunam: TextView
    private lateinit var tvRasiKadagam: TextView
    private lateinit var tvRasiSimmam: TextView
    private lateinit var tvRasiKanni: TextView
    private lateinit var tvRasiThulaam: TextView
    private lateinit var tvRasiViruchigam: TextView
    private lateinit var tvRasiDhanusu: TextView
    private lateinit var tvRasiMagaram: TextView
    private lateinit var tvRasiKumbam: TextView
    private lateinit var tvRasiMeenam: TextView

    // Expanded Mode Views
    private lateinit var tvExpSunTimings: TextView
    private lateinit var tvExpMoonTimings: TextView
    private lateinit var tvExpTithiName: TextView
    private lateinit var tvExpTithiTime: TextView
    private lateinit var tvExpNakshatraName: TextView
    private lateinit var tvExpNakshatraTime: TextView
    private lateinit var tvExpAmritadiYoga: TextView
    private lateinit var tvExpChandrashtamam: TextView
    private lateinit var tvExpNallaNeram: TextView
    private lateinit var tvExpGowriNeram: TextView
    private lateinit var tvExpRahu: TextView
    private lateinit var tvExpYamagandam: TextView
    private lateinit var tvExpKuligai: TextView
    private lateinit var tvExpNityaYoga: TextView
    private lateinit var tvExpKarana: TextView
    private lateinit var tvExpTyajyam: TextView
    private lateinit var tvExpSoolam: TextView
    private lateinit var tvExpParikaram: TextView
    private lateinit var tvExpFestivals: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_panchangam, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = TamilDatabaseHelper(requireContext())

        bindViews(view)
        setupToggleListeners()
        renderPanchangam(currentCalendar)
    }

    private fun bindViews(view: View) {
        tvBigGregorianDate = view.findViewById(R.id.tv_big_gregorian_date)
        tvBannerTamilYear = view.findViewById(R.id.tv_banner_tamil_year)
        tvBannerTamilMonth = view.findViewById(R.id.tv_banner_tamil_month)
        tvBannerTamilDate = view.findViewById(R.id.tv_banner_tamil_date)
        tvBannerTamilWeekday = view.findViewById(R.id.tv_banner_tamil_weekday)
        tvBannerEngMonth = view.findViewById(R.id.tv_banner_eng_month)
        tvBannerEngWeekday = view.findViewById(R.id.tv_banner_eng_weekday)

        layoutSimpleMode = view.findViewById(R.id.layout_simple_mode)
        layoutExpandedMode = view.findViewById(R.id.layout_expanded_mode)
        btnToggleExpand = view.findViewById(R.id.btn_toggle_expand)
        btnToggleCollapse = view.findViewById(R.id.btn_toggle_collapse)

        tvSimpleSummary = view.findViewById(R.id.tv_simple_panchangam_paragraph)
        tvSimpleNallaMorn = view.findViewById(R.id.tv_simple_nalla_neram_morning)
        tvSimpleNallaEve = view.findViewById(R.id.tv_simple_nalla_neram_evening)
        tvSimpleGowriMorn = view.findViewById(R.id.tv_simple_gowri_morning)
        tvSimpleGowriEve = view.findViewById(R.id.tv_simple_gowri_evening)
        tvSimpleRahu = view.findViewById(R.id.tv_simple_rahu)
        tvSimpleYamagandam = view.findViewById(R.id.tv_simple_yamagandam)
        tvSimpleKuligai = view.findViewById(R.id.tv_simple_kuligai)
        tvSimpleSoolam = view.findViewById(R.id.tv_simple_soolam)
        tvSimpleParikaram = view.findViewById(R.id.tv_simple_parikaram)
        tvSimpleChandrashtamam = view.findViewById(R.id.tv_simple_chandrashtamam)

        tvRasiMesham = view.findViewById(R.id.tv_rasi_mesham)
        tvRasiRishabam = view.findViewById(R.id.tv_rasi_rishabam)
        tvRasiMithunam = view.findViewById(R.id.tv_rasi_mithunam)
        tvRasiKadagam = view.findViewById(R.id.tv_rasi_kadagam)
        tvRasiSimmam = view.findViewById(R.id.tv_rasi_simmam)
        tvRasiKanni = view.findViewById(R.id.tv_rasi_kanni)
        tvRasiThulaam = view.findViewById(R.id.tv_rasi_thulaam)
        tvRasiViruchigam = view.findViewById(R.id.tv_rasi_viruchigam)
        tvRasiDhanusu = view.findViewById(R.id.tv_rasi_dhanusu)
        tvRasiMagaram = view.findViewById(R.id.tv_rasi_magaram)
        tvRasiKumbam = view.findViewById(R.id.tv_rasi_kumbam)
        tvRasiMeenam = view.findViewById(R.id.tv_rasi_meenam)

        tvExpSunTimings = view.findViewById(R.id.tv_exp_sun_timings)
        tvExpMoonTimings = view.findViewById(R.id.tv_exp_moon_timings)
        tvExpTithiName = view.findViewById(R.id.tv_exp_tithi_name)
        tvExpTithiTime = view.findViewById(R.id.tv_exp_tithi_time)
        tvExpNakshatraName = view.findViewById(R.id.tv_exp_nakshatra_name)
        tvExpNakshatraTime = view.findViewById(R.id.tv_exp_nakshatra_time)
        tvExpAmritadiYoga = view.findViewById(R.id.tv_exp_amritadi_yoga)
        tvExpChandrashtamam = view.findViewById(R.id.tv_exp_chandrashtamam)
        tvExpNallaNeram = view.findViewById(R.id.tv_exp_nalla_neram)
        tvExpGowriNeram = view.findViewById(R.id.tv_exp_gowri_neram)
        tvExpRahu = view.findViewById(R.id.tv_exp_rahu)
        tvExpYamagandam = view.findViewById(R.id.tv_exp_yamagandam)
        tvExpKuligai = view.findViewById(R.id.tv_exp_kuligai)
        tvExpNityaYoga = view.findViewById(R.id.tv_exp_nitya_yoga)
        tvExpKarana = view.findViewById(R.id.tv_exp_karana)
        tvExpTyajyam = view.findViewById(R.id.tv_exp_tyajyam)
        tvExpSoolam = view.findViewById(R.id.tv_exp_soolam)
        tvExpParikaram = view.findViewById(R.id.tv_exp_parikaram)
        tvExpFestivals = view.findViewById(R.id.tv_exp_festivals)
    }

    private fun setupToggleListeners() {
        btnToggleExpand.setOnClickListener {
            layoutSimpleMode.visibility = View.GONE
            layoutExpandedMode.visibility = View.VISIBLE
        }

        btnToggleCollapse.setOnClickListener {
            layoutExpandedMode.visibility = View.GONE
            layoutSimpleMode.visibility = View.VISIBLE
        }
    }

    fun updateDate(cal: Calendar) {
        currentCalendar = cal.clone() as Calendar
        if (isAdded && view != null) {
            renderPanchangam(currentCalendar)
        }
    }

    private fun renderPanchangam(cal: Calendar) {
        val queryFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateKey = queryFormat.format(cal.time)

        val dayFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val engMonthFormat = SimpleDateFormat("MMMM", Locale.ENGLISH)
        val engWeekdayFormat = SimpleDateFormat("EEEE", Locale.ENGLISH)

        tvBigGregorianDate.text = dayFormat.format(cal.time)
        tvBannerEngMonth.text = engMonthFormat.format(cal.time)
        tvBannerEngWeekday.text = engWeekdayFormat.format(cal.time)

        val dayData = dbHelper.getDayDetails(dateKey)
        if (dayData == null) {
            displayEmptyState()
            return
        }

        // Header Tamil details
        tvBannerTamilYear.text = dayData.tamilYear.ifEmpty { "---" }
        tvBannerTamilMonth.text = dayData.tamilMonth.ifEmpty { "---" }
        tvBannerTamilDate.text = if (dayData.tamilDate > 0) dayData.tamilDate.toString() else "---"
        tvBannerTamilWeekday.text = dayData.weekdayTa.ifEmpty { "---" }

        // Simple Mode Paragraph
        val summaryBuilder = StringBuilder().apply {
            append("திதி: ").append(dayData.tithiName.ifEmpty { "---" })
            if (dayData.tithiTime.isNotEmpty()) append(" (${dayData.tithiTime})")
            append(" • நட்சத்திரம்: ").append(dayData.nakshatraName.ifEmpty { "---" })
            if (dayData.nakshatraTime.isNotEmpty()) append(" (${dayData.nakshatraTime})")
            append(" • யோகம்: ").append(dayData.amritadiYoga.ifEmpty { dayData.yogaName.ifEmpty { "---" } })
        }
        tvSimpleSummary.text = summaryBuilder.toString()

        tvSimpleNallaMorn.text = "காலை: ${dayData.nallaNeramMorn.ifEmpty { "---" }}"
        tvSimpleNallaEve.text = "மாலை: ${dayData.nallaNeramEve.ifEmpty { "---" }}"

        tvSimpleGowriMorn.text = "காலை: ${dayData.gowriMorn.ifEmpty { "---" }}"
        tvSimpleGowriEve.text = "மாலை: ${dayData.gowriEve.ifEmpty { "---" }}"

        tvSimpleRahu.text = dayData.rahu.ifEmpty { "---" }
        tvSimpleYamagandam.text = dayData.yamagandam.ifEmpty { "---" }
        tvSimpleKuligai.text = dayData.kuligai.ifEmpty { "---" }

        tvSimpleSoolam.text = dayData.soolam.ifEmpty { "---" }
        tvSimpleParikaram.text = dayData.parikaram.ifEmpty { "---" }
        tvSimpleChandrashtamam.text = "சந்திராஷ்டமம் : ${dayData.chandrashtamamRasi.ifEmpty { "---" }}"

        // Simple Mode Rasi Grid
        tvRasiMesham.text = "மேஷம்\n${dayData.rasiMesham.ifEmpty { "---" }}"
        tvRasiRishabam.text = "ரிஷபம்\n${dayData.rasiRishabam.ifEmpty { "---" }}"
        tvRasiMithunam.text = "மிதுனம்\n${dayData.rasiMithunam.ifEmpty { "---" }}"
        tvRasiKadagam.text = "கடகம்\n${dayData.rasiKadagam.ifEmpty { "---" }}"
        tvRasiSimmam.text = "சிம்மம்\n${dayData.rasiSimmam.ifEmpty { "---" }}"
        tvRasiKanni.text = "கன்னி\n${dayData.rasiKanni.ifEmpty { "---" }}"
        tvRasiThulaam.text = "துலாம்\n${dayData.rasiThulaam.ifEmpty { "---" }}"
        tvRasiViruchigam.text = "விருச்சிகம்\n${dayData.rasiViruchigam.ifEmpty { "---" }}"
        tvRasiDhanusu.text = "தனுசு\n${dayData.rasiDhanusu.ifEmpty { "---" }}"
        tvRasiMagaram.text = "மகரம்\n${dayData.rasiMagaram.ifEmpty { "---" }}"
        tvRasiKumbam.text = "கும்பம்\n${dayData.rasiKumbam.ifEmpty { "---" }}"
        tvRasiMeenam.text = "மீனம்\n${dayData.rasiMeenam.ifEmpty { "---" }}"

        // Expanded Mode
        val sunText = if (dayData.sunrise.isNotEmpty() && dayData.sunset.isNotEmpty()) {
            "☀️ ${dayData.sunrise} • 🌘 ${dayData.sunset}"
        } else {
            "☀️ --:-- • 🌘 --:--"
        }
        tvExpSunTimings.text = sunText

        val moonText = if (dayData.moonrise.isNotEmpty() && dayData.moonset.isNotEmpty()) {
            "🌙 ${dayData.moonrise} • 🌑 ${dayData.moonset}"
        } else {
            "🌙 --:-- • 🌑 --:--"
        }
        tvExpMoonTimings.text = moonText

        tvExpTithiName.text = dayData.tithiName.ifEmpty { "---" }
        tvExpTithiTime.text = dayData.tithiTime.ifEmpty { "" }

        tvExpNakshatraName.text = dayData.nakshatraName.ifEmpty { "---" }
        tvExpNakshatraTime.text = dayData.nakshatraTime.ifEmpty { "" }

        tvExpAmritadiYoga.text = dayData.amritadiYoga.ifEmpty { dayData.yogaName.ifEmpty { "---" } }
        tvExpChandrashtamam.text = dayData.chandrashtamamRasi.ifEmpty { "---" }

        val nallaFull = StringBuilder().apply {
            append("காலை: ").append(dayData.nallaNeramMorn.ifEmpty { "---" })
            append(" | மாலை: ").append(dayData.nallaNeramEve.ifEmpty { "---" })
        }
        tvExpNallaNeram.text = nallaFull.toString()

        val gowriFull = StringBuilder().apply {
            append("காலை: ").append(dayData.gowriMorn.ifEmpty { "---" })
            append(" | மாலை: ").append(dayData.gowriEve.ifEmpty { "---" })
        }
        tvExpGowriNeram.text = gowriFull.toString()

        tvExpRahu.text = dayData.rahu.ifEmpty { "---" }
        tvExpYamagandam.text = dayData.yamagandam.ifEmpty { "---" }
        tvExpKuligai.text = dayData.kuligai.ifEmpty { "---" }

        tvExpNityaYoga.text = "நித்திய யோகம் : ${dayData.yogaName.ifEmpty { "---" }}"
        tvExpKarana.text = "கரணம் : ${dayData.karanaName.ifEmpty { "---" }}"
        tvExpTyajyam.text = dayData.tyajyamTime.ifEmpty { "---" }
        tvExpSoolam.text = "சூலம் : ${dayData.soolam.ifEmpty { "---" }}"
        tvExpParikaram.text = "பரிகாரம் : ${dayData.parikaram.ifEmpty { "---" }}"
        tvExpFestivals.text = dayData.festivalsEvents.ifEmpty { "இன்று விசேஷங்கள் இல்லை" }
    }

    private fun displayEmptyState() {
        val emptyDash = "---"

        tvBannerTamilYear.text = emptyDash
        tvBannerTamilMonth.text = emptyDash
        tvBannerTamilDate.text = emptyDash
        tvBannerTamilWeekday.text = emptyDash

        tvSimpleSummary.text = "தரவு கிடைக்கவில்லை"
        tvSimpleNallaMorn.text = "காலை: $emptyDash"
        tvSimpleNallaEve.text = "மாலை: $emptyDash"
        tvSimpleGowriMorn.text = "காலை: $emptyDash"
        tvSimpleGowriEve.text = "மாலை: $emptyDash"
        tvSimpleRahu.text = emptyDash
        tvSimpleYamagandam.text = emptyDash
        tvSimpleKuligai.text = emptyDash
        tvSimpleSoolam.text = emptyDash
        tvSimpleParikaram.text = emptyDash
        tvSimpleChandrashtamam.text = "சந்திராஷ்டமம் : $emptyDash"

        tvRasiMesham.text = "மேஷம்\n$emptyDash"
        tvRasiRishabam.text = "ரிஷபம்\n$emptyDash"
        tvRasiMithunam.text = "மிதுனம்\n$emptyDash"
        tvRasiKadagam.text = "கடகம்\n$emptyDash"
        tvRasiSimmam.text = "சிம்மம்\n$emptyDash"
        tvRasiKanni.text = "கன்னி\n$emptyDash"
        tvRasiThulaam.text = "துலாம்\n$emptyDash"
        tvRasiViruchigam.text = "விருச்சிகம்\n$emptyDash"
        tvRasiDhanusu.text = "தனுசு\n$emptyDash"
        tvRasiMagaram.text = "மகரம்\n$emptyDash"
        tvRasiKumbam.text = "கும்பம்\n$emptyDash"
        tvRasiMeenam.text = "மீனம்\n$emptyDash"

        tvExpSunTimings.text = "☀️ --:-- • 🌘 --:--"
        tvExpMoonTimings.text = "🌙 --:-- • 🌑 --:--"
        tvExpTithiName.text = emptyDash
        tvExpTithiTime.text = ""
        tvExpNakshatraName.text = emptyDash
        tvExpNakshatraTime.text = ""
        tvExpAmritadiYoga.text = emptyDash
        tvExpChandrashtamam.text = emptyDash
        tvExpNallaNeram.text = "காலை: $emptyDash | மாலை: $emptyDash"
        tvExpGowriNeram.text = "காலை: $emptyDash | மாலை: $emptyDash"
        tvExpRahu.text = emptyDash
        tvExpYamagandam.text = emptyDash
        tvExpKuligai.text = emptyDash
        tvExpNityaYoga.text = "நித்திய யோகம் : $emptyDash"
        tvExpKarana.text = "கரணம் : $emptyDash"
        tvExpTyajyam.text = emptyDash
        tvExpSoolam.text = "சூலம் : $emptyDash"
        tvExpParikaram.text = "பரிகாரம் : $emptyDash"
        tvExpFestivals.text = "தரவு கிடைக்கவில்லை"
    }
}
