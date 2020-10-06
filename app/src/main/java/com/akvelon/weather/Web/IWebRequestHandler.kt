package com.akvelon.weather.web

interface IWebRequestHandler {
    fun onRequestFinished(response: String?)
    fun onGetPlaceIdRequestFinished(response: String?)
    fun onGetCurrentPlaceRequestFinished(response: String)
    fun onSearchPlaceStart(response: String)
}