package com.example.moviechecklist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Corrected import
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults // For scrollBehavior
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.moviechecklist.screens.MovieListScreen // Main movie list from API
import com.example.moviechecklist.ui.screens.WatchedListScreen
import com.example.moviechecklist.ui.screens.WishlistScreen
import com.example.moviechecklist.viewmodel.MovieViewModel
import com.example.moviechecklist.viewmodel.ViewModelFactory
import com.example.moviechecklist.viewmodel.WatchedViewModel
import com.example.moviechecklist.viewmodel.WishlistViewModel
import androidx.hilt.navigation.compose.hiltViewModel

// Enum class representing the destinations in the app.
// Changed title from @StringRes Int to String
enum class MovieChecklistDestinations(val routeName: String, val title: String) {
    MOVIE_LIST("movie_list_screen", "Discover Movies"), // Route name and display title
    WATCHED("watched_screen", "Watched Movies"),
    WISHLIST("wishlist_screen", "My Wishlist")
    // Add more destinations here if needed, e.g., MovieDetails
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieChecklistApp(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: MovieChecklistDestinations.MOVIE_LIST.routeName

    // Determine current screen based on route for title
    val currentScreen = MovieChecklistDestinations.values().find { it.routeName == currentRoute }
        ?: MovieChecklistDestinations.MOVIE_LIST

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior() // Or enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MovieChecklistTopBar(
                title = currentScreen.title, // Use the string title
                showNavigationIcon = navController.previousBackStackEntry != null,
                scrollBehavior = scrollBehavior
            ) {
                navController.navigateUp()
            }
        }
        // If you want a bottom bar for navigation similar to NavGraph.kt:
        // bottomBar = { BottomNavigationBar(navController) } // You'd need to define/import BottomNavigationBar
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MovieChecklistDestinations.MOVIE_LIST.routeName,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            composable(MovieChecklistDestinations.MOVIE_LIST.routeName) {
                val movieViewModel: MovieViewModel = hiltViewModel() // Get ViewModel using Hilt
                MovieListScreen(
                    viewModel = movieViewModel,
                    onMovieClicked = { movie ->
                        // Handle movie click, e.g., navigate to a detail screen
                        // navController.navigate("${MovieDetailsDestination.name}/${movie.id}")
                    }
                )
            }

            composable(MovieChecklistDestinations.WATCHED.routeName) {
                val watchedViewModel: WatchedViewModel = hiltViewModel()
                WatchedListScreen(viewModel = watchedViewModel)
            }
            composable(MovieChecklistDestinations.WISHLIST.routeName) {
                val wishlistViewModel: WishlistViewModel = hiltViewModel()
                WishlistScreen(viewModel = wishlistViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieChecklistTopBar(
    title: String,
    showNavigationIcon: Boolean,
    scrollBehavior: TopAppBarScrollBehavior? = null, // Added scrollBehavior
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
        },
        scrollBehavior = scrollBehavior
    )
}

// Placeholder for BottomNavigationBar if you choose to integrate it here
// (similar to the one defined in the modified NavGraph.kt)
/*
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        MovieChecklistDestinations.MOVIE_LIST,
        MovieChecklistDestinations.WATCHED,
        MovieChecklistDestinations.WISHLIST
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        items.forEach { screen ->
            NavigationBarItem(
                icon = { /* Provide appropriate icons */
                    // Example: Icon(if (screen == MovieChecklistDestinations.MOVIE_LIST) Icons.Filled.Movie else if (screen == MovieChecklistDestinations.WATCHED) Icons.Filled.Visibility else Icons.Filled.Favorite, contentDescription = screen.title)
                    when(screen) {
                         MovieChecklistDestinations.MOVIE_LIST -> Icon(Icons.Filled.Home, contentDescription = screen.title)
                         MovieChecklistDestinations.WATCHED -> Icon(Icons.Filled.List, contentDescription = screen.title)
                         MovieChecklistDestinations.WISHLIST -> Icon(Icons.Filled.Star, contentDescription = screen.title)
                    }
                },
                label = { Text(screen.title) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.routeName } == true,
                onClick = {
                    navController.navigate(screen.routeName) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
*/