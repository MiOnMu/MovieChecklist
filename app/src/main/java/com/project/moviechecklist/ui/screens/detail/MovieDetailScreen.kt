package com.project.moviechecklist.ui.screens.detail

import android.content.Intent
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.project.moviechecklist.R
import com.project.moviechecklist.data.local.MovieEntity
import com.project.moviechecklist.data.local.MovieStatus
import com.project.moviechecklist.util.Constants
import com.project.moviechecklist.util.Resource
import com.project.moviechecklist.ui.screens.common.StarRatingInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    navController: NavController,
    movieId: Int,
    mediaType: String,
    viewModel: MovieDetailViewModel = hiltViewModel()
) {
    val movieDetailsResource by viewModel.movieDetails.collectAsState()
    val isWifiConnected by viewModel.isWifiConnected.collectAsState()
    var showRatingDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = when (val resource = movieDetailsResource) {
                        is Resource.Success -> resource.data?.title ?: "Details"
                        else -> "Details"
                    }
                    Text(text = title, maxLines = 1)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    movieDetailsResource.data?.let { movie ->
                        IconButton(onClick = {
                            val shareText = "Check out this ${movie.mediaType}: ${movie.title}\n\n${movie.overview}\n\nView on TMDb: https://www.themoviedb.org/${movie.mediaType}/${movie.id}"
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
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
                        resource.data?.let { movie ->
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
                    val imageBaseUrl = if (isWifiConnected) Constants.TMDB_IMAGE_BASE_URL_HIGH else Constants.TMDB_IMAGE_BASE_URL_LOW
                    
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(movie.backdropPath?.let { imageBaseUrl + it } ?: movie.posterPath?.let { imageBaseUrl + it })
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = movie.title, 
                                style = MaterialTheme.typography.headlineSmall, 
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            if (isWifiConnected) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Wifi, 
                                            contentDescription = "HD Active",
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "HD",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }

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

                        Text(
                            text = movie.status?.name?.replaceFirstChar { it.uppercase() } ?: "Not in Library",
                            style = MaterialTheme.typography.bodyLarge
                        )
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

                if (showRatingDialog && movie.status == MovieStatus.PLANNED) {
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
        when (movie.status) {
            MovieStatus.PLANNED -> {
                Button(onClick = { onShowRatingDialog() }) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Watched Icon")
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Mark as Watched & Rate")
                }
            }
            MovieStatus.WATCHED -> {
                Button(
                    onClick = { viewModel.markAsPlanned() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Planned Icon")
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Move to Planned")
                }
            }
            null -> {
                Button(onClick = { viewModel.addCurrentMovieToPlanned() }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Icon")
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Add to Planned List")
                }
            }
        }
    }
}


@Composable
fun RatingPromptDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int?) -> Unit
) {
    var currentRating by remember { mutableIntStateOf(0) }

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
            Button(onClick = { onConfirm(if (currentRating == 0) null else currentRating) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onConfirm(null)
                onDismiss()
            }) {
                Text("Skip Rating")
            }
        }
    )
}
