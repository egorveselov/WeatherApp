package com.akvelon.weather

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import android.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_today.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : FragmentActivity(), IWebRequestHandler {
    private val PAGE_COUNT = 3
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var toolbar: Toolbar
    private val fragmentList: ArrayList<Fragment> = ArrayList()
    private var savedTabColor: String? = null
    private var previousTabPosition = 0
    private var needToBackPressed = true
    private val REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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

        tabLayout = findViewById(R.id.tabLayout)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener { WebRequest(this, getRequestString(getLocation("lat"), getLocation("lon"))).execute() }
        toolbar = findViewById(R.id.toolBar)

        val tabTitles = arrayOf(getString(R.string.Today), getString(R.string.Tomorrow), getString(R.string.Week))
        TabLayoutMediator(tabLayout, viewPager, false) { tab, position ->
            tab.text = tabTitles[position]
            viewPager.setCurrentItem(tab.position, true)
        }.attach()

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.PLACES_APP_KEY), Locale.US);
        }

        getCurrentPlaceId()
        setAutocompleteFragment()

        savedTabColor?.let {
            changeMainWindowColors(it, it)
        }
    }

    private fun setAutocompleteFragment() {
        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        with(autocompleteFragment) {
            setPlaceFields(listOf(Place.Field.NAME, Place.Field.LAT_LNG))
            setOnPlaceSelectedListener(object : PlaceSelectionListener {
                override fun onPlaceSelected(place: Place) {
                    val lat = place.latLng?.latitude.toString()
                    val lon = place.latLng?.longitude.toString()

                    saveLocation(lat, lon, place.name)
                    WebRequest(this@MainActivity, getRequestString(lat, lon)).execute()
                }

                override fun onError(p0: Status) {
                    needToBackPressed = false
                }
            })
        }
    }

    override fun onBackPressed() {
        when {
            needToBackPressed -> super.onBackPressed()
            else -> needToBackPressed = true
        }
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

    private fun getRequestString(lat: String? = "55.751244", lon: String? = "37.618423"): String {
        return "${String.format(getString(R.string.URL), lat, lon) + getString(R.string.APP_ID)}"
    }

    override fun onRequestFinished(response: String?) {
        swipeRefresh.isRefreshing = false
        response?.let { resp ->
            JSONObject(resp).let {
                val colorId = WeatherDBWorker.setCurrentWeather(it.getJSONObject("current"))
                WeatherDBWorker.setWeekWeather(it.getJSONArray("daily"))
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

    private fun updateFragments() {
        for (fragment in 0 until fragmentList.size) {
            (fragmentList[fragment] as BaseFragment).updateUI()
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

    private fun saveLocation(lat: String, lon: String, city: String?) {
        val sharedPreferences = getPreferences(MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(getString(R.string.Latitude), lat)
            putString(getString(R.string.Longitude), lon)
            putString(getString(R.string.City), city)
            commit()
        }
    }

    private fun getLocation(parameter: String): String? {
        val sharedPreferences = getPreferences(MODE_PRIVATE)
        return when(parameter) {
            getString(R.string.Latitude) -> sharedPreferences.getString(getString(R.string.Latitude), "55.751244")
            getString(R.string.Longitude) -> sharedPreferences.getString(getString(R.string.Longitude), "37.618423")
            getString(R.string.City) -> sharedPreferences.getString(getString(R.string.City), "Moscow")
            else -> null
        }
    }

    private fun getCurrentPlaceId() {
        val request = FindCurrentPlaceRequest.newInstance(listOf(Place.Field.ID))
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val placeResponse = Places.createClient(this).findCurrentPlace(request)
            placeResponse.addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    task.result.placeLikelihoods[0].place.id?.let {
                        getCurrentPlace(it)
                    }
                }
            }
        }
    }

    private fun getCurrentPlace(id: String) {
        val request = FetchPlaceRequest.newInstance(id, listOf(Place.Field.ADDRESS_COMPONENTS, Place.Field.LAT_LNG))
        val addressPlaceResponse = Places.createClient(this).fetchPlace(request)
        addressPlaceResponse.addOnCompleteListener { task ->
            if(task.isSuccessful) {
                task.result.place.addressComponents?.asList()?.let {
                    val city = it[2].name
                    task.result.place.latLng?.let { latLng ->
                        val lat = latLng.latitude.toString()
                        val lon = latLng.longitude.toString()
                        saveLocation(lat, lon, city)
                        WebRequest(this, getRequestString(lat, lon)).execute()
                    }
                }
            }
        }
    }
}
