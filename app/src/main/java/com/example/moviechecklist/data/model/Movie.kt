package com.example.moviechecklist.model

import com.google.gson.annotations.SerializedName

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("poster_path") // Ensure this matches the JSON field name if your variable name differs significantly
    val posterPath: String?,
    val genreIds: List<Int>,
    val imdbRating: Double?,
    // Added fields for UI state coherence
    val isWatched: Boolean = false,
    val isInWishlist: Boolean = false
)

data class MovieResponse(
    val results: List<Movie>
)