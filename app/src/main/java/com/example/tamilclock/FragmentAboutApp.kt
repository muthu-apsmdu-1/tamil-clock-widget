package com.example.tamilclock

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class FragmentAboutApp : Fragment() {

    private var rootScrollView: ScrollView? = null
    private var cardFeatures: CardView? = null
    private var tvFeaturesTitle: TextView? = null
    private var tvFeaturesBody: TextView? = null
    private var btnShare: Button? = null
    private var btnFeedback: Button? = null
    private var tvVersion: TextView? = null
    private var tvFooter: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_about_app, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupVersion()
        setupListeners()
        applyTheme()
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
    }

    override fun onDestroyView() {
        rootScrollView = null
        cardFeatures = null
        tvFeaturesTitle = null
        tvFeaturesBody = null
        btnShare = null
        btnFeedback = null
        tvVersion = null
        tvFooter = null
        super.onDestroyView()
    }

    private fun bindViews(view: View) {
        rootScrollView = view.findViewById(R.id.scroll_about_root)
        cardFeatures = view.findViewById(R.id.card_features)
        tvFeaturesTitle = view.findViewById(R.id.tv_features_title)
        tvFeaturesBody = view.findViewById(R.id.tv_features_body)
        btnShare = view.findViewById(R.id.btn_share_app)
        btnFeedback = view.findViewById(R.id.btn_feedback)
        tvVersion = view.findViewById(R.id.tv_app_version)
        tvFooter = view.findViewById(R.id.tv_footer_copyright)
    }

    private fun setupVersion() {
        val versionName = try {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            "பதிப்பு ${pInfo.versionName}"
        } catch (_: Exception) {
            "பதிப்பு 1.0.0"
        }
        tvVersion?.text = versionName
    }

    private fun setupListeners() {
        btnShare?.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "தமிழ் நாட்காட்டி & கடிகாரம்"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    "தமிழ் நாட்காட்டி மற்றும் கடிகார விட்ஜெட் செயலி - துல்லியமான தினசரி பஞ்சாங்கம், நல்ல நேரம், கௌரி & ஹோரை தகவல்களுடன்!"
                )
            }
            startActivity(Intent.createChooser(shareIntent, "பகிரவும்"))
        }

        btnFeedback?.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("support@example.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Tamil Natkatti App Feedback")
            }
            try {
                startActivity(emailIntent)
            } catch (_: Exception) {}
        }
    }

    private fun applyTheme() {
        val context = context ?: return
        val palette = ThemeManager.getThemePalette(context)
        val currentBg = ThemeManager.getAppBackgroundColor(context)
        val isDark = currentBg == ThemeManager.APP_COLOR_AMOLED_BLACK ||
                     currentBg == ThemeManager.APP_COLOR_MIDNIGHT_VIOLET

        // 1. Root Background
        rootScrollView?.setBackgroundColor(palette.pageBackground)

        // 2. Features Card (Dynamic Content Background)
        cardFeatures?.setCardBackgroundColor(palette.cardBackground)

        // 3. Text Colors (Contrast-safe across all 5 themes)
        val headerCol = if (isDark) palette.accentGold else palette.primaryHeader
        val bodyCol = if (isDark) Color.WHITE else Color.parseColor("#2C1A14")
        val subTextCol = if (isDark) Color.parseColor("#AAAAAA") else palette.secondaryHeader

        tvFeaturesTitle?.setTextColor(headerCol)
        tvFeaturesBody?.setTextColor(bodyCol)
        tvVersion?.setTextColor(subTextCol)
        tvFooter?.setTextColor(subTextCol)

        // 4. Action Buttons (Using ColorStateList to ensure proper background tinting)
        btnShare?.backgroundTintList = ColorStateList.valueOf(palette.primaryHeader)
        btnShare?.setTextColor(palette.accentGold)

        btnFeedback?.backgroundTintList = ColorStateList.valueOf(palette.emptyCellBackground)
        btnFeedback?.setTextColor(if (isDark) Color.WHITE else palette.primaryHeader)
    }
}
