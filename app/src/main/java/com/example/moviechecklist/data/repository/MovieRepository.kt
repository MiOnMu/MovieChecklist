package com.example.moviechecklist.repository

import android.util.Log
import com.example.moviechecklist.api.TMDBService
import com.example.moviechecklist.data.local.MovieDao
import com.example.moviechecklist.data.local.MovieEntity
import com.example.moviechecklist.model.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.moviechecklist.BuildConfig // Import BuildConfig

// Helper extension functions for mapping
fun MovieEntity.toMovie(): Movie = Movie(
    id = this.id,
    title = this.title,
    overview = this.overview,
    posterPath = this.posterPath,
    genreIds = this.genreIds,
    imdbRating = this.imdbRating,
    isWatched = this.isWatched,
    isInWishlist = this.isInWishlist
)

fun Movie.toMovieEntity(): MovieEntity = MovieEntity(
    id = this.id,
    title = this.title,
    overview = this.overview,
    posterPath = this.posterPath,
    genreIds = this.genreIds,
    imdbRating = this.imdbRating,
    isWatched = this.isWatched,
    isInWishlist = this.isInWishlist
)

class MovieRepository /* @Inject constructor(...) */ ( // Constructor injection handled by Hilt
    private val tmdbService: TMDBService,
    private val movieDao: MovieDao
) {
    // IMPORTANT: Replace "YOUR_TMDB_API_KEY" with your actual TMDB API key
    private val apiKey = BuildConfig.TMDB_API_KEY

    suspend fun fetchMoviesByGenre(genreId: Int): List<Movie> {
        if (apiKey == "YOUR_TMDB_API_KEY") {
            Log.e("MovieRepository", "API Key not set. Please set your TMDB API key.")
            // Return an empty list or throw an error to indicate the problem
            return emptyList()
        }
        try {
            val response = tmdbService.getMoviesByGenre(
                apiKey = apiKey,
                genreId = genreId
            )
            // Here, we might want to enrich these Movies with local DB status (isWatched, isInWishlist)
            // For now, returning them as is from API. The ViewModel can handle merging if needed.
            return response.results.map { apiMovie ->
                val localMovie = movieDao.getMovieById(apiMovie.id)
                if (localMovie != null) {
                    // If movie exists locally, use its watched/wishlist status
                    apiMovie.copy(isWatched = localMovie.isWatched, isInWishlist = localMovie.isInWishlist)
                } else {
                    // Otherwise, it's a new movie from API, default flags are false
                    apiMovie
                }
            }
        } catch (e: Exception) {
            Log.e("MovieRepository", "Error fetching movies by genre: ${e.message}", e)
            return emptyList() // Return empty list on error
        }
    }

    fun getWatchedMovies(): Flow<List<Movie>> {
        return movieDao.getWatchedMovies().map { entities ->
            entities.map { it.toMovie() }
        }
    }

    fun getWishlistMovies(): Flow<List<Movie>> {
        return movieDao.getWishlistMovies().map { entities ->
            entities.map { it.toMovie() }
        }
    }

    suspend fun toggleWatchedStatus(movie: Movie) {
        val updatedMovie = movie.copy(isWatched = !movie.isWatched)
        // If moving to watched, optionally remove from wishlist
        // if (updatedMovie.isWatched && updatedMovie.isInWishlist) {
        // updatedMovie = updatedMovie.copy(isInWishlist = false)
        // }
        movieDao.insertMovie(updatedMovie.toMovieEntity())
    }

    suspend fun toggleWishlistStatus(movie: Movie) {
        val updatedMovie = movie.copy(isInWishlist = !movie.isInWishlist)
        movieDao.insertMovie(updatedMovie.toMovieEntity())
    }

    // Optional: Add a movie to the database if it's not already there,
    // typically when it's first fetched or interacted with.
    // The toggle functions with OnConflictStrategy.REPLACE already handle this.
    suspend fun addMovieToLocalCache(movie: Movie) {
        if (movieDao.getMovieById(movie.id) == null) {
            movieDao.insertMovie(movie.toMovieEntity())
        }
    }
}