package com.example.moviceapp.book

import androidx.lifecycle.ViewModel
import com.example.moviceapp.repo.Movie
import com.example.moviceapp.repo.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    val repository: MovieRepository
): ViewModel() {
    suspend fun getMovies(): List<Movie> = repository.getMovies()
}