package com.example.moviechecklist.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val genreIds: List<Int>,
    val imdbRating: Double?,
    val isWatched: Boolean = false,
    val isInWishlist: Boolean = false
)