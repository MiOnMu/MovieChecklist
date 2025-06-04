package com.example.moviechecklist.viewmodel // Corrected package name

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviechecklist.model.Movie
import com.example.moviechecklist.repository.MovieRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WishlistViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    val wishlistMovies: StateFlow<List<Movie>> = repository.getWishlistMovies()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleWatched(movie: Movie) { // Optionally move to watched
        viewModelScope.launch {
            // If moving to watched, ensure it's marked as watched and potentially removed from wishlist
            val updatedMovie = movie.copy(isWatched = !movie.isWatched, isInWishlist = if (!movie.isWatched) false else movie.isInWishlist)
            // This logic can be more refined in the repository or here based on exact UX desired
            repository.toggleWatchedStatus(updatedMovie) // This will use the new isWatched flag
            if (!movie.isWatched && movie.isInWishlist) { // if it was marked as watched and was in wishlist
                // repository.toggleWishlistStatus(updatedMovie.copy(isInWishlist = false)) // remove from wishlist
            }
        }
    }

    fun toggleWishlist(movie: Movie) { // Typically to remove from wishlist
        viewModelScope.launch {
            repository.toggleWishlistStatus(movie)
        }
    }
}