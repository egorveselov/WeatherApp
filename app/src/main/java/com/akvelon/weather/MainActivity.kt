package com.akvelon.weather

import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.net.URL
import kotlin.math.ceil

class MainActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle
    lateinit var updateWeather: Button
    lateinit var weatherTemperature: TextView
    lateinit var weatherMain: TextView
    lateinit var pressure: TextView
    lateinit var humidity: TextView
    lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        humidity = findViewById(R.id.humidityValue)
        pressure = findViewById(R.id.pressureValue)
        weatherMain = findViewById(R.id.weatherMain)
        weatherTemperature = findViewById(R.id.weatherTemperature)
        updateWeather = findViewById(R.id.updateWeather)
        updateWeather.setOnClickListener { WeatherRequestHandler(this,"London").execute() }
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        fusedLocationClient.lastLocation.addOnSuccessListener { location -> }
        WeatherRequestHandler().execute()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    inner class WeatherRequestHandler() : AsyncTask<String, String, String>() {

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
