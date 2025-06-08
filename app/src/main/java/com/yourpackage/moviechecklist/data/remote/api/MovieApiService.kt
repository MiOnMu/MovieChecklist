package com.yourpackage.moviechecklist.data.remote.api

import com.yourpackage.moviechecklist.BuildConfig
import com.yourpackage.moviechecklist.data.remote.dto.MovieDetailDto
import com.yourpackage.moviechecklist.data.remote.dto.SearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApiService {
    // Search for movies and TV shows
    @GET("search/multi") // 'multi' searches movies, tv, people. Filter client-side or use specific endpoints
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("api_key") apiKey: String = BuildConfig.TMDB_API_KEY,
        @Query("include_adult") includeAdult: Boolean = false
    ): SearchResponseDto

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String = BuildConfig.TMDB_API_KEY,
        @Query("append_to_response") appendToResponse: String? = null // e.g. "videos,credits"
    ): MovieDetailDto

    @GET("tv/{tv_id}")
    suspend fun getTvShowDetails(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String = BuildConfig.TMDB_API_KEY,
        @Query("append_to_response") appendToResponse: String? = null
    ): MovieDetailDto // Reusing MovieDetailDto, adapt if TV details differ significantly for your needs
}