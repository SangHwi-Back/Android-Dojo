package com.example.moviceapp.myinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviceapp.repo.APIResult
import com.example.moviceapp.repo.Booking
import com.example.moviceapp.repo.BookingRepository
import com.example.moviceapp.repo.Movie
import com.example.moviceapp.repo.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyInfoViewModel @Inject constructor(
    val movieRepository: MovieRepository,
    val bookingRepository: BookingRepository
) : ViewModel() {
    private var _myBookings = MutableStateFlow(listOf<Booking>())
    val myBookings = _myBookings.asStateFlow()

    fun fetchBookings() {
        viewModelScope.launch {
            val token = getAuthToken() ?: return@launch
            when (val result = bookingRepository.getBookings(token)) {
                is APIResult.Success<List<Booking>> ->
                    _myBookings.value = result.data
                is APIResult.Failure -> {
                    _myBookings.value = listOf()
                }
            }
        }
    }

    suspend fun fetchMovie(booking: Booking) : Movie? {
        return when (val result = movieRepository.getMovieDetail(booking.movie.id.toString())) {
            is APIResult.Success<Movie> ->
                result.data
            else ->
                null
        }
    }
}