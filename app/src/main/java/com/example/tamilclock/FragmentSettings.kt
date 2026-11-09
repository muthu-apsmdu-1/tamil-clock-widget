package com.example.tamilclock

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class FragmentSettings : Fragment() {

    private var rootScrollView: ScrollView? = null
    private var tvHeading: TextView? = null
    private var tvLblAppTheme: TextView? = null

    private var tvLblWidgetTheme: TextView? = null
    private var tvLblOuterBorder: TextView? = null
    private var switchOuterBorder: SwitchCompat? = null

    private var tvLblWidgetOpacity: TextView? = null
    private var tvWidgetOpacityVal: TextView? = null
    private var seekBarOpacity: SeekBar? = null

    // 4x2 and 4x4 Astrological Elements Selectors
    private var tvLblElements4x2: TextView? = null
    private var tvCountElements4x2: TextView? = null
    private var chipGroupElements4x2: ChipGroup? = null

    private var tvLblElements4x4: TextView? = null
    private var tvCountElements4x4: TextView? = null
    private var chipGroupElements4x4: ChipGroup? = null

    // Preview views
    private var tvLblWidgetPreview: TextView? = null
    private var framePreviewWrapper: FrameLayout? = null
    private var ivPreviewWidgetBg: ImageView? = null
    private var tvPreviewTitle: TextView? = null
    private var tvPreviewSub: TextView? = null

    // 5 App Background Theme Boxes: Pair<BoxContainer, LabelTextView>
    private val appBoxes = mutableMapOf<String, Pair<FrameLayout, TextView>>()

    // 6 Widget Preset Boxes: Pair<BoxContainer, LabelTextView>
    private val presetBoxes = mutableMapOf<ThemeManager.Preset, Pair<FrameLayout, TextView>>()

    // Debounce handler for IPC updates
    private val debounceHandler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    // Astrological Elements Available
    private val astrologicalOptions = listOf(
        Pair(ThemeManager.ELEM_PAKSHAM, "பக்ஷம்"),
        Pair(ThemeManager.ELEM_THITHI, "திதி"),
        Pair(ThemeManager.ELEM_NAKSHATRA, "நட்சத்திரம்"),
        Pair(ThemeManager.ELEM_KARNAM, "கரணம்"),
        Pair(ThemeManager.ELEM_THYAJAM, "தியாகம்"),
        Pair(ThemeManager.ELEM_NALLA_NERAM, "நல்ல நேரம்"),
        Pair(ThemeManager.ELEM_GOWRI_NALLA_NERAM, "கௌரி நல்ல நேரம்"),
        Pair(ThemeManager.ELEM_SOOLAM, "சூலம்"),
        Pair(ThemeManager.ELEM_SUNRISE_SET, "உதயம்/மறைவு")
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
        setupBorderSwitch()
        setupOpacitySeekBar()
        setupElements4x2()
        setupElements4x4()
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
        tvLblOuterBorder = view.findViewById(R.id.tv_lbl_outer_border)
        switchOuterBorder = view.findViewById(R.id.switch_outer_border)

        tvLblWidgetOpacity = view.findViewById(R.id.tv_lbl_widget_opacity)
        tvWidgetOpacityVal = view.findViewById(R.id.tv_widget_opacity_val)
        seekBarOpacity = view.findViewById(R.id.seekbar_widget_opacity)

        tvLblElements4x2 = view.findViewById(R.id.tv_lbl_elements_4x2)
        tvCountElements4x2 = view.findViewById(R.id.tv_count_elements_4x2)
        chipGroupElements4x2 = view.findViewById(R.id.chip_group_elements_4x2)

        tvLblElements4x4 = view.findViewById(R.id.tv_lbl_elements_4x4)
        tvCountElements4x4 = view.findViewById(R.id.tv_count_elements_4x4)
        chipGroupElements4x4 = view.findViewById(R.id.chip_group_elements_4x4)

        tvLblWidgetPreview = view.findViewById(R.id.tv_lbl_widget_preview)
        framePreviewWrapper = view.findViewById(R.id.frame_preview_wrapper)
        ivPreviewWidgetBg = view.findViewById(R.id.iv_preview_widget_bg)
        tvPreviewTitle = view.findViewById(R.id.tv_preview_title)
        tvPreviewSub = view.findViewById(R.id.tv_preview_sub)
    }

    // -------------------------------------------------------------
    // 1. APP BACKGROUND 5-BOX SETUP
    // -------------------------------------------------------------
    private fun setupAppBoxes(view: View) {
        appBoxes[ThemeManager.APP_COLOR_ANTIQUE_SANDAL] = Pair(
            view.findViewById(R.id.box_app_antique_sandal),
            view.findViewById(R.id.tv_app_antique_sandal)
        )
        appBoxes[ThemeManager.APP_COLOR_BRIGHT_LEMON] = Pair(
            view.findViewById(R.id.box_app_bright_lemon),
            view.findViewById(R.id.tv_app_bright_lemon)
        )
        appBoxes[ThemeManager.APP_COLOR_MIDNIGHT_VIOLET] = Pair(
            view.findViewById(R.id.box_app_midnight_violet),
            view.findViewById(R.id.tv_app_midnight_violet)
        )
        appBoxes[ThemeManager.APP_COLOR_OFF_WHITE] = Pair(
            view.findViewById(R.id.box_app_off_white),
            view.findViewById(R.id.tv_app_off_white)
        )
        appBoxes[ThemeManager.APP_COLOR_AMOLED_BLACK] = Pair(
            view.findViewById(R.id.box_app_amoled_black),
            view.findViewById(R.id.tv_app_amoled_black)
        )

        appBoxes.forEach { (colorHex, pair) ->
            pair.first.setOnClickListener {
                ThemeManager.saveAppBackgroundColor(requireContext(), colorHex)
                applyDynamicTheming()
                (activity as? MainActivity)?.loadDayData()
            }
        }
    }

    private fun renderAppBoxes() {
        val context = context ?: return
        val currentAppBg = ThemeManager.getAppBackgroundColor(context)

        appBoxes.forEach { (hex, pair) ->
            val (container, text) = pair
            val isSelected = hex.equals(currentAppBg, ignoreCase = true)
            val baseColor = Color.parseColor(hex)

            val isDarkCard = hex == ThemeManager.APP_COLOR_AMOLED_BLACK || hex == ThemeManager.APP_COLOR_MIDNIGHT_VIOLET
            text.setTextColor(if (isDarkCard) Color.WHITE else Color.parseColor("#2C1A14"))

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
    // 2. WIDGET 6-PRESET SETUP
    // -------------------------------------------------------------
    private fun setupPresetBoxes(view: View) {
        presetBoxes[ThemeManager.Preset.MIDNIGHT_GOLD] = Pair(
            view.findViewById(R.id.box_preset_midnight_gold),
            view.findViewById(R.id.tv_preset_midnight_gold)
        )
        presetBoxes[ThemeManager.Preset.HERITAGE_MAROON] = Pair(
            view.findViewById(R.id.box_preset_heritage_maroon),
            view.findViewById(R.id.tv_preset_heritage_maroon)
        )
        presetBoxes[ThemeManager.Preset.CLASSIC_MONO] = Pair(
            view.findViewById(R.id.box_preset_classic_mono),
            view.findViewById(R.id.tv_preset_classic_mono)
        )
        presetBoxes[ThemeManager.Preset.NEON_NIGHT] = Pair(
            view.findViewById(R.id.box_preset_neon_night),
            view.findViewById(R.id.tv_preset_neon_night)
        )
        presetBoxes[ThemeManager.Preset.PORCELAIN_BLUE] = Pair(
            view.findViewById(R.id.box_preset_porcelain_blue),
            view.findViewById(R.id.tv_preset_porcelain_blue)
        )
        presetBoxes[ThemeManager.Preset.PORCELAIN_RED] = Pair(
            view.findViewById(R.id.box_preset_porcelain_red),
            view.findViewById(R.id.tv_preset_porcelain_red)
        )

        presetBoxes.forEach { (preset, pair) ->
            pair.first.setOnClickListener {
                val context = requireContext()
                ThemeManager.savePreset(context, preset)
                renderPresetBoxes()
                updateWidgetPreview()
                debounceWidgetUpdate()
            }
        }
    }

    private fun renderPresetBoxes() {
        val context = context ?: return
        val currentPreset = ThemeManager.getSelectedPreset(context)

        presetBoxes.forEach { (preset, pair) ->
            val (container, text) = pair
            val isSelected = preset == currentPreset

            val bgColor = ContextCompat.getColor(context, preset.bgResId)
            val accentColor = ContextCompat.getColor(context, preset.accentResId)

            text.setTextColor(accentColor)

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
    // 3. OUTER BORDER SWITCH & OPACITY SLIDER
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

        seekBarOpacity?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val clamped = progress.coerceIn(0, 100)
                tvWidgetOpacityVal?.text = "$clamped%"
                tvPreviewSub?.text = "நேரலை மாதிரி • $clamped%"
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

    // -------------------------------------------------------------
    // 4. 4x2 SELECTION LOGIC (EXACTLY 3)
    // -------------------------------------------------------------
    private fun setupElements4x2() {
        val context = context ?: return
        val currentSet = ThemeManager.getElements4x2(context).toMutableSet()
        val group = chipGroupElements4x2 ?: return
        group.removeAllViews()

        tvCountElements4x2?.text = "(${currentSet.size}/3)"

        astrologicalOptions.forEach { (key, label) ->
            val chip = Chip(context).apply {
                text = label
                isCheckable = true
                isChecked = currentSet.contains(key)
                tag = key
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        if (currentSet.size >= 3) {
                            this.isChecked = false
                            Toast.makeText(context, "4x2 விட்ஜெட்டுக்கு அதிகபட்சம் 3 கூறுகளை மட்டுமே தேர்வு செய்யலாம்", Toast.LENGTH_SHORT).show()
                            return@setOnCheckedChangeListener
                        }
                        currentSet.add(key)
                    } else {
                        if (currentSet.size <= 1) {
                            this.isChecked = true
                            Toast.makeText(context, "குறைந்தது ஒரு கூறு தேர்ந்தெடுக்கப்பட வேண்டும்", Toast.LENGTH_SHORT).show()
                            return@setOnCheckedChangeListener
                        }
                        currentSet.remove(key)
                    }
                    tvCountElements4x2?.text = "(${currentSet.size}/3)"
                    ThemeManager.saveElements4x2(context, currentSet)
                    debounceWidgetUpdate()
                }
            }
            group.addView(chip)
        }
    }

    // -------------------------------------------------------------
    // 5. 4x4 SELECTION LOGIC (EXACTLY 5)
    // -------------------------------------------------------------
    private fun setupElements4x4() {
        val context = context ?: return
        val currentSet = ThemeManager.getElements4x4(context).toMutableSet()
        val group = chipGroupElements4x4 ?: return
        group.removeAllViews()

        tvCountElements4x4?.text = "(${currentSet.size}/5)"

        astrologicalOptions.forEach { (key, label) ->
            val chip = Chip(context).apply {
                text = label
                isCheckable = true
                isChecked = currentSet.contains(key)
                tag = key
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        if (currentSet.size >= 5) {
                            this.isChecked = false
                            Toast.makeText(context, "4x4 விட்ஜெட்டுக்கு அதிகபட்சம் 5 கூறுகளை மட்டுமே தேர்வு செய்யலாம்", Toast.LENGTH_SHORT).show()
                            return@setOnCheckedChangeListener
                        }
                        currentSet.add(key)
                    } else {
                        if (currentSet.size <= 1) {
                            this.isChecked = true
                            Toast.makeText(context, "குறைந்தது ஒரு கூறு தேர்ந்தெடுக்கப்பட வேண்டும்", Toast.LENGTH_SHORT).show()
                            return@setOnCheckedChangeListener
                        }
                        currentSet.remove(key)
                    }
                    tvCountElements4x4?.text = "(${currentSet.size}/5)"
                    ThemeManager.saveElements4x4(context, currentSet)
                    debounceWidgetUpdate()
                }
            }
            group.addView(chip)
        }
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
    // 6. THEME RESOLUTION & PREVIEW
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
