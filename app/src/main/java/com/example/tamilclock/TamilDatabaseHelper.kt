package com.example.tamilclock

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

class TamilDatabaseHelper(private val context: Context) : 
    SQLiteOpenHelper(context, "tamil_calendar.db", null, 2) {

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
                        put("day_name_ta", obj.optString("day_name_ta"))
                        put("tamil_month", obj.optString("tamil_month"))
                        put("tamil_day", obj.optInt("tamil_day"))
                        put("tithi", obj.optString("tithi"))
                        put("nakshatra", obj.optString("nakshatra"))
                        put("nalla_neram", obj.optString("nalla_neram"))
                        put("rahu_kalam", obj.optString("rahu_kalam"))
                        put("yama_gandam", obj.optString("yama_gandam"))
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
