package com.yourpackage.moviechecklist.ui.screens.planned

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.yourpackage.moviechecklist.R
import com.yourpackage.moviechecklist.data.local.MovieEntity
import com.yourpackage.moviechecklist.ui.navigation.Screen
import com.yourpackage.moviechecklist.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannedScreen(
    navController: NavController,
    viewModel: PlannedViewModel = hiltViewModel()
) {
    val plannedMovies by viewModel.plannedMovies.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Planned Movies & Series") })
        }
    ) { paddingValues ->
        if (plannedMovies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Visibility,
                        contentDescription = "Empty planned list",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Nothing in your watchlist yet.",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        "Use the Search tab to find movies and add them!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(plannedMovies, key = { it.id }) { movie ->
                    PlannedMovieItem(
                        movie = movie,
                        onMovieClick = {
                            navController.navigate(Screen.MovieDetail.createRoute(movie.id, movie.mediaType))
                        },
                        onMarkAsWatchedClick = {
                            viewModel.moveToWatched(movie)
                            navController.navigate(Screen.MovieDetail.createRoute(movie.id, movie.mediaType)) {
                                launchSingleTop = true
                            }
                        },
                        onRemoveClick = {
                            viewModel.removeFromPlanned(movie)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PlannedMovieItem(
    movie: MovieEntity,
    onMovieClick: () -> Unit,
    onMarkAsWatchedClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMovieClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
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
                    .width(90.dp)
                    .height(135.dp)
                    .aspectRatio(2f / 3f),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Released: ${movie.releaseDate?.takeIf { it.isNotBlank() } ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Type: ${movie.mediaType.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    movie.overview,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3, // Allow more lines for overview
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(135.dp)
            ) {
                IconButton(
                    onClick = onMarkAsWatchedClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Filled.CheckCircleOutline,
                        contentDescription = "Mark as Watched",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = "Remove from Planned",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}