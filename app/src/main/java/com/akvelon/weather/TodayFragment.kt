package com.akvelon.weather

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

class TodayFragment() : Fragment() {
    private lateinit var weatherTemperature: TextView
    private lateinit var currentDay: TextView
    private lateinit var weatherMain: TextView
    private lateinit var pressure: TextView
    private lateinit var humidity: TextView
    private lateinit var currentDate: TextView
    private lateinit var weatherIcon: ImageView
    private lateinit var dayTemp: TextView
    private lateinit var nightTemp: TextView
    private lateinit var feelsLike: TextView
    private lateinit var todayBackground: ImageView

    companion object {
        fun newInstance() = TodayFragment().apply {
            arguments = Bundle()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_today, container, false)
        currentDay = view.findViewById(R.id.currentDay)
        weatherIcon = view.findViewById(R.id.weatherIcon)
        humidity = view.findViewById(R.id.humidityValue)
        pressure = view.findViewById(R.id.pressureValue)
        weatherMain = view.findViewById(R.id.weatherMain)
        weatherTemperature = view.findViewById(R.id.weatherTemperature)
        currentDate = view.findViewById(R.id.currentDay)
        currentDate.text = LocalDate.now().dayOfWeek.toString()
        dayTemp = view.findViewById(R.id.dayTemp)
        nightTemp = view.findViewById(R.id.nightTemp)
        feelsLike = view.findViewById(R.id.feelsLike)
        todayBackground = view.findViewById(R.id.todayBackground)
        updateUI()
        return view;
    }

    private fun getCursor(): Cursor? = WeatherDBWorker.getDataFromDB(
        WeatherDBHelper.CurrentWeather.columns.first(),
        LocalDate.now().toString(),
        WeatherDBHelper.CurrentWeather.columns.subList(1, WeatherDBHelper.CurrentWeather.columns.size)
    )

    fun updateUI() {
        val info = WeatherDBWorker.getDataFromDB(
            WeatherDBHelper.WeekWeather.columns.first(),
            LocalDate.now().toString(),
            WeatherDBHelper.WeekWeather.columns.subList(1, WeatherDBHelper.WeekWeather.columns.size)
        )
        info?.let {
            if(it.moveToFirst()) {
                dayTemp.text = "Day ${it.getString(3)}°"
                nightTemp.text = "Night ${it.getString(6)}°"
            }
        }
        getCursor()?.let {
            if (it.moveToFirst()) {
                while (!it.isAfterLast) {
                    todayBackground.setImageResource(resources.getIdentifier("background_${it.getString(19)}", "drawable", context?.packageName))
                    currentDay.text = with(LocalDate.parse(it.getString(0))) {
                        "${dayOfWeek.name.toUpperCase()}, $dayOfMonth ${month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase()}"
                    }

                    weatherIcon.setImageResource(
                        resources.getIdentifier("weather_con_${it.getString(19)}", "drawable", context?.packageName)
                    )

                    weatherTemperature.text = it.getString(3)
                    weatherMain.text = it.getString(18)
                    pressure.text = it.getString(5)
                    humidity.text = it.getString(6)
                    feelsLike.text = "Feels like ${it.getString(4)}°"
                    break
                }
            }
        }
    }
}