package com.akvelon.weather

interface IWebRequestHandler {
    fun onRequestFinished(response: String?)
}