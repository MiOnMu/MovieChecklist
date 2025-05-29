package com.example.moviechecklist.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import com.example.moviechecklist.components.MovieCard
import com.example.moviechecklist.viewmodel.MovieViewModel

@Composable
fun MovieListScreen(
    viewModel: MovieViewModel
) {
    LazyColumn {
        items(viewModel.movies) { movie ->
            MovieCard(
                movie = movie,
                onToggleWatched = { viewModel.toggleWatched(movie) },
                onToggleWishlist = { viewModel.toggleWishlist(movie) }
            )
        }
    }
}