package com.akvelon.weather

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.json.JSONObject
import java.net.URL
import java.time.LocalDate
import kotlin.math.ceil

class TodayFragment : Fragment() {
    lateinit var weatherTemperature: TextView
    lateinit var weatherMain: TextView
    lateinit var pressure: TextView
    lateinit var humidity: TextView
    lateinit var currentDate: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view =  inflater.inflate(R.layout.fragment_today, container, false)
        humidity = view.findViewById(R.id.humidityValue)
        pressure = view.findViewById(R.id.pressureValue)
        weatherMain = view.findViewById(R.id.weatherMain)
        weatherTemperature = view.findViewById(R.id.weatherTemperature)
        currentDate = view.findViewById(R.id.currentDate)
        currentDate.text = LocalDate.now().dayOfWeek.toString()
        return view;
    }

    private fun updateUI(response: String?) {
        val jsonObject = JSONObject(response)
        val main = jsonObject.getJSONObject("main")
        val sys = jsonObject.getJSONObject("sys")
        val wind = jsonObject.getJSONObject("wind")
        val weather = jsonObject.getJSONArray("weather").getJSONObject(0)

        val weatherTemp = ceil(main.getString("temp").toDouble() - 273.15).toInt();

        weatherTemperature.text = weatherTemp.toString()
        weatherMain.text = weather.getString("main")
        pressure.text = main.getString("pressure")
        humidity.text = main.getString("humidity").prependIndent(" ")
    }
}