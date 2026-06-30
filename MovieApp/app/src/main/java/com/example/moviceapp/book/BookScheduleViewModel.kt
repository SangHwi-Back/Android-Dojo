package com.example.moviceapp.book

import androidx.lifecycle.ViewModel
import com.example.moviceapp.repo.APIResult
import com.example.moviceapp.repo.Booking
import com.example.moviceapp.repo.BookingRepository
import com.example.moviceapp.repo.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BookScheduleViewModel @Inject constructor(
    private val repository: BookingRepository
) : ViewModel() {
    suspend fun getBooking(request: BookRequest): List<Booking> =
        when (val result = repository.get(request)) {
            is APIResult.Success -> result.data
            is APIResult.Failure -> emptyList()
        }
}

sealed class BookRequest {
    class Id(val movieId: String): BookRequest()
    class Period(val movieId: String, val startDate: Date, val endDate: Date): BookRequest()
}