package com.akvelon.weather.web

import android.Manifest
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.akvelon.weather.R
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest

object Location {
    var hintList = mutableListOf<AutocompletePrediction>()

    fun getCurrentPlaceId(context: Context) {
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

    fun getCurrentPlace(context: Context, id: String?) {
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
                                saveLocation(context, lat, lon, city, id)
                                (context as IWebRequestHandler).onGetCurrentPlaceRequestFinished(city)
                            }
                        }
                    }
                }
            }
        }
    }

    fun getAutocompletePredictions(context: Context, s: CharSequence) {
        val maxHints = 3
        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setTypeFilter(TypeFilter.CITIES)
            .setSessionToken(token)
            .setQuery(s.toString())
            .build()

        Places.createClient(context).findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                val autocompletePredictionCount = response.autocompletePredictions.size
                hintList = if(autocompletePredictionCount > maxHints) {
                    response.autocompletePredictions.subList(0, maxHints)
                } else {
                    response.autocompletePredictions.subList(0, autocompletePredictionCount)
                }

                val hintListPlaces = mutableListOf<String>()
                for( i in 0 until hintList.size) {
                    hintListPlaces.add(hintList[i].getFullText(null).toString())
                }

                (context as IWebRequestHandler).onFindAutocompletePredictionsFinished(hintListPlaces)
            }
    }

    private fun saveLocation(context: Context, lat: String, lon: String, city: String?, placeId: String) {
        with(context) {
            val sharedPreferences = context.getSharedPreferences("settings", MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString(getString(R.string.Latitude), lat)
                putString(getString(R.string.Longitude), lon)
                putString(getString(R.string.City), city)
                putString(getString(R.string.placeid), placeId)
                commit()
            }
        }
    }

    fun getLocation(context: Context, parameter: String): String? {
        with(context) {
            val sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
            return when(parameter) {
                getString(R.string.Latitude) -> sharedPreferences.getString(getString(R.string.Latitude), null)
                getString(R.string.Longitude) -> sharedPreferences.getString(getString(R.string.Longitude), null)
                getString(R.string.City) -> sharedPreferences.getString(getString(R.string.City), null)
                else -> null
            }
        }
    }
}