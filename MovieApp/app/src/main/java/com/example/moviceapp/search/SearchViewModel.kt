package com.example.moviceapp.search

import androidx.lifecycle.ViewModel
import com.example.moviceapp.repo.Movie
import com.example.moviceapp.repo.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MovieRepository
): ViewModel() {
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
}