package com.example.tamilclock

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MainPagerAdapter(
    private val onPrevDayClick: () -> Unit,
    private val onNextDayClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_PANCHANGAM = 0
        private const val TYPE_PLACEHOLDER = 1
    }

    private val tabTitles = listOf("பஞ்சாங்கம்", "ராசிபலன்", "ஹோரை")
    private var panchangamData: TamilDateInfo? = null
    private var gregorianDateText: String = ""

    fun updateData(dateText: String, data: TamilDateInfo?) {
        this.gregorianDateText = dateText
        this.panchangamData = data
        notifyItemChanged(0)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_PANCHANGAM else TYPE_PLACEHOLDER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_PANCHANGAM) {
            val view = inflater.inflate(R.layout.fragment_panchangam, parent, false)
            PanchangamViewHolder(view)
        } else {
            val view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
            PlaceholderViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PanchangamViewHolder) {
            holder.bind(gregorianDateText, panchangamData, onPrevDayClick, onNextDayClick)
        } else if (holder is PlaceholderViewHolder) {
            holder.textView.text = "${tabTitles[position]} பக்கம் விரைவில்..."
            holder.textView.textSize = 18f
            holder.textView.setPadding(32, 64, 32, 32)
            holder.textView.setTextColor(0xFF2C1A14.toInt())
        }
    }

    override fun getItemCount(): Int = tabTitles.size

    class PanchangamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val btnPrev: ImageButton = itemView.findViewById(R.id.btn_prev_day)
        private val btnNext: ImageButton = itemView.findViewById(R.id.btn_next_day)
        private val tvGregorian: TextView = itemView.findViewById(R.id.tv_gregorian_header)
        private val tvWeekday: TextView = itemView.findViewById(R.id.tv_weekday_header)
        private val tvTamilDate: TextView = itemView.findViewById(R.id.tv_tamil_date_full)

        private val tvSunrise: TextView = itemView.findViewById(R.id.tv_sunrise_val)
        private val tvSunset: TextView = itemView.findViewById(R.id.tv_sunset_val)
        private val tvMoonrise: TextView = itemView.findViewById(R.id.tv_moonrise_val)
        private val tvMoonset: TextView = itemView.findViewById(R.id.tv_moonset_val)

        private val tvTithi: TextView = itemView.findViewById(R.id.tv_tithi_val)
        private val tvNakshatra: TextView = itemView.findViewById(R.id.tv_nakshatra_val)
        private val tvPaksham: TextView = itemView.findViewById(R.id.tv_paksham_val)

        private val tvNallaNeram: TextView = itemView.findViewById(R.id.tv_nalla_neram_val)
        private val tvGouri: TextView = itemView.findViewById(R.id.tv_gouri_val)
        private val tvRahu: TextView = itemView.findViewById(R.id.tv_rahu_val)
        private val tvYamagandam: TextView = itemView.findViewById(R.id.tv_yamagandam_val)

        fun bind(
            gregDate: String,
            data: TamilDateInfo?,
            onPrev: () -> Unit,
            onNext: () -> Unit
        ) {
            btnPrev.setOnClickListener { onPrev() }
            btnNext.setOnClickListener { onNext() }

            tvGregorian.text = gregDate

            if (data != null) {
                tvWeekday.text = data.dayName
                tvTamilDate.text = "${data.tamilYear} வருடம் - ${data.tamilMonth} ${data.tamilDay}"

                tvSunrise.text = data.sunrise.ifEmpty { "06:08 AM" }
                tvSunset.text = data.sunset.ifEmpty { "05:58 PM" }
                tvMoonrise.text = data.moonrise.ifEmpty { "04:20 PM" }
                tvMoonset.text = data.moonset.ifEmpty { "04:55 AM" }

                tvTithi.text = data.tithi
                tvNakshatra.text = data.nakshatra
                tvPaksham.text = data.paksham

                tvNallaNeram.text = "${data.nallaNeramMorning} / ${data.nallaNeramEvening}"
                tvGouri.text = "${data.gouriNallaMorning} / ${data.gouriNallaEvening}"
                tvRahu.text = data.rahuKalam
                tvYamagandam.text = data.yamaGandam
            }
        }
    }

    class PlaceholderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(android.R.id.text1)
    }
}
