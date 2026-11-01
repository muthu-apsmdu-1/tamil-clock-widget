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
    val tamilYear: String = "",
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

data class GowriSlotItem(
    val timeSlot: String,
    val gowriName: String,
    val isGood: Boolean
)

data class HoraiSlotItem(
    val timeSlot: String,
    val planetName: String
)

class TamilDatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, "panchang_clcon.db", null, 10) {

    companion object {
        const val DB_PAN = "panchang_clcon.db"
        const val DB_AUSPICIOUS = "auspicious_periods.db"
        const val DB_SUN_MOON = "sun_moon_timings.db"
        const val DB_RASIPALAN = "rasipalan_daily.db"
        const val DB_GOWRI = "gowri.db"
        const val DB_HORAI = "horai.db"
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
        copyDatabase(DB_GOWRI, force)
        copyDatabase(DB_HORAI, force)
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

    // 1. PANCHANGAM & DAILY DATA
    fun getDayDetails(dateStr: String): TamilDateInfo {
        var panDb: SQLiteDatabase? = null
        var panCursor: Cursor? = null

        var tamilYear = ""
        var weekdayTa = ""
        var tamilMonth = ""
        var tamilDate = 1
        var paksha = ""
        var tithi = ""
        var tithiTime = ""
        var nakshatra = ""
        var nakshatraTime = ""
        var yoga = ""
        var yogaTime = ""
        var karana = ""
        var karanaTime = ""
        var tyajyam = ""
        var festivals = ""
        var chandrashtamam = ""
        var soolam = ""
        var parikaram = ""
        var amritadiYoga = ""

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

                    tamilYear = getCol("tamil_year")
                    weekdayTa = getCol("weekday_ta")
                    tamilMonth = getCol("tamil_month")
                    tamilDate = getIntCol("tamil_date", defaultVal = 1)
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
                    soolam = getCol("soolam")
                    parikaram = getCol("parikaram")
                    amritadiYoga = getCol("amritadi_yoga")
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

    // 2. GOWRI PANCHANGAM (Queried directly from gowri.db -> table: weekday_gowri)
    fun getGowriPanchangam(weekdayTa: String): Pair<List<GowriSlotItem>, List<GowriSlotItem>> {
        val dayList = mutableListOf<GowriSlotItem>()
        val nightList = mutableListOf<GowriSlotItem>()

        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null

        val slotTimesDay = listOf(
            "காலை 06:00 - 07:30", "காலை 07:30 - 09:00", "காலை 09:00 - 10:30", "காலை 10:30 - 12:00",
            "மதியம் 12:00 - 01:30", "மதியம் 01:30 - 03:00", "மாலை 03:00 - 04:30", "மாலை 04:30 - 06:00"
        )

        val slotTimesNight = listOf(
            "மாலை 06:00 - 07:30", "இரவு 07:30 - 09:00", "இரவு 09:00 - 10:30", "இரவு 10:30 - 12:00",
            "நள்ளிரவு 12:00 - 01:30", "நள்ளிரவு 01:30 - 03:00", "விடியற்காலை 03:00 - 04:30", "விடியற்காலை 04:30 - 06:00"
        )

        val slotColumns = listOf(
            "slot_6_730", "slot_730_9", "slot_9_1030", "slot_1030_12",
            "slot_12_130", "slot_130_3", "slot_3_430", "slot_430_6"
        )

        try {
            db = openDatabase(DB_GOWRI)
            if (db != null) {
                cursor = db.rawQuery(
                    "SELECT * FROM weekday_gowri WHERE weekday_ta = ?",
                    arrayOf(weekdayTa)
                )

                while (cursor.moveToNext()) {
                    val kaalamIdx = cursor.getColumnIndex("kaalam")
                    val kaalam = if (kaalamIdx >= 0) cursor.getString(kaalamIdx)?.trim() ?: "" else ""
                    val isDay = kaalam.contains("பகல்")

                    for (i in slotColumns.indices) {
                        val colIdx = cursor.getColumnIndex(slotColumns[i])
                        if (colIdx >= 0 && !cursor.isNull(colIdx)) {
                            val name = cursor.getString(colIdx).trim()
                            val timeStr = if (isDay) slotTimesDay[i] else slotTimesNight[i]
                            val isGood = name in listOf("அமிர்தம்", "சுபம்", "லாபம்", "தனம்", "உத்தி")

                            if (isDay) {
                                dayList.add(GowriSlotItem(timeStr, name, isGood))
                            } else {
                                nightList.add(GowriSlotItem(timeStr, name, isGood))
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
        } finally {
            try { cursor?.close() } catch (_: Exception) {}
            try { db?.close() } catch (_: Exception) {}
        }

        return Pair(dayList, nightList)
    }

    // 3. HORAI TIMINGS (Queried directly from horai.db -> table: weekday_horai)
    fun getHoraiTimings(weekdayTa: String): List<HoraiSlotItem> {
        val list = mutableListOf<HoraiSlotItem>()
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null

        val slotTimes = listOf(
            "காலை 06:00 - 07:00",
            "காலை 07:00 - 08:00",
            "காலை 08:00 - 09:00",
            "காலை 09:00 - 10:00",
            "காலை 10:00 - 11:00",
            "காலை 11:00 - 12:00",
            "மதியம் 12:00 - 01:00",
            "மதியம் 01:00 - 02:00",
            "மதியம் 02:00 - 03:00",
            "மாலை 03:00 - 04:00",
            "மாலை 04:00 - 05:00",
            "மாலை 05:00 - 06:00"
        )

        val slotColumns = listOf(
            "slot_6_7", "slot_7_8", "slot_8_9", "slot_9_10",
            "slot_10_11", "slot_11_12", "slot_12_1", "slot_1_2",
            "slot_2_3", "slot_3_4", "slot_4_5", "slot_5_6"
        )

        try {
            db = openDatabase(DB_HORAI)
            if (db != null) {
                cursor = db.rawQuery(
                    "SELECT * FROM weekday_horai WHERE weekday_ta = ? AND kaalam LIKE '%பகல்%' LIMIT 1",
                    arrayOf(weekdayTa)
                )

                if (cursor.moveToFirst()) {
                    for (i in slotColumns.indices) {
                        val colIdx = cursor.getColumnIndex(slotColumns[i])
                        if (colIdx >= 0 && !cursor.isNull(colIdx)) {
                            val planetName = cursor.getString(colIdx).trim()
                            if (planetName.isNotEmpty()) {
                                list.add(HoraiSlotItem(slotTimes[i], planetName))
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
        } finally {
            try { cursor?.close() } catch (_: Exception) {}
            try { db?.close() } catch (_: Exception) {}
        }

        return list
    }

    // 4. AUSPICIOUS TIMINGS
    private fun getWeekdayTimingSummary(weekdayTa: String): WeekdayTiming {
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        var timing = WeekdayTiming()
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

                    timing = WeekdayTiming(
                        nallaNeramMorning = fmt("nalla_neram_morning_start", "nalla_neram_morning_end"),
                        nallaNeramEvening = fmt("nalla_neram_evening_start", "nalla_neram_evening_end"),
                        gowriDay = fmt("gowri_day_start", "gowri_day_end"),
                        gowriNight = fmt("gowri_night_start", "gowri_night_end"),
                        rahuKalam = fmt("raghu_start", "raghu_end"),
                        yamaGandam = fmt("emagandam_start", "emagandam_end"),
                        kuligai = fmt("kuligai_start", "kuligai_end")
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

    // 5. SUN & MOON TIMINGS
    private fun getSunMoonTiming(dateStr: String): SunMoonTiming {
        var timing = SunMoonTiming()
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
                        sunrise = cursor.getString(0) ?: "",
                        sunset = cursor.getString(1) ?: "",
                        moonrise = cursor.getString(2) ?: "",
                        moonset = cursor.getString(3) ?: ""
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

    // 6. RASI PALAN
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
                    fun getRasi(col: String): String {
                        val idx = cursor.getColumnIndex(col)
                        return if (idx >= 0 && !cursor.isNull(idx)) cursor.getString(idx).trim() else ""
                    }
                    map["mesham"] = getRasi("mesham")
                    map["rishabam"] = getRasi("rishabam")
                    map["mithunam"] = getRasi("mithunam")
                    map["kadagam"] = getRasi("katakam").ifEmpty { getRasi("kadagam") }
                    map["simmam"] = getRasi("simmam")
                    map["kanni"] = getRasi("kanni")
                    map["thulaam"] = getRasi("thulaam")
                    map["viruchigam"] = getRasi("viruchigam")
                    map["dhanusu"] = getRasi("dhanusu")
                    map["magaram"] = getRasi("makaram").ifEmpty { getRasi("magaram") }
                    map["kumbam"] = getRasi("kumbam")
                    map["meenam"] = getRasi("meenam")
                }
            }
        } catch (_: Exception) {
        } finally {
            try { cursor?.close() } catch (_: Exception) {}
            try { db?.close() } catch (_: Exception) {}
        }
        return map
    }
}
