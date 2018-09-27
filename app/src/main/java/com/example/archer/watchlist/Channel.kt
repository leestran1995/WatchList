package com.example.archer.watchlist

import android.os.Parcelable
import java.io.Serializable
import java.util.*

data class Channel(val title: String, var media: ArrayList<Media> = ArrayList<Media>(), var currentSeed: Int = 0) : Serializable{
    init {
        // We don't want all initial seeds to be the same
        val gen = Random()
        currentSeed = gen.nextInt(100)
    }
}