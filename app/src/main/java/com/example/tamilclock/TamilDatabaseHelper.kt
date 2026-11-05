package com.example.tamilclock

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.FileOutputStream

/**
 * Reads the six supplied SQLite databases.
 *
 * Required assets:
 * app/src/main/assets/panchang_clcon.db
 * app/src/main/assets/rasipalan_daily.db
 * app/src/main/assets/sun_moon_timings.db
 * app/src/main/assets/auspicious_periods.db
 * app/src/main/assets/gowri.db
 * app/src/main/assets/horai.db
 *
 * Original database schemas are preserved.
 */
class TamilDatabaseHelper(context: Context) {

    private val appContext = context.applicationContext

    private val databaseNames = listOf(
        "panchang_clcon.db",
        "rasipalan_daily.db",
        "sun_moon_timings.db",
        "auspicious_periods.db",
        "gowri.db",
        "horai.db"
    )

    private val databases = mutableMapOf<String, SQLiteDatabase>()

    init {
        copyDatabasesIfRequired()
    }

    // ---------------------------------------------------------
    // 1. COPY DATABASE ASSETS SAFELY
    // ---------------------------------------------------------

    private fun copyDatabasesIfRequired() {
        val dummyPath = appContext.getDatabasePath("dummy.db")
        val directory = dummyPath.parentFile ?: File(appContext.applicationInfo.dataDir, "databases")

        if (!directory.exists()) {
            directory.mkdirs()
        }

        for (databaseName in databaseNames) {
            val destination = File(directory, databaseName)

            if (!destination.exists()) {
                val temporaryFile = File(directory, "$databaseName.tmp")

                try {
                    appContext.assets.open(databaseName).use { input ->
                        FileOutputStream(temporaryFile).use { output ->
                            input.copyTo(output)
                            output.flush()
                        }
                    }

                    if (!temporaryFile.renameTo(destination)) {
                        temporaryFile.copyTo(destination, overwrite = true)
                        temporaryFile.delete()
                    }
                } catch (e: Exception) {
                    temporaryFile.delete()
                    e.printStackTrace()
                }
            }
        }
    }

    // ---------------------------------------------------------
    // 2. OPEN DATABASE
    // ---------------------------------------------------------

    private fun getDatabase(databaseName: String): SQLiteDatabase {
        databases[databaseName]?.let {
            if (it.isOpen) return it
        }

        val file = appContext.getDatabasePath(databaseName)

        val database = SQLiteDatabase.openDatabase(
            file.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY
        )

        databases[databaseName] = database
        return database
    }

    // ---------------------------------------------------------
    // 3. CURSOR HELPERS
    // ---------------------------------------------------------

    private fun Cursor.string(column: String): String {
        val index = getColumnIndex(column)
        return if (index >= 0 && !isNull(index)) {
            getString(index) ?: ""
        } else {
            ""
        }
    }

    private fun Cursor.intValue(column: String): Int {
        val index = getColumnIndex(column)
        return if (index >= 0 && !isNull(index)) {
            getInt(index)
        } else {
            0
        }
    }

