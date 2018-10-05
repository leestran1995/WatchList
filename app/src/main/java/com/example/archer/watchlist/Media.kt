package com.example.archer.watchlist

import java.io.Serializable

data class Media(
        val title: String,
        val imageLink: String = "https://www.computerhope.com/jargon/e/error.gif",
        val summary: String = "",
        val year: String = "0"
) : Serializable