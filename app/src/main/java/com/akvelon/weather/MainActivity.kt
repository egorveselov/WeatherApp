package com.akvelon.weather

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.akvelon.weather.database.*
import com.akvelon.weather.fragments.*
import com.akvelon.weather.web.*
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_today.*
import kotlinx.android.synthetic.main.fragment_tomorrow.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : FragmentActivity(), IWebRequestHandler {
    private val PAGE_COUNT = 3
    private val REQUEST_CODE = 1
    private var savedTabColor: String? = null
    private var previousTabPosition = 0
    private var needToBackPressed = true
    private val fragmentList: ArrayList<Fragment> = ArrayList()

    private lateinit var hamburger: ImageButton
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var toolbar: FrameLayout
    private lateinit var drawer: DrawerLayout
    private lateinit var celsius: Button
    private lateinit var fahrenheit: Button
    private lateinit var searchField: EditText

    private val weather = Weather(this)
    private val location = Location(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = resources.getColor(R.color.colorPrimary01d, null)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        checkPermissions()

        WeatherDBWorker.sqLiteDatabase = WeatherDBHelper(this).writableDatabase
        WeatherDBWorker.getCursorToday()?.let {
            if (it.moveToFirst()) {
                savedTabColor = it.getString(19)
            }
        }

        viewPager = findViewById<ViewPager2>(R.id.pager).apply {
            offscreenPageLimit = 2
            adapter = CustomFragmentStateAdapter(this@MainActivity)
            currentItem = 0
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    savedTabColor?.let { colorId ->
                        val oldColorId = when(previousTabPosition) {
                            0, 2 -> colorId
                            else -> ""
                        }

                        when(position) {
                            0, 2 -> changeMainWindowColors(colorId, oldColorId)
                            1 -> changeMainWindowColors("", colorId)
                        }
                    }
                    previousTabPosition = position
                }
            })
        }

        drawer = findViewById(R.id.drawerLayout)
        toolbar = findViewById(R.id.toolBar)

        searchField = findViewById(R.id.searchField)
        searchField.requestFocus()
        searchField.setOnClickListener {
            createSearchFragment(false)
        }

        findViewById<ImageButton>(R.id.searchButton).setOnClickListener {
            createSearchFragment(true)
        }

        val navigationView = findViewById<NavigationView>(R.id.navView)
        celsius = navigationView.getHeaderView(0).findViewById(R.id.celsius)
        celsius.setOnClickListener {
            weather.saveUnits(getString(R.string.Metric), getString(R.string.MetricUnit), getString(R.string.WindSpeedMetricUnit))
            setActiveButton()
            weather.execute()
        }
        fahrenheit = navigationView.getHeaderView(0).findViewById(R.id.fahrenheit)
        fahrenheit.setOnClickListener {
            weather.saveUnits(getString(R.string.Imperial), getString(R.string.ImperialUnit), getString(R.string.WindSpeedImperialUnit))
            setActiveButton()
            weather.execute()
        }

        tabLayout = findViewById(R.id.tabLayout)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            weather.execute()
        }

        hamburger = findViewById(R.id.hamburger)
        hamburger.setOnClickListener {
            drawer.open()
        }

        findViewById<ImageButton>(R.id.mylocation).setOnClickListener {
            location.getCurrentPlaceId()
        }

        val tabTitles = arrayOf(getString(R.string.Today), getString(R.string.Tomorrow), getString(R.string.Week))
        TabLayoutMediator(tabLayout, viewPager, false) { tab, position ->
            tab.text = tabTitles[position]
            viewPager.setCurrentItem(tab.position, true)
        }.attach()

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.PLACES_APP_KEY), Locale.US);
        }


        val isAutolocationChecked = getPreferences(MODE_PRIVATE).getBoolean(getString(R.string.autolocation), true)
        (navigationView.getHeaderView(0).findViewById<Switch>(R.id.autolocation)).apply {
            isChecked = isAutolocationChecked
                setOnCheckedChangeListener { buttonView, isChecked ->
                    getPreferences(MODE_PRIVATE).edit().putBoolean(getString(R.string.autolocation), isChecked).commit()
                }
        }

        if(isAutolocationChecked) {
            location.getCurrentPlaceId()
        } else {
            location.getCurrentPlace(getPreferences(MODE_PRIVATE).getString(getString(R.string.placeid), null))
        }

        setActiveButton()

        savedTabColor?.let {
            changeMainWindowColors(it, it)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE) {
            when(grantResults[0]) {
                -1 -> Toast.makeText(this, R.string.update_error, Toast.LENGTH_SHORT).show()
                else -> location.getCurrentPlaceId()
            }
        }
    }

    override fun onRequestFinished(response: String?) {
        swipeRefresh.isRefreshing = false
        response?.let { resp ->
            JSONObject(resp).let {
                val colorId = WeatherDBWorker.setCurrentWeather(it.getJSONObject(getString(R.string.current)))
                WeatherDBWorker.setWeekWeather(it.getJSONArray(getString(R.string.daily)))
                updateFragments()

                colorId?.let {
                    savedTabColor = colorId
                    when(viewPager.currentItem) {
                        1 -> changeMainWindowColors()
                        else -> changeMainWindowColors(colorId, colorId)
                    }
                }
            }
        } ?: Toast.makeText(this, R.string.update_error, Toast.LENGTH_SHORT).show()
    }

    override fun onGetPlaceIdRequestFinished(response: String?) {
        response?.let {
            location.getCurrentPlace(it)
        }
    }

    override fun onGetCurrentPlaceRequestFinished(response: String) {
        searchField.setText(response)
        weather.execute()
    }

    override fun onSearchPlaceStart(response: String) {
        location.getCurrentPlace(response)
    }

    private fun createSearchFragment(showKeyboard: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.test_container, SearchFragment(showKeyboard))
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun updateFragments() {
        for (fragment in 0 until fragmentList.size) {
            (fragmentList[fragment] as BaseFragment).updateUI()
        }
    }

    private fun setActiveButton() {
        if(weather.getTemperatureUnit() == getString(R.string.Metric)) {
            celsius.background = fahrenheit.background
            fahrenheit.background = null
        } else {
            fahrenheit.background = celsius.background
            celsius.background = null
        }
    }

    override fun onBackPressed() {
        when {
            needToBackPressed -> super.onBackPressed()
            else -> needToBackPressed = true
        }
    }

    private fun changeMainWindowColors(colorId: String = "", oldColorId: String = "") {
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), getColor(oldColorId), getColor(colorId))
        colorAnimation.duration = 200
        colorAnimation.addUpdateListener { animator ->
            window.statusBarColor = animator.animatedValue as Int
            tabLayout.setBackgroundColor(animator.animatedValue as Int)
            toolbar.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimation.start()
    }

    private fun getColor(colorId: String): Int = resources.getColor(resources.getIdentifier(
        "colorPrimary${colorId}", "color", this.packageName),
        null)

    private fun checkPermissions() {
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE
            )
        }
    }

    fun closeSearch() {
        val fragments = supportFragmentManager.fragments
        supportFragmentManager.beginTransaction().remove(fragments.last()).commit()
    }

    inner class CustomFragmentStateAdapter(activity: FragmentActivity) :
        FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = PAGE_COUNT

        override fun createFragment(position: Int): Fragment {
            when (position) {
                0 -> fragmentList.add(TodayFragment.newInstance())
                1 -> fragmentList.add(TomorrowFragment.newInstance())
                2 -> fragmentList.add(WeekFragment.newInstance())
                else -> throw IllegalStateException("Invalid adapter position")
            }

            return  fragmentList[position]
        }
    }
}
