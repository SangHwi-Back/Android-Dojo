package com.example.moviceapp.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviceapp.repo.APIResult
import com.example.moviceapp.repo.Movie
import com.example.moviceapp.repo.MovieRepository
import com.example.moviceapp.search.SearchFragmentEntity.*
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
        _browseAllMovies = when (val result = getMovies()) {
            is APIResult.Success -> result.data
            is APIResult.Failure -> emptyList()
        }
        _featuredMovies = when (val result = getFeaturedMovies()) {
            is APIResult.Success -> result.data
            is APIResult.Failure -> emptyList()
        }
    }

    suspend fun toggleScreen(query: String = "") {
        _currentScreen.value = when (_currentScreen.value) {
            is QueryResultMovie -> CategorizedMovie(
                _featuredMovies,
                _browseAllMovies
            )
            is CategorizedMovie -> QueryResultMovie(
                when (val result = searchMovies(query)) {
                    is APIResult.Success -> result.data
                    is APIResult.Failure -> emptyList()
                }
            )
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            val results = when (val result = searchMovies(query)) {
                is APIResult.Success -> result.data
                is APIResult.Failure -> emptyList()
            }
            _searchedMovies = results // Update searched movies property
            _currentScreen.value = QueryResultMovie(results)
        }
    }

    /**
     * 전체 영화 목록
     * - GET
     * - /api/movies
     */
    suspend fun getMovies() = repository.getMovies()
    /**
     * 현재 상영 중
     * - GET
     * - /api/movies/now-playing
     */
    suspend fun getNowPlayingMovies() = repository.getMovies("now-playing")
    /**
     * 곧 개봉
     * - GET
     * - /api/movies/coming-soon
     */
    suspend fun getComingSoonMovies() = repository.getMovies("coming-soon")
    /**
     * 피처드 배너용
     * - GET
     * - /api/movies/featured
     */
    suspend fun getFeaturedMovies() = repository.getMovies("featured")
    /**
     * 영화 상세
     * - GET
     * - /api/movies/:id
     */
    suspend fun getMovieDetail(id: String) = repository.getMovieDetail(id)

    suspend fun searchMovies(query: String) = repository.searchMovies(query)

    // Added methods to change _currentScreen based on properties
    fun showQueryResultScreen(query: String) {
        viewModelScope.launch {
            _currentScreen.value = QueryResultMovie(when (val result = searchMovies(query)) {
                is APIResult.Success -> result.data
                is APIResult.Failure -> emptyList()
            })
        }
    }

    fun showCategorizedMovieScreen() {
        _currentScreen.value = CategorizedMovie(_featuredMovies, _browseAllMovies)
    }
}