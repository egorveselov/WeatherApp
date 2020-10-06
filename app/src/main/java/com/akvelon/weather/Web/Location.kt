package com.akvelon.weather.web

import android.Manifest
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.akvelon.weather.R
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest

class Location(private val context: FragmentActivity) {
    fun getCurrentPlaceId() {
        val request = FindCurrentPlaceRequest.newInstance(listOf(Place.Field.ID))

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val placeResponse = Places.createClient(context).findCurrentPlace(request)
            placeResponse.addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    task.result.placeLikelihoods[0].place.id?.let {
                        (context as IWebRequestHandler).onGetPlaceIdRequestFinished(it)
                    }
                }
                else {
                    (context as IWebRequestHandler).onGetPlaceIdRequestFinished(null)
                }
            }
        }
    }

    fun getCurrentPlace(id: String?) {
        if(id == null) {
            return
        }

        val request = FetchPlaceRequest.newInstance(id, listOf(Place.Field.ADDRESS_COMPONENTS, Place.Field.LAT_LNG))
        val addressPlaceResponse = Places.createClient(context).fetchPlace(request)
        addressPlaceResponse.addOnCompleteListener { task ->
            if(task.isSuccessful) {
                task.result.place.addressComponents?.asList()?.let {
                    for(pos in 0 until it.size) {
                        if(it[pos].types[0] == context.getString(R.string.locality)) {
                            val city = it[pos].name
                            task.result.place.latLng?.let { latLng ->
                                val lat = latLng.latitude.toString()
                                val lon = latLng.longitude.toString()
                                saveLocation(lat, lon, city, id)
                                (context as IWebRequestHandler).onGetCurrentPlaceRequestFinished(city)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun saveLocation(lat: String, lon: String, city: String?, placeId: String) {
        with(context) {
            val sharedPreferences = getPreferences(MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString(getString(R.string.Latitude), lat)
                putString(getString(R.string.Longitude), lon)
                putString(getString(R.string.City), city)
                putString(getString(R.string.placeid), placeId)
                commit()
            }
        }
    }

    companion object {
        fun getLocation(context: FragmentActivity, parameter: String): String? {
            with(context) {
                val sharedPreferences = getPreferences(android.content.Context.MODE_PRIVATE)
                return when(parameter) {
                    getString(R.string.Latitude) -> sharedPreferences.getString(getString(R.string.Latitude), null)
                    getString(R.string.Longitude) -> sharedPreferences.getString(getString(R.string.Longitude), null)
                    getString(R.string.City) -> sharedPreferences.getString(getString(R.string.City), null)
                    else -> null
                }
            }
        }
    }
}