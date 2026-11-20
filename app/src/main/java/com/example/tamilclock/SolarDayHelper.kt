package com.example.tamilclock

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SolarDayHelper(context: Context) {

    private val appContext = context.applicationContext

    companion object {
        private const val TAG = "SolarDayHelper"
        private const val MINUTES_PER_DAY = 1440
    }

    // ---------------------------------------------------------
    // 1. DATA CLASSES
    // ---------------------------------------------------------

    data class DayWindowSnapshot(
        val gregorianDate: String,
        val weekdayTa: String,
        val tamilYear: String,
        val tamilMonth: String,
        val tamilDate: Int,
        val sunriseMins: Int,
        val sunsetMins: Int,
        val sunriseFormatted: String,
        val sunsetFormatted: String
    )

    data class SolarTamilDateResult(
        val effectiveGregorianDate: String,
        val weekdayTa: String,
        val tamilYear: String,
        val tamilMonth: String,
        val tamilDate: Int,
        val isBeforeSunrise: Boolean,
        val sunriseTime: String
    )

    data class PeriodSnapshot(
        val currentEnd: String = "--:--",
        val currentName: String = "---",
        val nextEnd: String = "--:--",
        val nextName: String = "---",
        val isCurrentAuspicious: Boolean = false,
        val isNextAuspicious: Boolean = false
    )

    data class SolarHoraiGowriResult(
        val effectiveGregorianDate: String,
        val weekdayTa: String,
        val isDayKaalam: Boolean,
        val horai: PeriodSnapshot,
        val gowri: PeriodSnapshot
    )

    // ---------------------------------------------------------
    // 2. RESOLVE EFFECTIVE TAMIL DATE
    // ---------------------------------------------------------

    fun resolveTamilDate(
        dbHelper: TamilDatabaseHelper,
        cal: Calendar = Calendar.getInstance()
    ): SolarTamilDateResult {

        val snapshots = loadThreeDaySnapshots(dbHelper, cal)

        val prevSnap = snapshots.prev
        val curSnap = snapshots.current
        val nextSnap = snapshots.next

        val nowMins =
            cal.get(Calendar.HOUR_OF_DAY) * 60 +
                cal.get(Calendar.MINUTE)

        val beforeCurSunrise = nowMins < curSnap.sunriseMins

        val activeSnap = if (beforeCurSunrise) {
            prevSnap
        } else {
            curSnap
        }

        val upcomingSunrise = if (beforeCurSunrise) {
            curSnap.sunriseFormatted
        } else {
            nextSnap.sunriseFormatted
        }

        return SolarTamilDateResult(
            effectiveGregorianDate = activeSnap.gregorianDate,
            weekdayTa = activeSnap.weekdayTa,
            tamilYear = activeSnap.tamilYear,
            tamilMonth = activeSnap.tamilMonth,
            tamilDate = activeSnap.tamilDate,
            isBeforeSunrise = beforeCurSunrise,
            sunriseTime = upcomingSunrise
        )
    }

    // ---------------------------------------------------------
    // 3. RESOLVE LIVE HORAI & GOWRI
    // ---------------------------------------------------------

    fun resolveHoraiAndGowri(
        dbHelper: TamilDatabaseHelper,
        cal: Calendar = Calendar.getInstance()
    ): SolarHoraiGowriResult {

        val snapshots = loadThreeDaySnapshots(dbHelper, cal)

        val prevSnap = snapshots.prev
        val curSnap = snapshots.current
        val nextSnap = snapshots.next

        val nowMins =
            cal.get(Calendar.HOUR_OF_DAY) * 60 +
                cal.get(Calendar.MINUTE)

        val beforeCurSunrise = nowMins < curSnap.sunriseMins

        val activeSnap = if (beforeCurSunrise) {
            prevSnap
        } else {
            curSnap
        }

        val followingSnap = if (beforeCurSunrise) {
            curSnap
        } else {
            nextSnap
        }

        // Pre-sunrise time belongs to the previous day's
        // continuous minute timeline.
        val shiftedNow = if (beforeCurSunrise) {
            nowMins + MINUTES_PER_DAY
        } else {
            nowMins
        }

        // -----------------------------------------------------
        // ACTIVE DAY / NIGHT BOUNDARIES
        // -----------------------------------------------------

        val dayStart = activeSnap.sunriseMins

        val dayEnd = if (
            activeSnap.sunsetMins <= activeSnap.sunriseMins
        ) {
            Log.w(TAG, "Invalid sunset for ${activeSnap.gregorianDate}; using 12-hour daytime fallback")
            activeSnap.sunriseMins + 720
        } else {
            activeSnap.sunsetMins
        }

        // Following date is one calendar day after active date.
        val followingSunriseAbs =
            followingSnap.sunriseMins + MINUTES_PER_DAY

        val nightEnd = followingSunriseAbs

        val isDayKaalam =
            shiftedNow >= dayStart && shiftedNow < dayEnd

        // -----------------------------------------------------
        // DATABASE SLOT DATA
        // -----------------------------------------------------

        val horaiDay = dbHelper
            .getHoraiDetails(activeSnap.weekdayTa, "பகல்")
            ?.slots
            ?: List(12) { "---" }

        val horaiNight = dbHelper
            .getHoraiDetails(activeSnap.weekdayTa, "இரவு")
            ?.slots
            ?: List(12) { "---" }

        val horaiNextMorning = dbHelper
            .getHoraiDetails(followingSnap.weekdayTa, "பகல்")
            ?.slots
            .orEmpty()

        val gowriDay = dbHelper
            .getGowriDetails(activeSnap.weekdayTa, "பகல்")
            ?.slots
            ?: List(8) { "---" }

        val gowriNight = dbHelper
            .getGowriDetails(activeSnap.weekdayTa, "இரவு")
            ?.slots
            ?: List(8) { "---" }

        val gowriNextMorning = dbHelper
            .getGowriDetails(followingSnap.weekdayTa, "பகல்")
            ?.slots
            .orEmpty()

        // -----------------------------------------------------
        // BUILD ACTIVE DAY + NIGHT SLICES
        // -----------------------------------------------------

        val horaiSlots = buildCombinedSlices(
            dayStart = dayStart,
            dayEnd = dayEnd,
            nightEnd = nightEnd,
            dayValues = horaiDay,
            nightValues = horaiNight,
            expectedDayCount = 12,
            expectedNightCount = 12,
            isHorai = true,
            label = "Horai"
        )

        val gowriSlots = buildCombinedSlices(
            dayStart = dayStart,
            dayEnd = dayEnd,
            nightEnd = nightEnd,
            dayValues = gowriDay,
            nightValues = gowriNight,
            expectedDayCount = 8,
            expectedNightCount = 8,
            isHorai = false,
            label = "Gowri"
        )

        // -----------------------------------------------------
        // NEXT MORNING FIRST SLOT END TIMES
        // -----------------------------------------------------

        val followingDayEnd = if (
            followingSnap.sunsetMins <= followingSnap.sunriseMins
        ) {
            followingSnap.sunriseMins + 720
        } else {
            followingSnap.sunsetMins
        }

        val followingDaySpan =
            (followingDayEnd - followingSnap.sunriseMins)
                .coerceAtLeast(1)

        val horaiFirstMorningEnd =
            followingSunriseAbs +
                Math.round(followingDaySpan.toDouble() / 12).toInt()

        val gowriFirstMorningEnd =
            followingSunriseAbs +
                Math.round(followingDaySpan.toDouble() / 8).toInt()

        // -----------------------------------------------------
        // PICK CURRENT + NEXT HORAI
        // -----------------------------------------------------

        val horaiSnapshot = pickPeriodSnapshot(
            slots = horaiSlots,
            shiftedNowMins = shiftedNow,
            fallbackNextName = horaiNextMorning.firstOrNull(),
            fallbackNextEndMins = horaiFirstMorningEnd,
            isHorai = true
        )

        // -----------------------------------------------------
        // PICK CURRENT + NEXT GOWRI
        // -----------------------------------------------------

        val gowriSnapshot = pickPeriodSnapshot(
            slots = gowriSlots,
            shiftedNowMins = shiftedNow,
            fallbackNextName = gowriNextMorning.firstOrNull(),
            fallbackNextEndMins = gowriFirstMorningEnd,
            isHorai = false
        )

        return SolarHoraiGowriResult(
            effectiveGregorianDate = activeSnap.gregorianDate,
            weekdayTa = activeSnap.weekdayTa,
            isDayKaalam = isDayKaalam,
            horai = horaiSnapshot,
            gowri = gowriSnapshot
        )
    }

    // ---------------------------------------------------------
    // 4. STANDALONE DATABASE ACCESS
    // ---------------------------------------------------------

    fun resolveTamilDate(
        cal: Calendar = Calendar.getInstance()
    ): SolarTamilDateResult {

        val dbHelper = TamilDatabaseHelper(appContext)

        return try {
            resolveTamilDate(dbHelper, cal)
        } finally {
            dbHelper.close()
        }
    }

    fun resolveHoraiAndGowri(
        cal: Calendar = Calendar.getInstance()
    ): SolarHoraiGowriResult {

        val dbHelper = TamilDatabaseHelper(appContext)

        return try {
            resolveHoraiAndGowri(dbHelper, cal)
        } finally {
            dbHelper.close()
        }
    }

    // ---------------------------------------------------------
    // 5. THREE-DAY SNAPSHOT LOADER
    // ---------------------------------------------------------

    private data class ThreeDaySnapshots(
        val prev: DayWindowSnapshot,
        val current: DayWindowSnapshot,
        val next: DayWindowSnapshot
    )

    private fun loadThreeDaySnapshots(
        dbHelper: TamilDatabaseHelper,
        cal: Calendar
    ): ThreeDaySnapshots {

        val formatter = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.ENGLISH
        )

        val prevCal = (cal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }

        val nextCal = (cal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }

        val prevDate = formatter.format(prevCal.time)
        val curDate = formatter.format(cal.time)
        val nextDate = formatter.format(nextCal.time)

        return ThreeDaySnapshots(
            prev = buildSnapshot(dbHelper, prevDate, prevCal),
            current = buildSnapshot(dbHelper, curDate, cal),
            next = buildSnapshot(dbHelper, nextDate, nextCal)
        )
    }

    // ---------------------------------------------------------
    // 6. BUILD DAILY SNAPSHOT
    // ---------------------------------------------------------

    private fun buildSnapshot(
        dbHelper: TamilDatabaseHelper,
        gregorianDate: String,
        cal: Calendar
    ): DayWindowSnapshot {

        val details = dbHelper.getDayDetails(gregorianDate)

        val normalizedWeekday = normalizeTamilWeekday(
            details?.weekdayTa
        ).ifEmpty {
            fallbackWeekday(cal)
        }

        val rawSunrise = cleanValue(details?.sunrise)
        val rawSunset = cleanValue(details?.sunset)

        val sunriseMins = parseMinutes24Hr(rawSunrise)
            ?: run {
                Log.w(TAG, "Missing/invalid sunrise for $gregorianDate; using 06:00")
                6 * 60
            }

        val sunsetMins = parseMinutes24Hr(rawSunset)
            ?: run {
                Log.w(TAG, "Missing/invalid sunset for $gregorianDate; using 18:00")
                18 * 60
            }

        return DayWindowSnapshot(
            gregorianDate = gregorianDate,
            weekdayTa = normalizedWeekday,
            tamilYear = cleanValue(details?.tamilYear).ifEmpty { "பராபவ" },
            tamilMonth = cleanValue(details?.tamilMonth).ifEmpty { "---" },
            tamilDate = details?.tamilDate ?: 0,
            sunriseMins = sunriseMins,
            sunsetMins = sunsetMins,
            sunriseFormatted = rawSunrise.ifEmpty {
                formatMinutes12Hr(sunriseMins)
            },
            sunsetFormatted = rawSunset.ifEmpty {
                formatMinutes12Hr(sunsetMins)
            }
        )
    }

    // ---------------------------------------------------------
    // 7. BUILD COMBINED SOLAR SLICES
    // ---------------------------------------------------------

    private data class ComputedSlot(
        val startMins: Int,
        val endMins: Int,
        val name: String,
        val isAuspicious: Boolean
    )

    private fun buildCombinedSlices(
        dayStart: Int,
        dayEnd: Int,
        nightEnd: Int,
        dayValues: List<String>,
        nightValues: List<String>,
        expectedDayCount: Int,
        expectedNightCount: Int,
        isHorai: Boolean,
        label: String
    ): List<ComputedSlot> {

        if (
            dayValues.size != expectedDayCount ||
            nightValues.size != expectedNightCount
        ) {
            Log.w(
                TAG,
                "$label slot mismatch: " +
                    "day=${dayValues.size}/$expectedDayCount, " +
                    "night=${nightValues.size}/$expectedNightCount"
            )
        }

        if (dayEnd <= dayStart || nightEnd <= dayEnd) {
            Log.e(
                TAG,
                "$label invalid solar boundaries: " +
                    "dayStart=$dayStart, dayEnd=$dayEnd, nightEnd=$nightEnd"
            )
            return emptyList()
        }

        val daySlices = buildHalfCycleSlices(
            startMins = dayStart,
            endMins = dayEnd,
            rawValues = dayValues,
            targetCount = expectedDayCount,
            isHorai = isHorai
        )

        val nightSlices = buildHalfCycleSlices(
            startMins = dayEnd,
            endMins = nightEnd,
            rawValues = nightValues,
            targetCount = expectedNightCount,
            isHorai = isHorai
        )

        val combined = (daySlices + nightSlices)
            .sortedBy { it.startMins }

        val validOrder = combined.zipWithNext().all { (a, b) ->
            a.startMins < b.startMins &&
                a.endMins == b.startMins
        }

        if (!validOrder) {
            Log.e(TAG, "$label slots failed chronological validation")
            return emptyList()
        }

        return combined
    }

    // ---------------------------------------------------------
    // 8. BUILD HALF-CYCLE SLICES
    // ---------------------------------------------------------

    private fun buildHalfCycleSlices(
        startMins: Int,
        endMins: Int,
        rawValues: List<String>,
        targetCount: Int,
        isHorai: Boolean
    ): List<ComputedSlot> {

        if (targetCount <= 0 || endMins <= startMins) {
            return emptyList()
        }

        val span = endMins - startMins

        if (span < targetCount) {
            Log.e(TAG, "Invalid half-cycle duration: span=$span, count=$targetCount")
            return emptyList()
        }

        val result = ArrayList<ComputedSlot>(targetCount)

        for (i in 0 until targetCount) {

            val start = startMins +
                Math.round((i.toDouble() * span) / targetCount).toInt()

            val end = startMins +
                Math.round(((i + 1).toDouble() * span) / targetCount).toInt()

            val cleanedName = cleanValue(
                rawValues.getOrNull(i)
            ).ifEmpty {
                "---"
            }

            result.add(
                ComputedSlot(
                    startMins = start,
                    endMins = end,
                    name = cleanedName,
                    isAuspicious = isAuspicious(cleanedName, isHorai)
                )
            )
        }

        return result
    }

    // ---------------------------------------------------------
    // 9. PICK CURRENT + NEXT PERIOD
    // ---------------------------------------------------------

    private fun pickPeriodSnapshot(
        slots: List<ComputedSlot>,
        shiftedNowMins: Int,
        fallbackNextName: String?,
        fallbackNextEndMins: Int,
        isHorai: Boolean
    ): PeriodSnapshot {

        val cleanedFallback = cleanValue(fallbackNextName)

        if (slots.isEmpty()) {

            val nextName = cleanedFallback.ifEmpty { "---" }

            return PeriodSnapshot(
                currentEnd = "--:--",
                currentName = "---",
                nextEnd = if (nextName != "---") {
                    formatShort12Hr(fallbackNextEndMins)
                } else {
                    "--:--"
                },
                nextName = nextName,
                isCurrentAuspicious = false,
                isNextAuspicious = isAuspicious(nextName, isHorai)
            )
        }

        val activeIdx = slots.indexOfFirst { slot ->
            shiftedNowMins >= slot.startMins &&
                shiftedNowMins < slot.endMins
        }

        if (activeIdx < 0) {

            val upcoming = slots.firstOrNull {
                it.startMins > shiftedNowMins
            }

            val nextName = cleanValue(
                upcoming?.name ?: cleanedFallback
            ).ifEmpty {
                "---"
            }

            val nextEnd = when {
                upcoming != null -> formatShort12Hr(upcoming.endMins)
                cleanedFallback.isNotEmpty() -> formatShort12Hr(fallbackNextEndMins)
                else -> "--:--"
            }

            return PeriodSnapshot(
                currentEnd = "--:--",
                currentName = "---",
                nextEnd = nextEnd,
                nextName = nextName,
                isCurrentAuspicious = false,
                isNextAuspicious = isAuspicious(nextName, isHorai)
            )
        }

        val currentSlot = slots[activeIdx]
        val nextSlot = slots.getOrNull(activeIdx + 1)

        val nextName: String
        val nextEnd: String

        if (nextSlot != null) {
            nextName = nextSlot.name
            nextEnd = formatShort12Hr(nextSlot.endMins)
        } else if (cleanedFallback.isNotEmpty()) {
            nextName = cleanedFallback
            nextEnd = formatShort12Hr(fallbackNextEndMins)
        } else {
            nextName = "---"
            nextEnd = "--:--"
        }

        return PeriodSnapshot(
            currentEnd = formatShort12Hr(currentSlot.endMins),
            currentName = currentSlot.name,
            nextEnd = nextEnd,
            nextName = nextName,
            isCurrentAuspicious = currentSlot.isAuspicious,
            isNextAuspicious = isAuspicious(nextName, isHorai)
        )
    }

    // ---------------------------------------------------------
    // 10. TAMIL WEEKDAY NORMALIZATION
    // ---------------------------------------------------------

    private fun normalizeTamilWeekday(raw: String?): String {

        val cleaned = raw
            ?.replace(Regex("\\s+"), " ")
            ?.trim()
            .orEmpty()

        return when {
            cleaned.isEmpty() || cleaned == "---" -> ""
            cleaned.contains("ஞாயிறு") -> "ஞாயிறு"
            cleaned.contains("திங்கள்") -> "திங்கள்"
            cleaned.contains("செவ்வாய்") -> "செவ்வாய்"
            cleaned.contains("புதன்") -> "புதன்"
            cleaned.contains("வியாழன்") -> "வியாழன்"
            cleaned.contains("வெள்ளி") -> "வெள்ளி"
            cleaned.contains("சனி") -> "சனி"
            else -> ""
        }
    }

    // ---------------------------------------------------------
    // 11. DATE-BASED WEEKDAY FALLBACK
    // ---------------------------------------------------------

    private fun fallbackWeekday(cal: Calendar): String {

        return when (cal.get(Calendar.DAY_OF_WEEK)) {
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

    // ---------------------------------------------------------
    // 12. CLEAN DATABASE VALUES
    // ---------------------------------------------------------

    private fun cleanValue(raw: String?): String {

        val trimmed = raw?.trim().orEmpty()

        return if (trimmed.isEmpty() || trimmed == "---") {
            ""
        } else {
            trimmed
        }
    }

    // ---------------------------------------------------------
    // 13. AUSPICIOUS PERIOD CLASSIFICATION
    // ---------------------------------------------------------

    private fun isAuspicious(
        name: String,
        isHorai: Boolean
    ): Boolean {

        val clean = name.trim()

        return if (isHorai) {
            clean == "சுக்கிரன்" ||
                clean == "புதன்" ||
                clean == "சந்திரன்" ||
                clean == "குரு"
        } else {
            clean == "உத்தி" ||
                clean == "அமிர்தம்" ||
                clean == "லாபம்" ||
                clean == "தனம்" ||
                clean == "சுகம்"
        }
    }

    // ---------------------------------------------------------
    // 14. PARSE SUNRISE / SUNSET TIME
    // ---------------------------------------------------------

    private fun parseMinutes24Hr(raw: String?): Int? {

        val cleanedRaw = cleanValue(raw)

        if (cleanedRaw.isEmpty()) return null

        return try {

            val cleaned = cleanedRaw
                .replace("*", "")
                .uppercase(Locale.ENGLISH)
                .trim()

            val regex = Regex(
                """^(\d{1,2})(?:[:.](\d{1,2}))?\s*(AM|PM)?$"""
            )

            val match = regex.matchEntire(cleaned)
                ?: return null

            var hour = match.groupValues[1].toInt()

            val minute = match.groupValues[2]
                .takeIf { it.isNotEmpty() }
                ?.toInt()
                ?: 0

            val amPm = match.groupValues[3]

            when (amPm) {
                "AM" -> if (hour == 12) hour = 0
                "PM" -> if (hour < 12) hour += 12
            }

            if (hour !in 0..23 || minute !in 0..59) {
                null
            } else {
                hour * 60 + minute
            }

        } catch (e: Exception) {
            Log.w(TAG, "Unable to parse time: $raw", e)
            null
        }
    }

    // ---------------------------------------------------------
    // 15. FORMAT TIME WITHOUT AM/PM
    // ---------------------------------------------------------

    private fun formatShort12Hr(epochMins: Int): String {

        val modMin =
            ((epochMins % MINUTES_PER_DAY) + MINUTES_PER_DAY) %
                MINUTES_PER_DAY

        val hour24 = modMin / 60
        val minute = modMin % 60

        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }

        return String.format(
            Locale.ENGLISH,
            "%d:%02d",
            hour12,
            minute
        )
    }

    // ---------------------------------------------------------
    // 16. FORMAT TIME WITH AM/PM
    // ---------------------------------------------------------

    private fun formatMinutes12Hr(epochMins: Int): String {

        val modMin =
            ((epochMins % MINUTES_PER_DAY) + MINUTES_PER_DAY) %
                MINUTES_PER_DAY

        val hour24 = modMin / 60
        val minute = modMin % 60

        val amPm = if (hour24 >= 12) "PM" else "AM"

        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }

        return String.format(
            Locale.ENGLISH,
            "%d:%02d %s",
            hour12,
            minute,
            amPm
        )
    }
}
