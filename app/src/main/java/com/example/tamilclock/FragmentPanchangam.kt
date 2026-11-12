
package com.example.tamilclock

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextClock
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FragmentPanchangam : Fragment() {

    private lateinit var dbHelper: TamilDatabaseHelper
    private var currentCalendar: Calendar = Calendar.getInstance()
    private var isExpandedMode: Boolean = false

    // View references are cleared in onDestroyView()
    private var vh: PanchangamViewHolder? = null

    companion object {
        private const val KEY_IS_EXPANDED = "key_is_expanded"
    }

    private class PanchangamViewHolder(view: View) {

        // Top Header & Capsule
        val tvBigGregorianDate: TextView =
            view.findViewById(R.id.tv_big_gregorian_date)

        val tcBannerTime: TextClock =
            view.findViewById(R.id.tc_banner_time)

        val viewDivider: View =
            view.findViewById(R.id.view_capsule_divider)

        val btnPrevDay: ImageButton =
            view.findViewById(R.id.btn_prev_day)

        val btnNextDay: ImageButton =
            view.findViewById(R.id.btn_next_day)

        // Date Strip
        val tvBannerTamilYear: TextView =
            view.findViewById(R.id.tv_banner_tamil_year)

        val tvBannerTamilMonth: TextView =
            view.findViewById(R.id.tv_banner_tamil_month)

        val tvBannerTamilDate: TextView =
            view.findViewById(R.id.tv_banner_tamil_date)

        val tvBannerTamilWeekday: TextView =
            view.findViewById(R.id.tv_banner_tamil_weekday)

        val tvBannerEngMonth: TextView =
            view.findViewById(R.id.tv_banner_eng_month)

        val tvBannerEngWeekday: TextView =
            view.findViewById(R.id.tv_banner_eng_weekday)

        // Containers & Toggle
        val layoutSimpleMode: View =
            view.findViewById(R.id.layout_simple_mode)

        val layoutExpandedMode: View =
            view.findViewById(R.id.layout_expanded_mode)

        val btnToggleExpand: ImageButton =
            view.findViewById(R.id.btn_toggle_expand)

        val btnToggleCollapse: ImageButton =
            view.findViewById(R.id.btn_toggle_collapse)

        // Simple Mode
        val tvSimpleSummary: TextView =
            view.findViewById(R.id.tv_simple_panchangam_paragraph)

        val lblSimpleNalla: TextView =
            view.findViewById(R.id.lbl_nalla_neram)

        val tvSimpleNallaMorn: TextView =
            view.findViewById(R.id.tv_simple_nalla_neram_morning)

        val tvSimpleNallaEve: TextView =
            view.findViewById(R.id.tv_simple_nalla_neram_evening)

        val lblSimpleGowri: TextView =
            view.findViewById(R.id.lbl_gowri_neram)

        val tvSimpleGowriMorn: TextView =
            view.findViewById(R.id.tv_simple_gowri_morning)

        val tvSimpleGowriEve: TextView =
            view.findViewById(R.id.tv_simple_gowri_evening)

        val lblSimpleRahu: TextView =
            view.findViewById(R.id.lbl_rahu)

        val tvSimpleRahu: TextView =
            view.findViewById(R.id.tv_simple_rahu)

        val lblSimpleYamagandam: TextView =
            view.findViewById(R.id.lbl_yamagandam)

        val tvSimpleYamagandam: TextView =
            view.findViewById(R.id.tv_simple_yamagandam)

        val lblSimpleKuligai: TextView =
            view.findViewById(R.id.lbl_kuligai)

        val tvSimpleKuligai: TextView =
            view.findViewById(R.id.tv_simple_kuligai)

        val lblSimpleSoolam: TextView =
            view.findViewById(R.id.lbl_soolam)

        val tvSimpleSoolam: TextView =
            view.findViewById(R.id.tv_simple_soolam)

        val lblSimpleParikaram: TextView =
            view.findViewById(R.id.lbl_parikaram)

        val tvSimpleParikaram: TextView =
            view.findViewById(R.id.tv_simple_parikaram)

        val tvSimpleChandrashtamam: TextView =
            view.findViewById(R.id.tv_simple_chandrashtamam)

        // Universal Rasi Grid
        val tvRasiTitle: TextView =
            view.findViewById(R.id.tv_rasi_header_title)

        val tvRasiMesham: TextView =
            view.findViewById(R.id.tv_rasi_mesham)

        val tvRasiRishabam: TextView =
            view.findViewById(R.id.tv_rasi_rishabam)

        val tvRasiMithunam: TextView =
            view.findViewById(R.id.tv_rasi_mithunam)

        val tvRasiKadagam: TextView =
            view.findViewById(R.id.tv_rasi_kadagam)

        val tvRasiSimmam: TextView =
            view.findViewById(R.id.tv_rasi_simmam)

        val tvRasiKanni: TextView =
            view.findViewById(R.id.tv_rasi_kanni)

        val tvRasiThulaam: TextView =
            view.findViewById(R.id.tv_rasi_thulaam)

        val tvRasiViruchigam: TextView =
            view.findViewById(R.id.tv_rasi_viruchigam)

        val tvRasiDhanusu: TextView =
            view.findViewById(R.id.tv_rasi_dhanusu)

        val tvRasiMagaram: TextView =
            view.findViewById(R.id.tv_rasi_magaram)

        val tvRasiKumbam: TextView =
            view.findViewById(R.id.tv_rasi_kumbam)

        val tvRasiMeenam: TextView =
            view.findViewById(R.id.tv_rasi_meenam)

        // Expanded Mode
        val tvExpModeTitle: TextView =
            view.findViewById(R.id.tv_exp_mode_title)

        val tvExpSunTimings: TextView =
            view.findViewById(R.id.tv_exp_sun_timings)

        val tvExpMoonTimings: TextView =
            view.findViewById(R.id.tv_exp_moon_timings)

        val lblExpTithi: TextView =
            view.findViewById(R.id.lbl_exp_tithi)

        val tvExpTithiName: TextView =
            view.findViewById(R.id.tv_exp_tithi_name)

        val lblExpNakshatra: TextView =
            view.findViewById(R.id.lbl_exp_nakshatra)

        val tvExpNakshatraName: TextView =
            view.findViewById(R.id.tv_exp_nakshatra_name)

        val lblExpYoga: TextView =
            view.findViewById(R.id.lbl_exp_yoga)

        val tvExpAmritadiYoga: TextView =
            view.findViewById(R.id.tv_exp_amritadi_yoga)

        val cardExpChandrashtamam: View? =
            view.findViewById(R.id.card_exp_chandrashtamam)

        val lblExpChandrashtamam: TextView =
            view.findViewById(R.id.lbl_exp_chandrashtamam)

        val tvExpChandrashtamam: TextView =
            view.findViewById(R.id.tv_exp_chandrashtamam)

        val lblExpNalla: TextView =
            view.findViewById(R.id.lbl_exp_nalla)

        val tvExpNallaNeram: TextView =
            view.findViewById(R.id.tv_exp_nalla_neram)

        val lblExpGowri: TextView =
            view.findViewById(R.id.lbl_exp_gowri)

        val tvExpGowriNeram: TextView =
            view.findViewById(R.id.tv_exp_gowri_neram)

        val lblExpRahu: TextView =
            view.findViewById(R.id.lbl_exp_rahu)

        val tvExpRahu: TextView =
            view.findViewById(R.id.tv_exp_rahu)

        val lblExpYamagandam: TextView =
            view.findViewById(R.id.lbl_exp_yamagandam)

        val tvExpYamagandam: TextView =
            view.findViewById(R.id.tv_exp_yamagandam)

        val lblExpKuligai: TextView =
            view.findViewById(R.id.lbl_exp_kuligai)

        val tvExpKuligai: TextView =
            view.findViewById(R.id.tv_exp_kuligai)

        val tvExpNityaYoga: TextView =
            view.findViewById(R.id.tv_exp_nitya_yoga)

        val tvExpKarana: TextView =
            view.findViewById(R.id.tv_exp_karana)

        val lblExpTyajyam: TextView =
            view.findViewById(R.id.lbl_exp_tyajyam)

        val tvExpTyajyam: TextView =
            view.findViewById(R.id.tv_exp_tyajyam)

        val tvExpSoolam: TextView =
            view.findViewById(R.id.tv_exp_soolam)

        val tvExpParikaram: TextView =
            view.findViewById(R.id.tv_exp_parikaram)

        val lblExpFestivals: TextView =
            view.findViewById(R.id.lbl_exp_festivals)

        val tvExpFestivals: TextView =
            view.findViewById(R.id.tv_exp_festivals)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbHelper = TamilDatabaseHelper(requireContext())

        if (savedInstanceState != null) {
            isExpandedMode =
                savedInstanceState.getBoolean(KEY_IS_EXPANDED, false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_IS_EXPANDED, isExpandedMode)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_panchangam,
            container,
            false
        )
    }

    // ---------------------------------------------------------
    // Fragment Lifecycle
    // ---------------------------------------------------------

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Sync initial calendar from MainActivity
        (activity as? MainActivity)?.selectedCalendar?.let {
            currentCalendar = it.clone() as Calendar
        }

        val holder = PanchangamViewHolder(view)
        vh = holder

        setupToggleListeners(holder)
        setupDayNavigationButtons(holder)

        // Restore display mode
        holder.layoutSimpleMode.visibility =
            if (isExpandedMode) View.GONE else View.VISIBLE

        holder.layoutExpandedMode.visibility =
            if (isExpandedMode) View.VISIBLE else View.GONE

        // Initial theme application and data rendering
        applyThemeAndRender()
    }

    override fun onResume() {
        super.onResume()

        // Refresh only when the view hierarchy exists
        if (vh != null) {
            applyThemeAndRender()
        }
    }

    override fun onDestroyView() {
        // Release all references to the destroyed view hierarchy
        vh = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        if (::dbHelper.isInitialized) {
            dbHelper.close()
        }

        super.onDestroy()
    }

    // ---------------------------------------------------------
    // Toggle & Navigation
    // ---------------------------------------------------------

    private fun setupToggleListeners(h: PanchangamViewHolder) {

        h.btnToggleExpand.setOnClickListener {
            isExpandedMode = true
            h.layoutSimpleMode.visibility = View.GONE
            h.layoutExpandedMode.visibility = View.VISIBLE
        }

        h.btnToggleCollapse.setOnClickListener {
            isExpandedMode = false
            h.layoutExpandedMode.visibility = View.GONE
            h.layoutSimpleMode.visibility = View.VISIBLE
        }
    }

    private fun setupDayNavigationButtons(h: PanchangamViewHolder) {

        h.btnPrevDay.setOnClickListener {
            currentCalendar.add(Calendar.DAY_OF_MONTH, -1)

            applyThemeAndRender()

            (activity as? MainActivity)?.syncSelectedDate(
                currentCalendar.clone() as Calendar
            )
        }

        h.btnNextDay.setOnClickListener {
            currentCalendar.add(Calendar.DAY_OF_MONTH, 1)

            applyThemeAndRender()

            (activity as? MainActivity)?.syncSelectedDate(
                currentCalendar.clone() as Calendar
            )
        }
    }

    fun updateDate(cal: Calendar) {
        currentCalendar = cal.clone() as Calendar

        if (isAdded && vh != null) {
            applyThemeAndRender()
        }
    }

    // ---------------------------------------------------------
    // Theme Helpers
    // ---------------------------------------------------------

    private fun isDarkPalette(
        palette: ThemeManager.AppThemePalette
    ): Boolean {
        return ColorUtils.calculateLuminance(
            palette.pageBackground
        ) < 0.35
    }

    private fun applyCardColor(
        container: View?,
        color: Int
    ) {
        if (container is CardView) {
            container.setCardBackgroundColor(color)
        } else {
            container?.setBackgroundColor(color)
        }
    }

    // ---------------------------------------------------------
    // Theme Application & Rendering
    // ---------------------------------------------------------

    private fun applyThemeAndRender() {

        val rootView = view ?: return
        val context = context ?: return
        val h = vh ?: return

        val palette = ThemeManager.getThemePalette(context)
        val isDark = isDarkPalette(palette)

        // 1. Root & Card Containers
        rootView.setBackgroundColor(palette.pageBackground)

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
            applyCardColor(
                rootView.findViewById(id),
                palette.cardBackground
            )
        }

        // Fixed Chandrashtamam Colors
        val dangerBg = Color.parseColor("#3E0007")
        val dangerText = Color.parseColor("#FFDF00")

        applyCardColor(h.tvSimpleChandrashtamam, dangerBg)
        h.tvSimpleChandrashtamam.setTextColor(dangerText)

        applyCardColor(h.cardExpChandrashtamam, dangerBg)
        h.lblExpChandrashtamam.setTextColor(dangerText)
        h.tvExpChandrashtamam.setTextColor(dangerText)

        // 2. Header Colors
        h.tvBigGregorianDate.setTextColor(
            if (isDark) Color.WHITE else palette.primaryHeader
        )

        h.viewDivider.setBackgroundColor(palette.accentGold)

        h.tcBannerTime.setTextColor(
            if (isDark) palette.accentGold else palette.primaryHeader
        )

        h.tvBannerTamilYear.setTextColor(
            if (isDark) Color.WHITE else palette.primaryHeader
        )

        h.tvBannerTamilMonth.setTextColor(
            if (isDark) palette.accentGold else palette.secondaryHeader
        )

        h.tvBannerTamilDate.setTextColor(
            if (isDark) Color.WHITE else palette.primaryHeader
        )

        h.tvBannerTamilWeekday.setTextColor(
            if (isDark) Color.WHITE else palette.primaryHeader
        )

        h.tvBannerEngMonth.setTextColor(
            if (isDark) Color.WHITE else palette.primaryHeader
        )

        h.tvBannerEngWeekday.setTextColor(
            if (isDark) palette.accentGold else palette.secondaryHeader
        )

        // Rasi Header
        h.tvRasiTitle.setBackgroundColor(palette.primaryHeader)
        h.tvRasiTitle.setTextColor(palette.accentGold)

        h.tvExpModeTitle.setTextColor(
            if (isDark) Color.WHITE else palette.primaryHeader
        )

        h.btnToggleExpand.setColorFilter(palette.accentGold)
        h.btnToggleCollapse.setColorFilter(palette.accentGold)

        // 3. Simple Mode Colors
        val labelCol =
            if (isDark) Color.WHITE else palette.primaryHeader

        val timingCol =
            if (isDark) palette.accentGold
            else Color.parseColor("#8A0B1A")

        h.lblSimpleNalla.setTextColor(labelCol)
        h.tvSimpleNallaMorn.setTextColor(timingCol)
        h.tvSimpleNallaEve.setTextColor(timingCol)

        h.lblSimpleGowri.setTextColor(labelCol)
        h.tvSimpleGowriMorn.setTextColor(timingCol)
        h.tvSimpleGowriEve.setTextColor(timingCol)

        h.lblSimpleRahu.setTextColor(labelCol)
        h.tvSimpleRahu.setTextColor(timingCol)

        h.lblSimpleYamagandam.setTextColor(labelCol)
        h.tvSimpleYamagandam.setTextColor(timingCol)

        h.lblSimpleKuligai.setTextColor(labelCol)
        h.tvSimpleKuligai.setTextColor(timingCol)

        h.lblSimpleSoolam.setTextColor(labelCol)
        h.tvSimpleSoolam.setTextColor(timingCol)

        h.lblSimpleParikaram.setTextColor(labelCol)
        h.tvSimpleParikaram.setTextColor(timingCol)

        // Universal Rasi Grid
        val rasiViews = listOf(
            h.tvRasiMesham,
            h.tvRasiRishabam,
            h.tvRasiMithunam,
            h.tvRasiKadagam,
            h.tvRasiSimmam,
            h.tvRasiKanni,
            h.tvRasiThulaam,
            h.tvRasiViruchigam,
            h.tvRasiDhanusu,
            h.tvRasiMagaram,
            h.tvRasiKumbam,
            h.tvRasiMeenam
        )

        val rasiTextCol =
            if (isDark) Color.WHITE
            else Color.parseColor("#1A1A1A")

        for (rv in rasiViews) {
            rv.setTextColor(rasiTextCol)
        }

        // 4. Expanded Mode Semantic Colors
        val green = Color.parseColor("#2E7D32")
        val red = Color.parseColor("#C62828")

        h.tvExpSunTimings.setTextColor(
            if (isDark) Color.WHITE else green
        )

        h.tvExpMoonTimings.setTextColor(
            if (isDark) Color.WHITE else green
        )

        h.lblExpTithi.setTextColor(
            if (isDark) Color.WHITE else green
        )

        h.tvExpTithiName.setTextColor(
            if (isDark) Color.WHITE else green
        )

        h.lblExpNakshatra.setTextColor(
            if (isDark) Color.WHITE else green
        )

        h.tvExpNakshatraName.setTextColor(
            if (isDark) Color.WHITE else green
        )

        h.lblExpYoga.setTextColor(
            if (isDark) Color.WHITE else green
        )

        h.tvExpAmritadiYoga.setTextColor(
            if (isDark) Color.WHITE else green
        )

        h.lblExpNalla.setTextColor(
            if (isDark) Color.WHITE else green
        )

        h.tvExpNallaNeram.setTextColor(
            if (isDark) Color.WHITE else green
        )

        h.lblExpGowri.setTextColor(
            if (isDark) Color.WHITE else green
        )

        h.tvExpGowriNeram.setTextColor(
            if (isDark) Color.WHITE else green
        )

        h.lblExpRahu.setTextColor(
            if (isDark) Color.WHITE else red
        )

        h.tvExpRahu.setTextColor(
            if (isDark) Color.WHITE else red
        )

        h.lblExpYamagandam.setTextColor(
            if (isDark) Color.WHITE else red
        )

        h.tvExpYamagandam.setTextColor(
            if (isDark) Color.WHITE else red
        )

        h.lblExpKuligai.setTextColor(
            if (isDark) Color.WHITE else green
        )

        h.tvExpKuligai.setTextColor(
            if (isDark) Color.WHITE else green
        )

        h.tvExpNityaYoga.setTextColor(
            if (isDark) Color.WHITE else green
        )

        h.tvExpKarana.setTextColor(
            if (isDark) Color.WHITE else green
        )

        h.lblExpTyajyam.setTextColor(
            if (isDark) Color.WHITE else red
        )

        h.tvExpTyajyam.setTextColor(
            if (isDark) Color.WHITE else red
        )

        h.tvExpSoolam.setTextColor(
            if (isDark) Color.WHITE else red
        )

        h.tvExpParikaram.setTextColor(
            if (isDark) Color.WHITE else green
        )

        h.lblExpFestivals.setTextColor(
            if (isDark) Color.WHITE else red
        )

        h.tvExpFestivals.setTextColor(
            if (isDark) Color.WHITE else green
        )

        renderDatabaseDetails(h, isDark, palette)
    }

    // ---------------------------------------------------------
    // Database Details
    // ---------------------------------------------------------

    private fun renderDatabaseDetails(
        h: PanchangamViewHolder,
        isDark: Boolean,
        palette: ThemeManager.AppThemePalette
    ) {

        val dateKey = String.format(
            Locale.ENGLISH,
            "%04d-%02d-%02d",
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH) + 1,
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        )

        val dayFormat =
            SimpleDateFormat("dd - MM - yyyy", Locale.ENGLISH)

        h.tvBigGregorianDate.text =
            dayFormat.format(currentCalendar.time)

        h.tvBannerEngMonth.text =
            SimpleDateFormat("MMMM", Locale.ENGLISH)
                .format(currentCalendar.time)

        h.tvBannerEngWeekday.text =
            SimpleDateFormat("EEEE", Locale.ENGLISH)
                .format(currentCalendar.time)

        val dayData = dbHelper.getDayDetails(dateKey)

        if (dayData == null) {
            displayEmptyState(h)
            return
        }

        h.tvBannerTamilYear.text = dayData.tamilYear
        h.tvBannerTamilMonth.text = dayData.tamilMonth

        h.tvBannerTamilDate.text =
            if (dayData.tamilDate > 0) {
                dayData.tamilDate.toString()
            } else {
                "---"
            }

        h.tvBannerTamilWeekday.text = dayData.weekdayTa

        buildSimpleSummarySpannable(
            h,
            dayData,
            isDark,
            palette
        )

        h.tvSimpleNallaMorn.text =
            "காலை: ${dayData.nallaNeramMorningStart} - ${dayData.nallaNeramMorningEnd}"

        h.tvSimpleNallaEve.text =
            "மாலை: ${dayData.nallaNeramEveningStart} - ${dayData.nallaNeramEveningEnd}"

        h.tvSimpleGowriMorn.text =
            "காலை: ${dayData.gowriDayStart} - ${dayData.gowriDayEnd}"

        h.tvSimpleGowriEve.text =
            "மாலை: ${dayData.gowriNightStart} - ${dayData.gowriNightEnd}"

        h.tvSimpleRahu.text =
            "${dayData.raghuStart} - ${dayData.raghuEnd}"

        h.tvSimpleYamagandam.text =
            "${dayData.emagandamStart} - ${dayData.emagandamEnd}"

        h.tvSimpleKuligai.text =
            "${dayData.kuligaiStart} - ${dayData.kuligaiEnd}"

        h.tvSimpleSoolam.text = dayData.soolam
        h.tvSimpleParikaram.text = dayData.parikaram

        h.tvSimpleChandrashtamam.text =
            "சந்திராஷ்டமம் : ${dayData.chandrashtamamRasi}"

        // Rasi 12 Houses
        h.tvRasiMesham.text =
            "மேஷம்\n${dayData.rasiMesham}"

        h.tvRasiRishabam.text =
            "ரிஷபம்\n${dayData.rasiRishabam}"

        h.tvRasiMithunam.text =
            "மிதுனம்\n${dayData.rasiMithunam}"

        h.tvRasiKadagam.text =
            "கடகம்\n${dayData.rasiKadagam}"

        h.tvRasiSimmam.text =
            "சிம்மம்\n${dayData.rasiSimmam}"

        h.tvRasiKanni.text =
            "கன்னி\n${dayData.rasiKanni}"

        h.tvRasiThulaam.text =
            "துலாம்\n${dayData.rasiThulaam}"

        h.tvRasiViruchigam.text =
            "விருச்சிகம்\n${dayData.rasiViruchigam}"

        h.tvRasiDhanusu.text =
            "தனுசு\n${dayData.rasiDhanusu}"

        h.tvRasiMagaram.text =
            "மகரம்\n${dayData.rasiMagaram}"

        h.tvRasiKumbam.text =
            "கும்பம்\n${dayData.rasiKumbam}"

        h.tvRasiMeenam.text =
            "மீனம்\n${dayData.rasiMeenam}"

        // Expanded Mode Details
        h.tvExpSunTimings.text =
            "☀️ ${dayData.sunrise} • 🌘 ${dayData.sunset}"

        h.tvExpMoonTimings.text =
            "🌙 ${dayData.moonrise} • 🌑 ${dayData.moonset}"

        h.tvExpTithiName.text =
            if (dayData.tithiTime != "---") {
                "${dayData.tithiName} (${dayData.tithiTime})"
            } else {
                dayData.tithiName
            }

        h.tvExpNakshatraName.text =
            if (dayData.nakshatraTime != "---") {
                "${dayData.nakshatraName} (${dayData.nakshatraTime})"
            } else {
                dayData.nakshatraName
            }

        h.tvExpAmritadiYoga.text =
            if (dayData.amritadiYoga != "---") {
                dayData.amritadiYoga
            } else {
                dayData.yogaName
            }

        h.tvExpChandrashtamam.text =
            dayData.chandrashtamamRasi

        h.tvExpNallaNeram.text =
            "காலை: ${dayData.nallaNeramMorningStart} - ${dayData.nallaNeramMorningEnd} | மாலை: ${dayData.nallaNeramEveningStart} - ${dayData.nallaNeramEveningEnd}"

        h.tvExpGowriNeram.text =
            "காலை: ${dayData.gowriDayStart} - ${dayData.gowriDayEnd} | மாலை: ${dayData.gowriNightStart} - ${dayData.gowriNightEnd}"

        h.tvExpRahu.text =
            "${dayData.raghuStart} - ${dayData.raghuEnd}"

        h.tvExpYamagandam.text =
            "${dayData.emagandamStart} - ${dayData.emagandamEnd}"

        h.tvExpKuligai.text =
            "${dayData.kuligaiStart} - ${dayData.kuligaiEnd}"

        h.tvExpNityaYoga.text =
            "நித்திய யோகம் : ${dayData.yogaName}"

        h.tvExpKarana.text =
            "கரணம் : ${dayData.karanaName}"

        h.tvExpTyajyam.text = dayData.tyajyamTime

        h.tvExpSoolam.text =
            "சூலம் : ${dayData.soolam}"

        h.tvExpParikaram.text =
            "பரிகாரம் : ${dayData.parikaram}"

        h.tvExpFestivals.text =
            if (dayData.festivalsEvents.isNotBlank()) {
                dayData.festivalsEvents
            } else {
                "---"
            }
    }

    // ---------------------------------------------------------
    // Simple Panchangam Summary
    // ---------------------------------------------------------

    private fun buildSimpleSummarySpannable(
        h: PanchangamViewHolder,
        dayData: TamilDatabaseHelper.DayPanchangDetails,
        isDark: Boolean,
        palette: ThemeManager.AppThemePalette
    ) {

        val labelCol =
            if (isDark) Color.WHITE else palette.primaryHeader

        val valCol =
            if (isDark) palette.accentGold
            else Color.parseColor("#8A0B1A")

        val sb = SpannableStringBuilder()

        fun appendItem(
            label: String,
            value: String,
            isLast: Boolean = false
        ) {

            val startLabel = sb.length

            sb.append(label)

            val endLabel = sb.length

            sb.setSpan(
                ForegroundColorSpan(labelCol),
                startLabel,
                endLabel,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            val startVal = sb.length

            sb.append(" $value")

            val endVal = sb.length

            sb.setSpan(
                ForegroundColorSpan(valCol),
                startVal,
                endVal,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            if (!isLast) {

                val startBullet = sb.length

                sb.append("  •  ")

                val endBullet = sb.length

                sb.setSpan(
                    ForegroundColorSpan(
                        if (isDark) Color.WHITE
                        else Color.parseColor("#9E9E9E")
                    ),
                    startBullet,
                    endBullet,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        val tithiVal =
            if (dayData.tithiTime != "---") {
                "${dayData.tithiName} (${dayData.tithiTime})"
            } else {
                dayData.tithiName
            }

        val nakshatraVal =
            if (dayData.nakshatraTime != "---") {
                "${dayData.nakshatraName} (${dayData.nakshatraTime})"
            } else {
                dayData.nakshatraName
            }

        val yogaVal =
            if (dayData.amritadiYoga != "---") {
                dayData.amritadiYoga
            } else {
                dayData.yogaName
            }

        appendItem("திதி :", tithiVal)
        appendItem("நட்சத்திரம் :", nakshatraVal)
        appendItem("யோகம் :", yogaVal, isLast = true)

        h.tvSimpleSummary.text = sb
    }

    // ---------------------------------------------------------
    // Empty Database State
    // ---------------------------------------------------------

    private fun displayEmptyState(h: PanchangamViewHolder) {

        val dash = "---"

        h.tvBannerTamilYear.text = dash
        h.tvBannerTamilMonth.text = dash
        h.tvBannerTamilDate.text = dash
        h.tvBannerTamilWeekday.text = dash

        h.tvSimpleSummary.text = dash

        h.tvSimpleNallaMorn.text = "காலை: $dash"
        h.tvSimpleNallaEve.text = "மாலை: $dash"

        h.tvSimpleGowriMorn.text = "காலை: $dash"
        h.tvSimpleGowriEve.text = "மாலை: $dash"

        h.tvSimpleRahu.text = dash
        h.tvSimpleYamagandam.text = dash
        h.tvSimpleKuligai.text = dash

        h.tvSimpleSoolam.text = dash
        h.tvSimpleParikaram.text = dash

        h.tvSimpleChandrashtamam.text =
            "சந்திராஷ்டமம் : $dash"

        // Rasi 12 Houses
        h.tvRasiMesham.text = "மேஷம்\n$dash"
        h.tvRasiRishabam.text = "ரிஷபம்\n$dash"
        h.tvRasiMithunam.text = "மிதுனம்\n$dash"
        h.tvRasiKadagam.text = "கடகம்\n$dash"
        h.tvRasiSimmam.text = "சிம்மம்\n$dash"
        h.tvRasiKanni.text = "கன்னி\n$dash"
        h.tvRasiThulaam.text = "துலாம்\n$dash"
        h.tvRasiViruchigam.text = "விருச்சிகம்\n$dash"
        h.tvRasiDhanusu.text = "தனுசு\n$dash"
        h.tvRasiMagaram.text = "மகரம்\n$dash"
        h.tvRasiKumbam.text = "கும்பம்\n$dash"
        h.tvRasiMeenam.text = "மீனம்\n$dash"

        // Expanded Mode
        h.tvExpSunTimings.text =
            "☀️ $dash • 🌘 $dash"

        h.tvExpMoonTimings.text =
            "🌙 $dash • 🌑 $dash"

        h.tvExpTithiName.text = dash
        h.tvExpNakshatraName.text = dash
        h.tvExpAmritadiYoga.text = dash

        h.tvExpChandrashtamam.text = dash

        h.tvExpNallaNeram.text =
            "காலை: $dash | மாலை: $dash"

        h.tvExpGowriNeram.text =
            "காலை: $dash | மாலை: $dash"

        h.tvExpRahu.text = dash
        h.tvExpYamagandam.text = dash
        h.tvExpKuligai.text = dash

        h.tvExpNityaYoga.text =
            "நித்திய யோகம் : $dash"

        h.tvExpKarana.text =
            "கரணம் : $dash"

        h.tvExpTyajyam.text = dash

        h.tvExpSoolam.text =
            "சூலம் : $dash"

        h.tvExpParikaram.text =
            "பரிகாரம் : $dash"

        h.tvExpFestivals.text = dash
    }
}
