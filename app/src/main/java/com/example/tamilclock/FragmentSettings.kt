package com.example.tamilclock

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
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
    private var tvLblClockStyle: TextView? = null
    private var tvLblOuterBorder: TextView? = null
    private var switchOuterBorder: SwitchCompat? = null

    private var cardSliderCapsule: LinearLayout? = null
    private var tvLblWidgetOpacity: TextView? = null
    private var tvWidgetOpacityVal: TextView? = null
    private var seekBarOpacity: SeekBar? = null

    // Clock style switcher
    private var boxClockDigital: FrameLayout? = null
    private var tvClockDigital: TextView? = null
    private var tickClockDigital: TextView? = null
    private var boxClockAnalog: FrameLayout? = null
    private var tvClockAnalog: TextView? = null
    private var tickClockAnalog: TextView? = null

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

    // 12 Astrological Elements ordered strictly by priority
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
        setupOpacitySeekBar()
        renderElementsTable4x2()
        renderElementsTable4x4()
        applyDynamicTheming()
    }

    override fun onResume() {
        super.onResume()
        applyDynamicTheming()
    }

    override fun onDestroyView() {
        updateRunnable?.let { debounceHandler.removeCallbacks(it) }
        appBoxes.clear()
        presetBoxes.clear()
        super.onDestroyView()
    }

    private fun bindViews(view: View) {
        rootScrollView = view.findViewById(R.id.scroll_settings_root)
        tvHeading = view.findViewById(R.id.tv_settings_main_heading)
        tvLblAppTheme = view.findViewById(R.id.tv_lbl_app_theme)

        tvLblWidgetTheme = view.findViewById(R.id.tv_lbl_widget_theme)
        tvLblClockStyle = view.findViewById(R.id.tv_lbl_clock_style)
        tvLblOuterBorder = view.findViewById(R.id.tv_lbl_outer_border)
        switchOuterBorder = view.findViewById(R.id.switch_outer_border)

        cardSliderCapsule = view.findViewById(R.id.card_slider_capsule)
        tvLblWidgetOpacity = view.findViewById(R.id.tv_lbl_widget_opacity)
        tvWidgetOpacityVal = view.findViewById(R.id.tv_widget_opacity_val)
        seekBarOpacity = view.findViewById(R.id.seekbar_widget_opacity)

        boxClockDigital = view.findViewById(R.id.box_clock_digital)
        tvClockDigital = view.findViewById(R.id.tv_clock_digital)
        tickClockDigital = view.findViewById(R.id.tick_clock_digital)
        boxClockAnalog = view.findViewById(R.id.box_clock_analog)
        tvClockAnalog = view.findViewById(R.id.tv_clock_analog)
        tickClockAnalog = view.findViewById(R.id.tick_clock_analog)

        tvLblElements4x2 = view.findViewById(R.id.tv_lbl_elements_4x2)
        tvCountElements4x2 = view.findViewById(R.id.tv_count_elements_4x2)
        tableElements4x2 = view.findViewById(R.id.table_elements_4x2)

        tvLblElements4x4 = view.findViewById(R.id.tv_lbl_elements_4x4)
        tvCountElements4x4 = view.findViewById(R.id.tv_count_elements_4x4)
        tableElements4x4 = view.findViewById(R.id.table_elements_4x4)

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
                ThemeManager.saveAppBackgroundColor(requireContext(), colorHex)
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
                val context = requireContext()
                ThemeManager.savePreset(context, preset)
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
    // 3. CLOCK STYLE SWITCHER (DIGITAL vs ANALOG)
    // -------------------------------------------------------------
    private fun setupClockStyleSwitcher() {
        boxClockDigital?.setOnClickListener {
            val context = requireContext()
            ThemeManager.saveClockFace(context, ThemeManager.CLOCK_DIGITAL)
            renderClockStyleSwitcher()
            debounceWidgetUpdate()
        }
        boxClockAnalog?.setOnClickListener {
            val context = requireContext()
            ThemeManager.saveClockFace(context, ThemeManager.CLOCK_ANALOG)
            renderClockStyleSwitcher()
            debounceWidgetUpdate()
        }
        renderClockStyleSwitcher()
    }

    private fun renderClockStyleSwitcher() {
        val context = context ?: return
        val currentStyle = ThemeManager.getClockFace(context)
        val palette = ThemeManager.getThemePalette(context)
        val density = context.resources.displayMetrics.density
        val currentBg = ThemeManager.getAppBackgroundColor(context)
        val isDark = currentBg == ThemeManager.APP_COLOR_AMOLED_BLACK ||
                     currentBg == ThemeManager.APP_COLOR_MIDNIGHT_VIOLET

        val borderCol = ContextCompat.getColor(context, R.color.settings_card_border)
        val goldCol = palette.accentGold
        val bgCol = palette.cardBackground
        val textCol = if (isDark) Color.WHITE else palette.primaryHeader

        val isDigital = currentStyle == ThemeManager.CLOCK_DIGITAL

        // Digital Box
        boxClockDigital?.background = GradientDrawable().apply {
            cornerRadius = 12f * density
            setColor(if (isDigital) goldCol else bgCol)
            setStroke(2, if (isDigital) goldCol else borderCol)
        }
        tvClockDigital?.setTextColor(if (isDigital) Color.BLACK else textCol)
        tickClockDigital?.setTextColor(if (isDigital) Color.BLACK else textCol)
        tickClockDigital?.visibility = if (isDigital) View.VISIBLE else View.GONE

        // Analog Box
        val isAnalog = !isDigital
        boxClockAnalog?.background = GradientDrawable().apply {
            cornerRadius = 12f * density
            setColor(if (isAnalog) goldCol else bgCol)
            setStroke(2, if (isAnalog) goldCol else borderCol)
        }
        tvClockAnalog?.setTextColor(if (isAnalog) Color.BLACK else textCol)
        tickClockAnalog?.setTextColor(if (isAnalog) Color.BLACK else textCol)
        tickClockAnalog?.visibility = if (isAnalog) View.VISIBLE else View.GONE
    }

    // -------------------------------------------------------------
    // 4. OUTER BORDER SWITCH & LIVE OPACITY SLIDER
    // -------------------------------------------------------------
    private fun setupBorderSwitch() {
        val context = context ?: return
        val borderEnabled = ThemeManager.isBorderEnabled(context)
        switchOuterBorder?.isChecked = borderEnabled

        switchOuterBorder?.setOnCheckedChangeListener { _, isChecked ->
            ThemeManager.saveBorderEnabled(requireContext(), isChecked)
            updateWidgetPreview()
            debounceWidgetUpdate()
        }
    }

    private fun setupOpacitySeekBar() {
        val context = context ?: return
        val currentOpacity = ThemeManager.getWidgetOpacity(context)

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
                    ThemeManager.saveWidgetOpacity(requireContext(), clamped)
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
    // 5. TABLE FORMAT: 4x2 SELECTION LOGIC (EXACTLY 3)
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
    // 6. TABLE FORMAT: 4x4 SELECTION LOGIC (EXACTLY 5)
    // -------------------------------------------------------------
    private fun renderElementsTable4x4() {
        val context = context ?: return
        val container = tableElements4x4 ?: return
        val currentSet = ThemeManager.getElements4x4(context).toMutableSet()
        container.removeAllViews()

        if (currentSet.size > 5) {
            val trimmed = currentSet.take(5).toMutableSet()
            currentSet.clear()
            currentSet.addAll(trimmed)
            ThemeManager.saveElements4x4(context, currentSet)
        }

        tvCountElements4x4?.text = "(${currentSet.size}/5)"
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
                    if (currentSet.size >= 5) {
                        Toast.makeText(
                            context,
                            "4x4 விட்ஜெட்டுக்கு அதிகபட்சம் 5 கூறுகளை மட்டுமே தேர்வு செய்யலாம்",
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

                tvCountElements4x4?.text = "(${currentSet.size}/5)"
                ThemeManager.saveElements4x4(context, currentSet)
                renderElementsTable4x4()
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
            if (isSelected) setTypeface(typeface, android.graphics.Typeface.BOLD)
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
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
        }
        rowLayout.addView(tvTick)

        return rowLayout
    }

    private fun debounceWidgetUpdate(immediate: Boolean = false) {
        updateRunnable?.let { debounceHandler.removeCallbacks(it) }
        val action = Runnable {
            val ctx = context ?: return@Runnable
            WidgetUpdateHelper.updateAllWidgets(ctx)
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
        tvLblClockStyle?.setTextColor(labelColor)
        tvLblOuterBorder?.setTextColor(labelColor)
        tvLblElements4x2?.setTextColor(labelColor)
        tvLblElements4x4?.setTextColor(labelColor)
        tvLblWidgetPreview?.setTextColor(labelColor)

        switchOuterBorder?.thumbTintList = ColorStateList.valueOf(palette.accentGold)
        switchOuterBorder?.trackTintList = ColorStateList.valueOf(Color.parseColor("#80B26A00"))

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
