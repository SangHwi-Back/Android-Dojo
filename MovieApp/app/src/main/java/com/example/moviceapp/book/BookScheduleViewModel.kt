package com.example.moviceapp.book

import androidx.lifecycle.ViewModel
import com.example.moviceapp.repo.APIResult
import com.example.moviceapp.repo.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BookScheduleViewModel @Inject constructor(
    private val repository: BookingRepository
) : ViewModel() {
    suspend fun getShowtimeDates(movieId: Int): List<String> =
        when (val result = repository.getShowtimeDates(movieId)) {
            is APIResult.Success -> result.data
            is APIResult.Failure -> emptyList()
        }
}
