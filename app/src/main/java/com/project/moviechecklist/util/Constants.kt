package com.project.moviechecklist.util

object Constants {
    const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
    const val TMDB_IMAGE_BASE_URL_LOW = "https://image.tmdb.org/t/p/w500"
    const val TMDB_IMAGE_BASE_URL_HIGH = "https://image.tmdb.org/t/p/original"
    
    // Default for backward compatibility if needed, but we'll use the ones above
    const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w780"
}
