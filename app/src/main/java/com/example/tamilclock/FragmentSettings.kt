package com.example.tamilclock

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment

class FragmentSettings : Fragment() {

    companion object {
        const val PREFS_NAME = "tamil_clock_settings"
        const val KEY_APP_THEME = "key_app_theme"
        const val KEY_WIDGET_COLOR = "key_widget_color"
        const val KEY_WIDGET_OPACITY = "key_widget_opacity"

        // Default selections
        const val DEFAULT_APP_THEME = "ivory_cream"
        const val DEFAULT_WIDGET_COLOR = "#4A0E17"
        const val DEFAULT_WIDGET_OPACITY = 85
    }

    private data class ColorOption(
        val id: String,
        val title: String,
        val colorHex: String,
        val textColorHex: String
    )

    private val appThemeOptions = listOf(
        ColorOption("ivory_cream", "Ivory Cream (Default)", "#FFFDF7", "#1E1E24"),
        ColorOption("white", "White", "#FFFFFF", "#212121"),
        ColorOption("temple_navy", "Temple Navy", "#0F172A", "#F8FAFC"),
        ColorOption("amoled_black", "Pitch Black (AMOLED)", "#000000", "#EDEDED")
    )

    private val widgetThemeOptions = listOf(
        ColorOption("deep_maroon", "Deep Maroon (Default)", "#4A0E17", "#FFFFFF"),
        ColorOption("midnight_black", "Midnight Black", "#000000", "#FFFFFF"),
        ColorOption("white", "White", "#FFFFFF", "#1E1E24"),
        ColorOption("teal_blue_green", "Teal Blue - Green", "#0D4D4D", "#FFFFFF")
    )

    private lateinit var layoutAppThemes: LinearLayout
    private lateinit var layoutWidgetThemes: LinearLayout
    private lateinit var seekbarOpacity: SeekBar
    private lateinit var tvOpacityVal: TextView

    private lateinit var cardWidgetPreview: LinearLayout
    private lateinit var tvPreviewTitle: TextView
    private lateinit var tvPreviewSub: TextView

    private var currentAppTheme = DEFAULT_APP_THEME
    private var currentWidgetColor = DEFAULT_WIDGET_COLOR
    private var currentWidgetOpacity = DEFAULT_WIDGET_OPACITY

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutAppThemes = view.findViewById(R.id.layout_app_theme_options)
        layoutWidgetThemes = view.findViewById(R.id.layout_widget_theme_options)
        seekbarOpacity = view.findViewById(R.id.seekbar_widget_opacity)
        tvOpacityVal = view.findViewById(R.id.tv_widget_opacity_val)

        cardWidgetPreview = view.findViewById(R.id.card_widget_preview)
        tvPreviewTitle = view.findViewById(R.id.tv_preview_title)
        tvPreviewSub = view.findViewById(R.id.tv_preview_sub)

