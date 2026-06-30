package com.example.moviceapp.book

import androidx.lifecycle.ViewModel
import com.example.moviceapp.repo.APIResult
import com.example.moviceapp.repo.Movie
import com.example.moviceapp.repo.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.jvm.Throws

@HiltViewModel
class BookViewModel @Inject constructor(
    val repository: MovieRepository
): ViewModel() {
    suspend fun getMovies(): List<Movie> {
        when (val result = repository.getMovies()) {
            is APIResult.Success -> return result.data
            is APIResult.Failure -> {
                print(result.error.toString())
                return emptyList()
            }
        }
    }
}