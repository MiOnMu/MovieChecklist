package com.example.moviechecklist.repository

import com.example.moviechecklist.api.TMDBService
import com.example.moviechecklist.data.local.MovieDao
import com.example.moviechecklist.model.Movie
import kotlinx.coroutines.flow.Flow

class MovieRepository(
    private val tmdbService: TMDBService,
    private val movieDao: MovieDao
) {
    suspend fun fetchMoviesByGenre(genreId: Int): List<Movie> {
        return tmdbService.getMoviesByGenre(
            apiKey = "YOUR_TMDB_API_KEY",
            genreId = genreId
        ).results
    }

    fun getWatchedMovies(): Flow<List<Movie>> {
        return movieDao.getWatchedMovies()
    }

    fun getWishlistMovies(): Flow<List<Movie>> {
        return movieDao.getWishlistMovies()
    }

    suspend fun toggleWatchedStatus(movie: Movie) {
        movieDao.updateMovie(movie.copy(isWatched = !movie.isWatched))
    }
}