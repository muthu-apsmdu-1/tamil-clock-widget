package com.example.tamilclock

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.FileOutputStream

data class TamilDateInfo(
    val gregorianDate: String = "",
    val tamilYear: String = "பராபவ",
    val dayName: String = "",
    val dayNameTa: String = "",
    val tamilMonth: String = "",
    val tamilDay: Int = 1,
    val paksham: String = "",
    val tithi: String = "",
    val nakshatra: String = "",
    val nallaNeram: String = "",
    val nallaNeramMorning: String = "",
    val nallaNeramEvening: String = "",
    val gouriNallaNeram: String = "",
    val gouriNallaMorning: String = "",
    val gouriNallaEvening: String = "",
    val rahuKalam: String = "",
    val yamaGandam: String = "",
    val kuligai: String = "",
    val sunrise: String = "",
    val sunset: String = "",
    val moonrise: String = "",
    val moonset: String = "",
    val festivalsEvents: String = ""
)

data class TimingSummary(
    val nallaNeramMorning: String,
    val nallaNeramEvening: String,
    val gowriDay: String,
    val gowriNight: String,
    val rahuKalam: String,
    val yamaGandam: String,
    val kuligai: String
)

data class SunMoonTiming(
    val sunrise: String,
    val sunset: String,
    val moonrise: String,
    val moonset: String
)

class TamilDatabaseHelper(private val context: Context) {

    companion object {
        // Incremented to 2 to trigger re-copy of new asset files on upgrade
        private const val DB_VERSION = 2
        private const val PREFS_NAME = "tamil_db_prefs"
        private const val KEY_DB_VERSION = "copied_db_version"

        // Updated filename matching app/src/main/assets/panchang_pure.db
        private const val DB_PAN = "panchang_pure.db"
        private const val DB_AUSPICIOUS = "auspicious_periods.db"
        private const val DB_SUN_MOON = "sun_moon_timings.db"
        private const val DB_GOWRI = "gowri.db"
        private const val DB_HORAI = "horai.db"

        private val ALL_DATABASES = arrayOf(
            DB_PAN,
            DB_AUSPICIOUS,
            DB_SUN_MOON,
            DB_GOWRI,
            DB_HORAI
        )
    }

    init {
        copyAllDatabasesIfNeeded()
    }

