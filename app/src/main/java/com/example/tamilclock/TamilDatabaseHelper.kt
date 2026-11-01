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
    val amritadiYoga: String = "",
    val chandrashtamamRasi: String = "",
    val rasiPalanMap: Map<String, String> = emptyMap()
)

data class WeekdayTiming(
    val nallaNeramMorning: String = "07:30 - 08:30",
    val nallaNeramEvening: String = "03:30 - 04:30",
    val gowriDay: String = "10:30 - 11:30",
    val gowriNight: String = "07:30 - 08:30",
    val rahuKalam: String = "04:30 - 06:00",
    val yamaGandam: String = "12:00 - 01:30",
    val kuligai: String = "03:00 - 04:30"
)

data class SunMoonTiming(
    val sunrise: String = "06:08 AM",
    val sunset: String = "05:55 PM",
    val moonrise: String = "08:56 PM",
    val moonset: String = "09:06 AM"
)

class TamilDatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, "panchang_clcon.db", null, 6) {

    companion object {
        const val DB_PAN = "panchang_clcon.db"
        const val DB_AUSPICIOUS = "auspicious_periods.db"
        const val DB_SUN_MOON = "sun_moon_timings.db"
        const val DB_RASIPALAN = "rasipalan_daily.db"
    }

    override fun onCreate(db: SQLiteDatabase?) {}
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        copyAllDatabases(force = true)
    }

    init {
        copyAllDatabases(force = false)
    }

    fun copyAllDatabases(force: Boolean) {
        copyDatabase(DB_PAN, force)
        copyDatabase(DB_AUSPICIOUS, force)
        copyDatabase(DB_SUN_MOON, force)
        copyDatabase(DB_RASIPALAN, force)
    }

    private fun copyDatabase(dbName: String, force: Boolean) {
        val dbPath = context.getDatabasePath(dbName)
        if (!force && dbPath.exists() && dbPath.length() > 1024) return

        dbPath.parentFile?.mkdirs()
        try {
            val input: InputStream = context.assets.open(dbName)
            val output: OutputStream = FileOutputStream(dbPath)
            val buffer = ByteArray(2048)
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
        val dbPath = context.getDatabasePath(dbName)
        if (!dbPath.exists() || dbPath.length() <= 1024) {
            copyDatabase(dbName, force = true)
        }
        return try {
            if (dbPath.exists()) {
                SQLiteDatabase.openDatabase(dbPath.path, null, SQLiteDatabase.OPEN_READONLY)
            } else null
        } catch (_: Exception) {
            null
        }
    }

    fun getDayDetails(dateStr: String): TamilDateInfo {
        var panDb: SQLiteDatabase? = null
        var panCursor: Cursor? = null

        var tamilYear = "பராபவ"
        var weekdayTa = "ஞாயிறு"
        var tamilMonth = "ஐப்பசி"
        var tamilDate = 15
        var paksha = "கிருஷ்ண பக்ஷம்"
        var tithi = "சப்தமி"
        var tithiTime = "19:24"
        var nakshatra = "புனர்பூசம்"
        var nakshatraTime = "10:37"
        var yoga = "சாத்தியம்"
        var yogaTime = "19:48"
        var karana = "பத்திரை"
        var karanaTime = "08:28"
        var tyajyam = "18:09"
        var festivals = ""
        var chandrashtamam = ""
        var soolam = "மேற்கு"
        var parikaram = "வெல்லம்"
        var amritadiYoga = "சித்த யோகம்"

        try {
            panDb = openDatabase(DB_PAN)
            if (panDb != null) {
                panCursor = panDb.rawQuery(
                    "SELECT * FROM panchangam_daily WHERE gregorian_date = ? LIMIT 1",
                    arrayOf(dateStr)
                )

                if (panCursor.moveToFirst()) {
                    fun getCol(vararg names: String): String {
                        for (name in names) {
                            val idx = panCursor.getColumnIndex(name)
                            if (idx >= 0 && !panCursor.isNull(idx)) {
                                val s = panCursor.getString(idx).trim()
                                if (s.isNotEmpty()) return s
                            }
                        }
                        return ""
                    }

                    fun getIntCol(vararg names: String, defaultVal: Int = 1): Int {
                        for (name in names) {
                            val idx = panCursor.getColumnIndex(name)
                            if (idx >= 0 && !panCursor.isNull(idx)) {
                                return panCursor.getInt(idx)
                            }
                        }
                        return defaultVal
                    }

                    tamilYear = getCol("tamil_year").ifEmpty { "பராபவ" }
                    weekdayTa = getCol("weekday_ta")
                    tamilMonth = getCol("tamil_month")
                    tamilDate = getIntCol("tamil_date", defaultVal = 15)
                    paksha = getCol("paksha")
                    tithi = getCol("tithi_name", "tithi")
                    tithiTime = getCol("tithi_time", "tithi_nazhikai")
                    nakshatra = getCol("nakshatra_name", "nakshatra")
                    nakshatraTime = getCol("nakshatra_time", "nakshatra_nazhikai")
                    yoga = getCol("yoga_name", "yoga")
                    yogaTime = getCol("yoga_time", "yoga_nazhikai")
                    karana = getCol("karana_name", "karana")
                    karanaTime = getCol("karana_time", "karana_nazhikai")
                    tyajyam = getCol("tyajyam_time", "tyajyam_nazhikai")
                    festivals = getCol("festivals_events", "festivals")
                    chandrashtamam = getCol("chandrashtaman_rasi", "chandrashtamam_rasi", "chandrashtama_rasi")
                    soolam = getCol("soolam").ifEmpty { "மேற்கு" }
                    parikaram = getCol("parikaram").ifEmpty { "வெல்லம்" }
                    amritadiYoga = getCol("amritadi_yoga").ifEmpty { "சித்த யோகம்" }
                }
            }
        } catch (_: Exception) {
        } finally {
            try { panCursor?.close() } catch (_: Exception) {}
            try { panDb?.close() } catch (_: Exception) {}
        }

        val timings = getWeekdayTimingSummary(weekdayTa)
        val sunMoon = getSunMoonTiming(dateStr)
        val rasiPalan = getDailyRasiPalan(dateStr)

        val nnMorning = timings.nallaNeramMorning
        val nnEvening = timings.nallaNeramEvening
        val gnMorning = timings.gowriDay
        val gnEvening = timings.gowriNight

        val nallaNeramCombined = listOf(nnMorning, nnEvening).filter { it.isNotBlank() && it != "--:--" }.joinToString(" / ")
        val gouriCombined = listOf(gnMorning, gnEvening).filter { it.isNotBlank() && it != "--:--" }.joinToString(" / ")

        return TamilDateInfo(
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
            amritadiYoga = amritadiYoga,
            chandrashtamamRasi = chandrashtamam,
            rasiPalanMap = rasiPalan
        )
    }

    private fun getDailyRasiPalan(dateStr: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            db = openDatabase(DB_RASIPALAN)
            if (db != null) {
                cursor = db.rawQuery(
                    "SELECT * FROM rasipalan_daily WHERE gregorian_date = ? LIMIT 1",
                    arrayOf(dateStr)
                )
                if (cursor.moveToFirst()) {
                    fun getRasi(col: String, fallback: String): String {
                        val idx = cursor.getColumnIndex(col)
                        return if (idx >= 0 && !cursor.isNull(idx)) cursor.getString(idx).trim() else fallback
                    }
                    map["mesham"] = getRasi("mesham", "உற்சாகம்")
                    map["rishabam"] = getRasi("rishabam", "இன்பம்")
                    map["mithunam"] = getRasi("mithunam", "பொறுமை")
                    map["kadagam"] = getRasi("katakam", "சுகம்").ifEmpty { getRasi("kadagam", "சுகம்") }
                    map["simmam"] = getRasi("simmam", "பக்தி")
                    map["kanni"] = getRasi("kanni", "ஆக்கம்")
                    map["thulaam"] = getRasi("thulaam", "முயற்சி")
                    map["viruchigam"] = getRasi("viruchigam", "வரவு")
                    map["dhanusu"] = getRasi("dhanusu", "சாந்தம்")
                    map["magaram"] = getRasi("makaram", "மேன்மை").ifEmpty { getRasi("magaram", "மேன்மை") }
                    map["kumbam"] = getRasi("kumbam", "தனம்")
                    map["meenam"] = getRasi("meenam", "புகழ்")
                }
            }
        } catch (_: Exception) {
        } finally {
            try { cursor?.close() } catch (_: Exception) {}
            try { db?.close() } catch (_: Exception) {}
        }
        return map
    }

    private fun getWeekdayTimingSummary(weekdayTa: String): WeekdayTiming {
        val staticMap = mapOf(
            "ஞாயிறு" to WeekdayTiming("07:30 - 08:30", "03:30 - 04:30", "10:30 - 11:30", "09:30 - 10:30", "04:30 - 06:00", "12:00 - 01:30", "03:00 - 04:30"),
            "திங்கள்" to WeekdayTiming("06:30 - 07:30", "04:30 - 05:30", "09:30 - 10:30", "07:30 - 08:30", "07:30 - 09:00", "01:30 - 03:00", "12:00 - 01:30"),
            "செவ்வாய்" to WeekdayTiming("07:30 - 08:30", "04:30 - 05:30", "10:30 - 11:30", "06:30 - 07:30", "03:00 - 04:30", "09:00 - 10:30", "12:00 - 01:30"),
            "புதன்" to WeekdayTiming("09:15 - 10:15", "04:45 - 05:45", "10:45 - 11:45", "06:30 - 07:30", "12:00 - 01:30", "07:30 - 09:00", "10:30 - 12:00"),
            "வியாழன்" to WeekdayTiming("10:45 - 11:45", "12:15 - 01:15", "06:30 - 07:30", "08:00 - 09:00", "01:30 - 03:00", "06:00 - 07:30", "09:00 - 10:30"),
            "வெள்ளி" to WeekdayTiming("09:15 - 10:15", "04:45 - 05:45", "10:45 - 11:45", "08:00 - 09:00", "10:30 - 12:00", "03:00 - 04:30", "07:30 - 09:00"),
            "சனி" to WeekdayTiming("07:30 - 08:30", "05:00 - 06:00", "10:45 - 11:45", "09:30 - 10:30", "09:00 - 10:30", "01:30 - 03:00", "06:00 - 07:30")
        )

        var timing = staticMap[weekdayTa] ?: WeekdayTiming()
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            db = openDatabase(DB_AUSPICIOUS)
            if (db != null) {
                cursor = db.rawQuery(
                    "SELECT * FROM weekday_timings_summary WHERE weekday_ta = ? LIMIT 1",
                    arrayOf(weekdayTa)
                )
                if (cursor.moveToFirst()) {
                    fun fmt(sCol: String, eCol: String): String {
                        val sIdx = cursor.getColumnIndex(sCol)
                        val eIdx = cursor.getColumnIndex(eCol)
                        val s = if (sIdx >= 0) cursor.getString(sIdx)?.trim() ?: "" else ""
                        val e = if (eIdx >= 0) cursor.getString(eIdx)?.trim() ?: "" else ""
                        return if (s.isNotEmpty() && e.isNotEmpty()) "$s - $e" else ""
                    }

                    val nnM = fmt("nalla_neram_morning_start", "nalla_neram_morning_end")
                    val nnE = fmt("nalla_neram_evening_start", "nalla_neram_evening_end")
                    val gd = fmt("gowri_day_start", "gowri_day_end")
                    val gn = fmt("gowri_night_start", "gowri_night_end")
                    val rk = fmt("raghu_start", "raghu_end")
                    val yg = fmt("emagandam_start", "emagandam_end")
                    val kg = fmt("kuligai_start", "kuligai_end")

                    timing = WeekdayTiming(
                        nallaNeramMorning = nnM.ifEmpty { timing.nallaNeramMorning },
                        nallaNeramEvening = nnE.ifEmpty { timing.nallaNeramEvening },
                        gowriDay = gd.ifEmpty { timing.gowriDay },
                        gowriNight = gn.ifEmpty { timing.gowriNight },
                        rahuKalam = rk.ifEmpty { timing.rahuKalam },
                        yamaGandam = yg.ifEmpty { timing.yamaGandam },
                        kuligai = kg.ifEmpty { timing.kuligai }
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
        var timing = SunMoonTiming("06:08 AM", "05:55 PM", "08:56 PM", "09:06 AM")
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
                        sunrise = cursor.getString(0) ?: "06:08 AM",
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
