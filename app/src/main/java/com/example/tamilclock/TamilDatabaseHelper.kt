package com.example.tamilclock

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

class TamilDatabaseHelper(private val context: Context) : 
    SQLiteOpenHelper(context, "tamil_calendar.db", null, 5) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS tamil_panchangam (
                gregorian_date TEXT PRIMARY KEY,
                tamil_year TEXT,
                day_name_ta TEXT,
                tamil_month TEXT,
                tamil_day INTEGER,
                paksham TEXT,
                tithi TEXT,
                nakshatra TEXT,
                nalla_neram_morning TEXT,
                nalla_neram_evening TEXT,
                gouri_nalla_neram_morning TEXT,
                gouri_nalla_neram_evening TEXT,
                rahu_kalam TEXT,
                yama_gandam TEXT,
                sunrise TEXT,
                sunset TEXT,
                moonrise TEXT,
                moonset TEXT
            )
        """.trimIndent())
        
        loadFromAssets(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS tamil_panchangam")
        onCreate(db)
    }

    private fun loadFromAssets(db: SQLiteDatabase) {
        try {
            val inputStream = context.assets.open("calendar_data.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            db.beginTransaction()
            try {
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val cv = ContentValues().apply {
                        put("gregorian_date", obj.optString("gregorian_date"))
                        put("tamil_year", obj.optString("tamil_year"))
                        put("day_name_ta", obj.optString("day_name_ta"))
                        put("tamil_month", obj.optString("tamil_month"))
                        put("tamil_day", obj.optInt("tamil_day"))
                        put("paksham", obj.optString("paksham"))
                        put("tithi", obj.optString("tithi"))
                        put("nakshatra", obj.optString("nakshatra"))
                        put("nalla_neram_morning", obj.optString("nalla_neram_morning"))
                        put("nalla_neram_evening", obj.optString("nalla_neram_evening"))
                        put("gouri_nalla_neram_morning", obj.optString("gouri_nalla_neram_morning"))
                        put("gouri_nalla_neram_evening", obj.optString("gouri_nalla_neram_evening"))
                        put("rahu_kalam", obj.optString("rahu_kalam"))
                        put("yama_gandam", obj.optString("yama_gandam"))
                        put("sunrise", obj.optString("sunrise"))
                        put("sunset", obj.optString("sunset"))
                        put("moonrise", obj.optString("moonrise"))
                        put("moonset", obj.optString("moonset"))
                    }
                    db.insertWithOnConflict("tamil_panchangam", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getDayDetails(dateString: String): TamilDateInfo? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            """SELECT tamil_year, day_name_ta, tamil_month, tamil_day, 
                      paksham, tithi, nakshatra, nalla_neram_morning, nalla_neram_evening, 
                      gouri_nalla_neram_morning, gouri_nalla_neram_evening, 
                      rahu_kalam, yama_gandam, sunrise, sunset, moonrise, moonset 
               FROM tamil_panchangam WHERE gregorian_date = ?""",
            arrayOf(dateString)
        )

        return cursor.use {
            if (it.moveToFirst()) {
                TamilDateInfo(
                    tamilYear = it.getString(0) ?: "",
                    dayName = it.getString(1) ?: "",
                    tamilMonth = it.getString(2) ?: "",
                    tamilDay = it.getInt(3),
                    paksham = it.getString(4) ?: "",
                    tithi = it.getString(5) ?: "",
                    nakshatra = it.getString(6) ?: "",
                    nallaNeramMorning = it.getString(7) ?: "",
                    nallaNeramEvening = it.getString(8) ?: "",
                    gouriNallaMorning = it.getString(9) ?: "",
                    gouriNallaEvening = it.getString(10) ?: "",
                    rahuKalam = it.getString(11) ?: "",
                    yamaGandam = it.getString(12) ?: "",
                    sunrise = it.getString(13) ?: "",
                    sunset = it.getString(14) ?: "",
                    moonrise = it.getString(15) ?: "",
                    moonset = it.getString(16) ?: ""
                )
            } else null
        }
    }
}

data class TamilDateInfo(
    val tamilYear: String,
    val dayName: String,
    val tamilMonth: String,
    val tamilDay: Int,
    val paksham: String,
    val tithi: String,
    val nakshatra: String,
    val nallaNeramMorning: String,
    val nallaNeramEvening: String,
    val gouriNallaMorning: String,
    val gouriNallaEvening: String,
    val rahuKalam: String,
    val yamaGandam: String,
    val sunrise: String,
    val sunset: String,
    val moonrise: String,
    val moonset: String
)
