package com.example.moviechecklist.ui.screens // Corrected package name

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.moviechecklist.components.MovieCard
import com.example.moviechecklist.viewmodel.WatchedViewModel

@Composable
fun WatchedListScreen(
    viewModel: WatchedViewModel
) {
    val watchedMovies by viewModel.watchedMovies.collectAsState()

    Column(Modifier.fillMaxSize()) {
        if (watchedMovies.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You haven't marked any movies as watched yet.")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(watchedMovies, key = { movie -> movie.id }) { movie ->
                    MovieCard(
                        movie = movie,
                        onToggleWatched = { viewModel.toggleWatched(movie) }, // Will mark as unwatched
                        onToggleWishlist = { viewModel.toggleWishlist(movie) }
                    )
                }
            }
        }
    }
}