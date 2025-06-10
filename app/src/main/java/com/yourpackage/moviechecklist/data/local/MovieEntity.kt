package com.yourpackage.moviechecklist.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double?,
    val genres: List<String>,
    var status: MovieStatus?,
    var userRating: Int? = null,
    val mediaType: String
)