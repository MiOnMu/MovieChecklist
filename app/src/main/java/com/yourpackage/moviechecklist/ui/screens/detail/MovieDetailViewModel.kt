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
            // 1. Try to get from local DB first
            repository.getMovieFromLibrary(movieId).collectLatest { localMovie ->
                if (localMovie != null) {
                    _movieDetails.value = Resource.Success(localMovie)
                    // Optionally, refresh from remote if data is stale or incomplete
                    if (localMovie.genres.isEmpty()) { // Example: if genres are missing, fetch full remote
                        fetchRemoteAndMerge(localMovie.status, localMovie.userRating)
                    }
                } else {
                    // 2. If not in local DB, fetch from remote and prepare to add
                    fetchRemoteAndMerge(MovieStatus.PLANNED, null) // Default to PLANNED if fetched fresh
                }
            }
        }
    }

    private fun fetchRemoteAndMerge(initialStatus: MovieStatus, initialUserRating: Int?) {
        viewModelScope.launch {
            repository.getRemoteMovieDetails(movieId, mediaType).collect { remoteResource ->
                when (remoteResource) {
                    is Resource.Success -> {
                        remoteResource.data?.let { dto ->
                            val entity = repository.mapMovieDetailDtoToEntity(dto, mediaType, initialStatus)
                            val finalEntity = entity.copy(userRating = initialUserRating) // Preserve rating if existed
                            // Don't automatically add to library here, let user action do that from UI
                            // unless it's being viewed for the first time after adding from search.
                            // The logic here is to *display* details. Adding happens via specific actions.
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
}