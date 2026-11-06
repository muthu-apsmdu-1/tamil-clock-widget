package com.example.tamilclock

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class FragmentAboutApp : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_about_app, container, false)

        // Bind Version Name
        val tvVersion = root.findViewById<TextView>(R.id.tv_app_version)
        val versionName = try {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            "பதிப்பு ${pInfo.versionName}"
        } catch (e: Exception) {
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

        return root
    }
}
