package com.example.tamilclock

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
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
import androidx.fragment.app.Fragment

class FragmentSettings : Fragment() {

    private var rootScrollView: ScrollView? = null
    private var tvHeading: TextView? = null
    private var tvLblAppTheme: TextView? = null
    private var rgAppThemes: RadioGroup? = null
    private var tvLblWidgetTheme: TextView? = null
    private var rgWidgetThemes: RadioGroup? = null
    private var tvLblWidgetOpacity: TextView? = null
    private var tvWidgetOpacityVal: TextView? = null
    private var seekBarOpacity: SeekBar? = null
    private var tvLblWidgetPreview: TextView? = null
    private var framePreviewWrapper: FrameLayout? = null
    private var ivPreviewWidgetBg: ImageView? = null
    private var tvPreviewTitle: TextView? = null
    private var tvPreviewSub: TextView? = null

    // 5 App Background Choices (Exact ThemeManager constants)
    private val appThemeOptions = listOf(
        Pair(ThemeManager.APP_COLOR_ANTIQUE_SANDAL, "பண்டைய சந்தனம் (Antique Sandal)"),
        Pair(ThemeManager.APP_COLOR_BRIGHT_LEMON, "எலுமிச்சை மஞ்சள் (Bright Lemon)"),
        Pair(ThemeManager.APP_COLOR_MIDNIGHT_VIOLET, "நள்ளிரவு ஊதா (Midnight Violet)"),
        Pair(ThemeManager.APP_COLOR_OFF_WHITE, "மங்கலான வெள்ளை (Off-White)"),
        Pair(ThemeManager.APP_COLOR_AMOLED_BLACK, "அமோலெட் கருப்பு (AMOLED Black)")
    )

    // 5 Widget Background Choices (Exact ThemeManager constants)
    private val widgetThemeOptions = listOf(
        Pair(ThemeManager.WIDGET_COLOR_VELVET_MAROON, "வெல்வெட் மெரூன் (Velvet Maroon)"),
        Pair(ThemeManager.WIDGET_COLOR_ROYAL_NAVY, "ராயல் நேவி நீலம் (Royal Navy)"),
        Pair(ThemeManager.WIDGET_COLOR_MIDNIGHT_VIOLET, "நள்ளிரவு ஊதா (Midnight Violet)"),
        Pair(ThemeManager.WIDGET_COLOR_OFF_WHITE, "மங்கலான வெள்ளை (Off-White)"),
        Pair(ThemeManager.WIDGET_COLOR_AMOLED_BLACK, "அமோலெட் கருப்பு (AMOLED Black)")
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
        setupWidgetThemeSelection()
        setupOpacitySeekBar()
        applyDynamicTheming()
    }

    override fun onResume() {
        super.onResume()
        applyDynamicTheming()
    }

    private fun bindViews(view: View) {
        rootScrollView = view.findViewById(R.id.scroll_settings_root)
        tvHeading = view.findViewById(R.id.tv_settings_main_heading)
        tvLblAppTheme = view.findViewById(R.id.tv_lbl_app_theme)
        rgAppThemes = view.findViewById(R.id.rg_app_theme_options)
        tvLblWidgetTheme = view.findViewById(R.id.tv_lbl_widget_theme)
        rgWidgetThemes = view.findViewById(R.id.rg_widget_theme_options)
        tvLblWidgetOpacity = view.findViewById(R.id.tv_lbl_widget_opacity)
        tvWidgetOpacityVal = view.findViewById(R.id.tv_widget_opacity_val)
        seekBarOpacity = view.findViewById(R.id.seekbar_widget_opacity)
        tvLblWidgetPreview = view.findViewById(R.id.tv_lbl_widget_preview)
        framePreviewWrapper = view.findViewById(R.id.frame_preview_wrapper)
        ivPreviewWidgetBg = view.findViewById(R.id.iv_preview_widget_bg)
        tvPreviewTitle = view.findViewById(R.id.tv_preview_title)
        tvPreviewSub = view.findViewById(R.id.tv_preview_sub)
    }

    private fun isDarkThemeActive(): Boolean {
        val context = context ?: return false
        val currentBg = ThemeManager.getAppBackgroundColor(context)
        return currentBg == ThemeManager.APP_COLOR_AMOLED_BLACK ||
               currentBg == ThemeManager.APP_COLOR_MIDNIGHT_VIOLET
    }

    // -------------------------------------------------------------
    // 1. APP THEME RADIOGROUP
    // -------------------------------------------------------------
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

