package com.example.moviceapp.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviceapp.repo.APIResult
import com.example.moviceapp.repo.Movie
import com.example.moviceapp.repo.MovieRepository
import com.example.moviceapp.search.SearchFragmentEntity.CategorizedMovie
import com.example.moviceapp.search.SearchFragmentEntity.QueryResultMovie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MovieRepository
): ViewModel() {
    private var _currentScreen: MutableStateFlow<SearchFragmentEntity> = MutableStateFlow(QueryResultMovie(listOf()))
    val currentScreen = _currentScreen.asStateFlow()

    // Added properties
    private var _featuredMovies: List<Movie> = listOf()
    private var _browseAllMovies: List<Movie> = listOf()
    private var _searchedMovies: List<Movie> = listOf()

    suspend fun refreshCategorizedScreen() {
        _browseAllMovies = when (val result = repository.randomMovies()) {
            is APIResult.Success -> {
                result.data
            }
            is APIResult.Failure -> emptyList()
        }
        _featuredMovies = when (val result = repository.getFeaturedMovies()) {
            is APIResult.Success -> result.data
            is APIResult.Failure -> emptyList()
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            val results = when (val result = repository.searchMovies(query)) {
                is APIResult.Success -> result.data
                is APIResult.Failure -> emptyList()
            }
            _searchedMovies = results // Update searched movies property
            _currentScreen.value = QueryResultMovie(results)
        }
    }

    // Added methods to change _currentScreen based on properties
    fun showQueryResultScreen(query: String) {
        viewModelScope.launch {
            _currentScreen.value = QueryResultMovie(when (val result = repository.searchMovies(query)) {
                is APIResult.Success -> result.data
                is APIResult.Failure -> emptyList()
            })
        }
    }

    fun showCategorizedMovieScreen() {
        _currentScreen.value = CategorizedMovie(_featuredMovies, _browseAllMovies)
    }
}