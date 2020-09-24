package com.akvelon.weather.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akvelon.weather.R
import com.akvelon.weather.database.WeatherDBWorker
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.day_info.view.*
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

class WeekFragment() : Fragment(), BaseFragment {
    private val DAYS_NUMBER = 8
    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var viewAdapter: RecyclerView.Adapter<*>

    companion object {
        fun newInstance() = WeekFragment().apply {
            arguments = Bundle()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_week, container, false)
        linearLayoutManager = LinearLayoutManager(requireContext())
        viewAdapter = CustomAdapter()
        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView).apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = viewAdapter
        }

        return view
    }

    override fun updateUI() {
        viewAdapter.notifyDataSetChanged()
    }

    inner class CustomAdapter() : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
        inner class ViewHolder(view: MaterialCardView) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.day_info, parent, false) as MaterialCardView
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = DAYS_NUMBER

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val card = holder.itemView as MaterialCardView
            val cursor = WeatherDBWorker.getCursorDayOfWeek(LocalDate.now().plusDays(position.toLong()).toString())
            cursor?.let {
                if (it.moveToFirst()) {
                    val date = cursor.getString(0)
                    card.tw_date.text = if (LocalDate.now().toString() == date) {
                        "today"
                    } else {
                        with(LocalDate.parse(date)) {
                            "${dayOfWeek.name.toLowerCase()}, $dayOfMonth ${
                                month.getDisplayName(
                                    TextStyle.SHORT,
                                    Locale.ENGLISH
                                ).toLowerCase()
                            }"
                        }
                    }

                    card.tw_state.text = cursor.getString(20)
                    card.tw_day.text = "Day ${cursor.getString(3)}°"
                    card.tw_night.text = "Night ${cursor.getString(6)}°"
                    card.iw_weatherCondition.setImageResource(
                        resources.getIdentifier(
                            "weather_con_${cursor.getString(22)}", "drawable", context?.packageName
                        )
                    )
                }
            }
        }
    }
}