package com.example.tamilclock

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

data class TamilDateInfo(
    val gregorianDate: String = "",
    val tamilYear: String = "பராபவ",
    val dayName: String = "",
    val dayNameTa: String = "",
    val tamilMonth: String = "",
    val tamilDay: Int = 1,
    val paksham: String = "",
    val tithi: String = "",
    val tithiTime: String = "",
    val nakshatra: String = "",
    val nakshatraTime: String = "",
    val yogaName: String = "",
    val yogaTime: String = "",
    val karanaName: String = "",
    val karanaTime: String = "",
    val tyajyamTime: String = "",
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
    val festivalsEvents: String = "",
    val soolam: String = "",
    val parikaram: String = "",
    val amritadiYoga: String = ""
)

data class WeekdayTiming(
    val nallaNeramMorning: String = "",
    val nallaNeramEvening: String = "",
    val gowriDay: String = "",
    val gowriNight: String = "",
    val rahuKalam: String = "",
    val yamaGandam: String = "",
    val kuligai: String = ""
)

data class SunMoonTiming(
    val sunrise: String = "",
    val sunset: String = "",
    val moonrise: String = "",
    val moonset: String = ""
)

class TamilDatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, "panchang_clcon.db", null, 4) {

    companion object {
        const val DB_PAN = "panchang_clcon.db"
        const val DB_AUSPICIOUS = "auspicious_periods.db"
        const val DB_SUN_MOON = "sun_moon_timings.db"
    }

