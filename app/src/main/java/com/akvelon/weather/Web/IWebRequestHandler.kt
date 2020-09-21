package com.akvelon.weather.web

interface IWebRequestHandler {
    fun onRequestFinished(response: String?)
}