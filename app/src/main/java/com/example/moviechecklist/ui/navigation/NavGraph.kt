package com.example.moviechecklist.ui.navigation // Corrected package name

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings // Example icon
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar // Corrected import for M3
import androidx.compose.material3.NavigationBarItem // Corrected import for M3
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.moviechecklist.screens.MovieListScreen
import com.example.moviechecklist.ui.screens.WatchedListScreen
import com.example.moviechecklist.ui.screens.WishlistScreen
import com.example.moviechecklist.viewmodel.MovieViewModel
import com.example.moviechecklist.viewmodel.ViewModelFactory
import com.example.moviechecklist.viewmodel.WatchedViewModel
import com.example.moviechecklist.viewmodel.WishlistViewModel
// Assuming MainActivity provides the ViewModelFactory instance or Repository
// For this example, let's assume factory is available via a composition local or passed down.
// This NavGraph is not currently used by MainActivity. MovieChecklistApp is used.

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Movies : Screen("movies", "Movies", Icons.Filled.Home)
    object Watched : Screen("watched", "Watched", Icons.Filled.List)
    object Wishlist : Screen("wishlist", "Wishlist", Icons.Filled.Star)
}

val bottomNavItems = listOf(
    Screen.Movies,
    Screen.Watched,
    Screen.Wishlist
)

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        bottomNavItems.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
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


@Composable
fun AppNavHost(
    navController: NavHostController,
    factory: ViewModelFactory, // Assume factory is provided
    modifier: Modifier = Modifier
) {
    NavHost(
        navController,
        startDestination = Screen.Movies.route,
        modifier = modifier
    ) {
        composable(Screen.Movies.route) {
            val movieViewModel: MovieViewModel = viewModel(factory = factory)
            MovieListScreen(viewModel = movieViewModel, onMovieClicked = { /* Handle movie click */})
        }
        composable(Screen.Watched.route) {
            val watchedViewModel: WatchedViewModel = viewModel(factory = factory)
            WatchedListScreen(viewModel = watchedViewModel)
        }
        composable(Screen.Wishlist.route) {
            val wishlistViewModel: WishlistViewModel = viewModel(factory = factory)
            WishlistScreen(viewModel = wishlistViewModel)
        }
        // Add other composable destinations here if needed
    }
}

@Composable
fun MainAppScaffoldWithBottomBar(factory: ViewModelFactory) { // Renamed from NavGraph to avoid confusion
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            factory = factory,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

// Note: This NavGraph structure (MainAppScaffoldWithBottomBar) provides an alternative UI structure
// with a bottom navigation bar. The current MainActivity uses MovieChecklistApp, which uses a top app bar
// and a different navigation setup. You would choose one or the other.
// To use this, MainActivity would call MainAppScaffoldWithBottomBar(factory).