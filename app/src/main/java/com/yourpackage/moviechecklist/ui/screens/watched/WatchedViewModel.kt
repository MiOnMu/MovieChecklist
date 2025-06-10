package com.yourpackage.moviechecklist.ui.screens.watched

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourpackage.moviechecklist.data.local.MovieEntity
import com.yourpackage.moviechecklist.data.local.MovieStatus
import com.yourpackage.moviechecklist.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchedViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    val watchedMovies: StateFlow<List<MovieEntity>> = repository.getWatchedMovies()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateUserRating(movie: MovieEntity, rating: Int?) {
        viewModelScope.launch {
            val updatedMovie = movie.copy(userRating = rating, status = MovieStatus.WATCHED) // Ensure status is correct
            repository.updateMovieInLibrary(updatedMovie)
        }
    }

    fun moveToPlanned(movie: MovieEntity) {
        viewModelScope.launch {
            val updatedMovie = movie.copy(status = MovieStatus.PLANNED, userRating = null) // Clear rating when moving to planned
            repository.updateMovieInLibrary(updatedMovie)
        }
    }
}