package com.example.tamilclock

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.json.JSONObject
import java.io.InputStream
import java.util.Calendar
import java.util.Locale

class FragmentGowriHorai : Fragment() {

    private lateinit var tabBtnGowri: TextView
    private lateinit var tabBtnHorai: TextView
    private lateinit var layoutGowriTable: View
    private lateinit var layoutHoraiTable: View
    private lateinit var tvHeaderStripDate: TextView

    private lateinit var containerGowriDay: LinearLayout
    private lateinit var containerGowriNight: LinearLayout
    private lateinit var containerHoraiDay: LinearLayout
    private lateinit var containerHoraiNight: LinearLayout

    private var isGowriSelected = true
    private var isSelectedDateToday = true
    private var selectedCalendar: Calendar = Calendar.getInstance()

    private val refreshHandler = Handler(Looper.getMainLooper())
    private val timeTickRunnable = object : Runnable {
        override fun run() {
            highlightCurrentSlotIfToday()
            refreshHandler.postDelayed(this, 30_000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gowri_horai, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabBtnGowri = view.findViewById(R.id.tab_btn_gowri)
        tabBtnHorai = view.findViewById(R.id.tab_btn_horai)
        layoutGowriTable = view.findViewById(R.id.layout_gowri_table)
        layoutHoraiTable = view.findViewById(R.id.layout_horai_table)
        tvHeaderStripDate = view.findViewById(R.id.tv_header_strip_date)

        containerGowriDay = view.findViewById(R.id.container_gowri_day_slots)
        containerGowriNight = view.findViewById(R.id.container_gowri_night_slots)
        containerHoraiDay = view.findViewById(R.id.container_horai_day_slots)
        containerHoraiNight = view.findViewById(R.id.container_horai_night_slots)

        tabBtnGowri.setOnClickListener { switchTab(showGowri = true) }
        tabBtnHorai.setOnClickListener { switchTab(showGowri = false) }

        updateDate(Calendar.getInstance())
    }

    private fun switchTab(showGowri: Boolean) {
        isGowriSelected = showGowri
        if (showGowri) {
            layoutGowriTable.visibility = View.VISIBLE
            layoutHoraiTable.visibility = View.GONE
            tabBtnGowri.setBackgroundResource(R.drawable.bg_tab_selected)
            tabBtnGowri.setTextColor(Color.WHITE)
            tabBtnHorai.setBackgroundResource(R.drawable.bg_tab_unselected)
            tabBtnHorai.setTextColor(Color.parseColor("#9E9E9E"))
        } else {
            layoutGowriTable.visibility = View.GONE
            layoutHoraiTable.visibility = View.VISIBLE
            tabBtnHorai.setBackgroundResource(R.drawable.bg_tab_selected)
            tabBtnHorai.setTextColor(Color.WHITE)
            tabBtnGowri.setBackgroundResource(R.drawable.bg_tab_unselected)
            tabBtnGowri.setTextColor(Color.parseColor("#9E9E9E"))
        }
        highlightCurrentSlotIfToday()
    }

    fun updateDate(cal: Calendar) {
        selectedCalendar = cal.clone() as Calendar
        val today = Calendar.getInstance()
        isSelectedDateToday = (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))

        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR) % 100
        val dayName = getTamilDayName(cal.get(Calendar.DAY_OF_WEEK))

        tvHeaderStripDate.text = String.format(Locale.getDefault(), "%d - %d - %02d, %s", day, month, year, dayName)

        if (isSelectedDateToday) {
            tvHeaderStripDate.textSize = 15f
            tvHeaderStripDate.setTextColor(Color.parseColor("#081E7D"))
        } else {
            tvHeaderStripDate.textSize = 13f
            tvHeaderStripDate.setTextColor(Color.parseColor("#B71C1C"))
        }

        renderTablesFromAssets(cal)
        highlightCurrentSlotIfToday()
    }

