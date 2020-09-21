package com.akvelon.weather.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class WeatherDBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private fun createTable(tableName: String, columns: List<String>) : String {
        var resultQuery = "CREATE TABLE $tableName (${BaseColumns._ID} INTEGER PRIMARY KEY,"
        for (index in columns.indices) {
            resultQuery = when (index) {
                columns.size - 1 -> {
                    resultQuery.plus(columns[index]).plus(" TEXT)")
                }
                else -> {
                    resultQuery.plus(columns[index]).plus(" TEXT,")
                }
            }
        }

        return resultQuery
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(createTable(CurrentWeather.columns.first(), CurrentWeather.columns.subList(1, CurrentWeather.columns.size)))
        db?.execSQL(createTable(WeekWeather.columns.first(), WeekWeather.columns.subList(1, WeekWeather.columns.size)))
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS ${WeekWeather.columns.first()}")
        db?.execSQL("DROP TABLE IF EXISTS ${CurrentWeather.columns.first()}")
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Weather.db"
    }

    object WeekWeather {
        val columns = listOf<String>(
            "week_weather",
            "dt",
            "sunrise",
            "sunset",
            "temp_day",
            "temp_min",
            "temp_max",
            "temp_night",
            "temp_eve",
            "temp_morn",
            "feels_like_day",
            "feels_like_night",
            "feels_like_eve",
            "feels_like_morn",
            "pressure",
            "humidity",
            "dew_point",
            "wind_gust",
            "wind_speed",
            "wind_deg",
            "id",
            "main",
            "description",
            "icon",
            "clouds",
            "uvi",
            "visibility",
            "pop",
            "rain",
            "snow"
        )
    }

    object CurrentWeather {
        val columns = listOf<String>(
            "current_weather",
            "dt",
            "sunrise",
            "sunset",
            "temp",
            "feels_like",
            "pressure",
            "humidity",
            "dew_point",
            "uvi",
            "clouds",
            "visibility",
            "wind_speed",
            "wind_gust",
            "wind_deg",
            "rain",
            "snow",
            "id",
            "main",
            "description",
            "icon"
        )
    }
}