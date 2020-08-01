package com.akvelon.weather

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

class WeatherDBWorker(private val database: SQLiteDatabase) {
    fun insertDataIntoDB(date: String, temp: String, state: String, pressure: String, humidity: String) {
        if(!database.isReadOnly) {
            val values = ContentValues().apply {
                put(WeatherDBHelper.Weather.Entry.COLUMN_DATE, date)
                put(WeatherDBHelper.Weather.Entry.COLUMN_TEMP, temp)
                put(WeatherDBHelper.Weather.Entry.COLUMN_STATE, state)
                put(WeatherDBHelper.Weather.Entry.COLUMN_PRESSURE, pressure)
                put(WeatherDBHelper.Weather.Entry.COLUMN_HUMIDITY, humidity)
            }
            database.insert(WeatherDBHelper.Weather.Entry.TABLE_NAME, null, values);
        }
    }

    fun clearDataFromDB() {
        database.delete(WeatherDBHelper.Weather.Entry.TABLE_NAME,"", arrayOf())
    }

    fun getDataFromDB(date: String) {
        val cursor = database.query(
            WeatherDBHelper.Weather.Entry.TABLE_NAME,
            arrayOf(
                WeatherDBHelper.Weather.Entry.COLUMN_DATE,
                WeatherDBHelper.Weather.Entry.COLUMN_TEMP,
                WeatherDBHelper.Weather.Entry.COLUMN_STATE,
                WeatherDBHelper.Weather.Entry.COLUMN_PRESSURE,
                WeatherDBHelper.Weather.Entry.COLUMN_HUMIDITY
            ),
            "${WeatherDBHelper.Weather.Entry.COLUMN_DATE} = ?",
            arrayOf(date),
            null, null, null
        )
    }
}