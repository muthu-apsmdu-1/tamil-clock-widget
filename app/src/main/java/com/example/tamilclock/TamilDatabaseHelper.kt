package com.example.tamilclock

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

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
    // 6. GOWRI & HORAI MODELS
    // ---------------------------------------------------------

    data class GowriDetails(
        val weekdayTa: String,
        val kaalam: String,
        val slots: List<String>
    )

    data class HoraiDetails(
        val weekdayTa: String,
        val kaalam: String,
        val slots: List<String>
    )

    data class TableSlotData(
        val timeLabel: String,
        val dayValue: String,
        val nightValue: String,
        val startMin: Int,
        val endMin: Int
    )

    data class SolarSlotItem(
        val phaseLabel: String,
        val timeRange: String,
        val valueName: String,
        val isAuspicious: Boolean,
        val startEpochMins: Int,
        val endEpochMins: Int
    )

    data class DynamicGowriHoraiResult(
        val sunriseToday: String,
        val sunsetToday: String,
        val sunriseTomorrow: String,
        val daySlots: List<SolarSlotItem>,
        val nightSlots: List<SolarSlotItem>
    )

    // ---------------------------------------------------------
    // 7. GET COMPLETE DAILY PANCHANGAM
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
        ) ?: queryOne(
            "sun_moon_timings.db",
            "sun_moon_timings",
            "gregorian_date = ?",
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
    // 8. GET DAILY RASI PALAN
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
    // 9. GET GOWRI & HORAI RAW ROWS
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
    // 10. LEGACY 3-COLUMN TABLE METHODS (KEPT FOR WIDGET COMPATIBILITY)
    // ---------------------------------------------------------

    fun getGowriTableData(weekdayTa: String): List<TableSlotData> {
        val list = mutableListOf<TableSlotData>()
        val gowriTimes = listOf(
            Triple("06:00 - 07:30", 360, 450),
            Triple("07:30 - 09:00", 450, 540),
            Triple("09:00 - 10:30", 540, 630),
            Triple("10:30 - 12:00", 630, 720),
            Triple("12:00 - 01:30", 720, 810),
            Triple("01:30 - 03:00", 810, 900),
            Triple("03:00 - 04:30", 900, 990),
            Triple("04:30 - 06:00", 990, 1080)
        )
        val dayDetails = getGowriDetails(weekdayTa, "பகல்")
        val nightDetails = getGowriDetails(weekdayTa, "இரவு")

        gowriTimes.forEachIndexed { index, (timeLabel, startMin, endMin) ->
            list.add(
                TableSlotData(
                    timeLabel = timeLabel,
                    dayValue = dayDetails?.slots?.getOrNull(index)?.takeIf { it != "---" } ?: "---",
                    nightValue = nightDetails?.slots?.getOrNull(index)?.takeIf { it != "---" } ?: "---",
                    startMin = startMin,
                    endMin = endMin
                )
            )
        }
        return list
    }

    fun getHoraiTableData(weekdayTa: String): List<TableSlotData> {
        val list = mutableListOf<TableSlotData>()
        val horaiTimes = listOf(
            Triple("06:00 - 07:00", 360, 420),
            Triple("07:00 - 08:00", 420, 480),
            Triple("08:00 - 09:00", 480, 540),
            Triple("09:00 - 10:00", 540, 600),
            Triple("10:00 - 11:00", 600, 660),
            Triple("11:00 - 12:00", 660, 720),
            Triple("12:00 - 01:00", 720, 780),
            Triple("01:00 - 02:00", 780, 840),
            Triple("02:00 - 03:00", 840, 900),
            Triple("03:00 - 04:00", 900, 960),
            Triple("04:00 - 05:00", 960, 1020),
            Triple("05:00 - 06:00", 1020, 1080)
        )
        val dayDetails = getHoraiDetails(weekdayTa, "பகல்")
        val nightDetails = getHoraiDetails(weekdayTa, "இரவு")

        horaiTimes.forEachIndexed { index, (timeLabel, startMin, endMin) ->
            list.add(
                TableSlotData(
                    timeLabel = timeLabel,
                    dayValue = dayDetails?.slots?.getOrNull(index)?.takeIf { it != "---" } ?: "---",
                    nightValue = nightDetails?.slots?.getOrNull(index)?.takeIf { it != "---" } ?: "---",
                    startMin = startMin,
                    endMin = endMin
                )
            )
        }
        return list
    }

    // ---------------------------------------------------------
    // 11. SOLAR-ADJUSTED DYNAMIC GOWRI & HORAI CALCULATIONS
    // ---------------------------------------------------------

    fun getDynamicHoraiData(
        todayDate: String,
        tomorrowDate: String,
        weekdayTa: String
    ): DynamicGowriHoraiResult {
        val sunToday = getSunTimesForDate(todayDate)
        val sunTomorrow = getSunTimesForDate(tomorrowDate)

        val riseTodayMin = parseTimeToMinutes(sunToday.first, 360)
        val setTodayMin = parseTimeToMinutes(sunToday.second, 1080)
        val riseTomorrowMin = parseTimeToMinutes(sunTomorrow.first, 360) + 1440

        val dayDetails = getHoraiDetails(weekdayTa, "பகல்")
        val nightDetails = getHoraiDetails(weekdayTa, "இரவு")

        val daySlots = buildSolarSlices(
            startMin = riseTodayMin,
            endMin = setTodayMin,
            values = dayDetails?.slots ?: List(12) { "---" },
            isHorai = true
        )

        val nightSlots = buildSolarSlices(
            startMin = setTodayMin,
            endMin = riseTomorrowMin,
            values = nightDetails?.slots ?: List(12) { "---" },
            isHorai = true
        )

        return DynamicGowriHoraiResult(
            sunriseToday = formatMinutes12Hr(riseTodayMin, includeAmPm = true),
            sunsetToday = formatMinutes12Hr(setTodayMin, includeAmPm = true),
            sunriseTomorrow = formatMinutes12Hr(riseTomorrowMin % 1440, includeAmPm = true),
            daySlots = daySlots,
            nightSlots = nightSlots
        )
    }

    fun getDynamicGowriData(
        todayDate: String,
        tomorrowDate: String,
        weekdayTa: String
    ): DynamicGowriHoraiResult {
        val sunToday = getSunTimesForDate(todayDate)
        val sunTomorrow = getSunTimesForDate(tomorrowDate)

        val riseTodayMin = parseTimeToMinutes(sunToday.first, 360)
        val setTodayMin = parseTimeToMinutes(sunToday.second, 1080)
        val riseTomorrowMin = parseTimeToMinutes(sunTomorrow.first, 360) + 1440

        val dayDetails = getGowriDetails(weekdayTa, "பகல்")
        val nightDetails = getGowriDetails(weekdayTa, "இரவு")

        val daySlots = buildSolarSlices(
            startMin = riseTodayMin,
            endMin = setTodayMin,
            values = dayDetails?.slots ?: List(8) { "---" },
            isHorai = false
        )

        val nightSlots = buildSolarSlices(
            startMin = setTodayMin,
            endMin = riseTomorrowMin,
            values = nightDetails?.slots ?: List(8) { "---" },
            isHorai = false
        )

        return DynamicGowriHoraiResult(
            sunriseToday = formatMinutes12Hr(riseTodayMin, includeAmPm = true),
            sunsetToday = formatMinutes12Hr(setTodayMin, includeAmPm = true),
            sunriseTomorrow = formatMinutes12Hr(riseTomorrowMin % 1440, includeAmPm = true),
            daySlots = daySlots,
            nightSlots = nightSlots
        )
    }

    private fun getSunTimesForDate(gregorianDate: String): Pair<String, String> {
        val cursor = queryOne(
            "sun_moon_timings.db",
            "sun_moon_timings",
            "date_gregorian = ?",
            arrayOf(gregorianDate)
        ) ?: queryOne(
            "sun_moon_timings.db",
            "sun_moon_timings",
            "gregorian_date = ?",
            arrayOf(gregorianDate)
        )

        return cursor?.use {
            Pair(it.safeString("sunrise"), it.safeString("sunset"))
        } ?: Pair("06:00", "18:00")
    }

    private fun buildSolarSlices(
        startMin: Int,
        endMin: Int,
        values: List<String>,
        isHorai: Boolean
    ): List<SolarSlotItem> {
        val count = values.size.coerceAtLeast(1)
        val totalDuration = (endMin - startMin).coerceAtLeast(count)
        val result = ArrayList<SolarSlotItem>(count)

        for (i in 0 until count) {
            val slotStart = startMin + Math.round((i.toDouble() * totalDuration) / count).toInt()
            val slotEnd = startMin + Math.round(((i + 1).toDouble() * totalDuration) / count).toInt()
            val valText = values[i]

            result.add(
                SolarSlotItem(
                    phaseLabel = getTamilDayPhase(slotStart),
                    timeRange = "${formatMinutes12Hr(slotStart, false)} - ${formatMinutes12Hr(slotEnd, false)}",
                    valueName = valText,
                    isAuspicious = isAuspiciousPeriod(valText, isHorai),
                    startEpochMins = slotStart,
                    endEpochMins = slotEnd
                )
            )
        }
        return result
    }

    private fun getTamilDayPhase(epochMins: Int): String {
        val modMin = ((epochMins % 1440) + 1440) % 1440
        val hour = modMin / 60
        return when {
            hour in 4..5 -> "அதிகாலை"
            hour in 6..11 -> "காலை"
            hour in 12..13 -> "மதியம்"
            hour in 14..16 -> "பிற்பகல்"
            hour in 17..18 -> "மாலை"
            hour in 19..23 -> "இரவு"
            else -> "நள்ளிரவு"
        }
    }

    private fun isAuspiciousPeriod(name: String, isHorai: Boolean): Boolean {
        val clean = name.trim()
        return if (isHorai) {
            clean == "சுக்கிரன்" || clean == "புதன்" || clean == "சந்திரன்" || clean == "குரு"
        } else {
            clean == "உத்தி" || clean == "அமிர்தம்" || clean == "லாபம்" || clean == "தனம்" || clean == "சுகம்"
        }
    }

    private fun parseTimeToMinutes(raw: String, fallbackMins: Int): Int {
        if (raw.isBlank() || raw == "---") return fallbackMins
        return try {
            val upper = raw.uppercase(Locale.ENGLISH).trim()
            val isPm = upper.contains("PM")
            val isAm = upper.contains("AM")
            val numericPart = upper.replace("AM", "").replace("PM", "").trim()
            val parts = numericPart.split(":")
            var hours = parts[0].trim().toInt()
            val mins = if (parts.size > 1) parts[1].trim().take(2).toInt() else 0

            if (isPm && hours < 12) hours += 12
            if (isAm && hours == 12) hours = 0
            (hours * 60) + mins
        } catch (_: Exception) {
            fallbackMins
        }
    }

    private fun formatMinutes12Hr(epochMins: Int, includeAmPm: Boolean): String {
        val modMin = ((epochMins % 1440) + 1440) % 1440
        val hour24 = modMin / 60
        val min = modMin % 60
        val amPm = if (hour24 >= 12) "PM" else "AM"
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        return if (includeAmPm) {
            String.format(Locale.ENGLISH, "%d:%02d %s", hour12, min, amPm)
        } else {
            String.format(Locale.ENGLISH, "%d:%02d", hour12, min)
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
