package com.yourpackage.moviechecklist.ui.screens.watched

import com.yourpackage.moviechecklist.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.yourpackage.moviechecklist.data.local.MovieEntity
import com.yourpackage.moviechecklist.ui.navigation.Screen
import com.yourpackage.moviechecklist.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchedScreen(
    navController: NavController,
    viewModel: WatchedViewModel = hiltViewModel()
) {
    val watchedMovies by viewModel.watchedMovies.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Watched Movies & Series") })
        }
    ) { paddingValues ->
        if (watchedMovies.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No movies marked as watched yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(watchedMovies, key = { it.id }) { movie ->
                    WatchedMovieItem(
                        movie = movie,
                        onMovieClick = {
                            navController.navigate(Screen.MovieDetail.createRoute(movie.id, movie.mediaType))
                        },
                        onRateClick = {
                            navController.navigate(Screen.MovieDetail.createRoute(movie.id, movie.mediaType))
                        },
                        onMoveToPlannedClick = {
                            viewModel.moveToPlanned(movie)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WatchedMovieItem(
    movie: MovieEntity,
    onMovieClick: () -> Unit,
    onRateClick: () -> Unit,
    onMoveToPlannedClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMovieClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movie.posterPath?.let { Constants.TMDB_IMAGE_BASE_URL + it } ?: "")
                    .crossfade(true)
                    .error(R.drawable.ic_launcher_background)
                    .placeholder(R.drawable.ic_launcher_background)
                    .build(),
                contentDescription = movie.title,
                modifier = Modifier
                    .width(80.dp)
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(movie.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Released: ${movie.releaseDate ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Type: ${movie.mediaType.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall
                )
                movie.userRating?.let { rating ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Your Rating: ", style = MaterialTheme.typography.bodySmall)
                        for (i in 1..5) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Star $i",
                                tint = if (i <= rating) Color.Yellow else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } ?: Text("Not rated yet", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    movie.overview,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onRateClick, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Star, contentDescription = "Rate Movie", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onMoveToPlannedClick, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Outlined.ListAlt, contentDescription = "Move to Planned",  tint = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}