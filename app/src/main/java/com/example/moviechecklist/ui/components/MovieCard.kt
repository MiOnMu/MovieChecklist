package com.example.moviechecklist.components

import android.util.Log
import androidx.compose.foundation.Image // Keep for Landscapist failure/success if you use it, or remove if only AsyncImage
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
// import androidx.compose.material3.Button // Not used in this snippet, remove if not needed elsewhere
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator // Import Compose M3 version
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Import Compose Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage // Using this one for the primary image
import coil.request.ImageRequest
import com.example.moviechecklist.R
import com.example.moviechecklist.model.Movie
// Remove: import com.google.android.material.progressindicator.CircularProgressIndicator
// Remove: import com.skydoves.landscapist.coil.CoilImage // Remove if not using the standalone one

@Composable
fun MovieCard(
    movie: Movie,
    onToggleWatched: () -> Unit,
    onToggleWishlist: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ADD THIS LOG
    Log.d("MovieCard_Debug", "Movie: ${movie.title}, Poster Path: ${movie.posterPath}")

    val imageUrl = movie.posterPath?.let { path -> "https://image.tmdb.org/t/p/w500$path" }
    // AND THIS LOG
    Log.d("MovieCard_Debug", "Constructed Image URL: $imageUrl")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl) // Use the logged imageUrl
                    .listener(onError = { request, result -> // Add error listener
                        Log.e("MovieCard_CoilError", "Image load failed for ${movie.title}: ${result.throwable.message}", result.throwable)
                    })
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(id = R.drawable.placeholder_image),
                error = painterResource(id = R.drawable.placeholder_image),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(100.dp, 150.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = movie.overview,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleWatched) {
                        Icon(
                            imageVector = if (movie.isWatched) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (movie.isWatched) "Mark as Unwatched" else "Mark as Watched",
                            tint = if (movie.isWatched) MaterialTheme.colorScheme.primary else Color.Gray // Now Color.Gray should resolve
                        )
                    }
                    Text(if (movie.isWatched) "Watched" else "Mark Watched")

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(onClick = onToggleWishlist) {
                        Icon(
                            imageVector = if (movie.isInWishlist) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (movie.isInWishlist) "Remove from Wishlist" else "Add to Wishlist",
                            tint = if (movie.isInWishlist) MaterialTheme.colorScheme.secondary else Color.Gray // Now Color.Gray should resolve
                        )
                    }
                    Text(if (movie.isInWishlist) "In Wishlist" else "Add to Wishlist")
                }
                movie.imdbRating?.let {
                    Text(
                        text = "Rating: $it/10",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
    // REMOVE THE STANDALONE CoilImage FROM HERE if you are using AsyncImage above
    /*
    CoilImage(
        imageModel = { movie.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" } },
        modifier = Modifier.size(100.dp, 150.dp),
        loading = {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) // Use Compose M3 CircularProgressIndicator
        },
        failure = {
            Image(painter = painterResource(id = R.drawable.placeholder_image), contentDescription = "Error")
        },
        success = { imageState, painter ->
            Image(
                painter = painter,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop // This is correct
            )
        },
        // contentScale = ContentScale.Crop, // This contentScale on CoilImage might be redundant if also in success->Image
        // placeholder = painterResource(id = R.drawable.placeholder_image)
    )
    */
}