package com.example.tamilclock

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream

// Compatibility data model required across activities and widgets
data class TamilDateInfo(
    val gregorianDate: String = "",
    val tamilYear: String = "பராபவ",
    val dayNameTa: String = "",
    val tamilMonth: String = "",
    val tamilDay: Int = 1,
    val paksham: String = "",
    val tithi: String = "",
    val nakshatra: String = "",
    val nallaNeram: String = "",
    val gouriNallaNeram: String = "",
    val rahuKalam: String = "",
    val yamaGandam: String = "",
    val sunrise: String = "06:08 AM",
    val sunset: String = "05:58 PM",
    val moonrise: String = "03:15 PM",
    val moonset: String = "03:30 AM",
    val festivalsEvents: String = ""
)

class TamilDatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "tamil_calendar.db"
        private const val DB_VERSION = 11
    }

    private val dbPath: String = context.getDatabasePath(DB_NAME).path

    init {
        copyDatabaseIfNeeded()
    }

    private fun copyDatabaseIfNeeded() {
        val dbFile = File(dbPath)
        val sp = context.getSharedPreferences("db_prefs", Context.MODE_PRIVATE)
        val savedVersion = sp.getInt("db_version", 0)

        if (!dbFile.exists() || savedVersion < DB_VERSION) {
            dbFile.parentFile?.mkdirs()
            try {
                context.assets.open(DB_NAME).use { input ->
                    FileOutputStream(dbFile).use { output ->
                        input.copyTo(output)
                    }
                }
                sp.edit().putInt("db_version", DB_VERSION).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {}

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dbFile = File(dbPath)
        if (dbFile.exists()) {
            dbFile.delete()
        }
        copyDatabaseIfNeeded()
    }

    // Core lookup called across all adapters, activities, and widget providers
    fun getDayDetails(dateStr: String): TamilDateInfo? {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery(
            """
            SELECT gregorian_date, tamil_year, tamil_month, tamil_date, 
                   weekday_ta, tithi_name, nakshatra_name, festivals_events 
            FROM panchangam_daily 
            WHERE gregorian_date = ? LIMIT 1
            """.trimIndent(),
            arrayOf(dateStr)
        )

        var result: TamilDateInfo? = null
        if (cursor.moveToFirst()) {
            val weekday = cursor.getString(cursor.getColumnIndexOrThrow("weekday_ta"))
            val tithi = cursor.getString(cursor.getColumnIndexOrThrow("tithi_name"))
            val nakshatra = cursor.getString(cursor.getColumnIndexOrThrow("nakshatra_name"))
            val festivals = cursor.getString(cursor.getColumnIndexOrThrow("festivals_events")) ?: ""

            // Clean almanac reference flags
            val cleanTithi = if (tithi == "காண்க") "சுப திதி" else tithi
            val cleanNak = if (nakshatra == "காண்க") "சுப நட்சத்திரம்" else nakshatra

            // Determine Paksham from tithi value
            val paksham = when {
                cleanTithi.contains("அமாவாசை") -> "தேய்பிறை"
                cleanTithi.contains("பௌர்ணமி") -> "வளர்பிறை"
                cleanTithi.contains("சதுர்த்தசி") || cleanTithi.contains("திரயோதசி") -> "வளர்பிறை"
                else -> "வளர்பிறை"
            }

            result = TamilDateInfo(
                gregorianDate = cursor.getString(cursor.getColumnIndexOrThrow("gregorian_date")),
                tamilYear = cursor.getString(cursor.getColumnIndexOrThrow("tamil_year")) ?: "பராபவ",
                dayNameTa = weekday,
                tamilMonth = cursor.getString(cursor.getColumnIndexOrThrow("tamil_month")),
                tamilDay = cursor.getInt(cursor.getColumnIndexOrThrow("tamil_date")),
                paksham = paksham,
                tithi = cleanTithi,
                nakshatra = cleanNak,
                nallaNeram = getNallaNeram(weekday),
                gouriNallaNeram = getGouriNallaNeram(weekday),
                rahuKalam = getRahuKalam(weekday),
                yamaGandam = getYamaGandam(weekday),
                festivalsEvents = festivals
            )
        }
        cursor.close()
        return result
    }

    private fun getRahuKalam(weekday: String): String = when (weekday.trim()) {
        "ஞாயிறு" -> "04:30 PM - 06:00 PM"
        "திங்கள்" -> "07:30 AM - 09:00 AM"
        "செவ்வாய்" -> "03:00 PM - 04:30 PM"
        "புதன்" -> "12:00 PM - 01:30 PM"
        "வியாழன்" -> "01:30 PM - 03:00 PM"
        "வெள்ளி" -> "10:30 AM - 12:00 PM"
        "சனி" -> "09:00 AM - 10:30 AM"
        else -> "07:30 AM - 09:00 AM"
    }

    private fun getYamaGandam(weekday: String): String = when (weekday.trim()) {
        "ஞாயிறு" -> "12:00 PM - 01:30 PM"
        "திங்கள்" -> "10:30 AM - 12:00 PM"
        "செவ்வாய்" -> "09:00 AM - 10:30 AM"
        "புதன்" -> "07:30 AM - 09:00 AM"
        "வியாழன்" -> "06:00 AM - 07:30 AM"
        "வெள்ளி" -> "03:00 PM - 04:30 PM"
        "சனி" -> "01:30 PM - 03:00 PM"
        else -> "01:30 PM - 03:00 PM"
    }

    private fun getNallaNeram(weekday: String): String = when (weekday.trim()) {
        "ஞாயிறு" -> "07:30 AM - 08:30 AM / 03:30 PM - 04:30 PM"
        "திங்கள்" -> "06:30 AM - 07:30 AM / 04:30 PM - 05:30 PM"
        "செவ்வாய்" -> "07:30 AM - 08:30 AM / 04:30 PM - 05:30 PM"
        "புதன்" -> "09:15 AM - 10:15 AM / 04:45 PM - 05:45 PM"
        "வியாழன்" -> "10:45 AM - 11:45 AM / 06:30 PM - 07:30 PM"
        "வெள்ளி" -> "09:15 AM - 10:15 AM / 04:45 PM - 05:45 PM"
        "சனி" -> "10:45 AM - 11:45 AM / 04:45 PM - 05:45 PM"
        else -> "09:15 AM - 10:15 AM"
    }

    private fun getGouriNallaNeram(weekday: String): String = when (weekday.trim()) {
        "ஞாயிறு" -> "01:30 PM - 02:30 PM"
        "திங்கள்" -> "09:30 AM - 10:30 AM"
        "செவ்வாய்" -> "01:30 PM - 02:30 PM"
        "புதன்" -> "09:30 AM - 10:30 AM"
        "வியாழன்" -> "09:30 AM - 10:30 AM"
        "வெள்ளி" -> "01:30 PM - 02:30 PM"
        "சனி" -> "06:30 PM - 07:30 PM"
        else -> "09:30 AM - 10:30 AM"
    }
}
