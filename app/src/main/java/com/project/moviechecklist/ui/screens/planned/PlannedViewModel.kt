package com.project.moviechecklist.ui.screens.planned

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
class PlannedViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    val plannedMovies: StateFlow<List<MovieEntity>> = repository.getPlannedMovies()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun moveToWatched(movie: MovieEntity) {
        viewModelScope.launch {
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