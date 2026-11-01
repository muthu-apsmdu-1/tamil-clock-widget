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
    val festivalsEvents: String = ""
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

class TamilDatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, "panchang_clcon.db", null, 2) {

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

    private fun openDatabase(dbName: String): SQLiteDatabase {
        copyDatabase(dbName, false)
        return SQLiteDatabase.openDatabase(
            context.getDatabasePath(dbName).path,
            null,
            SQLiteDatabase.OPEN_READONLY
        )
    }

    fun getDayDetails(dateStr: String): TamilDateInfo? {
        val panDb = openDatabase(DB_PAN)
        val panCursor: Cursor = panDb.rawQuery(
            """
            SELECT gregorian_date, tamil_year, tamil_month, tamil_date, 
                   weekday_ta, paksha, tithi_name, tithi_nazhikai, 
                   nakshatra_name, nakshatra_nazhikai, yoga_name, yoga_nazhikai, 
                   karana_name, karana_nazhikai, tyajyam_nazhikai, festivals_events 
            FROM panchangam_daily 
            WHERE gregorian_date = ? LIMIT 1
            """.trimIndent(),
            arrayOf(dateStr)
        )

        var result: TamilDateInfo? = null

        if (panCursor.moveToFirst()) {
            val tamilYear = panCursor.getString(panCursor.getColumnIndexOrThrow("tamil_year")) ?: "பராபவ"
            val weekdayTa = panCursor.getString(panCursor.getColumnIndexOrThrow("weekday_ta")) ?: ""
            val tamilMonth = panCursor.getString(panCursor.getColumnIndexOrThrow("tamil_month")) ?: ""
            val tamilDate = panCursor.getInt(panCursor.getColumnIndexOrThrow("tamil_date"))
            val paksha = panCursor.getString(panCursor.getColumnIndexOrThrow("paksha")) ?: ""
            val tithi = panCursor.getString(panCursor.getColumnIndexOrThrow("tithi_name")) ?: ""
            val tithiTime = panCursor.getString(panCursor.getColumnIndexOrThrow("tithi_nazhikai")) ?: ""
            val nakshatra = panCursor.getString(panCursor.getColumnIndexOrThrow("nakshatra_name")) ?: ""
            val nakshatraTime = panCursor.getString(panCursor.getColumnIndexOrThrow("nakshatra_nazhikai")) ?: ""
            val yoga = panCursor.getString(panCursor.getColumnIndexOrThrow("yoga_name")) ?: ""
            val yogaTime = panCursor.getString(panCursor.getColumnIndexOrThrow("yoga_nazhikai")) ?: ""
            val karana = panCursor.getString(panCursor.getColumnIndexOrThrow("karana_name")) ?: ""
            val karanaTime = panCursor.getString(panCursor.getColumnIndexOrThrow("karana_nazhikai")) ?: ""
            val tyajyam = panCursor.getString(panCursor.getColumnIndexOrThrow("tyajyam_nazhikai")) ?: ""
            val festivals = panCursor.getString(panCursor.getColumnIndexOrThrow("festivals_events")) ?: ""

            panCursor.close()
            panDb.close()

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
                festivalsEvents = festivals
            )
        } else {
            panCursor.close()
            panDb.close()
        }
        return result
    }

    private fun getWeekdayTimingSummary(weekdayTa: String): WeekdayTiming {
        val map = mapOf(
            "ஞாயிறு" to WeekdayTiming("07:30 - 08:30", "03:30 - 04:30", "10:30 - 11:30", "09:30 - 10:30", "04:30 - 06:00", "12:00 - 01:30", "03:00 - 04:30"),
            "திங்கள்" to WeekdayTiming("06:30 - 07:30", "04:30 - 05:30", "09:30 - 10:30", "07:30 - 08:30", "07:30 - 09:00", "01:30 - 03:00", "12:00 - 01:30"),
            "செவ்வாய்" to WeekdayTiming("07:30 - 08:30", "04:30 - 05:30", "10:30 - 11:30", "06:30 - 07:30", "03:00 - 04:30", "09:00 - 10:30", "12:00 - 01:30"),
            "புதன்" to WeekdayTiming("09:15 - 10:15", "04:45 - 05:45", "10:45 - 11:45", "06:30 - 07:30", "12:00 - 01:30", "07:30 - 09:00", "10:30 - 12:00"),
            "வியாழன்" to WeekdayTiming("10:45 - 11:45", "12:15 - 01:15", "06:30 - 07:30", "08:00 - 09:00", "01:30 - 03:00", "06:00 - 07:30", "09:00 - 10:30"),
            "வெள்ளி" to WeekdayTiming("09:15 - 10:15", "04:45 - 05:45", "10:45 - 11:45", "08:00 - 09:00", "10:30 - 12:00", "03:00 - 04:30", "07:30 - 09:00"),
            "சனி" to WeekdayTiming("07:30 - 08:30", "05:00 - 06:00", "10:45 - 11:45", "09:30 - 10:30", "09:00 - 10:30", "01:30 - 03:00", "06:00 - 07:30")
        )
        return map[weekdayTa] ?: WeekdayTiming()
    }

    private fun getSunMoonTiming(dateStr: String): SunMoonTiming {
        var timing = SunMoonTiming("06:07 AM", "05:55 PM", "08:56 PM", "09:06 AM")
        try {
            val db = openDatabase(DB_SUN_MOON)
            val cursor = db.rawQuery(
                "SELECT sunrise, sunset, moonrise, moonset FROM sun_moon WHERE date = ? LIMIT 1",
                arrayOf(dateStr)
            )
            if (cursor.moveToFirst()) {
                timing = SunMoonTiming(
                    cursor.getString(0) ?: "06:07 AM",
                    cursor.getString(1) ?: "05:55 PM",
                    cursor.getString(2) ?: "08:56 PM",
                    cursor.getString(3) ?: "09:06 AM"
                )
            }
            cursor.close()
            db.close()
        } catch (_: Exception) {}
        return timing
    }
}
