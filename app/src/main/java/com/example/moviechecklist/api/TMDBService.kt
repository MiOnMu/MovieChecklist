package com.example.moviechecklist.api

import com.example.moviechecklist.model.MovieResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TMDBService {
    @GET("discover/movie")
    suspend fun getMoviesByGenre(
        @Query("api_key") apiKey: String,
        @Query("with_genres") genreId: Int,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("page") page: Int = 1 // Added page for potentially more results
    ): MovieResponse
}