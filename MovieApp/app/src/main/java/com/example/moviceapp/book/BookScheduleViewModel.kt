package com.example.moviceapp.book

import com.example.moviceapp.repo.APIResult
import com.example.moviceapp.repo.Booking
import com.example.moviceapp.repo.BookingRepository
import com.example.moviceapp.repo.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class BookScheduleViewModel @Inject constructor(
    private val repository: BookingRepository,
    private val movie: Movie
) {
    private val movieId: String
        get() = movie.id.toString()
    suspend fun getAllBookings(): List<Booking> {
        when (val result = repository.getBookings(movieId)) {
            is APIResult.Success -> return result.data
            is APIResult.Failure -> {
                print(result.error.toString())
                return emptyList()
            }
        }
    }

    suspend fun getBookingByDate(startDate: Date, endDate: Date): List<Booking> {
        when (val result = repository.getBookingsFromDate(movieId, startDate, endDate)) {
            is APIResult.Success -> return result.data
            is APIResult.Failure -> {
                print(result.error.toString())
                return emptyList()
            }
        }
    }
}