package com.project.moviechecklist.data.local

import androidx.room.Entity
import com.google.firebase.firestore.Exclude

@Entity(tableName = "movies", primaryKeys = ["id", "userId"])
data class MovieEntity(
    val id: Int = 0,
    val userId: String = "",
    val title: String = "",
    val overview: String = "",
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val releaseDate: String? = null,
    val voteAverage: Double? = null,
    val genres: List<String> = emptyList(),
    var status: MovieStatus? = null,
    var userRating: Int? = null,
    val mediaType: String = ""
) {
    // Parameterless constructor for Firestore
    constructor() : this(0, "", "", "", null, null, null, null, emptyList(), null, null, "")
}
