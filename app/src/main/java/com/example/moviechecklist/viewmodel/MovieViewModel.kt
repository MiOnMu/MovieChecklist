package com.example.moviechecklist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviechecklist.repository.MovieRepository
import kotlinx.coroutines.launch

class MovieViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    fun fetchMovies(genreId: Int) = viewModelScope.launch {
        val movies = repository.fetchMoviesByGenre(genreId)
        // Update UI state
    }
}