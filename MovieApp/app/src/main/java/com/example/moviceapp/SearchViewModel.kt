package com.example.moviceapp

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.await
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val service: MovieService
): ViewModel() {
    suspend fun getMovies(): List<Movie> = service.getMovies().await()
}