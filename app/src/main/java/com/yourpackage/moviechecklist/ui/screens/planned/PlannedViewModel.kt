package com.yourpackage.moviechecklist.ui.screens.planned

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
class PlannedViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    val plannedMovies: StateFlow<List<MovieEntity>> = repository.getPlannedMovies()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L), // Keep active for 5s after last collector stops
            initialValue = emptyList()
        )

    fun moveToWatched(movie: MovieEntity) {
        viewModelScope.launch {
            // The UI (e.g., MovieDetailScreen or a dialog) will handle setting the rating.
            // Here, we just update the status.
            val updatedMovie = movie.copy(status = MovieStatus.WATCHED)
            repository.updateMovieInLibrary(updatedMovie)
        }
    }

    fun removeFromPlanned(movie: MovieEntity) {
        viewModelScope.launch {
            repository.deleteMovieFromLibrary(movie)
        }
    }
}