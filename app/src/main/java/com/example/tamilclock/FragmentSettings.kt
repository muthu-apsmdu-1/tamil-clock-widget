package com.example.tamilclock

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class FragmentSettings : Fragment() {

    private var rootScrollView: ScrollView? = null
    private var tvHeading: TextView? = null
    private var tvLblAppTheme: TextView? = null

    private var tvLblWidgetTheme: TextView? = null
    private var tvLblDateFormat: TextView? = null
    private var tvLblClockStyle: TextView? = null
    private var tvLblOuterBorder: TextView? = null
    private var switchOuterBorder: SwitchCompat? = null

    private var tvLblLiveHoraiGowri: TextView? = null
    private var switchLiveHoraiGowri: SwitchCompat? = null

    private var cardSliderCapsule: LinearLayout? = null
    private var tvLblWidgetOpacity: TextView? = null
    private var tvWidgetOpacityVal: TextView? = null
    private var seekBarOpacity: SeekBar? = null

    // N Date Format Selector Boxes
    private var boxDateFmtDash: FrameLayout? = null
    private var tvDateFmtDash: TextView? = null
    private var boxDateFmtText: FrameLayout? = null
    private var tvDateFmtText: TextView? = null
    private var boxDateFmtSlash: FrameLayout? = null
    private var tvDateFmtSlash: TextView? = null

    // Clock Type Switcher (Digital vs Analog)
    private var boxClockDigital: FrameLayout? = null
    private var tvClockDigital: TextView? = null
    private var tickClockDigital: TextView? = null
    private var boxClockAnalog: FrameLayout? = null
    private var tvClockAnalog: TextView? = null
    private var tickClockAnalog: TextView? = null

    // Sub-Style Thumbnail Cards (2 Digital + 3 Analog)
    private var groupDigitalFaces: LinearLayout? = null
    private var groupAnalogFaces: LinearLayout? = null

    private var cardDigitalClassic: LinearLayout? = null
    private var ivThumbDigitalClassic: ImageView? = null
    private var tvLabelDigitalClassic: TextView? = null

    private var cardDigitalNeon: LinearLayout? = null
    private var ivThumbDigitalNeon: ImageView? = null
    private var tvLabelDigitalNeon: TextView? = null

    private var cardAnalogClassic: LinearLayout? = null
    private var ivThumbAnalogClassic: ImageView? = null
    private var tvLabelAnalogClassic: TextView? = null

    private var cardAnalogWhite: LinearLayout? = null
    private var ivThumbAnalogWhite: ImageView? = null
    private var tvLabelAnalogWhite: TextView? = null

    private var cardAnalogHybrid: LinearLayout? = null
    private var ivThumbAnalogHybrid: ImageView? = null
    private var tvLabelAnalogHybrid: TextView? = null

    // 4x2 and 4x4 Table Selectors
    private var tvLblElements4x2: TextView? = null
    private var tvCountElements4x2: TextView? = null
    private var tableElements4x2: LinearLayout? = null

    private var tvLblElements4x4: TextView? = null
    private var tvCountElements4x4: TextView? = null
    private var tableElements4x4: LinearLayout? = null

    // Preview views
    private var tvLblWidgetPreview: TextView? = null
    private var framePreviewWrapper: FrameLayout? = null
    private var ivPreviewWidgetBg: ImageView? = null
    private var tvPreviewTitle: TextView? = null
    private var tvPreviewSub: TextView? = null

    // 5 App Background Theme Boxes: Triple<Container, LabelText, TickText>
    private val appBoxes = mutableMapOf<String, Triple<FrameLayout, TextView, TextView>>()

    // 6 Widget Preset Boxes: Triple<Container, LabelText, TickText>
    private val presetBoxes = mutableMapOf<ThemeManager.Preset, Triple<FrameLayout, TextView, TextView>>()

    // Debounce handler
    private val debounceHandler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    // 4x2 Astrological Elements (up to 3 selected)
    private val astrologicalOptions = listOf(
        Pair(ThemeManager.ELEM_PAKSHAM, "1. பக்ஷம் (Paksham)"),
        Pair(ThemeManager.ELEM_THITHI, "2. திதி நேரம் (Thithi & Timing)"),
        Pair(ThemeManager.ELEM_NAKSHATRA, "3. நட்சத்திரம் நேரம் (Nakshatra & Timing)"),
        Pair(ThemeManager.ELEM_AMIRTHATHI_YOGA, "4. அமிர்தாதி யோகம் (Amirthaitya Yoga)"),
        Pair(ThemeManager.ELEM_NALLA_NERAM, "5. நல்ல நேரம் (Nalla Neram)"),
        Pair(ThemeManager.ELEM_GOWRI_NALLA_NERAM, "6. கௌரி நல்ல நேரம் (Gowri Nalla Neram)"),
        Pair(ThemeManager.ELEM_RAHU_KALAM, "7. ராகு காலம் (Rahu Kalam)"),
        Pair(ThemeManager.ELEM_YEMAKANDAM, "8. எமகண்டம் (Yemakandam)"),
        Pair(ThemeManager.ELEM_KULIGAI, "9. குளிகை (Kuligai)"),
        Pair(ThemeManager.ELEM_SUNRISE_SET, "10. சூரிய உதயம் / மறைவு (Sun Rise/Set)"),
        Pair(ThemeManager.ELEM_CHANDRASTAMA, "11. சந்திராஷ்டமம் (Chandrastama)"),
        Pair(ThemeManager.ELEM_FESTIVAL, "12. சிறப்பு / திருவிழா (Festival)")
    )

    // 4x4 Single-Row Options (10 choices, up to 3 active rows)
    private val singleRow4x4Options = listOf(
        Pair(ThemeManager.OPT_PAKSHAM_THITHI_NAKSHATRA, "1. பக்ஷம் • திதி • நட்சத்திரம்"),
        Pair(ThemeManager.OPT_NALLA_NERAM, "2. நல்ல நேரம்"),
        Pair(ThemeManager.OPT_GOWRI_NALLA_NERAM, "3. கௌரி நல்ல நேரம்"),
        Pair(ThemeManager.OPT_AMIRTHATHI_NITHYA_YOGAM, "4. அமிர்தாதி • நித்ய யோகம்"),
        Pair(ThemeManager.OPT_RAHU_YEMAKANDAM, "5. ராகு காலம் • எமகண்டம்"),
        Pair(ThemeManager.OPT_KULIGAI_THYAJAM, "6. குளிகை • த்யாஜ்யம்"),
        Pair(ThemeManager.OPT_KARNAM_SOOLAM_PARIKARAM, "7. கரணம் • சூலம் / பரிகாரம்"),
        Pair(ThemeManager.OPT_SUN_TIMINGS, "8. சூரிய உதயம் / மறைவு"),
        Pair(ThemeManager.OPT_CHANDRASTAMA, "9. சந்திராஷ்டமம்"),
        Pair(ThemeManager.OPT_FESTIVAL, "10. சிறப்பு / திருவிழா")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupAppBoxes(view)
        setupPresetBoxes(view)
        setupClockStyleSwitcher()
        setupBorderSwitch()
        setupLiveHoraiGowriSwitch()
        setupOpacitySeekBar()
        applyDynamicTheming()
    }

    override fun onResume() {
        super.onResume()
        applyDynamicTheming()
    }

    override fun onDestroyView() {
        updateRunnable?.let { debounceHandler.removeCallbacks(it) }
        updateRunnable = null

        switchOuterBorder?.setOnCheckedChangeListener(null)
        switchLiveHoraiGowri?.setOnCheckedChangeListener(null)
        seekBarOpacity?.setOnSeekBarChangeListener(null)

        tableElements4x2?.removeAllViews()
        tableElements4x4?.removeAllViews()
        appBoxes.clear()
        presetBoxes.clear()

        rootScrollView = null
        tvHeading = null
        tvLblAppTheme = null
        tvLblWidgetTheme = null
        tvLblDateFormat = null
        tvLblClockStyle = null
        tvLblOuterBorder = null
        switchOuterBorder = null
        tvLblLiveHoraiGowri = null
        switchLiveHoraiGowri = null
        cardSliderCapsule = null
        tvLblWidgetOpacity = null
        tvWidgetOpacityVal = null
        seekBarOpacity = null
        boxDateFmtDash = null
        tvDateFmtDash = null
        boxDateFmtText = null
        tvDateFmtText = null
        boxDateFmtSlash = null
        tvDateFmtSlash = null
        boxClockDigital = null
        tvClockDigital = null
        tickClockDigital = null
        boxClockAnalog = null
        tvClockAnalog = null
        tickClockAnalog = null
        groupDigitalFaces = null
        groupAnalogFaces = null
        cardDigitalClassic = null
        ivThumbDigitalClassic = null
        tvLabelDigitalClassic = null
        cardDigitalNeon = null
        ivThumbDigitalNeon = null
        tvLabelDigitalNeon = null
        cardAnalogClassic = null
        ivThumbAnalogClassic = null
        tvLabelAnalogClassic = null
        cardAnalogWhite = null
        ivThumbAnalogWhite = null
        tvLabelAnalogWhite = null
        cardAnalogHybrid = null
        ivThumbAnalogHybrid = null
        tvLabelAnalogHybrid = null
        tvLblElements4x2 = null
        tvCountElements4x2 = null
        tableElements4x2 = null
        tvLblElements4x4 = null
        tvCountElements4x4 = null
        tableElements4x4 = null
        tvLblWidgetPreview = null
        framePreviewWrapper = null
        ivPreviewWidgetBg = null
        tvPreviewTitle = null
        tvPreviewSub = null

        super.onDestroyView()
    }

    private fun bindViews(view: View) {
        rootScrollView = view.findViewById(R.id.scroll_settings_root)
        tvHeading = view.findViewById(R.id.tv_settings_main_heading)
        tvLblAppTheme = view.findViewById(R.id.tv_lbl_app_theme)

        tvLblWidgetTheme = view.findViewById(R.id.tv_lbl_widget_theme)
        tvLblDateFormat = view.findViewById(R.id.tv_lbl_date_format)
        tvLblClockStyle = view.findViewById(R.id.tv_lbl_clock_style)
        tvLblOuterBorder = view.findViewById(R.id.tv_lbl_outer_border)
        switchOuterBorder = view.findViewById(R.id.switch_outer_border)

        tvLblLiveHoraiGowri = view.findViewById(R.id.tv_lbl_live_horai_gowri)
        switchLiveHoraiGowri = view.findViewById(R.id.switch_live_horai_gowri)

        cardSliderCapsule = view.findViewById(R.id.card_slider_capsule)
        tvLblWidgetOpacity = view.findViewById(R.id.tv_lbl_widget_opacity)
        tvWidgetOpacityVal = view.findViewById(R.id.tv_widget_opacity_val)
        seekBarOpacity = view.findViewById(R.id.seekbar_widget_opacity)

        // Date Format Boxes
        boxDateFmtDash = view.findViewById(R.id.box_date_fmt_dash)
        tvDateFmtDash = view.findViewById(R.id.tv_date_fmt_dash)
        boxDateFmtText = view.findViewById(R.id.box_date_fmt_text)
        tvDateFmtText = view.findViewById(R.id.tv_date_fmt_text)
        boxDateFmtSlash = view.findViewById(R.id.box_date_fmt_slash)
        tvDateFmtSlash = view.findViewById(R.id.tv_date_fmt_slash)

        // Clock Type Switcher
        boxClockDigital = view.findViewById(R.id.box_clock_digital)
        tvClockDigital = view.findViewById(R.id.tv_clock_digital)
        tickClockDigital = view.findViewById(R.id.tick_clock_digital)
        boxClockAnalog = view.findViewById(R.id.box_clock_analog)
        tvClockAnalog = view.findViewById(R.id.tv_clock_analog)
        tickClockAnalog = view.findViewById(R.id.tick_clock_analog)

        // Sub-Style Thumbnail Cards
        groupDigitalFaces = view.findViewById(R.id.group_digital_faces)
        groupAnalogFaces = view.findViewById(R.id.group_analog_faces)

        cardDigitalClassic = view.findViewById(R.id.card_face_digital_classic)
        ivThumbDigitalClassic = view.findViewById(R.id.iv_thumb_digital_classic)
        tvLabelDigitalClassic = view.findViewById(R.id.tv_label_digital_classic)

        cardDigitalNeon = view.findViewById(R.id.card_face_digital_neon)
        ivThumbDigitalNeon = view.findViewById(R.id.iv_thumb_digital_neon)
        tvLabelDigitalNeon = view.findViewById(R.id.tv_label_digital_neon)

        cardAnalogClassic = view.findViewById(R.id.card_face_analog_classic)
        ivThumbAnalogClassic = view.findViewById(R.id.iv_thumb_analog_classic)
        tvLabelAnalogClassic = view.findViewById(R.id.tv_label_analog_classic)

        cardAnalogWhite = view.findViewById(R.id.card_face_analog_white)
        ivThumbAnalogWhite = view.findViewById(R.id.iv_thumb_analog_white)
        tvLabelAnalogWhite = view.findViewById(R.id.tv_label_analog_white)

        cardAnalogHybrid = view.findViewById(R.id.card_face_analog_hybrid)
        ivThumbAnalogHybrid = view.findViewById(R.id.iv_thumb_analog_hybrid)
        tvLabelAnalogHybrid = view.findViewById(R.id.tv_label_analog_hybrid)

        // 4x2 and 4x4 Tables
        tvLblElements4x2 = view.findViewById(R.id.tv_lbl_elements_4x2)
        tvCountElements4x2 = view.findViewById(R.id.tv_count_elements_4x2)
        tableElements4x2 = view.findViewById(R.id.table_elements_4x2)

        tvLblElements4x4 = view.findViewById(R.id.tv_lbl_elements_4x4)
        tvCountElements4x4 = view.findViewById(R.id.tv_count_elements_4x4)
        tableElements4x4 = view.findViewById(R.id.table_elements_4x4)

        // Live Preview
        tvLblWidgetPreview = view.findViewById(R.id.tv_lbl_widget_preview)
        framePreviewWrapper = view.findViewById(R.id.frame_preview_wrapper)
        ivPreviewWidgetBg = view.findViewById(R.id.iv_preview_widget_bg)
        tvPreviewTitle = view.findViewById(R.id.tv_preview_title)
        tvPreviewSub = view.findViewById(R.id.tv_preview_sub)
    }

    // -------------------------------------------------------------
    // 1. APP BACKGROUND SETUP (5 BOXES WITH TICK)
    // -------------------------------------------------------------
    private fun setupAppBoxes(view: View) {
        appBoxes[ThemeManager.APP_COLOR_ANTIQUE_SANDAL] = Triple(
            view.findViewById(R.id.box_app_antique_sandal),
            view.findViewById(R.id.tv_app_antique_sandal),
            view.findViewById(R.id.tick_app_antique_sandal)
        )
        appBoxes[ThemeManager.APP_COLOR_BRIGHT_LEMON] = Triple(
            view.findViewById(R.id.box_app_bright_lemon),
            view.findViewById(R.id.tv_app_bright_lemon),
            view.findViewById(R.id.tick_app_bright_lemon)
        )
        appBoxes[ThemeManager.APP_COLOR_MIDNIGHT_VIOLET] = Triple(
            view.findViewById(R.id.box_app_midnight_violet),
            view.findViewById(R.id.tv_app_midnight_violet),
            view.findViewById(R.id.tick_app_midnight_violet)
        )
        appBoxes[ThemeManager.APP_COLOR_OFF_WHITE] = Triple(
            view.findViewById(R.id.box_app_off_white),
            view.findViewById(R.id.tv_app_off_white),
            view.findViewById(R.id.tick_app_off_white)
        )
        appBoxes[ThemeManager.APP_COLOR_AMOLED_BLACK] = Triple(
            view.findViewById(R.id.box_app_amoled_black),
            view.findViewById(R.id.tv_app_amoled_black),
            view.findViewById(R.id.tick_app_amoled_black)
        )

        appBoxes.forEach { (colorHex, triple) ->
            triple.first.setOnClickListener {
                val ctx = context ?: return@setOnClickListener
                ThemeManager.saveAppBackgroundColor(ctx, colorHex)
                applyDynamicTheming()
                (activity as? MainActivity)?.loadDayData()
            }
        }
    }

    private fun renderAppBoxes() {
        val context = context ?: return
        val currentAppBg = ThemeManager.getAppBackgroundColor(context)

        appBoxes.forEach { (hex, triple) ->
            val (container, text, tick) = triple
            val isSelected = hex.equals(currentAppBg, ignoreCase = true)
            val baseColor = Color.parseColor(hex)
            val isDarkCard = hex == ThemeManager.APP_COLOR_AMOLED_BLACK || hex == ThemeManager.APP_COLOR_MIDNIGHT_VIOLET
            val contentColor = if (isDarkCard) Color.WHITE else Color.parseColor("#2C1A14")

            text.setTextColor(contentColor)
            tick.setTextColor(contentColor)
            tick.visibility = if (isSelected) View.VISIBLE else View.GONE

            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 14f * context.resources.displayMetrics.density
                setColor(baseColor)
                if (isSelected) {
                    val strokeColor = ContextCompat.getColor(context, R.color.selection_gold_indicator)
                    setStroke(6, strokeColor)
                } else {
                    val borderColor = ContextCompat.getColor(context, R.color.settings_card_border)
                    setStroke(2, borderColor)
                }
            }
            container.background = drawable
        }
    }

    // -------------------------------------------------------------
    // 2. WIDGET PRESET SETUP (6 BOXES WITH TICK)
    // -------------------------------------------------------------
    private fun setupPresetBoxes(view: View) {
        presetBoxes[ThemeManager.Preset.MIDNIGHT_GOLD] = Triple(
            view.findViewById(R.id.box_preset_midnight_gold),
            view.findViewById(R.id.tv_preset_midnight_gold),
            view.findViewById(R.id.tick_preset_midnight_gold)
        )
        presetBoxes[ThemeManager.Preset.HERITAGE_MAROON] = Triple(
            view.findViewById(R.id.box_preset_heritage_maroon),
            view.findViewById(R.id.tv_preset_heritage_maroon),
            view.findViewById(R.id.tick_preset_heritage_maroon)
        )
        presetBoxes[ThemeManager.Preset.CLASSIC_MONO] = Triple(
            view.findViewById(R.id.box_preset_classic_mono),
            view.findViewById(R.id.tv_preset_classic_mono),
            view.findViewById(R.id.tick_preset_classic_mono)
        )
        presetBoxes[ThemeManager.Preset.NEON_NIGHT] = Triple(
            view.findViewById(R.id.box_preset_neon_night),
            view.findViewById(R.id.tv_preset_neon_night),
            view.findViewById(R.id.tick_preset_neon_night)
        )
        presetBoxes[ThemeManager.Preset.PORCELAIN_BLUE] = Triple(
            view.findViewById(R.id.box_preset_porcelain_blue),
            view.findViewById(R.id.tv_preset_porcelain_blue),
            view.findViewById(R.id.tick_preset_porcelain_blue)
        )
        presetBoxes[ThemeManager.Preset.PORCELAIN_RED] = Triple(
            view.findViewById(R.id.box_preset_porcelain_red),
            view.findViewById(R.id.tv_preset_porcelain_red),
            view.findViewById(R.id.tick_preset_porcelain_red)
        )

        presetBoxes.forEach { (preset, triple) ->
            triple.first.setOnClickListener {
                val ctx = context ?: return@setOnClickListener
                ThemeManager.savePreset(ctx, preset)
                renderPresetBoxes()
                updateLiveSliderBackground()
                updateWidgetPreview()
                debounceWidgetUpdate()
            }
        }
    }

    private fun renderPresetBoxes() {
        val context = context ?: return
        val currentPreset = ThemeManager.getSelectedPreset(context)

        presetBoxes.forEach { (preset, triple) ->
            val (container, text, tick) = triple
            val isSelected = preset == currentPreset

            val bgColor = ContextCompat.getColor(context, preset.bgResId)
            val accentColor = ContextCompat.getColor(context, preset.accentResId)

            text.setTextColor(accentColor)
            tick.setTextColor(accentColor)
            tick.visibility = if (isSelected) View.VISIBLE else View.GONE

            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f * context.resources.displayMetrics.density
                setColor(bgColor)
                if (isSelected) {
                    val strokeColor = ContextCompat.getColor(context, R.color.selection_gold_indicator)
                    setStroke(6, strokeColor)
                } else {
                    val borderColor = ContextCompat.getColor(context, R.color.settings_card_border)
                    setStroke(2, borderColor)
                }
            }
            container.background = drawable
        }
    }

    // -------------------------------------------------------------
    // 3. DATE FORMAT & CLOCK FACE SWITCHER (WITH THUMBNAIL SAMPLES)
    // -------------------------------------------------------------
    private fun setupClockStyleSwitcher() {
        val ctx = context ?: return

        ivThumbDigitalClassic?.setImageBitmap(
            ClockFaceRenderer.createSettingsButtonThumbnail(ctx, ThemeManager.CLOCK_DIGITAL_CLASSIC)
        )
        ivThumbDigitalNeon?.setImageBitmap(
            ClockFaceRenderer.createSettingsButtonThumbnail(ctx, ThemeManager.CLOCK_DIGITAL_AMOLED_NEON)
        )
        ivThumbAnalogClassic?.setImageBitmap(
            ClockFaceRenderer.createSettingsButtonThumbnail(ctx, ThemeManager.CLOCK_ANALOG_CLASSIC)
        )
        ivThumbAnalogWhite?.setImageBitmap(
            ClockFaceRenderer.createSettingsButtonThumbnail(ctx, ThemeManager.CLOCK_ANALOG_AMOLED_WHITE)
        )
        ivThumbAnalogHybrid?.setImageBitmap(
            ClockFaceRenderer.createSettingsButtonThumbnail(ctx, ThemeManager.CLOCK_ANALOG_AMOLED_HYBRID)
        )

        boxDateFmtDash?.setOnClickListener {
            val c = context ?: return@setOnClickListener
            ThemeManager.saveWidgetDateFormat(c, ThemeManager.DATE_FORMAT_DASH)
            renderClockStyleSwitcher()
            debounceWidgetUpdate()
        }
        boxDateFmtText?.setOnClickListener {
            val c = context ?: return@setOnClickListener
            ThemeManager.saveWidgetDateFormat(c, ThemeManager.DATE_FORMAT_TEXT)
            renderClockStyleSwitcher()
            debounceWidgetUpdate()
        }
        boxDateFmtSlash?.setOnClickListener {
            val c = context ?: return@setOnClickListener
            ThemeManager.saveWidgetDateFormat(c, ThemeManager.DATE_FORMAT_SLASH)
            renderClockStyleSwitcher()
            debounceWidgetUpdate()
        }

        boxClockDigital?.setOnClickListener {
            val c = context ?: return@setOnClickListener
            if (ThemeManager.isAnalogClockSelected(c)) {
                ThemeManager.saveClockFace(c, ThemeManager.CLOCK_DIGITAL_CLASSIC)
                renderClockStyleSwitcher()
                debounceWidgetUpdate()
            }
        }
        boxClockAnalog?.setOnClickListener {
            val c = context ?: return@setOnClickListener
            if (!ThemeManager.isAnalogClockSelected(c)) {
                ThemeManager.saveClockFace(c, ThemeManager.CLOCK_ANALOG_AMOLED_WHITE)
                renderClockStyleSwitcher()
                debounceWidgetUpdate()
            }
        }

        cardDigitalClassic?.setOnClickListener {
            val c = context ?: return@setOnClickListener
            ThemeManager.saveClockFace(c, ThemeManager.CLOCK_DIGITAL_CLASSIC)
            renderClockStyleSwitcher()
            debounceWidgetUpdate()
        }
        cardDigitalNeon?.setOnClickListener {
            val c = context ?: return@setOnClickListener
            ThemeManager.saveClockFace(c, ThemeManager.CLOCK_DIGITAL_AMOLED_NEON)
            renderClockStyleSwitcher()
            debounceWidgetUpdate()
        }

        cardAnalogClassic?.setOnClickListener {
            val c = context ?: return@setOnClickListener
            ThemeManager.saveClockFace(c, ThemeManager.CLOCK_ANALOG_CLASSIC)
            renderClockStyleSwitcher()
            debounceWidgetUpdate()
        }
        cardAnalogWhite?.setOnClickListener {
            val c = context ?: return@setOnClickListener
            ThemeManager.saveClockFace(c, ThemeManager.CLOCK_ANALOG_AMOLED_WHITE)
            renderClockStyleSwitcher()
            debounceWidgetUpdate()
        }
        cardAnalogHybrid?.setOnClickListener {
            val c = context ?: return@setOnClickListener
            ThemeManager.saveClockFace(c, ThemeManager.CLOCK_ANALOG_AMOLED_HYBRID)
            renderClockStyleSwitcher()
            debounceWidgetUpdate()
        }

        renderClockStyleSwitcher()
    }

    private fun renderClockStyleSwitcher() {
        val context = context ?: return
        val activeFace = ThemeManager.getClockFace(context)
        val isAnalog = ThemeManager.isAnalogClockSelected(context)
        val dateFmt = ThemeManager.getWidgetDateFormat(context)
        val palette = ThemeManager.getThemePalette(context)
        val density = context.resources.displayMetrics.density
        val currentBg = ThemeManager.getAppBackgroundColor(context)
        val isDark = currentBg == ThemeManager.APP_COLOR_AMOLED_BLACK ||
                currentBg == ThemeManager.APP_COLOR_MIDNIGHT_VIOLET

        val borderCol = ContextCompat.getColor(context, R.color.settings_card_border)
        val goldCol = palette.accentGold
        val bgCol = palette.cardBackground
        val textCol = if (isDark) Color.WHITE else palette.primaryHeader

        fun stylePill(box: FrameLayout?, tv: TextView?, selected: Boolean) {
            box?.background = GradientDrawable().apply {
                cornerRadius = 12f * density
                setColor(if (selected) goldCol else bgCol)
                setStroke(2, if (selected) goldCol else borderCol)
            }
            tv?.setTextColor(if (selected) Color.BLACK else textCol)
        }

        stylePill(boxDateFmtDash, tvDateFmtDash, dateFmt == ThemeManager.DATE_FORMAT_DASH)
        stylePill(boxDateFmtText, tvDateFmtText, dateFmt == ThemeManager.DATE_FORMAT_TEXT)
        stylePill(boxDateFmtSlash, tvDateFmtSlash, dateFmt == ThemeManager.DATE_FORMAT_SLASH)

        stylePill(boxClockDigital, tvClockDigital, !isAnalog)
        tickClockDigital?.setTextColor(Color.BLACK)
        tickClockDigital?.visibility = if (!isAnalog) View.VISIBLE else View.GONE

        stylePill(boxClockAnalog, tvClockAnalog, isAnalog)
        tickClockAnalog?.setTextColor(Color.BLACK)
        tickClockAnalog?.visibility = if (isAnalog) View.VISIBLE else View.GONE

        groupDigitalFaces?.visibility = if (!isAnalog) View.VISIBLE else View.GONE
        groupAnalogFaces?.visibility = if (isAnalog) View.VISIBLE else View.GONE

        fun styleFaceCard(card: LinearLayout?, label: TextView?, selected: Boolean) {
            card?.background = GradientDrawable().apply {
                cornerRadius = 14f * density
                setColor(if (selected) Color.parseColor("#28B26A00") else bgCol)
                setStroke(if (selected) 5 else 2, if (selected) goldCol else borderCol)
            }
            label?.setTextColor(if (selected) goldCol else textCol)
        }

        styleFaceCard(cardDigitalClassic, tvLabelDigitalClassic, activeFace == ThemeManager.CLOCK_DIGITAL_CLASSIC)
        styleFaceCard(cardDigitalNeon, tvLabelDigitalNeon, activeFace == ThemeManager.CLOCK_DIGITAL_AMOLED_NEON)
        styleFaceCard(cardAnalogClassic, tvLabelAnalogClassic, activeFace == ThemeManager.CLOCK_ANALOG_CLASSIC)
        styleFaceCard(cardAnalogWhite, tvLabelAnalogWhite, activeFace == ThemeManager.CLOCK_ANALOG_AMOLED_WHITE)
        styleFaceCard(cardAnalogHybrid, tvLabelAnalogHybrid, activeFace == ThemeManager.CLOCK_ANALOG_AMOLED_HYBRID)
    }

    // -------------------------------------------------------------
    // 4. OUTER BORDER SWITCH, LIVE HORAI/GOWRI & OPACITY SLIDER
    // -------------------------------------------------------------
    private fun setupBorderSwitch() {
        val ctx = context ?: return
        switchOuterBorder?.isChecked = ThemeManager.isBorderEnabled(ctx)

        switchOuterBorder?.setOnCheckedChangeListener { _, isChecked ->
            val c = context ?: return@setOnCheckedChangeListener
            ThemeManager.saveBorderEnabled(c, isChecked)
            updateWidgetPreview()
            debounceWidgetUpdate()
        }
    }

    private fun setupLiveHoraiGowriSwitch() {
        val ctx = context ?: return
        switchLiveHoraiGowri?.isChecked = ThemeManager.isLiveHoraiGowriEnabled(ctx)

        switchLiveHoraiGowri?.setOnCheckedChangeListener { _, isChecked ->
            val c = context ?: return@setOnCheckedChangeListener
            ThemeManager.saveLiveHoraiGowriEnabled(c, isChecked)
            debounceWidgetUpdate(immediate = true)
        }
    }

    private fun setupOpacitySeekBar() {
        val ctx = context ?: return
        val currentOpacity = ThemeManager.getWidgetOpacity(ctx)

        seekBarOpacity?.progress = currentOpacity
        tvWidgetOpacityVal?.text = "$currentOpacity%"
        updateLiveSliderBackground()

        seekBarOpacity?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val clamped = progress.coerceIn(0, 100)
                tvWidgetOpacityVal?.text = "$clamped%"
                tvPreviewSub?.text = "நேரலை மாதிரி • $clamped%"
                updateLiveSliderBackground()

                if (fromUser) {
                    val c = context ?: return
                    ThemeManager.saveWidgetOpacity(c, clamped)
                    updateWidgetPreview()
                    debounceWidgetUpdate()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                debounceWidgetUpdate(immediate = true)
            }
        })
    }

    private fun updateLiveSliderBackground() {
        val context = context ?: return
        val themeConfig = ThemeManager.getActiveTheme(context)
        val density = context.resources.displayMetrics.density

        val alphaInt = ((themeConfig.opacityPercent / 100f).coerceIn(0.15f, 1f) * 255).toInt()
        val baseBg = themeConfig.resolvedBackgroundColor
        val liveColor = Color.argb(alphaInt, Color.red(baseBg), Color.green(baseBg), Color.blue(baseBg))

        val bg = GradientDrawable().apply {
            cornerRadius = 16f * density
            setColor(liveColor)
            setStroke(2, themeConfig.accentColor)
        }
        cardSliderCapsule?.background = bg
    }

    // -------------------------------------------------------------
    // 5. TABLE FORMAT: 4x2 SELECTION LOGIC (UP TO 3)
    // -------------------------------------------------------------
    private fun renderElementsTable4x2() {
        val context = context ?: return
        val container = tableElements4x2 ?: return
        val currentSet = ThemeManager.getElements4x2(context).toMutableSet()
        container.removeAllViews()

        if (currentSet.size > 3) {
            val trimmed = currentSet.take(3).toMutableSet()
            currentSet.clear()
            currentSet.addAll(trimmed)
            ThemeManager.saveElements4x2(context, currentSet)
        }

        tvCountElements4x2?.text = "(${currentSet.size}/3)"
        val palette = ThemeManager.getThemePalette(context)
        val density = context.resources.displayMetrics.density
        val currentBg = ThemeManager.getAppBackgroundColor(context)
        val isDark = currentBg == ThemeManager.APP_COLOR_AMOLED_BLACK ||
                currentBg == ThemeManager.APP_COLOR_MIDNIGHT_VIOLET
        val normalTextCol = if (isDark) Color.WHITE else palette.primaryHeader

        astrologicalOptions.forEachIndexed { index, (key, label) ->
            val isSelected = currentSet.contains(key)
            val row = createTableRow(context, label, isSelected, palette.accentGold, normalTextCol, density) {
                if (!isSelected) {
                    if (currentSet.size >= 3) {
                        Toast.makeText(
                            context,
                            "4x2 விட்ஜெட்டுக்கு அதிகபட்சம் 3 கூறுகளை மட்டுமே தேர்வு செய்யலாம்",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@createTableRow
                    }
                    currentSet.add(key)
                } else {
                    if (currentSet.size <= 1) {
                        Toast.makeText(
                            context,
                            "குறைந்தது ஒரு கூறு தேர்ந்தெடுக்கப்பட வேண்டும்",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@createTableRow
                    }
                    currentSet.remove(key)
                }

                tvCountElements4x2?.text = "(${currentSet.size}/3)"
                ThemeManager.saveElements4x2(context, currentSet)
                renderElementsTable4x2()
                debounceWidgetUpdate()
            }
            container.addView(row)

            if (index < astrologicalOptions.size - 1) {
                val divider = View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        (1 * density).toInt()
                    )
                    setBackgroundColor(Color.parseColor("#15888888"))
                }
                container.addView(divider)
            }
        }
    }

    // -------------------------------------------------------------
    // 6. TABLE FORMAT: 4x4 SINGLE-ROW SELECTION LOGIC (UP TO 3 OF 10)
    // -------------------------------------------------------------
    private fun renderElementsTable4x4() {
        val context = context ?: return
        val container = tableElements4x4 ?: return
        val rawSelected = ThemeManager.getSelectedOptions4x4(context).distinct()
        val selectedList = if (rawSelected.size > 3) {
            val trimmed = rawSelected.take(3)
            ThemeManager.saveSelectedOptions4x4(context, trimmed)
            trimmed.toMutableList()
        } else {
            rawSelected.toMutableList()
        }

        container.removeAllViews()
        tvCountElements4x4?.text = "(${selectedList.size}/3)"

        val palette = ThemeManager.getThemePalette(context)
        val density = context.resources.displayMetrics.density
        val currentBg = ThemeManager.getAppBackgroundColor(context)
        val isDark = currentBg == ThemeManager.APP_COLOR_AMOLED_BLACK ||
                currentBg == ThemeManager.APP_COLOR_MIDNIGHT_VIOLET
        val normalTextCol = if (isDark) Color.WHITE else palette.primaryHeader

        singleRow4x4Options.forEachIndexed { index, (key, label) ->
            val isSelected = selectedList.contains(key)
            val row = createTableRow(context, label, isSelected, palette.accentGold, normalTextCol, density) {
                if (!isSelected) {
                    if (selectedList.size >= 3) {
                        Toast.makeText(
                            context,
                            "4x4 விட்ஜெட்டுக்கு அதிகபட்சம் 3 வரிசைகளை மட்டுமே தேர்வு செய்யலாம்",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@createTableRow
                    }
                    selectedList.add(key)
                } else {
                    if (selectedList.size <= 1) {
                        Toast.makeText(
                            context,
                            "குறைந்தது ஒரு வரிசை தேர்ந்தெடுக்கப்பட வேண்டும்",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@createTableRow
                    }
                    selectedList.remove(key)
                }

                ThemeManager.saveSelectedOptions4x4(context, selectedList.toList())
                tvCountElements4x4?.text = "(${selectedList.size}/3)"
                renderElementsTable4x4()
                debounceWidgetUpdate()
            }
            container.addView(row)

            if (index < singleRow4x4Options.size - 1) {
                val divider = View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        (1 * density).toInt()
                    )
                    setBackgroundColor(Color.parseColor("#15888888"))
                }
                container.addView(divider)
            }
        }
    }

    private fun createTableRow(
        context: Context,
        label: String,
        isSelected: Boolean,
        accentColor: Int,
        normalTextColor: Int,
        density: Float,
        onClick: () -> Unit
    ): View {
        val rowLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding((12 * density).toInt(), (10 * density).toInt(), (12 * density).toInt(), (10 * density).toInt())
            isClickable = true
            isFocusable = true
            background = GradientDrawable().apply {
                cornerRadius = 8f * density
                setColor(if (isSelected) Color.parseColor("#18B26A00") else Color.TRANSPARENT)
            }
            setOnClickListener { onClick() }
        }

        val tvLabel = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = label
            textSize = 13.5f
            setTextColor(if (isSelected) accentColor else normalTextColor)
            if (isSelected) setTypeface(typeface, Typeface.BOLD)
        }
        rowLayout.addView(tvLabel)

        val tvTick = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "✓"
            textSize = 15f
            setTextColor(accentColor)
            setTypeface(typeface, Typeface.BOLD)
            visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
        }
        rowLayout.addView(tvTick)

        return rowLayout
    }

    private fun debounceWidgetUpdate(immediate: Boolean = false) {
        updateRunnable?.let { debounceHandler.removeCallbacks(it) }
        val action = Runnable {
            val ctx = context ?: return@Runnable
            if (isAdded) {
                WidgetUpdateHelper.updateAllWidgets(ctx)
            }
        }
        updateRunnable = action
        if (immediate) {
            debounceHandler.post(action)
        } else {
            debounceHandler.postDelayed(action, 250)
        }
    }

    // -------------------------------------------------------------
    // 7. THEME RESOLUTION & PREVIEW
    // -------------------------------------------------------------
    private fun applyDynamicTheming() {
        val context = context ?: return
        val palette = ThemeManager.getThemePalette(context)
        val currentBg = ThemeManager.getAppBackgroundColor(context)
        val isDark = currentBg == ThemeManager.APP_COLOR_AMOLED_BLACK ||
                currentBg == ThemeManager.APP_COLOR_MIDNIGHT_VIOLET

        rootScrollView?.setBackgroundColor(palette.pageBackground)
        framePreviewWrapper?.setBackgroundColor(palette.cardBackground)

        val headingColor = if (isDark) Color.WHITE else palette.primaryHeader
        val labelColor = if (isDark) Color.WHITE else palette.secondaryHeader

        tvHeading?.setTextColor(headingColor)
        tvLblAppTheme?.setTextColor(labelColor)
        tvLblWidgetTheme?.setTextColor(labelColor)
        tvLblDateFormat?.setTextColor(labelColor)
        tvLblClockStyle?.setTextColor(labelColor)
        tvLblOuterBorder?.setTextColor(labelColor)
        tvLblLiveHoraiGowri?.setTextColor(labelColor)
        tvLblElements4x2?.setTextColor(labelColor)
        tvLblElements4x4?.setTextColor(labelColor)
        tvLblWidgetPreview?.setTextColor(labelColor)

        switchOuterBorder?.thumbTintList = ColorStateList.valueOf(palette.accentGold)
        switchOuterBorder?.trackTintList = ColorStateList.valueOf(Color.parseColor("#80B26A00"))

        switchLiveHoraiGowri?.thumbTintList = ColorStateList.valueOf(palette.accentGold)
        switchLiveHoraiGowri?.trackTintList = ColorStateList.valueOf(Color.parseColor("#80B26A00"))
        switchLiveHoraiGowri?.isChecked = ThemeManager.isLiveHoraiGowriEnabled(context)

        seekBarOpacity?.thumbTintList = ColorStateList.valueOf(palette.accentGold)
        seekBarOpacity?.progressTintList = ColorStateList.valueOf(palette.accentGold)

        renderAppBoxes()
        renderPresetBoxes()
        renderClockStyleSwitcher()
        updateLiveSliderBackground()
        renderElementsTable4x2()
        renderElementsTable4x4()
        updateWidgetPreview()
    }

    private fun updateWidgetPreview() {
        val context = context ?: return
        val themeConfig = ThemeManager.getActiveTheme(context)
        val bgBitmap = WidgetUpdateHelper.createWidgetBackgroundBitmap(context, 480, 140, themeConfig)
        ivPreviewWidgetBg?.setImageBitmap(bgBitmap)

        val isWhiteCanvas = themeConfig.preset == ThemeManager.Preset.PORCELAIN_BLUE ||
                themeConfig.preset == ThemeManager.Preset.PORCELAIN_RED

        val previewTextColor = if (isWhiteCanvas) themeConfig.accentColor else Color.WHITE
        val previewSubColor = if (isWhiteCanvas) Color.parseColor("#424242") else themeConfig.accentColor

        tvPreviewTitle?.setTextColor(previewTextColor)
        tvPreviewSub?.setTextColor(previewSubColor)
    }
}
