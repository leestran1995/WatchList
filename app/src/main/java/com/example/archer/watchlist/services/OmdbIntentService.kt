package com.example.archer.watchlist.services

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.example.archer.watchlist.constants.API_GOOD_RESPONSE
import com.example.archer.watchlist.constants.API_INVALID_TITLE
import com.example.archer.watchlist.constants.API_NO_RESPONSE
import com.example.archer.watchlist.constants.OMDB_RESPONSE
import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.IOException


/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * helper methods.
 */
class OmdbIntentService : IntentService("MyIntentService") {

    override fun onHandleIntent(intent: Intent?) {

        val title: String? = intent?.getStringExtra("title")
        val url = "https://www.omdbapi.com/?apikey=77a591dc&t=" + title
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("LEETAG", "Request to API failed")
                val outgoingIntent: Intent = Intent(OMDB_RESPONSE)
                outgoingIntent.putExtra("status", API_NO_RESPONSE)
                sendBroadcast(outgoingIntent)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response?.body()?.string()
                val gson = GsonBuilder().create()
                val mediaBody: OmdbResponse = gson.fromJson(body, OmdbResponse::class.java)
                val outgoingIntent: Intent = Intent(OMDB_RESPONSE)

                if(mediaBody.Title == null) {
                    outgoingIntent.putExtra("status", API_INVALID_TITLE)
                } else {
                    outgoingIntent.putExtra("status", API_GOOD_RESPONSE)
                }

                outgoingIntent.putExtra("title", mediaBody.Title)
                outgoingIntent.putExtra("year", mediaBody.Year)
                outgoingIntent.putExtra("plot", mediaBody.Plot)
                outgoingIntent.putExtra("imageUrl", mediaBody.Poster)
                sendBroadcast(outgoingIntent)
            }

        })
    }
}

class OmdbResponse(val Title: String? = null, val Year: String, val Plot: String, val Poster: String)
