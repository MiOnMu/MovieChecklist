package com.yourpackage.moviechecklist.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: Int, // Using TMDB movie ID as primary key
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?, // Or Date type with TypeConverter
    val voteAverage: Double?, // TMDB rating
    val genres: List<String>, // Store as JSON string or use relation
    var status: MovieStatus,
    var userRating: Int? = null, // User's own rating (e.g., 1-5 stars)
    val mediaType: String // "movie" or "tv"
)