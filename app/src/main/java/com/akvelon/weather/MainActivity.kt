package com.akvelon.weather

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.widget.Toast
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.akvelon.weather.database.*
import com.akvelon.weather.fragments.*
import com.akvelon.weather.web.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.json.JSONObject


class MainActivity : FragmentActivity(), IWebRequestHandler {
    private val PAGE_COUNT = 3
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var toolbar: Toolbar
    private val tabTitles = arrayOf("Today", "Tomorrow", "7 days")
    private val fragmentList: ArrayList<Fragment> = ArrayList()
    private var savedTabColor: String? = null
    private var previousTabPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WeatherDBWorker.sqLiteDatabase = WeatherDBHelper(this).writableDatabase
        WebRequest(this, getRequestString()).execute()
        
        WeatherDBWorker.getCursorToday()?.let {
            if (it.moveToFirst()) {
                savedTabColor = it.getString(19)
            }
        }

        viewPager = findViewById<ViewPager2>(R.id.pager).apply {
            offscreenPageLimit = 2
            adapter = CustomFragmentStateAdapter(this@MainActivity)
            currentItem = 0
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                savedTabColor?.let {
                    when(position) {
                        0 -> changeMainWindowColors(it,
                            if(previousTabPosition == 2) {
                                it
                            } else { "2dn"}
                        )
                        1 -> changeMainWindowColors("2dn", it)
                        2 -> changeMainWindowColors(it,
                            if(previousTabPosition == 0) {
                                it
                            } else { "2dn"}
                        )
                    }
                }
                previousTabPosition = position
                super.onPageSelected(position)
            }
        })

        tabLayout = findViewById(R.id.tabLayout)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener { WebRequest(this, getRequestString()).execute() }
        toolbar = findViewById(R.id.toolBar)

        TabLayoutMediator(tabLayout, viewPager, false) { tab, position ->
            tab.text = tabTitles[position]
            viewPager.setCurrentItem(tab.position, true)
        }.attach()

        savedTabColor?.let {
            changeMainWindowColors(it, it)
        }
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
                    fragmentList.add(TomorrowFragment.newInstance()); fragmentList[1]
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
                WeatherDBWorker.setWeekWeather(it.getJSONArray("daily"))

                for (fragment in 0 until fragmentList.size) {
                    when (fragment) {
                        0 -> (fragmentList[0] as TodayFragment).updateUI()
                        1 -> (fragmentList[1] as TomorrowFragment).updateUI()
                        2 -> (fragmentList[2] as WeekFragment).updateUI()
                    }
                }
                val colorId = WeatherDBWorker.setCurrentWeather(it.getJSONObject("current"))
                colorId?.let {
                    savedTabColor = colorId
                    if(viewPager.currentItem == 1) {
                        changeMainWindowColors("2dn", "2dn")
                    } else {
                        changeMainWindowColors(colorId, colorId)
                    }
                }
            }
        } ?: Toast.makeText(this, R.string.update_error, Toast.LENGTH_SHORT).show()
    }

    private fun changeMainWindowColors(colorId: String, oldColorId: String) { //        val resource = resources.getIdentifier(

        val colorFrom = resources.getColor(resources.getIdentifier(
            "colorPrimary${oldColorId}", "color", this.packageName),
            null)

        val colorTo = resources.getColor(resources.getIdentifier(
            "colorPrimary${colorId}", "color", this.packageName),
            null)

        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 200
        colorAnimation.addUpdateListener { animator ->
            window.statusBarColor = animator.animatedValue as Int
            tabLayout.setBackgroundColor(animator.animatedValue as Int)
            toolbar.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimation.start()

    }
}
