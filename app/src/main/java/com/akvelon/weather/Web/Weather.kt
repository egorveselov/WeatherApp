package com.akvelon.weather.web

import androidx.fragment.app.FragmentActivity
import com.akvelon.weather.MainActivity
import com.akvelon.weather.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class Weather(private val context: FragmentActivity) {
    private val scope = CoroutineScope(Dispatchers.Main)

    fun execute() {
        scope.launch {
            getWeather(getRequestString())
        }
    }

    private suspend fun getWeather(request: String) {
        val data = networkRequest(request)
        (context as IWebRequestHandler).onRequestFinished(data)
    }

    private suspend fun networkRequest(request: String): String? = withContext(Dispatchers.IO) {
        return@withContext URL(request).readText()
    }

    private fun getRequestString(): String = with(context) {
        return "${String.format(getString(R.string.URL),
            Location.getLocation(context, getString(R.string.Latitude)),
            Location.getLocation(context, getString(R.string.Longitude)),
            getTemperatureUnit()) + getString(R.string.APP_ID)}"
    }

    fun getTemperatureUnit(): String? = with(context) {
        getPreferences(FragmentActivity.MODE_PRIVATE).getString(getString(R.string.Unit), getString(R.string.Metric))
    }

    fun saveUnits(unit: String, tempUnit: String, windSpeedUnit: String) {
        with(context) {
            val sharedPreferences = getPreferences(FragmentActivity.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString(getString(R.string.Unit), unit)
                putString(getString(R.string.TempUnit), tempUnit)
                putString(getString(R.string.WindSpeedUnit), windSpeedUnit)
                commit()
            }
        }
    }
}