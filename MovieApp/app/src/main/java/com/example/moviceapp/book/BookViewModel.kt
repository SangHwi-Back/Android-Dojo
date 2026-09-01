package com.example.moviceapp.book

import androidx.lifecycle.ViewModel
import com.example.moviceapp.repo.APIResult
import com.example.moviceapp.repo.Booking
import com.example.moviceapp.repo.BookingRepository
import com.example.moviceapp.repo.Movie
import com.example.moviceapp.repo.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    val movieRepository: MovieRepository,
    val bookingRepository: BookingRepository,
): ViewModel() {
    private var _movies = MutableStateFlow<List<Movie>>(listOf())
    val movies: StateFlow<List<Movie>>
        get() = _movies.asStateFlow()
    private var _bookings = MutableStateFlow<List<Booking>>(listOf())
    val bookings: StateFlow<List<Booking>>
        get() = _bookings.asStateFlow()
    suspend fun fetchMovies() {
        when (val result = movieRepository.getMovies()) {
            is APIResult.Success -> {
                val movies = result.data
                _movies.value = movies
            }
            is APIResult.Failure -> {
                print(result.error.toString())
                _movies.value = listOf()
            }
        }
    }

    suspend fun fetchBookings() {
        when (val result = bookingRepository.getBookings()) {
            is APIResult.Success -> {
                val bookings = result.data
                _bookings.value = bookings
            }
            is APIResult.Failure -> {
                print(result.error.toString())
                _bookings.value = listOf()
            }
        }
    }
}