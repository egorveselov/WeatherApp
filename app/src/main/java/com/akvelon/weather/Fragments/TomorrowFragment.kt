package com.akvelon.weather.fragments

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
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*


class TomorrowFragment: Fragment(), BaseFragment {
    private lateinit var background: View
    private lateinit var mornTemp: TextView
    private lateinit var dayTemp: TextView
    private lateinit var eveTemp: TextView
    private lateinit var nightTemp: TextView
    private lateinit var date: TextView
    private lateinit var wind: TextView
    private lateinit var weatherMain: TextView
    private lateinit var sunset: TextView
    private lateinit var sunrise: TextView
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
        background = view.findViewById(R.id.todayBackground)
        mornTemp = view.findViewById(R.id.mornTemp)
        dayTemp = view.findViewById(R.id.dayTemp)
        eveTemp = view.findViewById(R.id.eveTemp)
        nightTemp = view.findViewById(R.id.nightTemp)
        date = view.findViewById(R.id.date)
        wind = view.findViewById(R.id.wind)
        weatherMain = view.findViewById(R.id.main)
        sunset = view.findViewById(R.id.sunset)
        sunrise = view.findViewById(R.id.sunrise)
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

                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = it.getString(1).toLong() * 1000
                    sunrise.text = "Sunrise ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}"

                    calendar.timeInMillis = it.getString(2).toLong() * 1000
                    sunset.text = "Sunset ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(java.util.Calendar.MINUTE)}"

                    mornTemp.text = "Morning ${it.getString(8)} C°"
                    dayTemp.text = "Day ${it.getString(3)} C°"
                    eveTemp.text = "Evening ${it.getString(7)} C°"
                    nightTemp.text = "Night ${it.getString(6)} C°"
                    wind.text = "Wind ${it.getString(17)} m/sec"

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