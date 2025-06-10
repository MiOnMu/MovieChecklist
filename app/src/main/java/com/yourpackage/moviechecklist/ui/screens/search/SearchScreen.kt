package com.yourpackage.moviechecklist.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.yourpackage.moviechecklist.R
import com.yourpackage.moviechecklist.data.remote.dto.MovieResultDto
import com.yourpackage.moviechecklist.data.local.MovieStatus
import com.yourpackage.moviechecklist.ui.navigation.Screen
import com.yourpackage.moviechecklist.util.Constants
import com.yourpackage.moviechecklist.util.Resource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery
    val searchResults by viewModel.searchResults.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val libraryMoviesMap by viewModel.libraryMoviesMap.collectAsState()
    val mediaTypeFilter by viewModel.mediaTypeFilter


    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Search Movies & Series") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Search...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear Search")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    keyboardController?.hide()
                })
            )

            // Filters
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(selected = mediaTypeFilter == null, onClick = { viewModel.setMediaTypeFilter(null) }, label = {Text("All")})
                FilterChip(selected = mediaTypeFilter == "movie", onClick = { viewModel.setMediaTypeFilter("movie") }, label = {Text("Movies")})
                FilterChip(selected = mediaTypeFilter == "tv", onClick = { viewModel.setMediaTypeFilter("tv") }, label = {Text("Series")})
            }


            // Results
            when (val resource = searchResults) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    val movies = resource.data?.results
                    if (movies.isNullOrEmpty() && searchQuery.length > 2) {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text("No results found for \"$searchQuery\". Try a different search term or adjust filters.", textAlign = TextAlign.Center)
                        }
                    } else if (!movies.isNullOrEmpty()){
                        LazyColumn(
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(movies, key = { it.id }) { movie ->
                                val movieStatus = libraryMoviesMap[movie.id]
                                SearchResultItem(
                                    movie = movie,
                                    movieStatus = movieStatus,
                                    onItemClick = {
                                        navController.navigate(Screen.MovieDetail.createRoute(movie.id, movie.mediaType ?: "movie"))
                                    },
                                    onAddClick = {
                                        viewModel.addMovieToPlanned(movie)
                                    }
                                )
                            }
                        }
                    } else if (searchQuery.isBlank()){
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text("Start typing to search for movies or TV series.", textAlign = TextAlign.Center)
                        }
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("Error: ${resource.message}", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    movie: MovieResultDto,
    movieStatus: MovieStatus?,
    onItemClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
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
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .build(),
                contentDescription = movie.title ?: movie.name,
                modifier = Modifier.width(60.dp).height(90.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(movie.title ?: movie.name ?: "Unknown", style = MaterialTheme.typography.titleSmall)
                Text(
                    "(${movie.mediaType?.replaceFirstChar { it.uppercase() } ?: "N/A"}) ${movie.releaseDate?.take(4) ?: movie.firstAirDate?.take(4) ?: ""}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                when (movieStatus) {
                    MovieStatus.WATCHED -> {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Already Watched",
                            tint = Color(0xFF4CAF50)
                        )
                    }

                    MovieStatus.PLANNED -> {
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = "Already Planned",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    null -> {
                        IconButton(onClick = onAddClick) {
                            Icon(
                                Icons.Filled.AddCircleOutline,
                                contentDescription = "Add to Planned",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}