            // Refresh MainActivity top banner & bar immediately
            (activity as? MainActivity)?.apply {
                loadDayData()
            }
        }
    }

    // -------------------------------------------------------------
    // 2. WIDGET THEME RADIOGROUP
    // -------------------------------------------------------------
    private fun setupWidgetThemeSelection() {
        val context = context ?: return
        val currentWidgetTheme = ThemeManager.getWidgetBackgroundColor(context)
        val rg = rgWidgetThemes ?: return
        rg.removeAllViews()

        widgetThemeOptions.forEach { pair ->
            val rb = RadioButton(context).apply {
                id = View.generateViewId()
                text = pair.second
                textSize = 14f
                tag = pair.first
                isChecked = pair.first.equals(currentWidgetTheme, ignoreCase = true)
                setPadding(12, 10, 12, 10)
            }
            rg.addView(rb)
        }

        rg.setOnCheckedChangeListener { group, checkedId ->
            val selectedRb = group.findViewById<RadioButton>(checkedId)
            val selectedHex = selectedRb?.tag as? String ?: return@setOnCheckedChangeListener

            ThemeManager.saveWidgetBackgroundColor(requireContext(), selectedHex)
            updateWidgetPreview()
            WidgetUpdateHelper.updateAllWidgets(requireContext())
        }
    }

    // -------------------------------------------------------------
    // 3. WIDGET OPACITY SLIDER
    // -------------------------------------------------------------
    private fun setupOpacitySeekBar() {
        val context = context ?: return
        val currentOpacity = ThemeManager.getWidgetOpacity(context)

        seekBarOpacity?.progress = currentOpacity
        tvWidgetOpacityVal?.text = "$currentOpacity%"

        seekBarOpacity?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val clamped = progress.coerceIn(10, 100)
                tvWidgetOpacityVal?.text = "$clamped%"
                tvPreviewSub?.text = "நேரலை மாதிரி • $clamped%"
                if (fromUser) {
                    ThemeManager.saveWidgetOpacity(requireContext(), clamped)
                    updateWidgetPreview()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val finalOpacity = (seekBar?.progress ?: 85).coerceIn(10, 100)
                ThemeManager.saveWidgetOpacity(requireContext(), finalOpacity)
                updateWidgetPreview()
                WidgetUpdateHelper.updateAllWidgets(requireContext())
            }
        })
    }

    // -------------------------------------------------------------
    // 4. DYNAMIC THEMING & PURE WHITE TEXT COLOR RULES
    // -------------------------------------------------------------
    private fun applyDynamicTheming() {
        val context = context ?: return
        val palette = ThemeManager.getThemePalette(context)
        val isDark = isDarkThemeActive()

        // Page Root & Container Backgrounds
        rootScrollView?.setBackgroundColor(palette.pageBackground)
        framePreviewWrapper?.setBackgroundColor(palette.cardBackground)

        // Text Colors: Pure White (#FFFFFF) for dark themes; designated palette colors for light themes
        val headingColor = if (isDark) Color.WHITE else palette.primaryHeader
        val labelColor = if (isDark) Color.WHITE else palette.secondaryHeader
        val radioTextColor = if (isDark) Color.WHITE else palette.primaryText

        tvHeading?.setTextColor(headingColor)
        tvLblAppTheme?.setTextColor(labelColor)
        tvLblWidgetTheme?.setTextColor(labelColor)
        tvLblWidgetOpacity?.setTextColor(labelColor)
        tvWidgetOpacityVal?.setTextColor(palette.accentGold)
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

        rgWidgetThemes?.let { group ->
            for (i in 0 until group.childCount) {
                (group.getChildAt(i) as? RadioButton)?.apply {
                    setTextColor(radioTextColor)
                    buttonTintList = radioTint
                }
            }
        }

        seekBarOpacity?.thumbTintList = ColorStateList.valueOf(palette.accentGold)
        seekBarOpacity?.progressTintList = ColorStateList.valueOf(palette.accentGold)

        updateWidgetPreview()
    }

    // -------------------------------------------------------------
    // 5. LIVE PREVIEW BITMAP RENDERING
    // -------------------------------------------------------------
    private fun updateWidgetPreview() {
        val context = context ?: return
        val bgBitmap = WidgetUpdateHelper.createWidgetBackgroundBitmap(context, 480, 140)
        ivPreviewWidgetBg?.setImageBitmap(bgBitmap)

        val hexColor = ThemeManager.getWidgetBackgroundColor(context)
        val isWidgetLight = hexColor.equals(ThemeManager.WIDGET_COLOR_OFF_WHITE, ignoreCase = true)

        val previewTextColor = if (isWidgetLight) Color.parseColor("#081E7D") else Color.WHITE
        val previewSubColor = if (isWidgetLight) Color.parseColor("#D81B60") else Color.parseColor("#FFDF00")

        tvPreviewTitle?.setTextColor(previewTextColor)
        tvPreviewSub?.setTextColor(previewSubColor)
    }
}