    override fun onCreate(db: SQLiteDatabase?) {}
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        copyAllDatabases(force = true)
    }

    fun copyAllDatabasesIfNeeded() {
        copyDatabase(DB_PAN, false)
        copyDatabase(DB_AUSPICIOUS, false)
        copyDatabase(DB_SUN_MOON, false)
    }

    private fun copyAllDatabases(force: Boolean) {
        copyDatabase(DB_PAN, force)
        copyDatabase(DB_AUSPICIOUS, force)
        copyDatabase(DB_SUN_MOON, force)
    }

    private fun copyDatabase(dbName: String, force: Boolean) {
        val dbPath = context.getDatabasePath(dbName)
        if (!force && dbPath.exists() && dbPath.length() > 0) return

        dbPath.parentFile?.mkdirs()
        try {
            val input: InputStream = context.assets.open(dbName)
            val output: OutputStream = FileOutputStream(dbPath)
            val buffer = ByteArray(1024)
            var length: Int
            while (input.read(buffer).also { length = it } > 0) {
                output.write(buffer, 0, length)
            }
            output.flush()
            output.close()
            input.close()
        } catch (_: Exception) {}
    }

    private fun openDatabase(dbName: String): SQLiteDatabase? {
        copyDatabase(dbName, false)
        return try {
            val path = context.getDatabasePath(dbName).path
            if (File(path).exists()) {
                SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY)
            } else null
        } catch (_: Exception) {
            null
        }
    }

    fun getDayDetails(dateStr: String): TamilDateInfo? {
        var result: TamilDateInfo? = null
        var panDb: SQLiteDatabase? = null
        var panCursor: Cursor? = null

        try {
            panDb = openDatabase(DB_PAN) ?: return null
            panCursor = panDb.rawQuery(
                """
                SELECT id, gregorian_date, tamil_year, tamil_month, tamil_date, 
                       weekday_ta, paksha, tithi_name, tithi_time, 
                       nakshatra_name, nakshatra_time, yoga_name, yoga_time, 
                       karana_name, karana_time, tyajyam_time, festivals_events,
                       soolam, parikaram, amritadi_yoga
                FROM panchangam_daily 
                WHERE gregorian_date = ? LIMIT 1
                """.trimIndent(),
                arrayOf(dateStr)
            )

            if (panCursor.moveToFirst()) {
                fun getSafeString(colName: String): String {
                    val idx = panCursor.getColumnIndex(colName)
                    return if (idx >= 0 && !panCursor.isNull(idx)) panCursor.getString(idx).trim() else ""
                }

                fun getSafeInt(colName: String): Int {
                    val idx = panCursor.getColumnIndex(colName)
                    return if (idx >= 0 && !panCursor.isNull(idx)) panCursor.getInt(idx) else 1
                }

                val tamilYear = getSafeString("tamil_year").ifEmpty { "பராபவ" }
                val weekdayTa = getSafeString("weekday_ta")
                val tamilMonth = getSafeString("tamil_month")
                val tamilDate = getSafeInt("tamil_date")
                val paksha = getSafeString("paksha")
                val tithi = getSafeString("tithi_name")
                val tithiTime = getSafeString("tithi_time")
                val nakshatra = getSafeString("nakshatra_name")
                val nakshatraTime = getSafeString("nakshatra_time")
                val yoga = getSafeString("yoga_name")
                val yogaTime = getSafeString("yoga_time")
                val karana = getSafeString("karana_name")
                val karanaTime = getSafeString("karana_time")
                val tyajyam = getSafeString("tyajyam_time")
                val festivals = getSafeString("festivals_events")
                val soolam = getSafeString("soolam")
                val parikaram = getSafeString("parikaram")
                val amritadiYoga = getSafeString("amritadi_yoga")

                val timings = getWeekdayTimingSummary(weekdayTa)
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
                    tithiTime = tithiTime,
                    nakshatra = nakshatra,
                    nakshatraTime = nakshatraTime,
                    yogaName = yoga,
                    yogaTime = yogaTime,
                    karanaName = karana,
                    karanaTime = karanaTime,
                    tyajyamTime = tyajyam,
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
                    festivalsEvents = festivals,
                    soolam = soolam,
                    parikaram = parikaram,
                    amritadiYoga = amritadiYoga
                )
            }
        } catch (_: Exception) {
        } finally {
            try { panCursor?.close() } catch (_: Exception) {}
            try { panDb?.close() } catch (_: Exception) {}
        }
        return result
    }

    private fun getWeekdayTimingSummary(weekdayTa: String): WeekdayTiming {
        var timing = WeekdayTiming()
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            db = openDatabase(DB_AUSPICIOUS)
            if (db != null) {
                cursor = db.rawQuery(
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
                    arrayOf(weekdayTa)
                )
                if (cursor.moveToFirst()) {
                    fun fmt(sIdx: Int, eIdx: Int): String {
                        val s = cursor.getString(sIdx)?.trim() ?: ""
                        val e = cursor.getString(eIdx)?.trim() ?: ""
                        return if (s.isNotEmpty() && e.isNotEmpty()) "$s - $e" else ""
                    }

                    timing = WeekdayTiming(
                        nallaNeramMorning = fmt(0, 1),
                        nallaNeramEvening = fmt(2, 3),
                        gowriDay = fmt(4, 5),
                        gowriNight = fmt(6, 7),
                        rahuKalam = fmt(8, 9),
                        yamaGandam = fmt(10, 11),
                        kuligai = fmt(12, 13)
                    )
                }
            }
        } catch (_: Exception) {
        } finally {
            try { cursor?.close() } catch (_: Exception) {}
            try { db?.close() } catch (_: Exception) {}
        }
        return timing
    }

    private fun getSunMoonTiming(dateStr: String): SunMoonTiming {
        var timing = SunMoonTiming("06:07 AM", "05:55 PM", "08:56 PM", "09:06 AM")
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            db = openDatabase(DB_SUN_MOON)
            if (db != null) {
                cursor = db.rawQuery(
                    "SELECT sunrise, sunset, moonrise, moonset FROM sun_moon_timings WHERE date_gregorian = ? LIMIT 1",
                    arrayOf(dateStr)
                )
                if (cursor.moveToFirst()) {
                    timing = SunMoonTiming(
                        sunrise = cursor.getString(0) ?: "06:07 AM",
                        sunset = cursor.getString(1) ?: "05:55 PM",
                        moonrise = cursor.getString(2) ?: "08:56 PM",
                        moonset = cursor.getString(3) ?: "09:06 AM"
                    )
                }
            }
        } catch (_: Exception) {
        } finally {
            try { cursor?.close() } catch (_: Exception) {}
            try { db?.close() } catch (_: Exception) {}
        }
        return timing
    }
}
