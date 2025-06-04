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
import com.example.moviechecklist.viewmodel.WishlistViewModel

@Composable
fun WishlistScreen(
    viewModel: WishlistViewModel
) {
    val wishlistMovies by viewModel.wishlistMovies.collectAsState()

    Column(Modifier.fillMaxSize()) {
        if (wishlistMovies.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your wishlist is empty. Add some movies!")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(wishlistMovies, key = { movie -> movie.id }) { movie ->
                    MovieCard(
                        movie = movie,
                        onToggleWatched = { viewModel.toggleWatched(movie) }, // Will move to watched
                        onToggleWishlist = { viewModel.toggleWishlist(movie) } // Will remove from wishlist
                    )
                }
            }
        }
    }
}