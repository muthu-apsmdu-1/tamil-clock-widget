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
 * Missing/blank values resolve to "---".
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
    // 3. CURSOR HELPERS WITH "---" SAFE RESOLUTION
    // ---------------------------------------------------------

    private fun Cursor.safeString(column: String): String {
        val index = getColumnIndex(column)
        return if (index >= 0 && !isNull(index)) {
            val value = getString(index)?.trim()
            if (value.isNullOrEmpty()) "---" else value
        } else {
            "---"
        }
    }

    private fun Cursor.safeInt(column: String): Int {
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
        } catch (_: Exception) {
            null
        }
    }

    // ---------------------------------------------------------
    // 4. MAIN PANCHANGAM DATA MODEL
    // ---------------------------------------------------------

    data class DayPanchangDetails(
        val gregorianDate: String = "---",
        val tamilYear: String = "---",
        val tamilMonth: String = "---",
        val tamilDate: Int = 0,
        val weekdayTa: String = "---",
        val paksha: String = "---",
        val tithiName: String = "---",
        val tithiTime: String = "---",
        val nakshatraName: String = "---",
        val nakshatraTime: String = "---",
        val yogaName: String = "---",
        val yogaTime: String = "---",
        val amritadiYoga: String = "---",
        val karanaName: String = "---",
        val karanaTime: String = "---",
        val tyajyamTime: String = "---",
        val festivalsEvents: String = "",
        val chandrashtamamRasi: String = "---",
        val soolam: String = "---",
        val parikaram: String = "---",

        // Sun and moon database
        val sunrise: String = "---",
        val sunset: String = "---",
        val moonrise: String = "---",
        val moonset: String = "---",

        // Auspicious periods database
        val nallaNeramMorningStart: String = "---",
        val nallaNeramMorningEnd: String = "---",
        val nallaNeramEveningStart: String = "---",
        val nallaNeramEveningEnd: String = "---",
        val gowriDayStart: String = "---",
        val gowriDayEnd: String = "---",
        val gowriNightStart: String = "---",
        val gowriNightEnd: String = "---",
        val raghuStart: String = "---",
        val raghuEnd: String = "---",
        val emagandamStart: String = "---",
        val emagandamEnd: String = "---",
        val kuligaiStart: String = "---",
        val kuligaiEnd: String = "---",

        // Daily Rasi Palan
        val rasiPalan: RasiPalanDetails? = null
    ) {
        // Compatibility Aliases
        val tamilDay: Int get() = tamilDate
        val tamilDateStr: String get() = if (tamilDate > 0) tamilDate.toString() else "---"
        val tamilDayStr: String get() = tamilDateStr

        val dayName: String get() = weekdayTa
        val dayNameTa: String get() = weekdayTa

        val tithi: String get() = tithiName
        val nakshatra: String get() = nakshatraName
        val yoga: String get() = if (amritadiYoga != "---" && amritadiYoga.isNotBlank()) amritadiYoga else yogaName
        val paksham: String get() = paksha
        val karanam: String get() = karanaName
        val chandrashtamam: String get() = chandrashtamamRasi
        val festivals: String get() = festivalsEvents
        val tyajyam: String get() = tyajyamTime

        val nallaNeramMorning: String get() = combine(nallaNeramMorningStart, nallaNeramMorningEnd)
        val nallaNeramEvening: String get() = combine(nallaNeramEveningStart, nallaNeramEveningEnd)
        val nallaNeramMorn: String get() = nallaNeramMorning
        val nallaNeramEve: String get() = nallaNeramEvening

        val gowriMorning: String get() = combine(gowriDayStart, gowriDayEnd)
        val gowriNight: String get() = combine(gowriNightStart, gowriNightEnd)
        val gowriMorn: String get() = gowriMorning
        val gowriEve: String get() = gowriNight

        val rahuKaalam: String get() = combine(raghuStart, raghuEnd)
        val rahu: String get() = rahuKaalam
        val yamagandam: String get() = combine(emagandamStart, emagandamEnd)
        val kuligai: String get() = combine(kuligaiStart, kuligaiEnd)

        // Rasi 12-box aliases
        val rasiMesham: String get() = rasiPalan?.mesham ?: "---"
        val rasiRishabam: String get() = rasiPalan?.rishabam ?: "---"
        val rasiMithunam: String get() = rasiPalan?.mithunam ?: "---"
        val rasiKadagam: String get() = rasiPalan?.katakam ?: "---"
        val rasiSimmam: String get() = rasiPalan?.simmam ?: "---"
        val rasiKanni: String get() = rasiPalan?.kanni ?: "---"
        val rasiThulaam: String get() = rasiPalan?.thulaam ?: "---"
        val rasiViruchigam: String get() = rasiPalan?.viruchigam ?: "---"
        val rasiDhanusu: String get() = rasiPalan?.dhanusu ?: "---"
        val rasiMagaram: String get() = rasiPalan?.makaram ?: "---"
        val rasiKumbam: String get() = rasiPalan?.kumbam ?: "---"
        val rasiMeenam: String get() = rasiPalan?.meenam ?: "---"

        private fun combine(start: String, end: String): String {
            return if (start != "---" && end != "---" && start.isNotBlank() && end.isNotBlank()) {
                "$start - $end"
            } else {
                "---"
            }
        }
    }

    // ---------------------------------------------------------
    // 5. RASI PALAN DATA MODEL
    // ---------------------------------------------------------

    data class RasiPalanDetails(
        val gregorianDate: String = "---",
        val tamilMonth: String = "---",
        val tamilDate: Int = 0,
        val weekdayTa: String = "---",
        val nakshatraName: String = "---",
        val mesham: String = "---",
        val rishabam: String = "---",
        val mithunam: String = "---",
        val katakam: String = "---",
        val simmam: String = "---",
        val kanni: String = "---",
        val thulaam: String = "---",
        val viruchigam: String = "---",
        val dhanusu: String = "---",
        val makaram: String = "---",
        val kumbam: String = "---",
        val meenam: String = "---"
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
                gregorianDate = cursor.safeString("gregorian_date"),
                tamilYear = cursor.safeString("tamil_year"),
                tamilMonth = cursor.safeString("tamil_month"),
                tamilDate = cursor.safeInt("tamil_date"),
                weekdayTa = cursor.safeString("weekday_ta"),
                paksha = cursor.safeString("paksha"),
                tithiName = cursor.safeString("tithi_name"),
                tithiTime = cursor.safeString("tithi_time"),
                nakshatraName = cursor.safeString("nakshatra_name"),
                nakshatraTime = cursor.safeString("nakshatra_time"),
                yogaName = cursor.safeString("yoga_name"),
                yogaTime = cursor.safeString("yoga_time"),
                amritadiYoga = cursor.safeString("amritadi_yoga"),
                karanaName = cursor.safeString("karana_name"),
                karanaTime = cursor.safeString("karana_time"),
                tyajyamTime = cursor.safeString("tyajyam_time"),
                festivalsEvents = if (cursor.getColumnIndex("festivals_events") >= 0 && !cursor.isNull(cursor.getColumnIndex("festivals_events"))) {
                    cursor.getString(cursor.getColumnIndex("festivals_events")) ?: ""
                } else "",
                chandrashtamamRasi = cursor.safeString("chandrashtamam_rasi"),
                soolam = cursor.safeString("soolam"),
                parikaram = cursor.safeString("parikaram")
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
                cursor.safeString("sunrise"),
                cursor.safeString("sunset"),
                cursor.safeString("moonrise"),
                cursor.safeString("moonset")
            )
        } ?: listOf("---", "---", "---", "---")

        val auspiciousCursor = queryOne(
            "auspicious_periods.db",
            "weekday_timings_summary",
            "weekday_ta = ?",
            arrayOf(panchang.weekdayTa)
        )

        val auspicious = auspiciousCursor?.use { cursor ->
            listOf(
                cursor.safeString("nalla_neram_morning_start"),
                cursor.safeString("nalla_neram_morning_end"),
                cursor.safeString("nalla_neram_evening_start"),
                cursor.safeString("nalla_neram_evening_end"),
                cursor.safeString("gowri_day_start"),
                cursor.safeString("gowri_day_end"),
                cursor.safeString("gowri_night_start"),
                cursor.safeString("gowri_night_end"),
                cursor.safeString("raghu_start"),
                cursor.safeString("raghu_end"),
                cursor.safeString("emagandam_start"),
                cursor.safeString("emagandam_end"),
                cursor.safeString("kuligai_start"),
                cursor.safeString("kuligai_end")
            )
        } ?: List(14) { "---" }

        val rasi = getRasiDetails(gregorianDate)

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
            kuligaiEnd = auspicious[13],
            rasiPalan = rasi
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
                gregorianDate = it.safeString("gregorian_date"),
                tamilMonth = it.safeString("tamil_month"),
                tamilDate = it.safeInt("tamil_date"),
                weekdayTa = it.safeString("weekday_ta"),
                nakshatraName = it.safeString("nakshatra_name"),
                mesham = it.safeString("mesham"),
                rishabam = it.safeString("rishabam"),
                mithunam = it.safeString("mithunam"),
                katakam = it.safeString("katakam"),
                simmam = it.safeString("simmam"),
                kanni = it.safeString("kanni"),
                thulaam = it.safeString("thulaam"),
                viruchigam = it.safeString("viruchigam"),
                dhanusu = it.safeString("dhanusu"),
                makaram = it.safeString("makaram"),
                kumbam = it.safeString("kumbam"),
                meenam = it.safeString("meenam")
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
                weekdayTa = it.safeString("weekday_ta"),
                kaalam = it.safeString("kaalam"),
                slots = listOf(
                    it.safeString("slot_6_730"),
                    it.safeString("slot_730_9"),
                    it.safeString("slot_9_1030"),
                    it.safeString("slot_1030_12"),
                    it.safeString("slot_12_130"),
                    it.safeString("slot_130_3"),
                    it.safeString("slot_3_430"),
                    it.safeString("slot_430_6")
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
                weekdayTa = it.safeString("weekday_ta"),
                kaalam = it.safeString("kaalam"),
                slots = listOf(
                    it.safeString("slot_6_7"),
                    it.safeString("slot_7_8"),
                    it.safeString("slot_8_9"),
                    it.safeString("slot_9_10"),
                    it.safeString("slot_10_11"),
                    it.safeString("slot_11_12"),
                    it.safeString("slot_12_1"),
                    it.safeString("slot_1_2"),
                    it.safeString("slot_2_3"),
                    it.safeString("slot_3_4"),
                    it.safeString("slot_4_5"),
                    it.safeString("slot_5_6")
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