    private fun copyAllDatabasesIfNeeded() {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedVersion = sp.getInt(KEY_DB_VERSION, 0)

        val needsCopy = savedVersion < DB_VERSION || ALL_DATABASES.any { fileName ->
            !File(context.getDatabasePath(fileName).path).exists()
        }

        if (needsCopy) {
            for (dbName in ALL_DATABASES) {
                val dbFile = context.getDatabasePath(dbName)
                dbFile.parentFile?.mkdirs()
                try {
                    context.assets.open(dbName).use { input ->
                        FileOutputStream(dbFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            sp.edit().putInt(KEY_DB_VERSION, DB_VERSION).apply()
        }
    }

    private fun openDatabase(dbName: String): SQLiteDatabase {
        val path = context.getDatabasePath(dbName).path
        return SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY)
    }

    /**
     * Primary lookup combining panchang_pure.db, auspicious_periods.db, and sun_moon_timings.db
     */
    fun getDayDetails(dateStr: String): TamilDateInfo? {
        val panDb = openDatabase(DB_PAN)
        val panCursor: Cursor = panDb.rawQuery(
            """
            SELECT gregorian_date, tamil_year, tamil_month, tamil_date, 
                   weekday_ta, paksha, tithi_name, nakshatra_name, festivals_events 
            FROM panchangam_daily 
            WHERE gregorian_date = ? LIMIT 1
            """.trimIndent(),
            arrayOf(dateStr)
        )

        var result: TamilDateInfo? = null

        if (panCursor.moveToFirst()) {
            val tamilYear = panCursor.getString(panCursor.getColumnIndexOrThrow("tamil_year")) ?: "பராபவ"
            val weekdayTa = panCursor.getString(panCursor.getColumnIndexOrThrow("weekday_ta"))
            val tamilMonth = panCursor.getString(panCursor.getColumnIndexOrThrow("tamil_month"))
            val tamilDate = panCursor.getInt(panCursor.getColumnIndexOrThrow("tamil_date"))
            val paksha = panCursor.getString(panCursor.getColumnIndexOrThrow("paksha"))
            val tithi = panCursor.getString(panCursor.getColumnIndexOrThrow("tithi_name"))
            val nakshatra = panCursor.getString(panCursor.getColumnIndexOrThrow("nakshatra_name"))
            val festivals = panCursor.getString(panCursor.getColumnIndexOrThrow("festivals_events")) ?: ""

            panCursor.close()
            panDb.close()

            // Fetch dynamic auspicious & inauspicious windows from auspicious_periods.db
            val timings = getWeekdayTimingSummary(weekdayTa)

            // Fetch dynamic sunrise, sunset, moonrise, moonset from sun_moon_timings.db
            val sunMoon = getSunMoonTiming(dateStr)

            val nnMorning = timings.nallaNeramMorning
            val nnEvening = timings.nallaNeramEvening
            val gnMorning = timings.gowriDay
            val gnEvening = timings.gowriNight

            val nallaNeramCombined = listOf(nnMorning, nnEvening).filter { it.isNotBlank() && it != "--:--" }.joinToString(" / ")
            val gouriCombined = listOf(gnMorning, gnEvening).filter { it.isNotBlank() && it != "--:--" }.joinToString(" / ")

            result = TamilDateInfo(
                gregorianDate = dateStr,
                tamilYear = tamilYear,
                dayName = weekdayTa,
                dayNameTa = weekdayTa,
                tamilMonth = tamilMonth,
                tamilDay = tamilDate,
                paksham = paksha,
                tithi = tithi,
                nakshatra = nakshatra,
                nallaNeram = nallaNeramCombined,
                nallaNeramMorning = nnMorning,
                nallaNeramEvening = nnEvening,
                gouriNallaNeram = gouriCombined,
                gouriNallaMorning = gnMorning,
                gouriNallaEvening = gnEvening,
                rahuKalam = timings.rahuKalam,
                yamaGandam = timings.yamaGandam,
                kuligai = timings.kuligai,
                sunrise = sunMoon.sunrise,
                sunset = sunMoon.sunset,
                moonrise = sunMoon.moonrise,
                moonset = sunMoon.moonset,
                festivalsEvents = festivals
            )
        } else {
            panCursor.close()
            panDb.close()
        }

        return result
    }

    /**
     * Reads all periods dynamically from weekday_timings_summary in auspicious_periods.db
     */
    fun getWeekdayTimingSummary(weekdayTa: String): TimingSummary {
        val db = openDatabase(DB_AUSPICIOUS)
        val cursor = db.rawQuery(
            """
            SELECT nalla_neram_morning_start, nalla_neram_morning_end,
                   nalla_neram_evening_start, nalla_neram_evening_end,
                   gowri_day_start, gowri_day_end,
                   gowri_night_start, gowri_night_end,
                   raghu_start, raghu_end,
                   emagandam_start, emagandam_end,
                   kuligai_start, kuligai_end
            FROM weekday_timings_summary
            WHERE weekday_ta = ? LIMIT 1
            """.trimIndent(),
            arrayOf(weekdayTa.trim())
        )

        var summary = TimingSummary("", "", "", "", "", "", "")

        if (cursor.moveToFirst()) {
            val formatRange: (String, String) -> String = { start, end ->
                if (start.isBlank() || start == "--:--" || end.isBlank() || end == "--:--") ""
                else "$start - $end"
            }

            summary = TimingSummary(
                nallaNeramMorning = formatRange(
                    cursor.getString(0) ?: "",
                    cursor.getString(1) ?: ""
                ),
                nallaNeramEvening = formatRange(
                    cursor.getString(2) ?: "",
                    cursor.getString(3) ?: ""
                ),
                gowriDay = formatRange(
                    cursor.getString(4) ?: "",
                    cursor.getString(5) ?: ""
                ),
                gowriNight = formatRange(
                    cursor.getString(6) ?: "",
                    cursor.getString(7) ?: ""
                ),
                rahuKalam = formatRange(
                    cursor.getString(8) ?: "",
                    cursor.getString(9) ?: ""
                ),
                yamaGandam = formatRange(
                    cursor.getString(10) ?: "",
                    cursor.getString(11) ?: ""
                ),
                kuligai = formatRange(
                    cursor.getString(12) ?: "",
                    cursor.getString(13) ?: ""
                )
            )
        }
        cursor.close()
        db.close()
        return summary
    }

    /**
     * Reads sunrise, sunset, moonrise, and moonset dynamically from sun_moon_timings.db
     */
    fun getSunMoonTiming(dateStr: String): SunMoonTiming {
        val db = openDatabase(DB_SUN_MOON)
        val cursor = db.rawQuery(
            """
            SELECT sunrise, sunset, moonrise, moonset 
            FROM sun_moon_timings 
            WHERE date_gregorian = ? LIMIT 1
            """.trimIndent(),
            arrayOf(dateStr)
        )

        var timing = SunMoonTiming("", "", "", "")
        if (cursor.moveToFirst()) {
            timing = SunMoonTiming(
                sunrise = cursor.getString(0) ?: "",
                sunset = cursor.getString(1) ?: "",
                moonrise = cursor.getString(2) ?: "",
                moonset = cursor.getString(3) ?: ""
            )
        }
        cursor.close()
        db.close()
        return timing
    }

    /**
     * Helper to read Gowri slots dynamically from gowri.db
     * @param kaalam "பகல்" or "இரவு"
     */
    fun getGowriSlots(weekdayTa: String, kaalam: String): Map<String, String> {
        val db = openDatabase(DB_GOWRI)
        val cursor = db.rawQuery(
            """
            SELECT slot_6_730, slot_730_9, slot_9_1030, slot_1030_12,
                   slot_12_130, slot_130_3, slot_3_430, slot_430_6
            FROM weekday_gowri
            WHERE weekday_ta = ? AND kaalam = ? LIMIT 1
            """.trimIndent(),
            arrayOf(weekdayTa.trim(), kaalam.trim())
        )

        val slots = linkedMapOf<String, String>()
        if (cursor.moveToFirst()) {
            slots["06:00 - 07:30"] = cursor.getString(0) ?: ""
            slots["07:30 - 09:00"] = cursor.getString(1) ?: ""
            slots["09:00 - 10:30"] = cursor.getString(2) ?: ""
            slots["10:30 - 12:00"] = cursor.getString(3) ?: ""
            slots["12:00 - 01:30"] = cursor.getString(4) ?: ""
            slots["01:30 - 03:00"] = cursor.getString(5) ?: ""
            slots["03:00 - 04:30"] = cursor.getString(6) ?: ""
            slots["04:30 - 06:00"] = cursor.getString(7) ?: ""
        }
        cursor.close()
        db.close()
        return slots
    }

    /**
     * Helper to read 12 hourly Horai planetary rulers dynamically from horai.db
     * @param kaalam "பகல்" or "இரவு"
     */
    fun getHoraiSlots(weekdayTa: String, kaalam: String): Map<String, String> {
        val db = openDatabase(DB_HORAI)
        val cursor = db.rawQuery(
            """
            SELECT slot_6_7, slot_7_8, slot_8_9, slot_9_10, slot_10_11, slot_11_12,
                   slot_12_1, slot_1_2, slot_2_3, slot_3_4, slot_4_5, slot_5_6
            FROM weekday_horai
            WHERE weekday_ta = ? AND kaalam = ? LIMIT 1
            """.trimIndent(),
            arrayOf(weekdayTa.trim(), kaalam.trim())
        )

        val slots = linkedMapOf<String, String>()
        if (cursor.moveToFirst()) {
            slots["06:00 - 07:00"] = cursor.getString(0) ?: ""
            slots["07:00 - 08:00"] = cursor.getString(1) ?: ""
            slots["08:00 - 09:00"] = cursor.getString(2) ?: ""
            slots["09:00 - 10:00"] = cursor.getString(3) ?: ""
            slots["10:00 - 11:00"] = cursor.getString(4) ?: ""
            slots["11:00 - 12:00"] = cursor.getString(5) ?: ""
            slots["12:00 - 01:00"] = cursor.getString(6) ?: ""
            slots["01:00 - 02:00"] = cursor.getString(7) ?: ""
            slots["02:00 - 03:00"] = cursor.getString(8) ?: ""
            slots["03:00 - 04:00"] = cursor.getString(9) ?: ""
            slots["04:00 - 05:00"] = cursor.getString(10) ?: ""
            slots["05:00 - 06:00"] = cursor.getString(11) ?: ""
        }
        cursor.close()
        db.close()
        return slots
    }
}
