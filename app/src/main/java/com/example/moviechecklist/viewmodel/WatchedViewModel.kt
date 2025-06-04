package com.example.moviechecklist.viewmodel // Corrected package name

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviechecklist.model.Movie
import com.example.moviechecklist.repository.MovieRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WatchedViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    val watchedMovies: StateFlow<List<Movie>> = repository.getWatchedMovies()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleWatched(movie: Movie) { // Typically to unwatch
        viewModelScope.launch {
            repository.toggleWatchedStatus(movie)
        }
    }

    fun toggleWishlist(movie: Movie) { // Optionally add to wishlist if unwatching
        viewModelScope.launch {
            repository.toggleWishlistStatus(movie)
        }
    }
}