package com.example.moviechecklist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moviechecklist.repository.MovieRepository

class ViewModelFactory(private val repository: MovieRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovieViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(WatchedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WatchedViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(WishlistViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WishlistViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}