package com.project.moviechecklist.ui.screens.watched

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.moviechecklist.data.local.MovieEntity
import com.project.moviechecklist.data.local.MovieStatus
import com.project.moviechecklist.data.repository.MovieRepository
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
            val updatedMovie = movie.copy(userRating = rating, status = MovieStatus.WATCHED)
            repository.updateMovieInLibrary(updatedMovie)
        }
    }

    fun moveToPlanned(movie: MovieEntity) {
        viewModelScope.launch {
            val updatedMovie = movie.copy(status = MovieStatus.PLANNED, userRating = null)
            repository.updateMovieInLibrary(updatedMovie)
        }
    }
}