    private fun queryOne(
        databaseName: String,
        tableName: String,
        selection: String,
        selectionArgs: Array<String>
    ): Cursor? {
        return try {
            getDatabase(databaseName).query(
                tableName,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null,
                "1"
            ).let { cursor ->
                if (cursor.moveToFirst()) cursor else {
                    cursor.close()
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    // ---------------------------------------------------------
    // 4. MAIN PANCHANGAM DATA MODEL
    // ---------------------------------------------------------

data class DayPanchangDetails(
    val gregorianDate: String = "",
    val tamilYear: String = "",
    val tamilMonth: String = "",
    val tamilDate: Int = 0,               // DB Column (INTEGER)
    val weekdayTa: String = "",           // DB Column (TEXT)
    val paksha: String = "",
    val tithiName: String = "",
    val tithiTime: String = "",
    val nakshatraName: String = "",
    val nakshatraTime: String = "",
    val yogaName: String = "",
    val yogaTime: String = "",
    val amritadiYoga: String = "",
    val karanaName: String = "",
    val karanaTime: String = "",
    val tyajyamTime: String = "",
    val festivalsEvents: String = "",
    val chandrashtamamRasi: String = "",
    val soolam: String = "",
    val parikaram: String = "",
    val sunrise: String = "",
    val sunset: String = "",
    val moonrise: String = "",
    val moonset: String = "",
    val nallaNeramMorningStart: String = "",
    val nallaNeramMorningEnd: String = "",
    val nallaNeramEveningStart: String = "",
    val nallaNeramEveningEnd: String = "",
    val gowriDayStart: String = "",
    val gowriDayEnd: String = "",
    val gowriNightStart: String = "",
    val gowriNightEnd: String = "",
    val raghuStart: String = "",
    val raghuEnd: String = "",
    val emagandamStart: String = "",
    val emagandamEnd: String = "",
    val kuligaiStart: String = "",
    val kuligaiEnd: String = ""
) {
    // -------------------------------------------------------------
    // DATE & DAY COMPATIBILITY ALIASES
    // -------------------------------------------------------------
    
    // Resolves CalendarMonthAdapter.kt & Widget date number text as String
    val tamilDay: String get() = if (tamilDate > 0) tamilDate.toString() else ""
    val tamilDateStr: String get() = tamilDay

    // Weekday Name String
    val dayName: String get() = weekdayTa
    val dayNameTa: String get() = weekdayTa

    // -------------------------------------------------------------
    // ASTROLOGICAL ALIASES
    // -------------------------------------------------------------
    val tithi: String get() = tithiName
    val nakshatra: String get() = nakshatraName
    val yoga: String get() = if (amritadiYoga.isNotBlank()) amritadiYoga else yogaName
    val paksham: String get() = paksha
    val karanam: String get() = karanaName
    val chandrashtamam: String get() = chandrashtamamRasi
    val festivals: String get() = festivalsEvents
    val tyajyam: String get() = tyajyamTime

    // Timings aliases for Widgets and FragmentPanchangam
    val nallaNeramMorning: String get() = combineTime(nallaNeramMorningStart, nallaNeramMorningEnd)
    val nallaNeramEvening: String get() = combineTime(nallaNeramEveningStart, nallaNeramEveningEnd)
    val nallaNeramMorn: String get() = nallaNeramMorning
    val nallaNeramEve: String get() = nallaNeramEvening

    val gowriMorning: String get() = combineTime(gowriDayStart, gowriDayEnd)
    val gowriNight: String get() = combineTime(gowriNightStart, gowriNightEnd)
    val gowriMorn: String get() = gowriMorning
    val gowriEve: String get() = gowriNight

    val rahuKaalam: String get() = combineTime(raghuStart, raghuEnd)
    val rahu: String get() = rahuKaalam
    val yamagandam: String get() = combineTime(emagandamStart, emagandamEnd)
    val kuligai: String get() = combineTime(kuligaiStart, kuligaiEnd)

    private fun combineTime(start: String, end: String): String {
        return if (start.isNotBlank() && end.isNotBlank()) "$start - $end" else ""
    }
}


    // ---------------------------------------------------------
    // 5. RASI PALAN DATA MODEL
    // ---------------------------------------------------------

    data class RasiPalanDetails(
        val gregorianDate: String = "",
        val tamilMonth: String = "",
        val tamilDate: Int = 0,
        val weekdayTa: String = "",
        val nakshatraName: String = "",
        val mesham: String = "",
        val rishabam: String = "",
        val mithunam: String = "",
        val katakam: String = "",
        val simmam: String = "",
        val kanni: String = "",
        val thulaam: String = "",
        val viruchigam: String = "",
        val dhanusu: String = "",
        val makaram: String = "",
        val kumbam: String = "",
        val meenam: String = ""
    ) {
        val rasiKadagam: String get() = katakam
    }

    // ---------------------------------------------------------
    // 6. GOWRI DATABASE DATA MODEL
    // ---------------------------------------------------------

    data class GowriDetails(
        val weekdayTa: String,
        val kaalam: String,
        val slots: List<String>
    )

    // ---------------------------------------------------------
    // 7. HORAI DATABASE DATA MODEL
    // ---------------------------------------------------------

    data class HoraiDetails(
        val weekdayTa: String,
        val kaalam: String,
        val slots: List<String>
    )

    // ---------------------------------------------------------
    // 8. GET COMPLETE DAILY PANCHANGAM
    // ---------------------------------------------------------

    fun getDayDetails(gregorianDate: String): DayPanchangDetails? {
        val panchangCursor = queryOne(
            "panchang_clcon.db",
            "panchangam_daily",
            "gregorian_date = ?",
            arrayOf(gregorianDate)
        ) ?: return null

        val panchang = panchangCursor.use { cursor ->
            DayPanchangDetails(
                gregorianDate = cursor.string("gregorian_date"),
                tamilYear = cursor.string("tamil_year"),
                tamilMonth = cursor.string("tamil_month"),
                tamilDate = cursor.intValue("tamil_date"),
                weekdayTa = cursor.string("weekday_ta"),
                paksha = cursor.string("paksha"),
                tithiName = cursor.string("tithi_name"),
                tithiTime = cursor.string("tithi_time"),
                nakshatraName = cursor.string("nakshatra_name"),
                nakshatraTime = cursor.string("nakshatra_time"),
                yogaName = cursor.string("yoga_name"),
                yogaTime = cursor.string("yoga_time"),
                amritadiYoga = cursor.string("amritadi_yoga"),
                karanaName = cursor.string("karana_name"),
                karanaTime = cursor.string("karana_time"),
                tyajyamTime = cursor.string("tyajyam_time"),
                festivalsEvents = cursor.string("festivals_events"),
                chandrashtamamRasi = cursor.string("chandrashtamam_rasi"),
                soolam = cursor.string("soolam"),
                parikaram = cursor.string("parikaram")
            )
        }

        val sunMoonCursor = queryOne(
            "sun_moon_timings.db",
            "sun_moon_timings",
            "date_gregorian = ?",
            arrayOf(gregorianDate)
        )

        val sunMoon = sunMoonCursor?.use { cursor ->
            listOf(
                cursor.string("sunrise"),
                cursor.string("sunset"),
                cursor.string("moonrise"),
                cursor.string("moonset")
            )
        } ?: listOf("", "", "", "")

        val auspiciousCursor = queryOne(
            "auspicious_periods.db",
            "weekday_timings_summary",
            "weekday_ta = ?",
            arrayOf(panchang.weekdayTa)
        )

        val auspicious = auspiciousCursor?.use { cursor ->
            listOf(
                cursor.string("nalla_neram_morning_start"),
                cursor.string("nalla_neram_morning_end"),
                cursor.string("nalla_neram_evening_start"),
                cursor.string("nalla_neram_evening_end"),
                cursor.string("gowri_day_start"),
                cursor.string("gowri_day_end"),
                cursor.string("gowri_night_start"),
                cursor.string("gowri_night_end"),
                cursor.string("raghu_start"),
                cursor.string("raghu_end"),
                cursor.string("emagandam_start"),
                cursor.string("emagandam_end"),
                cursor.string("kuligai_start"),
                cursor.string("kuligai_end")
            )
        } ?: List(14) { "" }

        return panchang.copy(
            sunrise = sunMoon[0],
            sunset = sunMoon[1],
            moonrise = sunMoon[2],
            moonset = sunMoon[3],
            nallaNeramMorningStart = auspicious[0],
            nallaNeramMorningEnd = auspicious[1],
            nallaNeramEveningStart = auspicious[2],
            nallaNeramEveningEnd = auspicious[3],
            gowriDayStart = auspicious[4],
            gowriDayEnd = auspicious[5],
            gowriNightStart = auspicious[6],
            gowriNightEnd = auspicious[7],
            raghuStart = auspicious[8],
            raghuEnd = auspicious[9],
            emagandamStart = auspicious[10],
            emagandamEnd = auspicious[11],
            kuligaiStart = auspicious[12],
            kuligaiEnd = auspicious[13]
        )
    }

    // ---------------------------------------------------------
    // 9. GET DAILY RASI PALAN
    // ---------------------------------------------------------

    fun getRasiDetails(gregorianDate: String): RasiPalanDetails? {
        val cursor = queryOne(
            "rasipalan_daily.db",
            "rasipalan_daily",
            "gregorian_date = ?",
            arrayOf(gregorianDate)
        ) ?: return null

        return cursor.use {
            RasiPalanDetails(
                gregorianDate = it.string("gregorian_date"),
                tamilMonth = it.string("tamil_month"),
                tamilDate = it.intValue("tamil_date"),
                weekdayTa = it.string("weekday_ta"),
                nakshatraName = it.string("nakshatra_name"),
                mesham = it.string("mesham"),
                rishabam = it.string("rishabam"),
                mithunam = it.string("mithunam"),
                katakam = it.string("katakam"),
                simmam = it.string("simmam"),
                kanni = it.string("kanni"),
                thulaam = it.string("thulaam"),
                viruchigam = it.string("viruchigam"),
                dhanusu = it.string("dhanusu"),
                makaram = it.string("makaram"),
                kumbam = it.string("kumbam"),
                meenam = it.string("meenam")
            )
        }
    }

    // ---------------------------------------------------------
    // 10. GET GOWRI DAY / NIGHT ROW
    // ---------------------------------------------------------

    fun getGowriDetails(weekdayTa: String, kaalam: String): GowriDetails? {
        val cursor = queryOne(
            "gowri.db",
            "weekday_gowri",
            "weekday_ta = ? AND kaalam = ?",
            arrayOf(weekdayTa, kaalam)
        ) ?: return null

        return cursor.use {
            GowriDetails(
                weekdayTa = it.string("weekday_ta"),
                kaalam = it.string("kaalam"),
                slots = listOf(
                    it.string("slot_6_730"),
                    it.string("slot_730_9"),
                    it.string("slot_9_1030"),
                    it.string("slot_1030_12"),
                    it.string("slot_12_130"),
                    it.string("slot_130_3"),
                    it.string("slot_3_430"),
                    it.string("slot_430_6")
                )
            )
        }
    }

    // ---------------------------------------------------------
    // 11. GET HORAI DAY / NIGHT ROW
    // ---------------------------------------------------------

    fun getHoraiDetails(weekdayTa: String, kaalam: String): HoraiDetails? {
        val cursor = queryOne(
            "horai.db",
            "weekday_horai",
            "weekday_ta = ? AND kaalam = ?",
            arrayOf(weekdayTa, kaalam)
        ) ?: return null

        return cursor.use {
            HoraiDetails(
                weekdayTa = it.string("weekday_ta"),
                kaalam = it.string("kaalam"),
                slots = listOf(
                    it.string("slot_6_7"),
                    it.string("slot_7_8"),
                    it.string("slot_8_9"),
                    it.string("slot_9_10"),
                    it.string("slot_10_11"),
                    it.string("slot_11_12"),
                    it.string("slot_12_1"),
                    it.string("slot_1_2"),
                    it.string("slot_2_3"),
                    it.string("slot_3_4"),
                    it.string("slot_4_5"),
                    it.string("slot_5_6")
                )
            )
        }
    }

    // ---------------------------------------------------------
    // 12. CLOSE ALL DATABASE CONNECTIONS
    // ---------------------------------------------------------

    fun closeDatabases() {
        databases.values.forEach { database ->
            if (database.isOpen) {
                database.close()
            }
        }
        databases.clear()
    }

    fun close() {
        closeDatabases()
    }
}
