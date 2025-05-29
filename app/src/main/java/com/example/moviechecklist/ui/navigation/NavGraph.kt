package com.example.moviechecklist.com.example.moviechecklist.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        NavHost(navController, startDestination = "movies", Modifier.padding(padding)) {
            composable("movies") { MovieListScreen() }
            composable("watched") { WatchedListScreen() }
            composable("wishlist") { WishlistScreen() }
        }
    }
}
