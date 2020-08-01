package com.akvelon.weather

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
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
import com.google.android.play.core.splitcompat.d
import org.json.JSONObject
import java.net.URL
import java.text.DateFormatSymbols
import java.time.LocalDate

class MainActivity : FragmentActivity() {
    private val PAGE_COUNT = 3
    private val URL = "https://api.openweathermap.org/data/2.5/onecall?"
    private val APP_ID = "a0f70c8a7410e976dcecbfd5ba3ee4ed"

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private val tabTitles = arrayOf("Today", "Tomorrow", "7 days")
    private var dbHelper = WeatherDBHelper(this)
    private lateinit var dbWorker: WeatherDBWorker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbWorker = WeatherDBWorker(try {
            dbHelper.writableDatabase
        } catch(ex : SQLiteException){
            dbHelper.readableDatabase
        })

        viewPager = findViewById(R.id.pager)
        viewPager.adapter = CustomFragmentStateAdapter(this)
        viewPager.currentItem = 0
        tabLayout = findViewById(R.id.tabLayout)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener { WeatherRequestHandler().execute() }

        TabLayoutMediator(tabLayout, viewPager, true) { tab, position ->
            tab.text = tabTitles[position]
            viewPager.setCurrentItem(tab.position, true)
        }.attach()
    }

    inner class CustomFragmentStateAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = PAGE_COUNT

        override fun createFragment(position: Int): Fragment = when(position) {
            0 -> TodayFragment()
            1 -> TodayFragment()
            2 -> TodayFragment()
            else -> TodayFragment()
        }
    }

    inner class WeatherRequestHandler(private val lat: String = "33.441792", private val lon: String = "94.037689") : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg url: String?): String? = try {
            URL("${URL}lat=${lat}&lon=${lon}&exclude=hourly&appid=${APP_ID}").readText()
        } catch (e: Exception) {
            Toast.makeText(this@MainActivity, "Couldn't update weather!", Toast.LENGTH_SHORT).show()
            null
        }
        finally {
            swipeRefresh.isRefreshing = false
        }

        override fun onPostExecute(response: String?) {
            super.onPostExecute(response)
            response?.let { resp ->
                JSONObject(resp).let {
                    getCurrentWeather(it.getJSONObject("current"))
                    viewPager.adapter?.notifyDataSetChanged()
                } }
        }

        private fun getCurrentWeather(currentWeather: JSONObject) {
            with(currentWeather) {
                dbWorker.insertDataIntoDB(
                    LocalDate.now().toString(),
                    getString("temp"),
                    getJSONArray("weather").getJSONObject(0).getString("main"),
                    getString("pressure"),
                    getString("humidity")
                )
            }
        }

        private fun getTimezone(timeZone: JSONObject) {
            val timezone = timeZone.getString("timezone")
        }
    }
}