        loadSavedPreferences()
        renderAppThemeOptions()
        renderWidgetThemeOptions()
        setupOpacitySlider()
        updateWidgetPreview()
    }

    private fun loadSavedPreferences() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentAppTheme = prefs.getString(KEY_APP_THEME, DEFAULT_APP_THEME) ?: DEFAULT_APP_THEME
        currentWidgetColor = prefs.getString(KEY_WIDGET_COLOR, DEFAULT_WIDGET_COLOR) ?: DEFAULT_WIDGET_COLOR
        currentWidgetOpacity = prefs.getInt(KEY_WIDGET_OPACITY, DEFAULT_WIDGET_OPACITY)
    }

    private fun savePreference(key: String, value: Any) {
        val editor = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        when (value) {
            is String -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
        }
        editor.apply()
    }

    private fun renderAppThemeOptions() {
        layoutAppThemes.removeAllViews()
        for (option in appThemeOptions) {
            val isSelected = option.id == currentAppTheme
            val box = createColorOptionBox(
                title = option.title,
                bgColor = Color.parseColor(option.colorHex),
                textColor = Color.parseColor(option.textColorHex),
                isSelected = isSelected
            ) {
                currentAppTheme = option.id
                savePreference(KEY_APP_THEME, currentAppTheme)
                renderAppThemeOptions()
                // Trigger ThemeManager or activity recreation if dynamic reload is desired
                ThemeManager.applyTheme(requireActivity())
            }
            layoutAppThemes.addView(box)
        }
    }

    private fun renderWidgetThemeOptions() {
        layoutWidgetThemes.removeAllViews()
        for (option in widgetThemeOptions) {
            val isSelected = option.colorHex.equals(currentWidgetColor, ignoreCase = true)
            val box = createColorOptionBox(
                title = option.title,
                bgColor = Color.parseColor(option.colorHex),
                textColor = Color.parseColor(option.textColorHex),
                isSelected = isSelected
            ) {
                currentWidgetColor = option.colorHex
                savePreference(KEY_WIDGET_COLOR, currentWidgetColor)
                renderWidgetThemeOptions()
                updateWidgetPreview()
                notifyWidgetsUpdate()
            }
            layoutWidgetThemes.addView(box)
        }
    }

    private fun setupOpacitySlider() {
        seekbarOpacity.progress = currentWidgetOpacity
        tvOpacityVal.text = "$currentWidgetOpacity%"

        seekbarOpacity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentWidgetOpacity = progress
                tvOpacityVal.text = "$progress%"
                updateWidgetPreview()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                savePreference(KEY_WIDGET_OPACITY, currentWidgetOpacity)
                notifyWidgetsUpdate()
            }
        })
    }

    private fun createColorOptionBox(
        title: String,
        bgColor: Int,
        textColor: Int,
        isSelected: Boolean,
        onClick: () -> Unit
    ): View {
        val root = FrameLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 4, 0, 8) }
            setBackgroundResource(
                if (isSelected) R.drawable.bg_color_chip_selected
                else R.drawable.bg_color_chip_unselected
            )
            setPadding(4, 4, 4, 4)
            setOnClickListener { onClick() }
        }

        val innerCard = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            val shape = GradientDrawable().apply {
                setColor(bgColor)
                cornerRadius = 8f * resources.displayMetrics.density
            }
            background = shape
            setPadding(16, 14, 16, 14)
        }

        val tvTitle = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
            text = title
            setTextColor(textColor)
            textSize = 14f
            textStyle = android.graphics.Typeface.BOLD
        }

        val tvIndicator = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = if (isSelected) "✔" else ""
            setTextColor(if (isSelected) Color.parseColor("#FFD043") else Color.TRANSPARENT)
            textSize = 16f
            textStyle = android.graphics.Typeface.BOLD
        }

        innerCard.addView(tvTitle)
        innerCard.addView(tvIndicator)
        root.addView(innerCard)
        return root
    }

    private fun updateWidgetPreview() {
        val baseColor = Color.parseColor(currentWidgetColor)
        val alpha = (currentWidgetOpacity * 255) / 100
        val finalColor = Color.argb(alpha, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))

        val bgShape = GradientDrawable().apply {
            setColor(finalColor)
            cornerRadius = 12f * resources.displayMetrics.density
        }
        cardWidgetPreview.background = bgShape

        // Adjust preview text color contrast depending on widget color
        val isLightWidget = currentWidgetColor.equals("#FFFFFF", ignoreCase = true)
        val contentTextColor = if (isLightWidget) Color.BLACK else Color.WHITE
        tvPreviewTitle.setTextColor(contentTextColor)
        tvPreviewSub.setTextColor(contentTextColor)
    }

    private fun notifyWidgetsUpdate() {
        // Trigger WidgetUpdateHelper to refresh all active widgets immediately
        try {
            WidgetUpdateHelper.updateAllWidgets(requireContext())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
