package com.project.moviechecklist.ui.screens.search

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.moviechecklist.data.local.MovieStatus
import com.project.moviechecklist.data.remote.dto.MovieResultDto
import com.project.moviechecklist.data.remote.dto.SearchResponseDto
import com.project.moviechecklist.data.repository.MovieRepository
import com.project.moviechecklist.util.Resource
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

    private val _searchResults = MutableStateFlow<Resource<SearchResponseDto>>(
        Resource.Success(
            SearchResponseDto(
                page = 0,
                results = emptyList(),
                totalPages = 0,
                totalResults = 0
            )
        )
    )
    val searchResults: StateFlow<Resource<SearchResponseDto>> = _searchResults.asStateFlow()

    val mediaTypeFilter = mutableStateOf<String?>(null)


    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.length > 2) {
            searchJob = viewModelScope.launch {
                delay(500L)
                performRemoteSearch(query)
            }
        } else {
            _searchResults.value = Resource.Success(
                SearchResponseDto(
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
                if (result is Resource.Success && result.data != null) {
                    val filteredResults = result.data.results.filter {
                        mediaTypeFilter.value == null || it.mediaType.equals(mediaTypeFilter.value, ignoreCase = true)
                    }
                    _searchResults.value = Resource.Success(result.data.copy(results = filteredResults))
                } else if (result is Resource.Error) {
                    _searchResults.value = Resource.Error(result.message ?: "Unknown error", code = result.code)
                } else if (result is Resource.Loading) {
                    _searchResults.value = Resource.Loading()
                }
            }
        }
    }


    fun addMovieToPlanned(apiMovie: MovieResultDto) {
        viewModelScope.launch {
            val movieEntity = repository.mapMovieResultDtoToEntity(apiMovie, MovieStatus.PLANNED)
            repository.addMovieToLibrary(movieEntity)
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
                        detailedEntity.copy(userRating = existing.userRating, status = existing.status)
                    } else {
                        detailedEntity
                    }
                    repository.addMovieToLibrary(entityToSave)
                }
            }
        }
    }

    fun setMediaTypeFilter(type: String?) {
        mediaTypeFilter.value = type
        if (_searchQuery.value.length > 2) {
            performRemoteSearch(_searchQuery.value)
        }
    }

    val libraryMoviesMap: StateFlow<Map<Int, MovieStatus?>> = repository.getLibraryMoviesMap()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )
}