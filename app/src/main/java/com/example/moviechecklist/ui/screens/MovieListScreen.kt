package com.example.moviechecklist.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.moviechecklist.components.MovieCard
import com.example.moviechecklist.model.Movie
import com.example.moviechecklist.viewmodel.MovieViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    viewModel: MovieViewModel,
    onMovieClicked: (Movie) -> Unit // Changed to pass the whole Movie object
) {
    val movies by viewModel.movies.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") } // Simple search by title locally

    // Example: Fetch movies when the screen is first composed or based on some trigger
    // This is now handled in ViewModel's init block for a default genre.
    // You might want to add UI elements to trigger fetching different genres.
    // LaunchedEffect(Unit) {
    //     viewModel.fetchMoviesByGenre(28) // Example: Fetch Action movies
    // }

    Column(modifier = Modifier.fillMaxSize()) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search movies by title...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (movies.isEmpty() && searchQuery.isBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No movies found. Try fetching or check your API key.")
                }
            } else {
                val filteredMovies = if (searchQuery.isBlank()) {
                    movies
                } else {
                    movies.filter { it.title.contains(searchQuery, ignoreCase = true) }
                }

                if (filteredMovies.isEmpty() && searchQuery.isNotBlank()) {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No movies match your search query.")
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredMovies, key = { movie -> movie.id }) { movie ->
                            MovieCard(
                                movie = movie,
                                onToggleWatched = { viewModel.toggleWatched(movie) },
                                onToggleWishlist = { viewModel.toggleWishlist(movie) }
                                // modifier = Modifier.clickable { onMovieClicked(movie) } // If you want to navigate on click
                            )
                        }
                    }
                }
            }
        }
    }
}