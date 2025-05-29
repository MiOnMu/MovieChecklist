package com.example.moviechecklist.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.moviechecklist.model.Movie

@Composable
fun MovieCard(
    movie: Movie,
    onToggleWatched: () -> Unit,
    onToggleWishlist: () -> Unit
) {
    Card {
        Column {
            Text(text = movie.title)
            Text(text = movie.overview)
            Button(onClick = onToggleWatched) {
                Text("Watched")
            }
            Button(onClick = onToggleWishlist) {
                Text("Wishlist")
            }
        }
    }
}