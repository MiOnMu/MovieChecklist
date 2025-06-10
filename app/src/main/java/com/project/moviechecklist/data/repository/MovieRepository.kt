package com.project.moviechecklist.data.repository

import com.project.moviechecklist.data.local.MovieEntity
import com.project.moviechecklist.data.local.MovieStatus
import com.project.moviechecklist.data.remote.dto.MovieDetailDto
import com.project.moviechecklist.data.remote.dto.SearchResponseDto
import com.project.moviechecklist.util.Resource
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    suspend fun addMovieToLibrary(movie: MovieEntity)
    suspend fun updateMovieInLibrary(movie: MovieEntity)
    suspend fun deleteMovieFromLibrary(movie: MovieEntity)
    fun getMovieFromLibrary(id: Int): Flow<MovieEntity?>
    fun getWatchedMovies(): Flow<List<MovieEntity>>
    fun getPlannedMovies(): Flow<List<MovieEntity>>
    fun searchLocalMovies(query: String, statusFilter: MovieStatus? = null, typeFilter: String? = null): Flow<List<MovieEntity>>
    fun getLibraryMoviesMap(): Flow<Map<Int, MovieStatus?>>

    suspend fun searchRemoteMovies(query: String, page: Int): Flow<Resource<SearchResponseDto>>
    suspend fun getRemoteMovieDetails(movieId: Int, mediaType: String): Flow<Resource<MovieDetailDto>>

    fun mapMovieDetailDtoToEntity(dto: MovieDetailDto, mediaTypeFromSearch: String): MovieEntity
    fun mapMovieResultDtoToEntity(dto: com.project.moviechecklist.data.remote.dto.MovieResultDto, status: MovieStatus = MovieStatus.PLANNED): MovieEntity
}