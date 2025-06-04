package com.example.moviechecklist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviechecklist.model.Movie
import com.example.moviechecklist.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject // Make sure this import is present

@HiltViewModel
class MovieViewModel @Inject constructor( // Add @Inject here
    private val repository: MovieRepository
) : ViewModel() {

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun fetchMoviesByGenre(genreId: Int = 28) {
        viewModelScope.launch {
            _isLoading.value = true
            val fetchedMovies = repository.fetchMoviesByGenre(genreId)
            _movies.value = fetchedMovies
            _isLoading.value = false
        }
    }

    fun toggleWatched(movie: Movie) {
        viewModelScope.launch {
            repository.toggleWatchedStatus(movie)
            updateMovieInState(movie.copy(isWatched = !movie.isWatched))
        }
    }

    fun toggleWishlist(movie: Movie) {
        viewModelScope.launch {
            repository.toggleWishlistStatus(movie)
            updateMovieInState(movie.copy(isInWishlist = !movie.isInWishlist))
        }
    }

    private fun updateMovieInState(updatedMovie: Movie) {
        _movies.value = _movies.value.map {
            if (it.id == updatedMovie.id) updatedMovie else it
        }
    }

    init {
        fetchMoviesByGenre()
    }
}