package com.yourpackage.moviechecklist.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.yourpackage.moviechecklist.R
import com.yourpackage.moviechecklist.data.local.MovieEntity
import com.yourpackage.moviechecklist.data.local.MovieStatus
import com.yourpackage.moviechecklist.util.Constants
import com.yourpackage.moviechecklist.util.Resource
import com.yourpackage.moviechecklist.ui.screens.common.StarRatingInput
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    navController: NavController,
    movieId: Int, // Passed via navigation
    mediaType: String, // Passed via navigation
    viewModel: MovieDetailViewModel = hiltViewModel()
) {
    val movieDetailsResource by viewModel.movieDetails.collectAsState()
    var showRatingDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = when (movieDetailsResource) {
                        is Resource.Success -> (movieDetailsResource.data?.title ?: "Details")
                        else -> "Details"
                    }
                    Text(text = title, maxLines = 1)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val resource = movieDetailsResource) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${resource.message}", color = MaterialTheme.colorScheme.error)
                        resource.data?.let { movie -> // Show partial data if available
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Partial data for: ${movie.title}", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(16.dp))
                            AddOrChangeStatusButtons(movie = movie, viewModel = viewModel, onShowRatingDialog = { showRatingDialog = true })
                        }
                    }
                }
            }
            is Resource.Success -> {
                val movie = resource.data
                if (movie == null) {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text("Movie details not found.")
                    }
                    return@Scaffold
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(movie.backdropPath?.let { Constants.TMDB_IMAGE_BASE_URL_W500 + it } ?: movie.posterPath?.let { Constants.TMDB_IMAGE_BASE_URL_W500 + it })
                            .crossfade(true)
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_background)
                            .build(),
                        contentDescription = movie.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Crop
                    )

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(movie.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "(${movie.mediaType.replaceFirstChar { it.uppercase() }}) Released: ${movie.releaseDate ?: "N/A"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        movie.voteAverage?.let { tmdbRating ->
                            Text("TMDB Rating: %.1f/10".format(tmdbRating), style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (movie.genres.isNotEmpty()) {
                            Text("Genres: ${movie.genres.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Text("Overview", style = MaterialTheme.typography.titleMedium)
                        Text(movie.overview, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Status and Rating Section
                        Text("Your Status:", style = MaterialTheme.typography.titleMedium)
                        Text(movie.status.name.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyLarge)

                        if (movie.status == MovieStatus.WATCHED) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Your Rating:", style = MaterialTheme.typography.titleMedium)
                            StarRatingInput(
                                currentRating = movie.userRating ?: 0,
                                onRatingChange = { newRating ->
                                    viewModel.updateRating(newRating)
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        AddOrChangeStatusButtons(movie = movie, viewModel = viewModel, onShowRatingDialog = { showRatingDialog = true })
                    }
                }

                if (showRatingDialog && movie.status == MovieStatus.PLANNED) { // Only show for planned when moving to watched
                    RatingPromptDialog(
                        onDismiss = { showRatingDialog = false },
                        onConfirm = { rating ->
                            viewModel.markAsWatched(rating)
                            showRatingDialog = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddOrChangeStatusButtons(movie: MovieEntity, viewModel: MovieDetailViewModel, onShowRatingDialog: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        if (movie.status == MovieStatus.PLANNED) {
            Button(onClick = { onShowRatingDialog() }) { // Show dialog before marking as watched
                Icon(Icons.Filled.CheckCircle, contentDescription = "Watched Icon")
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Mark as Watched & Rate")
            }
        } else if (movie.status == MovieStatus.WATCHED) {
            Button(
                onClick = { viewModel.markAsPlanned() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Planned Icon")
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Move to Planned")
            }
        } else { // If not in library (e.g. viewing from fresh search result)
            Button(onClick = { viewModel.addCurrentMovieToPlanned() }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Icon")
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Add to Planned List")
            }
        }
    }
}


@Composable
fun RatingPromptDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int?) -> Unit // Rating can be optional if user skips
) {
    var currentRating by remember { mutableIntStateOf(0) } // Initial rating for the dialog

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate this Movie/Series") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("How would you rate it (1-5 stars)?")
                Spacer(modifier = Modifier.height(16.dp))
                StarRatingInput(
                    currentRating = currentRating,
                    onRatingChange = { rating -> currentRating = rating }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(if (currentRating == 0) null else currentRating) }) { // Pass null if not rated
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onConfirm(null) // User chose to skip rating
                onDismiss()
            }) {
                Text("Skip Rating")
            }
        }
    )
}