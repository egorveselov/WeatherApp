package com.akvelon.weather

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.json.JSONObject
import java.net.URL
import java.time.LocalDate
import java.util.*
import kotlin.math.ceil


class MainFragment : Fragment() {
    lateinit var weatherTemperature: TextView
    lateinit var weatherMain: TextView
    lateinit var pressure: TextView
    lateinit var humidity: TextView
    lateinit var swipeRefresh: SwipeRefreshLayout
    lateinit var currentDate: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view =  inflater.inflate(R.layout.fragment_main, container, false)
        humidity = view.findViewById(R.id.humidityValue)
        pressure = view.findViewById(R.id.pressureValue)
        weatherMain = view.findViewById(R.id.weatherMain)
        weatherTemperature = view.findViewById(R.id.weatherTemperature)
        currentDate = view.findViewById(R.id.currentDate)
        currentDate.text = LocalDate.now().dayOfWeek.toString()
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener { WeatherRequestHandler().execute() }
        WeatherRequestHandler().execute()
        return view;
    }

    inner class WeatherRequestHandler() : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg url: String?): String {
            var response: String? = try {
                URL("https://api.openweathermap.org/data/2.5/weather?q=London&appid=a0f70c8a7410e976dcecbfd5ba3ee4ed").readText()
            } catch (e: Exception) {
                null
            }
            return response ?: ""
        }

        override fun onPostExecute(response: String?) {
            super.onPostExecute(response)
            parseJsonResponse(response)
        }

        private fun parseJsonResponse(response: String?) {
            if(swipeRefresh.isRefreshing) {
                swipeRefresh.isRefreshing = false
            }

            if(response.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Couldn't update weather!", Toast.LENGTH_SHORT).show()
                return
            }

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
}