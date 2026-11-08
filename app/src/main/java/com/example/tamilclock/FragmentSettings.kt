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
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class FragmentSettings : Fragment() {

    private var rootScrollView: ScrollView? = null
    private var tvHeading: TextView? = null
    private var tvLblAppTheme: TextView? = null
    private var rgAppThemes: RadioGroup? = null

    private var tvLblWidgetTheme: TextView? = null
    private var tvLblOuterBorder: TextView? = null
    private var switchOuterBorder: SwitchCompat? = null

    private var tvLblWidgetOpacity: TextView? = null
    private var tvWidgetOpacityVal: TextView? = null
    private var seekBarOpacity: SeekBar? = null

    private var tvLblWidgetPreview: TextView? = null
    private var framePreviewWrapper: FrameLayout? = null
    private var ivPreviewWidgetBg: ImageView? = null
    private var tvPreviewTitle: TextView? = null
    private var tvPreviewSub: TextView? = null

    // 6 Preset Containers and Labels
    private val presetBoxes = mutableMapOf<ThemeManager.Preset, Pair<FrameLayout, TextView>>()

    // Debounce handler for IPC updates
    private val debounceHandler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    // 5 App Background Choices
    private val appThemeOptions = listOf(
        Pair(ThemeManager.APP_COLOR_ANTIQUE_SANDAL, "பண்டைய சந்தனம் (Antique Sandal)"),
        Pair(ThemeManager.APP_COLOR_BRIGHT_LEMON, "எலுமிச்சை மஞ்சள் (Bright Lemon)"),
        Pair(ThemeManager.APP_COLOR_MIDNIGHT_VIOLET, "நள்ளிரவு ஊதா (Midnight Violet)"),
        Pair(ThemeManager.APP_COLOR_OFF_WHITE, "மங்கலான வெள்ளை (Off-White)"),
        Pair(ThemeManager.APP_COLOR_AMOLED_BLACK, "அமோலெட் கருப்பு (AMOLED Black)")
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
        setupAppThemeSelection()
        setupPresetBoxes(view)
        setupBorderSwitch()
        setupOpacitySeekBar()
        applyDynamicTheming()
    }

    override fun onResume() {
        super.onResume()
        applyDynamicTheming()
    }

    override fun onDestroyView() {
        updateRunnable?.let { debounceHandler.removeCallbacks(it) }
        presetBoxes.clear()
        super.onDestroyView()
    }

    private fun bindViews(view: View) {
        rootScrollView = view.findViewById(R.id.scroll_settings_root)
        tvHeading = view.findViewById(R.id.tv_settings_main_heading)
        tvLblAppTheme = view.findViewById(R.id.tv_lbl_app_theme)
        rgAppThemes = view.findViewById(R.id.rg_app_theme_options)

        tvLblWidgetTheme = view.findViewById(R.id.tv_lbl_widget_theme)
        tvLblOuterBorder = view.findViewById(R.id.tv_lbl_outer_border)
        switchOuterBorder = view.findViewById(R.id.switch_outer_border)

        tvLblWidgetOpacity = view.findViewById(R.id.tv_lbl_widget_opacity)
        tvWidgetOpacityVal = view.findViewById(R.id.tv_widget_opacity_val)
        seekBarOpacity = view.findViewById(R.id.seekbar_widget_opacity)

        tvLblWidgetPreview = view.findViewById(R.id.tv_lbl_widget_preview)
        framePreviewWrapper = view.findViewById(R.id.frame_preview_wrapper)
        ivPreviewWidgetBg = view.findViewById(R.id.iv_preview_widget_bg)
        tvPreviewTitle = view.findViewById(R.id.tv_preview_title)
        tvPreviewSub = view.findViewById(R.id.tv_preview_sub)
    }

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

    private fun setupAppThemeSelection() {
        val context = context ?: return
        val currentAppTheme = ThemeManager.getAppBackgroundColor(context)
        val rg = rgAppThemes ?: return
        rg.removeAllViews()

        appThemeOptions.forEach { pair ->
            val rb = RadioButton(context).apply {
                id = View.generateViewId()
                text = pair.second
                textSize = 14f
                tag = pair.first
                isChecked = pair.first.equals(currentAppTheme, ignoreCase = true)
                setPadding(12, 10, 12, 10)
            }
            rg.addView(rb)
        }

        rg.setOnCheckedChangeListener { group, checkedId ->
            val selectedRb = group.findViewById<RadioButton>(checkedId)
            val selectedHex = selectedRb?.tag as? String ?: return@setOnCheckedChangeListener

            ThemeManager.saveAppBackgroundColor(requireContext(), selectedHex)
            applyDynamicTheming()
            (activity as? MainActivity)?.loadDayData()
        }
    }

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
        val radioTextColor = if (isDark) Color.WHITE else palette.primaryText

        tvHeading?.setTextColor(headingColor)
        tvLblAppTheme?.setTextColor(labelColor)
        tvLblWidgetTheme?.setTextColor(labelColor)
        tvLblOuterBorder?.setTextColor(labelColor)
        tvLblWidgetPreview?.setTextColor(labelColor)

        val radioTint = ColorStateList.valueOf(palette.accentGold)
        rgAppThemes?.let { group ->
            for (i in 0 until group.childCount) {
                (group.getChildAt(i) as? RadioButton)?.apply {
                    setTextColor(radioTextColor)
                    buttonTintList = radioTint
                }
            }
        }

        switchOuterBorder?.thumbTintList = ColorStateList.valueOf(palette.accentGold)
        switchOuterBorder?.trackTintList = ColorStateList.valueOf(Color.parseColor("#80B26A00"))

        seekBarOpacity?.thumbTintList = ColorStateList.valueOf(palette.accentGold)
        seekBarOpacity?.progressTintList = ColorStateList.valueOf(palette.accentGold)

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
