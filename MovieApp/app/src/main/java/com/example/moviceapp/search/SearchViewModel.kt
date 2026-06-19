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
    suspend fun getMovies(): List<Movie> = repository.getMovies()
}