package com.project.moviechecklist.data.repository

import com.project.moviechecklist.data.local.MovieDao
import com.project.moviechecklist.data.local.MovieEntity
import com.project.moviechecklist.data.local.MovieStatus
import com.project.moviechecklist.data.remote.api.MovieApiService
import com.project.moviechecklist.data.remote.dto.MovieDetailDto
import com.project.moviechecklist.data.remote.dto.MovieResultDto as ApiMovieResultDto
import com.project.moviechecklist.data.remote.dto.SearchResponseDto
import com.project.moviechecklist.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepositoryImpl @Inject constructor(
    private val movieDao: MovieDao,
    private val movieApiService: MovieApiService,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : MovieRepository {

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    private fun getUserCollection() = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users").document(uid).collection("movies")
    }

    override suspend fun addMovieToLibrary(movie: MovieEntity) {
        val movieWithUser = movie.copy(userId = currentUserId)
        movieDao.insertMovie(movieWithUser)
        syncMovieToFirestore(movieWithUser)
    }

    override suspend fun updateMovieInLibrary(movie: MovieEntity) {
        val movieWithUser = movie.copy(userId = currentUserId)
        movieDao.updateMovie(movieWithUser)
        syncMovieToFirestore(movieWithUser)
    }

    override suspend fun deleteMovieFromLibrary(movie: MovieEntity) {
        movieDao.deleteMovie(movie.copy(userId = currentUserId))
        getUserCollection()?.document(movie.id.toString())?.delete()
    }

    private suspend fun syncMovieToFirestore(movie: MovieEntity) {
        getUserCollection()?.document(movie.id.toString())?.set(movie)
    }

    override suspend fun syncWithFirestore() {
        val collection = getUserCollection() ?: return
        try {
            val snapshot = collection.get().await()
            val remoteMovies = snapshot.toObjects(MovieEntity::class.java)
            
            remoteMovies.forEach { movie ->
                movieDao.insertMovie(movie)
            }
            
            val localMovies = movieDao.getAllMovies(currentUserId).first()
            localMovies.forEach { movie ->
                syncMovieToFirestore(movie)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getMovieFromLibrary(id: Int): Flow<MovieEntity?> = movieDao.getMovieById(id, currentUserId)

    override fun getWatchedMovies(): Flow<List<MovieEntity>> = movieDao.getMoviesByStatus(MovieStatus.WATCHED, currentUserId)

    override fun getPlannedMovies(): Flow<List<MovieEntity>> = movieDao.getMoviesByStatus(MovieStatus.PLANNED, currentUserId)

    override fun searchLocalMovies(query: String, statusFilter: MovieStatus?, typeFilter: String?): Flow<List<MovieEntity>> {
        return if (query.isBlank() && statusFilter == null && typeFilter == null) {
            movieDao.getAllMovies(currentUserId)
        } else if (statusFilter != null) {
            movieDao.getMoviesByStatus(statusFilter, currentUserId)
        }
        else {
            movieDao.searchLocalMovies(query, currentUserId)
        }
    }

    override suspend fun searchRemoteMovies(query: String, page: Int): Flow<Resource<SearchResponseDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = movieApiService.searchMulti(query = query, page = page)
            val filteredResults = response.results.filter { it.mediaType == "movie" || it.mediaType == "tv" }
            emit(Resource.Success(response.copy(results = filteredResults)))
        } catch (e: HttpException) {
            emit(Resource.Error(message = "Oops, something went wrong!", data = null, code = e.code()))
        } catch (e: IOException) {
            emit(Resource.Error(message = "Couldn't reach server, check your internet connection.", data = null))
        }
    }

    override suspend fun getRemoteMovieDetails(movieId: Int, mediaType: String): Flow<Resource<MovieDetailDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = if (mediaType.equals("movie", ignoreCase = true)) {
                movieApiService.getMovieDetails(movieId)
            } else {
                movieApiService.getTvShowDetails(movieId)
            }
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            emit(Resource.Error(message = "Details not found.", data = null, code = e.code()))
        } catch (e: IOException) {
            emit(Resource.Error(message = "Couldn't reach server for details.", data = null))
        }
    }

    override fun mapMovieDetailDtoToEntity(dto: MovieDetailDto, mediaTypeFromSearch: String): MovieEntity {
        return MovieEntity(
            id = dto.id,
            userId = currentUserId,
            title = dto.title ?: dto.name ?: "Unknown Title",
            overview = dto.overview,
            posterPath = dto.posterPath,
            backdropPath = dto.backdropPath,
            releaseDate = dto.releaseDate ?: dto.firstAirDate,
            voteAverage = dto.voteAverage,
            genres = dto.genres.map { it.name },
            status = null,
            userRating = null,
            mediaType = mediaTypeFromSearch
        )
    }

    override fun mapMovieResultDtoToEntity(dto: ApiMovieResultDto, status: MovieStatus): MovieEntity {
        val effectiveMediaType = dto.mediaType ?: if (dto.title != null) "movie" else "tv"
        return MovieEntity(
            id = dto.id,
            userId = currentUserId,
            title = dto.title ?: dto.name ?: "Unknown Title",
            overview = dto.overview,
            posterPath = dto.posterPath,
            backdropPath = dto.backdropPath,
            releaseDate = dto.releaseDate ?: dto.firstAirDate,
            voteAverage = dto.voteAverage,
            genres = emptyList(),
            status = status,
            userRating = null,
            mediaType = effectiveMediaType
        )
    }

    override fun getLibraryMoviesMap(): Flow<Map<Int, MovieStatus?>> {
        return movieDao.getAllMovies(currentUserId).map { movieList ->
            movieList.associateBy({ movie: MovieEntity -> movie.id }, { movie: MovieEntity -> movie.status })
        }
    }
}
