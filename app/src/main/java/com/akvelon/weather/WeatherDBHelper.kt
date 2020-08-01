package com.akvelon.weather

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class WeatherDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private val SQL_CREATE_ENTRIES =
        "CREATE TABLE ${Weather.Entry.TABLE_NAME} (" +
                "${Weather.Entry.ID} INTEGER PRIMARY KEY," +
                "${Weather.Entry.COLUMN_DATE} TEXT," +
                "${Weather.Entry.COLUMN_TEMP} TEXT," +
                "${Weather.Entry.COLUMN_STATE} TEXT," +
                "${Weather.Entry.COLUMN_PRESSURE} TEXT," +
                "${Weather.Entry.COLUMN_HUMIDITY} TEXT);"

    private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${Weather.Entry.TABLE_NAME}"

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        const val  DATABASE_VERSION = 1
        const val  DATABASE_NAME = "Weather.db"
    }

    object Weather {
        object Entry {
            const val TABLE_NAME = "weather"
            const val COLUMN_DATE = "date"
            const val COLUMN_TEMP = "temp"
            const val COLUMN_STATE = "state"
            const val COLUMN_PRESSURE = "pressure"
            const val COLUMN_HUMIDITY = "humidity"
            const val ID = BaseColumns._ID
        }
    }
}