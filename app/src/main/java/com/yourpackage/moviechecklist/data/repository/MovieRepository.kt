package com.yourpackage.moviechecklist.data.repository

import com.yourpackage.moviechecklist.data.local.MovieEntity
import com.yourpackage.moviechecklist.data.local.MovieStatus
import com.yourpackage.moviechecklist.data.remote.dto.MovieDetailDto
import com.yourpackage.moviechecklist.data.remote.dto.SearchResponseDto
import com.yourpackage.moviechecklist.util.Resource
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    // Local DB Operations
    suspend fun addMovieToLibrary(movie: MovieEntity)
    suspend fun updateMovieInLibrary(movie: MovieEntity)
    suspend fun deleteMovieFromLibrary(movie: MovieEntity)
    fun getMovieFromLibrary(id: Int): Flow<MovieEntity?>
    fun getWatchedMovies(): Flow<List<MovieEntity>>
    fun getPlannedMovies(): Flow<List<MovieEntity>>
    fun searchLocalMovies(query: String, statusFilter: MovieStatus? = null, typeFilter: String? = null): Flow<List<MovieEntity>>
    fun getLibraryMoviesMap(): Flow<Map<Int, MovieStatus?>>


    // Remote API Operations
    suspend fun searchRemoteMovies(query: String, page: Int): Flow<Resource<SearchResponseDto>>
    suspend fun getRemoteMovieDetails(movieId: Int, mediaType: String): Flow<Resource<MovieDetailDto>>

    // Helper to map DTO to Entity (consider moving to a mapper class)
    fun mapMovieDetailDtoToEntity(dto: MovieDetailDto, mediaTypeFromSearch: String): MovieEntity
    fun mapMovieResultDtoToEntity(dto: com.yourpackage.moviechecklist.data.remote.dto.MovieResultDto, status: MovieStatus = MovieStatus.PLANNED): MovieEntity
}