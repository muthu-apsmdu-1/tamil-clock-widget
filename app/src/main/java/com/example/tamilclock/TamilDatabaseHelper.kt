package com.example.tamilclock

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream

class TamilDatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "panchang_clcon.db"
        private const val DB_VERSION = 1
    }

    init {
        copyDatabaseIfNeeded()
    }

    private fun copyDatabaseIfNeeded() {
        val dbFile = context.getDatabasePath(DB_NAME)
        if (!dbFile.exists()) {
            dbFile.parentFile?.mkdirs()
            val tempFile = File(dbFile.parentFile, "$DB_NAME.tmp")
            if (tempFile.exists()) tempFile.delete()

            context.assets.open(DB_NAME).use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                    output.fd.sync()
                }
            }
            if (!tempFile.renameTo(dbFile)) {
                tempFile.delete()
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {}
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

    data class DayPanchangDetails(
        val tamilYear: String,
        val tamilMonth: String,
        val tamilDate: Int,
        val weekdayTa: String,
        val paksha: String,
        val tithiName: String,
        val tithiTime: String,
        val nakshatraName: String,
        val nakshatraTime: String,
        val yogaName: String,
        val amritadiYoga: String,
        val karanaName: String,
        val tyajyamTime: String,
        val festivalsEvents: String,
        val chandrashtamamRasi: String,
        val soolam: String,
        val parikaram: String,
        val sunrise: String,
        val sunset: String,
        val moonrise: String,
        val moonset: String,
        val nallaNeramMorn: String,
        val nallaNeramEve: String,
        val gowriMorn: String,
        val gowriEve: String,
        val rahu: String,
        val yamagandam: String,
        val kuligai: String,
        val rasiMesham: String,
        val rasiRishabam: String,
        val rasiMithunam: String,
        val rasiKadagam: String,
        val rasiSimmam: String,
        val rasiKanni: String,
        val rasiThulaam: String,
        val rasiViruchigam: String,
        val rasiDhanusu: String,
        val rasiMagaram: String,
        val rasiKumbam: String,
        val rasiMeenam: String
    ) {
        // Backward-compatibility aliases for CalendarMonthAdapter, Widgets, and WidgetUpdateHelper
        val tamilDay: String get() = weekdayTa
        val dayName: String get() = weekdayTa
        val dayNameTa: String get() = weekdayTa
        val tithi: String get() = tithiName
        val nakshatra: String get() = nakshatraName
        val yoga: String get() = if (amritadiYoga.isNotEmpty()) amritadiYoga else yogaName
        val paksham: String get() = paksha
        val chandrashtamam: String get() = chandrashtamamRasi
        val nallaNeramMorning: String get() = nallaNeramMorn
        val nallaNeramEvening: String get() = nallaNeramEve
        val gowriNallaNeramMorning: String get() = gowriMorn
        val gowriNallaNeramEvening: String get() = gowriEve
        val rahuKaalam: String get() = rahu
        val festivals: String get() = festivalsEvents
        val karanam: String get() = karanaName
        val tyajyam: String get() = tyajyamTime
    }

    fun getDayDetails(gregorianDate: String): DayPanchangDetails? {
        val db = readableDatabase

        // 1. Query panchangam_daily
        var tYear = ""
        var tMonth = ""
        var tDate = 0
        var wdayTa = ""
        var pak = ""
        var tithiN = ""
        var tithiT = ""
        var nakN = ""
        var nakT = ""
        var yogN = ""
        var amrYoga = ""
        var karN = ""
        var tyajT = ""
        var fest = ""
        var chandra = ""
        var sool = ""
        var pari = ""

        db.rawQuery(
            "SELECT tamil_year, tamil_month, tamil_date, weekday_ta, paksha, tithi_name, tithi_time, " +
                    "nakshatra_name, nakshatra_time, yoga_name, amritadi_yoga, karana_name, " +
                    "tyajyam_time, festivals_events, chandrashtamam_rasi, soolam, parikaram " +
                    "FROM panchangam_daily WHERE gregorian_date = ?",
            arrayOf(gregorianDate)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                tYear = cursor.getString(0) ?: ""
                tMonth = cursor.getString(1) ?: ""
                tDate = cursor.getInt(2)
                wdayTa = cursor.getString(3) ?: ""
                pak = cursor.getString(4) ?: ""
                tithiN = cursor.getString(5) ?: ""
                tithiT = cursor.getString(6) ?: ""
                nakN = cursor.getString(7) ?: ""
                nakT = cursor.getString(8) ?: ""
                yogN = cursor.getString(9) ?: ""
                amrYoga = cursor.getString(10) ?: ""
                karN = cursor.getString(11) ?: ""
                tyajT = cursor.getString(12) ?: ""
                fest = cursor.getString(13) ?: ""
                chandra = cursor.getString(14) ?: ""
                sool = cursor.getString(15) ?: ""
                pari = cursor.getString(16) ?: ""
            } else {
                return null
            }
        }

        // 2. Query sun_moon_timings
        var sRise = ""
        var sSet = ""
        var mRise = ""
        var mSet = ""
        db.rawQuery(
            "SELECT sunrise, sunset, moonrise, moonset FROM sun_moon_timings WHERE date_gregorian = ?",
            arrayOf(gregorianDate)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                sRise = cursor.getString(0) ?: ""
                sSet = cursor.getString(1) ?: ""
                mRise = cursor.getString(2) ?: ""
                mSet = cursor.getString(3) ?: ""
            }
        }

        // 3. Query weekday_timings_summary
        var nallaM = ""
        var nallaE = ""
        var gowriM = ""
        var gowriE = ""
        var rk = ""
        var yk = ""
        var kk = ""
        db.rawQuery(
            "SELECT nalla_neram_morning_start, nalla_neram_morning_end, " +
                    "nalla_neram_evening_start, nalla_neram_evening_end, " +
                    "gowri_day_start, gowri_day_end, gowri_night_start, gowri_night_end, " +
                    "raghu_start, raghu_end, emagandam_start, emagandam_end, " +
                    "kuligai_start, kuligai_end FROM weekday_timings_summary WHERE weekday_ta = ?",
            arrayOf(wdayTa)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                val nms = cursor.getString(0) ?: ""
                val nme = cursor.getString(1) ?: ""
                val nes = cursor.getString(2) ?: ""
                val nee = cursor.getString(3) ?: ""
                val gds = cursor.getString(4) ?: ""
                val gde = cursor.getString(5) ?: ""
                val gns = cursor.getString(6) ?: ""
                val gne = cursor.getString(7) ?: ""
                val rs = cursor.getString(8) ?: ""
                val re = cursor.getString(9) ?: ""
                val ys = cursor.getString(10) ?: ""
                val ye = cursor.getString(11) ?: ""
                val ks = cursor.getString(12) ?: ""
                val ke = cursor.getString(13) ?: ""

                if (nms.isNotEmpty() && nme.isNotEmpty()) nallaM = "$nms - $nme"
                if (nes.isNotEmpty() && nee.isNotEmpty()) nallaE = "$nes - $nee"
                if (gds.isNotEmpty() && gde.isNotEmpty()) gowriM = "$gds - $gde"
                if (gns.isNotEmpty() && gne.isNotEmpty()) gowriE = "$gns - $gne"
                if (rs.isNotEmpty() && re.isNotEmpty()) rk = "$rs - $re"
                if (ys.isNotEmpty() && ye.isNotEmpty()) yk = "$ys - $ye"
                if (ks.isNotEmpty() && ke.isNotEmpty()) kk = "$ks - $ke"
            }
        }

        // 4. Query rasipalan_daily
        var rMesham = ""
        var rRishabam = ""
        var rMithunam = ""
        var rKadagam = ""
        var rSimmam = ""
        var rKanni = ""
        var rThulaam = ""
        var rViruchigam = ""
        var rDhanusu = ""
        var rMagaram = ""
        var rKumbam = ""
        var rMeenam = ""
        db.rawQuery(
            "SELECT mesham, rishabam, mithunam, katakam, simmam, kanni, " +
                    "thulaam, viruchigam, dhanusu, makaram, kumbam, meenam " +
                    "FROM rasipalan_daily WHERE gregorian_date = ?",
            arrayOf(gregorianDate)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                rMesham = cursor.getString(0) ?: ""
                rRishabam = cursor.getString(1) ?: ""
                rMithunam = cursor.getString(2) ?: ""
                rKadagam = cursor.getString(3) ?: ""
                rSimmam = cursor.getString(4) ?: ""
                rKanni = cursor.getString(5) ?: ""
                rThulaam = cursor.getString(6) ?: ""
                rViruchigam = cursor.getString(7) ?: ""
                rDhanusu = cursor.getString(8) ?: ""
                rMagaram = cursor.getString(9) ?: ""
                rKumbam = cursor.getString(10) ?: ""
                rMeenam = cursor.getString(11) ?: ""
            }
        }

        return DayPanchangDetails(
            tamilYear = tYear,
            tamilMonth = tMonth,
            tamilDate = tDate,
            weekdayTa = wdayTa,
            paksha = pak,
            tithiName = tithiN,
            tithiTime = tithiT,
            nakshatraName = nakN,
            nakshatraTime = nakT,
            yogaName = yogN,
            amritadiYoga = amrYoga,
            karanaName = karN,
            tyajyamTime = tyajT,
            festivalsEvents = fest,
            chandrashtamamRasi = chandra,
            soolam = sool,
            parikaram = pari,
            sunrise = sRise,
            sunset = sSet,
            moonrise = mRise,
            moonset = mSet,
            nallaNeramMorn = nallaM,
            nallaNeramEve = nallaE,
            gowriMorn = gowriM,
            gowriEve = gowriE,
            rahu = rk,
            yamagandam = yk,
            kuligai = kk,
            rasiMesham = rMesham,
            rasiRishabam = rRishabam,
            rasiMithunam = rMithunam,
            rasiKadagam = rKadagam,
            rasiSimmam = rSimmam,
            rasiKanni = rKanni,
            rasiThulaam = rThulaam,
            rasiViruchigam = rViruchigam,
            rasiDhanusu = rDhanusu,
            rasiMagaram = rMagaram,
            rasiKumbam = rKumbam,
            rasiMeenam = rMeenam
        )
    }
}
