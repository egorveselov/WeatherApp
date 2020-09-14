package com.akvelon.weather

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.math.roundToInt

object WeatherDBWorker {
    lateinit var sqLiteDatabase: SQLiteDatabase

    fun setCurrentWeather(currentWeather: JSONObject): String? {
        val columns = WeatherDBHelper.CurrentWeather.columns.subList(1, WeatherDBHelper.CurrentWeather.columns.size)
        val map: MutableMap<String, String> = mutableMapOf()
        with(currentWeather) {
            for(value in columns) {
                try {
                    when(value) {
                        "dt" -> map[value] = getLocalTime(getInt(value).toLong())
                        "main" -> map[value] = getJSONArray("weather").getJSONObject(0).getString(value)
                        "description" -> map[value] = getJSONArray("weather").getJSONObject(0).getString(value)
                        "icon" -> map[value] = getJSONArray("weather").getJSONObject(0).getString(value)
                        "temp" -> map[value] = getDouble("temp").roundToInt().toString()
                        "feels_like" -> map[value] = getDouble("feels_like").roundToInt().toString()
                        else -> {
                            map[value] = getString(value)
                        }

                    }
                } catch (ex: Exception) {
                    map[value] = ""
                }
            }
        }

        clearDataFromDB(WeatherDBHelper.CurrentWeather.columns.first())
        insertDataIntoDB(map, WeatherDBHelper.CurrentWeather.columns.first())

        return map["icon"];
    }

    fun setWeekWeather(weekWeather: JSONArray) {
        val columns = WeatherDBHelper.WeekWeather.columns.subList(1, WeatherDBHelper.WeekWeather.columns.size)
        val map: MutableMap<String, String> = mutableMapOf()
        for (day in 0 until weekWeather.length()) {
            with(weekWeather.get(day) as JSONObject) {
                for(value in columns) {
                    try {
                        when(value) {
                            "dt" -> map[value] = getLocalTime(getInt(value).toLong())
                            "main" -> map[value] = getJSONArray("weather").getJSONObject(0).getString(value)
                            "description" -> map[value] = getJSONArray("weather").getJSONObject(0).getString(value)
                            "icon" -> map[value] = getJSONArray("weather").getJSONObject(0).getString(value)
                            "temp_day" -> map[value] = getJSONObject("temp").getDouble("day").roundToInt().toString()
                            "temp_night" -> map[value] = getJSONObject("temp").getDouble("night").roundToInt().toString()
                            "temp_eve" -> map[value] = getJSONObject("temp").getDouble("eve").roundToInt().toString()
                            "temp_morn" -> map[value] = getJSONObject("temp").getDouble("morn").roundToInt().toString()
                            else -> {
                                map[value] = getString(value)
                            }

                        }
                    } catch (ex: Exception) {
                        map[value] = ""
                    }
                }
            }

            insertDataIntoDB(map, WeatherDBHelper.WeekWeather.columns.first())
        }
    }

    fun getCursorToday(): Cursor? = getDataFromDB(
        WeatherDBHelper.CurrentWeather.columns.first(),
        LocalDate.now().toString(),
        WeatherDBHelper.CurrentWeather.columns.subList(1, WeatherDBHelper.CurrentWeather.columns.size)
    )

    fun getCursorDayOfWeek(day: String): Cursor? = getDataFromDB(
        WeatherDBHelper.WeekWeather.columns.first(),
        day,
        WeatherDBHelper.WeekWeather.columns.subList(1, WeatherDBHelper.WeekWeather.columns.size)
    )

    private fun insertDataIntoDB(map: MutableMap<String, String>, tableName: String) {
        if (!sqLiteDatabase.isReadOnly) {
            val values = ContentValues().apply {
                for ((property, value) in map) {
                    put(property, value)
                }
            }
            sqLiteDatabase.insert(tableName, null, values)
        }
    }

    private fun clearDataFromDB(tableName: String) {
        sqLiteDatabase.delete(tableName, null, arrayOf())
    }

    fun getDataFromDB(tableName: String, date: String, columns: List<String>): Cursor? = sqLiteDatabase.query(
        tableName,
        columns.toTypedArray(),
        "dt = ?",
        arrayOf(date),
        null, null, null
    )

    private fun getLocalTime(timestamp: Long) = with(SimpleDateFormat("yyyy-MM-dd")) {
        format(Date(timestamp * 1000L))
    }
}