    private fun loadJsonFromAssets(fileName: String): JSONObject? {
        return try {
            val inputStream: InputStream = requireContext().assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            JSONObject(String(buffer, Charsets.UTF_8))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun renderTablesFromAssets(cal: Calendar) {
        containerGowriDay.removeAllViews()
        containerGowriNight.removeAllViews()
        containerHoraiDay.removeAllViews()
        containerHoraiNight.removeAllViews()

        // 1. Read day data from assets JSON (e.g. panchangam_data.json)
        val rootJson = loadJsonFromAssets("panchangam_data.json")
        val dateKey = String.format(
            Locale.US,
            "%04d-%02d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        val dayData = rootJson?.optJSONObject(dateKey)

        // 2. Populate Gowri Table from Assets
        val gowriDayArr = dayData?.optJSONArray("gowri_day")
        if (gowriDayArr != null) {
            for (i in 0 until gowriDayArr.length()) {
                val obj = gowriDayArr.getJSONObject(i)
                val item = SlotItem(
                    timeRange = obj.optString("time"),
                    name = obj.optString("name"),
                    startMin = obj.optInt("start_min"),
                    endMin = obj.optInt("end_min"),
                    isAuspicious = obj.optBoolean("is_good", true)
                )
                containerGowriDay.addView(createSlotRowView(item))
            }
        }

        val gowriNightArr = dayData?.optJSONArray("gowri_night")
        if (gowriNightArr != null) {
            for (i in 0 until gowriNightArr.length()) {
                val obj = gowriNightArr.getJSONObject(i)
                val item = SlotItem(
                    timeRange = obj.optString("time"),
                    name = obj.optString("name"),
                    startMin = obj.optInt("start_min"),
                    endMin = obj.optInt("end_min"),
                    isAuspicious = obj.optBoolean("is_good", true)
                )
                containerGowriNight.addView(createSlotRowView(item))
            }
        }

        // 3. Populate Horai Table from Assets
        val horaiDayArr = dayData?.optJSONArray("horai_day")
        if (horaiDayArr != null) {
            for (i in 0 until horaiDayArr.length()) {
                val obj = horaiDayArr.getJSONObject(i)
                val item = SlotItem(
                    timeRange = obj.optString("time"),
                    name = obj.optString("name"),
                    startMin = obj.optInt("start_min"),
                    endMin = obj.optInt("end_min"),
                    isAuspicious = obj.optBoolean("is_good", true)
                )
                containerHoraiDay.addView(createSlotRowView(item))
            }
        }

        val horaiNightArr = dayData?.optJSONArray("horai_night")
        if (horaiNightArr != null) {
            for (i in 0 until horaiNightArr.length()) {
                val obj = horaiNightArr.getJSONObject(i)
                val item = SlotItem(
                    timeRange = obj.optString("time"),
                    name = obj.optString("name"),
                    startMin = obj.optInt("start_min"),
                    endMin = obj.optInt("end_min"),
                    isAuspicious = obj.optBoolean("is_good", true)
                )
                containerHoraiNight.addView(createSlotRowView(item))
            }
        }
    }

    private fun createSlotRowView(item: SlotItem): View {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 4, 0, 4) }
            setBackgroundResource(R.drawable.bg_row4_chip)
            setPadding(12, 12, 12, 12)
            tag = item
        }

        val tvTime = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
            text = item.timeRange
            textSize = 12.5f
            setTextColor(Color.parseColor(if (item.isAuspicious) "#0D5C2E" else "#B71C1C"))
        }

        val tvName = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
            text = item.name
            textSize = 13f
            textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            setTextColor(Color.parseColor(if (item.isAuspicious) "#0D5C2E" else "#B71C1C"))
        }

        row.addView(tvTime)
        row.addView(tvName)
        return row
    }

    private fun highlightCurrentSlotIfToday() {
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val activeContainers = if (isGowriSelected) {
            listOf(containerGowriDay, containerGowriNight)
        } else {
            listOf(containerHoraiDay, containerHoraiNight)
        }

        for (container in activeContainers) {
            for (i in 0 until container.childCount) {
                val row = container.getChildAt(i)
                val item = row.tag as? SlotItem ?: continue

                if (isSelectedDateToday && currentMinutes in item.startMin until item.endMin) {
                    row.setBackgroundResource(R.drawable.bg_chandrashtamam_card)
                } else {
                    row.setBackgroundResource(R.drawable.bg_row4_chip)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshHandler.post(timeTickRunnable)
    }

    override fun onPause() {
        super.onPause()
        refreshHandler.removeCallbacks(timeTickRunnable)
    }

    private fun getTamilDayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "ஞாயிறு"
            Calendar.MONDAY -> "திங்கள்"
            Calendar.TUESDAY -> "செவ்வாய்"
            Calendar.WEDNESDAY -> "புதன்"
            Calendar.THURSDAY -> "வியாழன்"
            Calendar.FRIDAY -> "வெள்ளி"
            Calendar.SATURDAY -> "சனி"
            else -> ""
        }
    }

    data class SlotItem(
        val timeRange: String,
        val name: String,
        val startMin: Int,
        val endMin: Int,
        val isAuspicious: Boolean
    )
}
