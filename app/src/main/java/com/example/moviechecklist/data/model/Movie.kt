package com.example.moviechecklist.model

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val genreIds: List<Int>,
    val imdbRating: Double?
)

data class MovieResponse(
    val results: List<Movie>
)