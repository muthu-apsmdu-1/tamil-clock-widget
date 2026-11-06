package com.example.tamilclock

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class FragmentAboutApp : Fragment() {

    private var rootScrollView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_about_app, container, false)
        rootScrollView = root

        // Bind Version Name
        val tvVersion = root.findViewById<TextView>(R.id.tv_app_version)
        val versionName = try {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            "பதிப்பு ${pInfo.versionName}"
        } catch (_: Exception) {
            "பதிப்பு 1.0.0"
        }
        tvVersion?.text = versionName

        // Share App Button
        root.findViewById<Button>(R.id.btn_share_app)?.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    "தமிழ் காலண்டர் மற்றும் கடிகார விட்ஜெட் செயலி - ஆஃப்லைன் பஞ்சாங்கம், நல்ல நேரம், கௌரி & ஹோரை தகவல்களுடன்!"
                )
            }
            startActivity(Intent.createChooser(shareIntent, "பகிரவும்"))
        }

        // Feedback / Support Email Button
        root.findViewById<Button>(R.id.btn_feedback)?.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("support@example.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Tamil Clock Widget Feedback")
            }
            try {
                startActivity(emailIntent)
            } catch (_: Exception) {}
        }

        applyTheme(root)
        return root
    }

    override fun onResume() {
        super.onResume()
        rootScrollView?.let { applyTheme(it) }
    }

    private fun applyTheme(root: View) {
        val context = context ?: return
        val palette = ThemeManager.getThemePalette(context)

        // 1. Root ScrollView background
        root.setBackgroundColor(palette.pageBackground)

        // 2. Header banner card
        val headerCard = root.findViewById<CardView?>(R.id.header_card)
            ?: (root.findViewById<View>(R.id.iv_app_logo)?.parent?.parent as? CardView)
        headerCard?.setCardBackgroundColor(palette.primaryHeader)

        // 3. Content & Feature Cards
        val allCards = mutableListOf<CardView>()
        findAllCards(root as ViewGroup, allCards)
        for (card in allCards) {
            if (card != headerCard) {
                card.setCardBackgroundColor(palette.cardBackground)
            }
        }

        // 4. Action Buttons
        val btnShare = root.findViewById<Button?>(R.id.btn_share_app)
        btnShare?.setBackgroundColor(palette.primaryHeader)
        btnShare?.setTextColor(palette.accentGold)

        val btnFeedback = root.findViewById<Button?>(R.id.btn_feedback)
        btnFeedback?.setBackgroundColor(palette.emptyCellBackground)
        btnFeedback?.setTextColor(palette.primaryHeader)
    }

    private fun findAllCards(viewGroup: ViewGroup, list: MutableList<CardView>) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is CardView) {
                list.add(child)
            } else if (child is ViewGroup) {
                findAllCards(child, list)
            }
        }
    }
}
