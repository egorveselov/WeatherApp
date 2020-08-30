package com.akvelon.weather

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.Toast
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.json.JSONObject
import java.time.LocalDate


class MainActivity : FragmentActivity(), IWebRequestHandler {
    private val PAGE_COUNT = 3
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var toolbar: Toolbar
    private val tabTitles = arrayOf("Today", "Tomorrow", "7 days")
    private val fragmentList: ArrayList<Fragment> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WeatherDBWorker.sqLiteDatabase = WeatherDBHelper(this).writableDatabase
        WebRequest(this, getRequestString()).execute()

        viewPager = findViewById<ViewPager2>(R.id.pager).apply {
            offscreenPageLimit = 2
            adapter = CustomFragmentStateAdapter(this@MainActivity)
            currentItem = 0
        }

        tabLayout = findViewById(R.id.tabLayout)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener { WebRequest(this, getRequestString()).execute() }
        toolbar = findViewById(R.id.toolBar)

        val cursor = WeatherDBWorker.getDataFromDB(
            WeatherDBHelper.CurrentWeather.columns.first(),
            LocalDate.now().toString(),
            WeatherDBHelper.CurrentWeather.columns.subList(1, WeatherDBHelper.CurrentWeather.columns.size)
        )

        cursor?.let {
            if (it.moveToFirst()) {
                changeMainWindowColors(it.getString(19))
            }
        }

        TabLayoutMediator(tabLayout, viewPager, false) { tab, position ->
            tab.text = tabTitles[position]
            viewPager.setCurrentItem(tab.position, true)
        }.attach()
    }

    inner class CustomFragmentStateAdapter(activity: FragmentActivity) :
        FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = PAGE_COUNT

        override fun createFragment(position: Int): Fragment =
            when (position) {
                0 -> {
                    fragmentList.add(TodayFragment.newInstance()); fragmentList[0]
                }
                1 -> {
                    fragmentList.add(TodayFragment.newInstance()); fragmentList[1]
                }
                2 -> {
                    fragmentList.add(WeekFragment.newInstance()); fragmentList[2]
                }
                else -> throw IllegalStateException("Invalid adapter position")
            }
    }

    private fun getRequestString(lat: String = "55.751244", lon: String = "37.618423"): String {
        return "${String.format(getString(R.string.URL), lat, lon) + getString(R.string.APP_ID)}"
    }

    override fun onRequestFinished(response: String?) {
        swipeRefresh.isRefreshing = false
        response?.let { resp ->
            JSONObject(resp).let {
                val colorId = WeatherDBWorker.setCurrentWeather(it.getJSONObject("current"))
                colorId?.let { changeMainWindowColors(colorId) }
                WeatherDBWorker.setWeekWeather(it.getJSONArray("daily"))

                for (fragment in 0 until fragmentList.size) {
                    when (fragment) {
                        0 -> (fragmentList[0] as TodayFragment).updateUI()
                        2 -> (fragmentList[2] as WeekFragment).updateUI()
                    }
                }
            }
        } ?: Toast.makeText(this, R.string.update_error, Toast.LENGTH_SHORT).show()
    }

    private fun changeMainWindowColors(colorId: String) {
        val resource = resources.getIdentifier(
            "colorPrimary${colorId}", "color", this.packageName
        )
        window.statusBarColor = resources.getColor(resource, null)
        tabLayout.setBackgroundColor(resources.getColor(resource, null))
        toolbar.setBackgroundColor(resources.getColor(resource, null))
    }
}
