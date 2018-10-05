package com.example.archer.watchlist.dataclasses

import java.io.Serializable

class OmdbSearchResponse(val Search: ArrayList<MediaResponse>, val Response: String) : Serializable
class MediaResponse(val Title: String, val Year: String, val imdbId: String, val Poster: String) : Serializable