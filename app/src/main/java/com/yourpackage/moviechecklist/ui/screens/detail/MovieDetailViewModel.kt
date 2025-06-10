package com.yourpackage.moviechecklist.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourpackage.moviechecklist.data.local.MovieEntity
import com.yourpackage.moviechecklist.data.local.MovieStatus
import com.yourpackage.moviechecklist.data.remote.dto.MovieDetailDto
import com.yourpackage.moviechecklist.data.repository.MovieRepository
import com.yourpackage.moviechecklist.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val repository: MovieRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val movieId: Int = savedStateHandle.get<Int>("movieId")!!
    private val mediaType: String = savedStateHandle.get<String>("mediaType")!! // Get mediaType

    private val _movieDetails = MutableStateFlow<Resource<MovieEntity?>>(Resource.Loading())
    val movieDetails: StateFlow<Resource<MovieEntity?>> = _movieDetails.asStateFlow()

    init {
        loadMovieDetails()
    }

    private fun loadMovieDetails() {
        viewModelScope.launch {
            repository.getMovieFromLibrary(movieId).collectLatest { localMovie ->
                if (localMovie != null) {
                    // If the movie IS in our library, emit it directly. This is correct.
                    _movieDetails.value = Resource.Success(localMovie)

                    // Optional: You can still refresh details from the API if needed
                    // without losing the user's status and rating.
                    if (localMovie.genres.isEmpty()) {
                        fetchRemoteAndMerge(localMovie.status, localMovie.userRating)
                    }
                } else {
                    // If the movie IS NOT in our library, fetch details from the API
                    // but DO NOT assign a status.
                    fetchRemoteDetailsOnly()
                }
            }
        }
    }

    private fun fetchRemoteAndMerge(initialStatus: MovieStatus?, initialUserRating: Int?) {
        viewModelScope.launch {
            repository.getRemoteMovieDetails(movieId, mediaType).collect { remoteResource ->
                when (remoteResource) {
                    is Resource.Success -> {
                        remoteResource.data?.let { dto ->
                            val entity = repository.mapMovieDetailDtoToEntity(dto, mediaType)
                            val finalEntity = entity.copy(
                                status = initialStatus,
                                userRating = initialUserRating
                            )
                            _movieDetails.value = Resource.Success(finalEntity)
                        } ?: run {
                            _movieDetails.value = Resource.Error("Movie details not found (DTO null).", null)
                        }
                    }
                    is Resource.Error -> {
                        _movieDetails.value = Resource.Error(remoteResource.message ?: "Unknown error fetching details.", null)
                    }
                    is Resource.Loading -> {
                        _movieDetails.value = Resource.Loading()
                    }
                }
            }
        }
    }


    fun addCurrentMovieToPlanned() {
        val currentMovieResource = _movieDetails.value
        if (currentMovieResource is Resource.Success && currentMovieResource.data != null) {
            val movieToAdd = currentMovieResource.data.copy(status = MovieStatus.PLANNED, userRating = null)
            viewModelScope.launch {
                repository.addMovieToLibrary(movieToAdd)
                _movieDetails.value = Resource.Success(movieToAdd) // Update UI state
            }
        }
    }

    fun markAsWatched(rating: Int?) {
        val currentMovieResource = _movieDetails.value
        if (currentMovieResource is Resource.Success && currentMovieResource.data != null) {
            val movieToUpdate = currentMovieResource.data.copy(status = MovieStatus.WATCHED, userRating = rating)
            viewModelScope.launch {
                repository.addMovieToLibrary(movieToUpdate) // Add/Update
                _movieDetails.value = Resource.Success(movieToUpdate)
            }
        } else if (currentMovieResource is Resource.Error && currentMovieResource.data != null) {
            // This case might occur if we had an error but still have some partial data to save
            val movieToUpdate = currentMovieResource.data.copy(status = MovieStatus.WATCHED, userRating = rating)
            viewModelScope.launch {
                repository.addMovieToLibrary(movieToUpdate) // Add/Update
                _movieDetails.value = Resource.Success(movieToUpdate) // Update UI state to reflect saved data
            }
        }
    }

    fun markAsPlanned() {
        val currentMovieResource = _movieDetails.value
        if (currentMovieResource is Resource.Success && currentMovieResource.data != null) {
            val movieToUpdate = currentMovieResource.data.copy(status = MovieStatus.PLANNED, userRating = null)
            viewModelScope.launch {
                repository.addMovieToLibrary(movieToUpdate) // Add/Update
                _movieDetails.value = Resource.Success(movieToUpdate)
            }
        } else if (currentMovieResource is Resource.Error && currentMovieResource.data != null) {
            val movieToUpdate = currentMovieResource.data.copy(status = MovieStatus.PLANNED, userRating = null)
            viewModelScope.launch {
                repository.addMovieToLibrary(movieToUpdate) // Add/Update
                _movieDetails.value = Resource.Success(movieToUpdate)
            }
        }
    }

    fun updateRating(newRating: Int) {
        val currentMovieResource = _movieDetails.value
        if (currentMovieResource is Resource.Success && currentMovieResource.data != null && currentMovieResource.data.status == MovieStatus.WATCHED) {
            val movieToUpdate = currentMovieResource.data.copy(userRating = newRating)
            viewModelScope.launch {
                repository.updateMovieInLibrary(movieToUpdate)
                _movieDetails.value = Resource.Success(movieToUpdate)
            }
        }
    }

    private fun fetchRemoteDetailsOnly() {
        viewModelScope.launch {
            repository.getRemoteMovieDetails(movieId, mediaType).collect { remoteResource ->
                when (remoteResource) {
                    is Resource.Success -> {
                        remoteResource.data?.let { dto ->
                            // Map the DTO to an Entity, but DO NOT assign a status.
                            // We will handle the null status in the UI.
                            // The repository's map function has a default status, so we create the entity here.
                            val transientEntity = MovieEntity(
                                id = dto.id,
                                title = dto.title ?: dto.name ?: "Unknown Title",
                                overview = dto.overview,
                                posterPath = dto.posterPath,
                                backdropPath = dto.backdropPath,
                                releaseDate = dto.releaseDate ?: dto.firstAirDate,
                                voteAverage = dto.voteAverage,
                                genres = dto.genres.map { it.name },
                                // ** THE KEY CHANGE: Status is determined by what's in the DB, so this is transient **
                                // We will let the AddOrChangeStatusButtons handle a "null" movie status.
                                // For this to work, the MovieEntity's status property must be nullable. Let's adjust that.
                                status = null, // We'll make MovieEntity.status nullable
                                userRating = null,
                                mediaType = mediaType
                            )
                            _movieDetails.value = Resource.Success(transientEntity)
                        } ?: run {
                            _movieDetails.value = Resource.Error("Movie details not found.", null)
                        }
                    }
                    is Resource.Error -> {
                        _movieDetails.value = Resource.Error(remoteResource.message ?: "Unknown error.", null)
                    }
                    is Resource.Loading -> {
                        _movieDetails.value = Resource.Loading()
                    }
                }
            }
        }
    }
}