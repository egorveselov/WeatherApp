package com.akvelon.weather

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.json.JSONObject
import java.net.URL

class MainActivity : FragmentActivity() {
    lateinit var viewPager2: ViewPager2
    lateinit var tabLayout: TabLayout
    lateinit var swipeRefresh: SwipeRefreshLayout
    private val tabTitles = arrayOf("Today", "Tomorrow", "7 days")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager2 = findViewById(R.id.pager)
        viewPager2.adapter = CustomFragmentStateAdapter(this)
        viewPager2.currentItem = 0
        tabLayout = findViewById(R.id.tabLayout)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener { WeatherRequestHandler().execute() }

        TabLayoutMediator(tabLayout, viewPager2, false) { tab, position ->
            tab.text = tabTitles[position]
            viewPager2.setCurrentItem(tab.position, true)
        }.attach()
    }

    inner class CustomFragmentStateAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int {
            return 3
        }

        override fun createFragment(position: Int): Fragment = when(position) {
            0 -> TodayFragment()
            else -> TodayFragment()
        }

        override fun getItemViewType(position: Int): Int {
            return super.getItemViewType(position)
        }
    }

    inner class WeatherRequestHandler() : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg url: String?): String? {
            return try {
                URL("https://api.openweathermap.org/data/2.5/onecall?lat=33.441792&lon=-94.037689&\n" +
                        "exclude=hourly&appid=a0f70c8a7410e976dcecbfd5ba3ee4ed").readText()
            } catch (e: Exception) {
                null
            }
        }

        override fun onPostExecute(response: String?) {
            super.onPostExecute(response)
            if(swipeRefresh.isRefreshing) {
                swipeRefresh.isRefreshing = false
            }

            if(response.isNullOrEmpty()) {
                Toast.makeText(this@MainActivity, "Couldn't update weather!", Toast.LENGTH_SHORT).show()
                return
            }

            val jsonObject = JSONObject(response)
            getCurrentWeather(jsonObject.getJSONObject("current"))

            viewPager2.adapter?.notifyDataSetChanged()
        }

        private fun getCurrentWeather(currentWeather: JSONObject) {
            val currentTemp = currentWeather.getDouble("temp")
            val currentPressure = currentWeather.getDouble("pressure")
            val currentHumidity = currentWeather.getDouble("humidity")
        }

        private fun getTimezone(timeZone: JSONObject) {
            val timezone = timeZone.getString("timezone")
        }

        private fun getDailyWeather(daily: JSONObject) {

        }
    }
}
