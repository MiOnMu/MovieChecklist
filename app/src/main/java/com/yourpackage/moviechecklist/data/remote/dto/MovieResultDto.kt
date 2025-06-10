package com.yourpackage.moviechecklist.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MovieResultDto(
    val id: Int,
    val title: String?, // For movies
    val name: String?,   // For TV shows
    @SerializedName("overview") val overview: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("release_date") val releaseDate: String?, // For movies
    @SerializedName("first_air_date") val firstAirDate: String?, // For TV shows
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("genre_ids") val genreIds: List<Int>,
    @SerializedName("media_type") var mediaType: String? // "movie" or "tv", TMDB search multi might provide this
)

data class SearchResponseDto(
    val page: Int,
    val results: List<MovieResultDto>,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int
)

// You'll also need a MovieDetailDto for fetching details (including full genre names)
data class GenreDto(
    val id: Int,
    val name: String
)

data class MovieDetailDto(
    val id: Int,
    val title: String?,
    val name: String?,
    val overview: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("vote_average") val voteAverage: Double,
    val genres: List<GenreDto>,
    val runtime: Int?, // For movies
    @SerializedName("episode_run_time") val episodeRunTime: List<Int>?, // For TV
    val status: String // e.g., "Released"
    // Add other fields as needed from TMDB movie/tv detail endpoint
)