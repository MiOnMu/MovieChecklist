package com.project.moviechecklist.data.remote.api

import com.project.moviechecklist.BuildConfig
import com.project.moviechecklist.data.remote.dto.MovieDetailDto
import com.project.moviechecklist.data.remote.dto.SearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApiService {
    @GET("search/multi")
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
        @Query("append_to_response") appendToResponse: String? = null
    ): MovieDetailDto

    @GET("tv/{tv_id}")
    suspend fun getTvShowDetails(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String = BuildConfig.TMDB_API_KEY,
        @Query("append_to_response") appendToResponse: String? = null
    ): MovieDetailDto
}