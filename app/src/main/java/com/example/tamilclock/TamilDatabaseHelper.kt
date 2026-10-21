package com.example.tamilclock

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TamilDatabaseHelper(context: Context) : 
    SQLiteOpenHelper(context, "tamil_calendar.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS tamil_panchangam (
                gregorian_date TEXT PRIMARY KEY,
                day_name_ta TEXT,
                tamil_month TEXT,
                tamil_day INTEGER,
                tithi TEXT,
                nakshatra TEXT,
                nalla_neram TEXT,
                rahu_kalam TEXT,
                yama_gandam TEXT
            )
        """.trimIndent())
        
        insertSampleData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    private fun insertSampleData(db: SQLiteDatabase) {
        val cv = ContentValues().apply {
            put("gregorian_date", "2026-10-21")
            put("day_name_ta", "புதன்")
            put("tamil_month", "ஐப்பசி")
            put("tamil_day", 4)
            put("tithi", "ஏகாதசி")
            put("nakshatra", "ரோகிணி")
            put("nalla_neram", "09:15 AM - 10:15 AM")
            put("rahu_kalam", "12:00 PM - 01:30 PM")
            put("yama_gandam", "07:30 AM - 09:00 AM")
        }
        db.insertWithOnConflict("tamil_panchangam", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getDayDetails(dateString: String): TamilDateInfo? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT day_name_ta, tamil_month, tamil_day, tithi, nakshatra, nalla_neram, rahu_kalam, yama_gandam FROM tamil_panchangam WHERE gregorian_date = ?",
            arrayOf(dateString)
        )

        return cursor.use {
            if (it.moveToFirst()) {
                TamilDateInfo(
                    dayName = it.getString(0),
                    tamilMonth = it.getString(1),
                    tamilDay = it.getInt(2),
                    tithi = it.getString(3),
                    nakshatra = it.getString(4),
                    nallaNeram = it.getString(5),
                    rahuKalam = it.getString(6),
                    yamaGandam = it.getString(7)
                )
            } else null
        }
    }
}

data class TamilDateInfo(
    val dayName: String,
    val tamilMonth: String,
    val tamilDay: Int,
    val tithi: String,
    val nakshatra: String,
    val nallaNeram: String,
    val rahuKalam: String,
    val yamaGandam: String
)
