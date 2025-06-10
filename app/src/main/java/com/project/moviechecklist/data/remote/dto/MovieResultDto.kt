package com.project.moviechecklist.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MovieResultDto(
    val id: Int,
    val title: String?,
    val name: String?,
    @SerializedName("overview") val overview: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("genre_ids") val genreIds: List<Int>,
    @SerializedName("media_type") var mediaType: String?
)

data class SearchResponseDto(
    val page: Int,
    val results: List<MovieResultDto>,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int
)

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
    @SerializedName("episode_run_time") val episodeRunTime: List<Int>?,
    val status: String
)