package com.example.moviechecklist

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.moviechecklist.ui.screens.MovieListScreen

/**
 * Enum class representing the destinations in the app.
 */
enum class MovieChecklistDestinations(@StringRes val title: Int) {
    MOVIE_LIST(R.string.movie_list_title),
    WATCHED(R.string.watched_title),
    WISHLIST(R.string.wishlist_title)
}

/**
 * Main entry point of the app, handling navigation and top bar.
 */
@Composable
fun MovieChecklistApp(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = MovieChecklistDestinations.valueOf(
        backStackEntry?.destination?.route ?: MovieChecklistDestinations.MOVIE_LIST.name
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MovieChecklistTopBar(
                title = stringResource(currentScreen.title),
                showNavigationIcon = navController.previousBackStackEntry != null
            ) {
                navController.navigateUp()
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MovieChecklistDestinations.MOVIE_LIST.name,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Ekran główny: lista filmów (pobrana z TMDB)
            composable(MovieChecklistDestinations.MOVIE_LIST.name) {
                MovieListScreen(
                    onMovieClicked = { movieId ->
                        // Przejdź do szczegółów filmu (opcjonalnie)
                        // navController.navigate("${MovieDetailsDestination.name}/$movieId")
                    }
                )
            }

            // Ekran obejrzanych filmów
            composable(MovieChecklistDestinations.WATCHED.name) {
                WatchedMoviesScreen()
            }

            // Ekran wishlisty
            composable(MovieChecklistDestinations.WISHLIST.name) {
                WishlistScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieChecklistTopBar(
    title: String,
    showNavigationIcon: Boolean,
    onNavigateUp: () -> Unit,
) {
    TopAppBar(
        title = { Text(text = title, style = MaterialTheme.typography.headlineMedium) },
        navigationIcon = {
            if (showNavigationIcon) {
                IconButton(onClick = onNavigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        }
    )
}