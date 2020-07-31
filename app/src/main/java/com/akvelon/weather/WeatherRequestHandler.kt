package com.akvelon.weather

import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import org.json.JSONObject
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.net.URL
import java.text.DecimalFormat
import kotlin.math.ceil

class WeatherRequestHandler(private val context: Context, private val cityName: String) : AsyncTask<String, String, String>() {

    override fun doInBackground(vararg url: String?): String {
        var response: String? = try{
            URL("https://api.openweathermap.org/data/2.5/weather?q=London&appid=a0f70c8a7410e976dcecbfd5ba3ee4ed").readText()
        }catch (e: Exception){
            null
        }
        return response ?: ""
    }

    override fun onPostExecute(response: String?) {
        super.onPostExecute(response)
        parseJsonResponse(response)
    }

    private fun parseJsonResponse(response: String?) {
        val jsonObj = JSONObject(response)
        val main = jsonObj.getJSONObject("main")
        val sys = jsonObj.getJSONObject("sys")
        val wind = jsonObj.getJSONObject("wind")
        val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

        val weatherTemp = ceil(main.getString("temp").toDouble() - 273.15).toInt();
        Toast.makeText(context.applicationContext, weatherTemp.toString(), Toast.LENGTH_SHORT).show()
    }
}