package com.akvelon.weather.web

import android.os.AsyncTask
import java.net.URL

class WebRequest(private val webRequestHandler: IWebRequestHandler, private val request: String) :
    AsyncTask<String, String, String>() {
    override fun doInBackground(vararg url: String?): String? = try {
        URL(request).readText()
    } catch (e: Exception) {
        null
    }

    override fun onPostExecute(response: String?) {
        super.onPostExecute(response)
        webRequestHandler.onRequestFinished(response)
    }
}