package com.yourpackage.moviechecklist.ui.screens.search

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourpackage.moviechecklist.data.local.MovieEntity
import com.yourpackage.moviechecklist.data.local.MovieStatus
import com.yourpackage.moviechecklist.data.remote.dto.MovieResultDto
import com.yourpackage.moviechecklist.data.remote.dto.SearchResponseDto
import com.yourpackage.moviechecklist.data.repository.MovieRepository
import com.yourpackage.moviechecklist.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _searchQuery = mutableStateOf("")
    val searchQuery = _searchQuery

    // Remote search results
    private val _searchResults = MutableStateFlow<Resource<SearchResponseDto>>(
        Resource.Success(
            SearchResponseDto( // Provide a default empty SearchResponseDto
                page = 0,
                results = emptyList(),
                totalPages = 0,
                totalResults = 0
            )
        )
    )
    val searchResults: StateFlow<Resource<SearchResponseDto>> = _searchResults.asStateFlow()

    // Filter states - add more as needed
    val mediaTypeFilter = mutableStateOf<String?>(null) // "movie", "tv", or null for all


    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel() // Cancel previous job
        if (query.length > 2) { // Start search after 3 characters
            searchJob = viewModelScope.launch {
                delay(500L) // Debounce
                performRemoteSearch(query)
                // performLocalSearch(query) // If you want local search too
            }
        } else {
            _searchResults.value = Resource.Success(
                SearchResponseDto( // Clear to a default empty SearchResponseDto
                    page = 0,
                    results = emptyList(),
                    totalPages = 0,
                    totalResults = 0
                )
            )
        }
    }

    private fun performRemoteSearch(query: String) {
        viewModelScope.launch {
            repository.searchRemoteMovies(query, 1).collect { result ->
                // Apply client-side filtering for mediaType if needed, or modify API call
                if (result is Resource.Success && result.data != null) {
                    val filteredResults = result.data.results.filter {
                        mediaTypeFilter.value == null || it.mediaType.equals(mediaTypeFilter.value, ignoreCase = true)
                    }
                    _searchResults.value = Resource.Success(result.data.copy(results = filteredResults))
                } else if (result is Resource.Error) { // Handle other states explicitly
                    _searchResults.value = Resource.Error(result.message ?: "Unknown error", code = result.code)
                } else if (result is Resource.Loading) {
                    _searchResults.value = Resource.Loading()
                }
            }
        }
    }


    fun addMovieToPlanned(apiMovie: MovieResultDto) {
        viewModelScope.launch {
            // Fetch full details if needed to get genre names, etc., or use basic info
            // For simplicity, using basic info from search result directly
            val movieEntity = repository.mapMovieResultDtoToEntity(apiMovie, MovieStatus.PLANNED)
            repository.addMovieToLibrary(movieEntity)
            // Optionally, fetch full details in background and update
            // This provides immediate feedback
            fetchAndStoreFullDetails(movieEntity.id, movieEntity.mediaType)
        }
    }

    private fun fetchAndStoreFullDetails(movieId: Int, mediaType: String) {
        viewModelScope.launch {
            repository.getRemoteMovieDetails(movieId, mediaType).collect { resource ->
                if (resource is Resource.Success && resource.data != null) {
                    val mappedEntity = repository.mapMovieDetailDtoToEntity(resource.data, mediaType)
                    val detailedEntity = mappedEntity.copy(status = MovieStatus.PLANNED)
                    val existing = repository.getMovieFromLibrary(detailedEntity.id).firstOrNull()
                    val entityToSave = if (existing != null) {
                        detailedEntity.copy(userRating = existing.userRating, status = existing.status) // Preserve user data
                    } else {
                        detailedEntity
                    }
                    repository.addMovieToLibrary(entityToSave) // This will replace due to OnConflictStrategy.REPLACE
                }
            }
        }
    }

    fun setMediaTypeFilter(type: String?) {
        mediaTypeFilter.value = type
        // Re-trigger search if query is not empty
        if (_searchQuery.value.length > 2) {
            performRemoteSearch(_searchQuery.value)
            // performLocalSearch(_searchQuery.value)
        }
    }

    val libraryMoviesMap: StateFlow<Map<Int, MovieStatus?>> = repository.getLibraryMoviesMap()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )
}