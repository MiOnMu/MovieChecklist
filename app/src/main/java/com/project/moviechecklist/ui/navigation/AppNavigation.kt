package com.project.moviechecklist.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.project.moviechecklist.ui.screens.detail.MovieDetailScreen
import com.project.moviechecklist.ui.screens.planned.PlannedScreen
import com.project.moviechecklist.ui.screens.search.SearchScreen
import com.project.moviechecklist.ui.screens.watched.WatchedScreen

@Composable
fun AppNavigation(navController: NavHostController, paddingValues: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = Screen.Watched.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Screen.Watched.route) {
            WatchedScreen(navController = navController)
        }
        composable(Screen.Planned.route) {
            PlannedScreen(navController = navController)
        }
        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }
        composable(
            route = Screen.MovieDetail.route,
            arguments = listOf(
                navArgument("movieId") { type = NavType.IntType },
                navArgument("mediaType") { type = NavType.StringType } // Added mediaType
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId") ?: 0
            val mediaType = backStackEntry.arguments?.getString("mediaType") ?: "movie" // default if somehow null
            MovieDetailScreen(
                navController = navController,
                movieId = movieId,
                mediaType = mediaType
            )
        }
    }
}