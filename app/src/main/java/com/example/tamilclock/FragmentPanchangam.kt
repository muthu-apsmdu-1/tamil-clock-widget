package com.example.tamilclock

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextClock
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FragmentPanchangam : Fragment() {

    private lateinit var dbHelper: TamilDatabaseHelper
    private var currentCalendar: Calendar = Calendar.getInstance()
    private var rootScrollView: ScrollView? = null

    // Top Capsules
    private lateinit var tvBigGregorianDate: TextView
    private lateinit var tcBannerTime: TextClock
    private lateinit var tcBannerAmPm: TextClock
    private lateinit var viewDivider: View
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

    // Simple Mode Rasi Grid
    private lateinit var tvRasiTitle: TextView
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
    private lateinit var tvExpModeTitle: TextView
    private lateinit var tvExpSunTimings: TextView
    private lateinit var tvExpMoonTimings: TextView
    private lateinit var tvExpTithiName: TextView
    private lateinit var tvExpNakshatraName: TextView
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
        rootScrollView = view.findViewById(R.id.scroll_panchangam)

        bindViews(view)
        setupToggleListeners()
        applyDynamicTheme(view)
        renderPanchangam(currentCalendar)
    }

    override fun onResume() {
        super.onResume()
        rootScrollView?.let {
            applyDynamicTheme(it)
            renderPanchangam(currentCalendar)
        }
    }

    private fun bindViews(view: View) {
        tvBigGregorianDate = view.findViewById(R.id.tv_big_gregorian_date)
        tcBannerTime = view.findViewById(R.id.tc_banner_time)
        tcBannerAmPm = view.findViewById(R.id.tc_banner_ampm)
        viewDivider = view.findViewById(R.id.view_capsule_divider)

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

        tvRasiTitle = view.findViewById(R.id.tv_rasi_header_title)
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

        tvExpModeTitle = view.findViewById(R.id.tv_exp_mode_title)
        tvExpSunTimings = view.findViewById(R.id.tv_exp_sun_timings)
        tvExpMoonTimings = view.findViewById(R.id.tv_exp_moon_timings)
        tvExpTithiName = view.findViewById(R.id.tv_exp_tithi_name)
        tvExpNakshatraName = view.findViewById(R.id.tv_exp_nakshatra_name)
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

    private fun applyDynamicTheme(view: View) {
        val palette = ThemeManager.getThemePalette(requireContext())

        // 1. Root background
        view.setBackgroundColor(palette.pageBackground)

        // 2. All Card Containers
        val cardIds = listOf(
            R.id.card_date_clock_capsule,
            R.id.card_tamil_year_month,
            R.id.card_tamil_date_weekday,
            R.id.card_eng_month_weekday,
            R.id.card_simple_summary,
            R.id.card_nalla_neram,
            R.id.card_gowri_neram,
            R.id.card_rahu,
            R.id.card_yamagandam,
            R.id.card_kuligai,
            R.id.card_soolam,
            R.id.card_parikaram,
            R.id.card_rasi_grid,
            R.id.card_exp_sun,
            R.id.card_exp_moon,
            R.id.card_exp_tithi,
            R.id.card_exp_nakshatra,
            R.id.card_exp_yoga,
            R.id.card_exp_nalla,
            R.id.card_exp_gowri,
            R.id.card_exp_rahu,
            R.id.card_exp_yamagandam,
            R.id.card_exp_kuligai,
            R.id.card_exp_nitya,
            R.id.card_exp_tyajyam,
            R.id.card_exp_soolam,
            R.id.card_exp_festivals
        )
        for (id in cardIds) {
            view.findViewById<View?>(id)?.setBackgroundColor(palette.cardBackground)
        }

        // 3. Chandrashtamam warning cards
        view.findViewById<View?>(R.id.tv_simple_chandrashtamam)?.setBackgroundColor(palette.sundayHighlight)
        view.findViewById<View?>(R.id.card_exp_chandrashtamam)?.setBackgroundColor(palette.sundayHighlight)

        // 4. Headers and Accent Colors
        tvBigGregorianDate.setTextColor(palette.primaryHeader)
        viewDivider.setBackgroundColor(palette.accentGold)
        tcBannerTime.setTextColor(palette.secondaryHeader)
        tcBannerAmPm.setTextColor(palette.secondaryHeader)

        tvBannerTamilYear.setTextColor(palette.primaryHeader)
        tvBannerTamilMonth.setTextColor(palette.primaryHeader)
        tvBannerTamilDate.setTextColor(palette.primaryHeader)
        tvBannerTamilWeekday.setTextColor(palette.primaryHeader)

        tvBannerEngMonth.setTextColor(palette.secondaryHeader)
        tvBannerEngWeekday.setTextColor(palette.secondaryHeader)

        tvSimpleSummary.setTextColor(palette.primaryText)
        tvSimpleChandrashtamam.setTextColor(Color.parseColor("#C62828"))

        tvRasiTitle.setBackgroundColor(palette.primaryHeader)
        tvRasiTitle.setTextColor(palette.accentGold)

        tvExpModeTitle.setTextColor(palette.primaryHeader)
        btnToggleExpand.setColorFilter(palette.primaryHeader)
        btnToggleCollapse.setColorFilter(palette.primaryHeader)
    }

    private fun renderPanchangam(cal: Calendar) {
        val queryFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val dateKey = queryFormat.format(cal.time)

        val dayFormat = SimpleDateFormat("dd - MM - yyyy", Locale.ENGLISH)
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

        // Header Tamil Details
        tvBannerTamilYear.text = dayData.tamilYear
        tvBannerTamilMonth.text = dayData.tamilMonth
        tvBannerTamilDate.text = if (dayData.tamilDate > 0) dayData.tamilDate.toString() else "---"
        tvBannerTamilWeekday.text = dayData.weekdayTa

        // Simple Mode Paragraph
        val summaryBuilder = StringBuilder().apply {
            append("திதி: ").append(dayData.tithiName)
            if (dayData.tithiTime != "---") append(" (${dayData.tithiTime})")
            append(" • நட்சத்திரம்: ").append(dayData.nakshatraName)
            if (dayData.nakshatraTime != "---") append(" (${dayData.nakshatraTime})")
            append(" • யோகம்: ").append(if (dayData.amritadiYoga != "---") dayData.amritadiYoga else dayData.yogaName)
        }
        tvSimpleSummary.text = summaryBuilder.toString()

        tvSimpleNallaMorn.text = "காலை: ${dayData.nallaNeramMorningStart} - ${dayData.nallaNeramMorningEnd}"
        tvSimpleNallaEve.text = "மாலை: ${dayData.nallaNeramEveningStart} - ${dayData.nallaNeramEveningEnd}"

        tvSimpleGowriMorn.text = "காலை: ${dayData.gowriDayStart} - ${dayData.gowriDayEnd}"
        tvSimpleGowriEve.text = "மாலை: ${dayData.gowriNightStart} - ${dayData.gowriNightEnd}"

        tvSimpleRahu.text = "${dayData.raghuStart} - ${dayData.raghuEnd}"
        tvSimpleYamagandam.text = "${dayData.emagandamStart} - ${dayData.emagandamEnd}"
        tvSimpleKuligai.text = "${dayData.kuligaiStart} - ${dayData.kuligaiEnd}"

        tvSimpleSoolam.text = dayData.soolam
        tvSimpleParikaram.text = dayData.parikaram
        tvSimpleChandrashtamam.text = "சந்திராஷ்டமம் : ${dayData.chandrashtamamRasi}"

        // 12 Rasi Houses
        tvRasiMesham.text = "மேஷம்\n${dayData.rasiMesham}"
        tvRasiRishabam.text = "ரிஷபம்\n${dayData.rasiRishabam}"
        tvRasiMithunam.text = "மிதுனம்\n${dayData.rasiMithunam}"
        tvRasiKadagam.text = "கடகம்\n${dayData.rasiKadagam}"
        tvRasiSimmam.text = "சிம்மம்\n${dayData.rasiSimmam}"
        tvRasiKanni.text = "கன்னி\n${dayData.rasiKanni}"
        tvRasiThulaam.text = "துலாம்\n${dayData.rasiThulaam}"
        tvRasiViruchigam.text = "விருச்சிகம்\n${dayData.rasiViruchigam}"
        tvRasiDhanusu.text = "தனுசு\n${dayData.rasiDhanusu}"
        tvRasiMagaram.text = "மகரம்\n${dayData.rasiMagaram}"
        tvRasiKumbam.text = "கும்பம்\n${dayData.rasiKumbam}"
        tvRasiMeenam.text = "மீனம்\n${dayData.rasiMeenam}"

        // Expanded Mode Details
        tvExpSunTimings.text = "☀️ ${dayData.sunrise} • 🌘 ${dayData.sunset}"
        tvExpMoonTimings.text = "🌙 ${dayData.moonrise} • 🌑 ${dayData.moonset}"

        tvExpTithiName.text = if (dayData.tithiTime != "---") "${dayData.tithiName} (${dayData.tithiTime})" else dayData.tithiName
        tvExpNakshatraName.text = if (dayData.nakshatraTime != "---") "${dayData.nakshatraName} (${dayData.nakshatraTime})" else dayData.nakshatraName
        tvExpAmritadiYoga.text = if (dayData.amritadiYoga != "---") dayData.amritadiYoga else dayData.yogaName
        tvExpChandrashtamam.text = dayData.chandrashtamamRasi

        tvExpNallaNeram.text = "காலை: ${dayData.nallaNeramMorningStart} - ${dayData.nallaNeramMorningEnd} | மாலை: ${dayData.nallaNeramEveningStart} - ${dayData.nallaNeramEveningEnd}"
        tvExpGowriNeram.text = "காலை: ${dayData.gowriDayStart} - ${dayData.gowriDayEnd} | மாலை: ${dayData.gowriNightStart} - ${dayData.gowriNightEnd}"

        tvExpRahu.text = "${dayData.raghuStart} - ${dayData.raghuEnd}"
        tvExpYamagandam.text = "${dayData.emagandamStart} - ${dayData.emagandamEnd}"
        tvExpKuligai.text = "${dayData.kuligaiStart} - ${dayData.kuligaiEnd}"

        tvExpNityaYoga.text = "நித்திய யோகம் : ${dayData.yogaName}"
        tvExpKarana.text = "கரணம் : ${dayData.karanaName}"
        tvExpTyajyam.text = dayData.tyajyamTime
        tvExpSoolam.text = "சூலம் : ${dayData.soolam}"
        tvExpParikaram.text = "பரிகாரம் : ${dayData.parikaram}"
        tvExpFestivals.text = if (dayData.festivalsEvents.isNotBlank()) dayData.festivalsEvents else "---"
    }

    private fun displayEmptyState() {
        val dash = "---"

        tvBannerTamilYear.text = dash
        tvBannerTamilMonth.text = dash
        tvBannerTamilDate.text = dash
        tvBannerTamilWeekday.text = dash

        tvSimpleSummary.text = dash
        tvSimpleNallaMorn.text = "காலை: $dash"
        tvSimpleNallaEve.text = "மாலை: $dash"
        tvSimpleGowriMorn.text = "காலை: $dash"
        tvSimpleGowriEve.text = "மாலை: $dash"
        tvSimpleRahu.text = dash
        tvSimpleYamagandam.text = dash
        tvSimpleKuligai.text = dash
        tvSimpleSoolam.text = dash
        tvSimpleParikaram.text = dash
        tvSimpleChandrashtamam.text = "சந்திராஷ்டமம் : $dash"

        tvRasiMesham.text = "மேஷம்\n$dash"
        tvRasiRishabam.text = "ரிஷபம்\n$dash"
        tvRasiMithunam.text = "மிதுனம்\n$dash"
        tvRasiKadagam.text = "கடகம்\n$dash"
        tvRasiSimmam.text = "சிம்மம்\n$dash"
        tvRasiKanni.text = "கன்னி\n$dash"
        tvRasiThulaam.text = "துலாம்\n$dash"
        tvRasiViruchigam.text = "விருச்சிகம்\n$dash"
        tvRasiDhanusu.text = "தனுசு\n$dash"
        tvRasiMagaram.text = "மகரம்\n$dash"
        tvRasiKumbam.text = "கும்பம்\n$dash"
        tvRasiMeenam.text = "மீனம்\n$dash"

        tvExpSunTimings.text = "☀️ $dash • 🌘 $dash"
        tvExpMoonTimings.text = "🌙 $dash • 🌑 $dash"
        tvExpTithiName.text = dash
        tvExpNakshatraName.text = dash
        tvExpAmritadiYoga.text = dash
        tvExpChandrashtamam.text = dash
        tvExpNallaNeram.text = "காலை: $dash | மாலை: $dash"
        tvExpGowriNeram.text = "காலை: $dash | மாலை: $dash"
        tvExpRahu.text = dash
        tvExpYamagandam.text = dash
        tvExpKuligai.text = dash
        tvExpNityaYoga.text = "நித்திய யோகம் : $dash"
        tvExpKarana.text = "கரணம் : $dash"
        tvExpTyajyam.text = dash
        tvExpSoolam.text = "சூலம் : $dash"
        tvExpParikaram.text = "பரிகாரம் : $dash"
        tvExpFestivals.text = dash
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}
