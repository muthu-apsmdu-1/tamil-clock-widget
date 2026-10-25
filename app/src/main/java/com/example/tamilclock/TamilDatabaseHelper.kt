package com.example.tamilclock

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream

class TamilDatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "tamil_calendar.db"
        private const val DB_VERSION = 10 // Bumped to force refresh
    }

    private val dbPath: String = context.getDatabasePath(DB_NAME).path

    init {
        copyDatabaseIfNeeded()
    }

    private fun copyDatabaseIfNeeded() {
        val dbFile = File(dbPath)
        val sp = context.getSharedPreferences("db_prefs", Context.MODE_PRIVATE)
        val savedVersion = sp.getInt("db_version", 0)

        if (!dbFile.exists() || savedVersion < DB_VERSION) {
            dbFile.parentFile?.mkdirs()
            context.assets.open(DB_NAME).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            sp.edit().putInt("db_version", DB_VERSION).apply()
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Managed by pre-built assets file
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Force replace database with asset version
        val dbFile = File(dbPath)
        if (dbFile.exists()) {
            dbFile.delete()
        }
        copyDatabaseIfNeeded()
    }

    fun getPanchangamForDate(dateStr: String): PanchangamDay? {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery(
            """
            SELECT gregorian_date, tamil_year, tamil_month, tamil_date, 
                   weekday_ta, tithi_name, tithi_nazhikai, nakshatra_name, 
                   nakshatra_nazhikai, yoga_name, festivals_events 
            FROM panchangam_daily 
            WHERE gregorian_date = ? LIMIT 1
            """.trimIndent(),
            arrayOf(dateStr)
        )

        var result: PanchangamDay? = null
        if (cursor.moveToFirst()) {
            val tithi = cursor.getString(cursor.getColumnIndexOrThrow("tithi_name"))
            val nakshatra = cursor.getString(cursor.getColumnIndexOrThrow("nakshatra_name"))
            
            // Filter placeholder strings
            val cleanTithi = if (tithi == "காண்க") "சுப திதி" else tithi
            val cleanNakshatra = if (nakshatra == "காண்க") "சுப நட்சத்திரம்" else nakshatra

            result = PanchangamDay(
                gregorianDate = cursor.getString(cursor.getColumnIndexOrThrow("gregorian_date")),
                tamilYear = cursor.getString(cursor.getColumnIndexOrThrow("tamil_year")) ?: "பராபவ",
                tamilMonth = cursor.getString(cursor.getColumnIndexOrThrow("tamil_month")),
                tamilDate = cursor.getInt(cursor.getColumnIndexOrThrow("tamil_date")),
                weekdayTa = cursor.getString(cursor.getColumnIndexOrThrow("weekday_ta")),
                tithiName = cleanTithi,
                nakshatraName = cleanNakshatra,
                yogaName = cursor.getString(cursor.getColumnIndexOrThrow("yoga_name")) ?: "",
                festivalsEvents = cursor.getString(cursor.getColumnIndexOrThrow("festivals_events")) ?: ""
            )
        }
        cursor.close()
        return result
    }
}

data class PanchangamDay(
    val gregorianDate: String,
    val tamilYear: String,
    val tamilMonth: String,
    val tamilDate: Int,
    val weekdayTa: String,
    val tithiName: String,
    val nakshatraName: String,
    val yogaName: String,
    val festivalsEvents: String
)
