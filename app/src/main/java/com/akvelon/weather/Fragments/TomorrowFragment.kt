package com.akvelon.weather.fragments

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.akvelon.weather.R
import com.akvelon.weather.database.*
import kotlinx.android.synthetic.main.fragment_today.view.*
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*


class TomorrowFragment: Fragment(), BaseFragment {
    private lateinit var background: View
    private lateinit var mornTemp: TextView
    private lateinit var dayTemp: TextView
    private lateinit var dayTempMain: TextView
    private lateinit var eveTemp: TextView
    private lateinit var nightTemp: TextView
    private lateinit var date: TextView
    private lateinit var wind: TextView
    private lateinit var weatherMain: TextView
    private lateinit var pressure: TextView
    private lateinit var humidity: TextView
    private lateinit var weatherIcon: ImageView

    companion object {
        fun newInstance() = TomorrowFragment().apply {
            arguments = Bundle()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_tomorrow, container, false)
        background = view.findViewById(R.id.tomorrowBackground)
        mornTemp = view.findViewById(R.id.mornTemp)
        dayTemp = view.findViewById(R.id.dayTemp)
        eveTemp = view.findViewById(R.id.eveTemp)
        nightTemp = view.findViewById(R.id.nightTemp)
        date = view.findViewById(R.id.date)
        wind = view.findViewById(R.id.wind)
        weatherMain = view.findViewById(R.id.main)
        dayTempMain = view.findViewById(R.id.dayTempMain)
        pressure = view.findViewById(R.id.pressure)
        humidity = view.findViewById(R.id.humidity)
        weatherIcon = view.findViewById(R.id.weatherIcon)

        return view
    }

    private fun getCursor(): Cursor? = WeatherDBWorker.getDataFromDB(
        WeatherDBHelper.WeekWeather.columns.first(),
        LocalDate.now().plusDays(1).toString(),
        WeatherDBHelper.WeekWeather.columns.subList(1, WeatherDBHelper.WeekWeather.columns.size)
    )

    override fun updateUI() {
        getCursor()?.let {
            if (it.moveToFirst()) {
                while (!it.isAfterLast) {
                    date.text = with(LocalDate.parse(it.getString(0))) {
                        "${dayOfWeek.name.toUpperCase()}, $dayOfMonth ${month.getDisplayName(
                            TextStyle.SHORT, Locale.ENGLISH
                        ).toUpperCase()}"
                    }

                    var windSpeedUnit = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)?.getString(getString(R.string.WindSpeedUnit), getString(R.string.WindSpeedMetricUnit))
                    mornTemp.text = "${it.getString(8)}°"
                    dayTemp.text = "${it.getString(3)}°"
                    dayTempMain.text = "${it.getString(3)}°"
                    eveTemp.text = "${it.getString(7)}°"
                    nightTemp.text = "${it.getString(6)}°"
                    wind.text = "Wind ${it.getString(17)} $windSpeedUnit"
                    pressure.text = "Pressure ${it.getString(13)}"
                    humidity.text = "Humidity ${it.getString(14)}"
                    weatherMain.text = "${it.getString(20)}"

                    weatherIcon.setImageResource(
                        resources.getIdentifier(
                            "weather_con_${it.getString(22)}",
                            "drawable",
                            context?.packageName
                        )
                    )
                    break
                }
            }
        }
    }
}