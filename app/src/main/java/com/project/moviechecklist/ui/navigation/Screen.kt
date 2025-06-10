package com.project.moviechecklist.ui.navigation

sealed class Screen(val route: String) {
    object Watched : Screen("watched_screen")
    object Planned : Screen("planned_screen")
    object Search : Screen("search_screen")
    object MovieDetail : Screen("movie_detail_screen/{movieId}/{mediaType}") { // Added mediaType
        fun createRoute(movieId: Int, mediaType: String) = "movie_detail_screen/$movieId/$mediaType"
    }
}