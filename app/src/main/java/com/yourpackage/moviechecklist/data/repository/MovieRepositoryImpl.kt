package com.yourpackage.moviechecklist.data.repository

import com.yourpackage.moviechecklist.data.local.MovieDao
import com.yourpackage.moviechecklist.data.local.MovieEntity
import com.yourpackage.moviechecklist.data.local.MovieStatus
import com.yourpackage.moviechecklist.data.remote.api.MovieApiService
import com.yourpackage.moviechecklist.data.remote.dto.MovieDetailDto
import com.yourpackage.moviechecklist.data.remote.dto.MovieResultDto as ApiMovieResultDto // Alias to avoid name clash
import com.yourpackage.moviechecklist.data.remote.dto.SearchResponseDto
import com.yourpackage.moviechecklist.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepositoryImpl @Inject constructor(
    private val movieDao: MovieDao,
    private val movieApiService: MovieApiService
) : MovieRepository {

    // --- Local DB Operations ---
    override suspend fun addMovieToLibrary(movie: MovieEntity) {
        movieDao.insertMovie(movie)
    }

    override suspend fun updateMovieInLibrary(movie: MovieEntity) {
        movieDao.updateMovie(movie)
    }

    override suspend fun deleteMovieFromLibrary(movie: MovieEntity) {
        movieDao.deleteMovie(movie)
    }

    override fun getMovieFromLibrary(id: Int): Flow<MovieEntity?> = movieDao.getMovieById(id)

    override fun getWatchedMovies(): Flow<List<MovieEntity>> = movieDao.getMoviesByStatus(MovieStatus.WATCHED)

    override fun getPlannedMovies(): Flow<List<MovieEntity>> = movieDao.getMoviesByStatus(MovieStatus.PLANNED)

    override fun searchLocalMovies(query: String, statusFilter: MovieStatus?, typeFilter: String?): Flow<List<MovieEntity>> {
        // Implement more sophisticated local filtering logic here if needed
        return if (query.isBlank() && statusFilter == null && typeFilter == null) {
            movieDao.getAllMovies()
        } else if (statusFilter != null) {
            // This example is simplified. You'd need more complex queries or client-side filtering.
            movieDao.getMoviesByStatus(statusFilter) // Needs adjustment for query & type
        }
        else {
            movieDao.searchLocalMovies(query) // Basic title search
        }
    }


    // --- Remote API Operations ---
    override suspend fun searchRemoteMovies(query: String, page: Int): Flow<Resource<SearchResponseDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = movieApiService.searchMulti(query = query, page = page)
            // Filter out results that aren't movies or TV shows if 'multi' returns persons etc.
            val filteredResults = response.results.filter { it.mediaType == "movie" || it.mediaType == "tv" }
            emit(Resource.Success(response.copy(results = filteredResults)))
        } catch (e: HttpException) {
            emit(Resource.Error(message = "Oops, something went wrong! (HTTP)", data = null, code = e.code()))
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
            emit(Resource.Error(message = "Details not found. (HTTP)", data = null, code = e.code()))
        } catch (e: IOException) {
            emit(Resource.Error(message = "Couldn't reach server for details.", data = null))
        }
    }

    // --- Mappers ---
    override fun mapMovieDetailDtoToEntity(dto: MovieDetailDto, mediaTypeFromSearch: String): MovieEntity {
        return MovieEntity(
            id = dto.id,
            title = dto.title ?: dto.name ?: "Unknown Title",
            overview = dto.overview,
            posterPath = dto.posterPath,
            backdropPath = dto.backdropPath,
            releaseDate = dto.releaseDate ?: dto.firstAirDate,
            voteAverage = dto.voteAverage,
            genres = dto.genres.map { it.name },
            status = null, // Default to null, ViewModel will set it
            userRating = null,
            mediaType = mediaTypeFromSearch
        )
    }

    override fun mapMovieResultDtoToEntity(dto: ApiMovieResultDto, status: MovieStatus): MovieEntity {
        // mediaType might be null from some TMDB responses, ensure it's set.
        val effectiveMediaType = dto.mediaType ?: if (dto.title != null) "movie" else "tv"
        return MovieEntity(
            id = dto.id,
            title = dto.title ?: dto.name ?: "Unknown Title",
            overview = dto.overview,
            posterPath = dto.posterPath,
            backdropPath = dto.backdropPath,
            releaseDate = dto.releaseDate ?: dto.firstAirDate,
            voteAverage = dto.voteAverage,
            genres = emptyList(), // Genre IDs are provided, full names need another call or a predefined map
            status = status,
            userRating = null,
            mediaType = effectiveMediaType
        )
    }

    override fun getLibraryMoviesMap(): Flow<Map<Int, MovieStatus?>> {
        return movieDao.getAllMovies().map { movieList ->
            movieList.associateBy({ movie: MovieEntity -> movie.id }, { movie: MovieEntity -> movie.status })
        }
    }
}