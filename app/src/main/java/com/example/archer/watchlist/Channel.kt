package com.example.archer.watchlist

import android.os.Parcelable
import java.io.Serializable

data class Channel(val title: String, var media: ArrayList<Media> = ArrayList<Media>()) : Serializable