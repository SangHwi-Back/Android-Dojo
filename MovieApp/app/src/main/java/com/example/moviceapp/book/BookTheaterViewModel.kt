package com.example.moviceapp.book

import androidx.lifecycle.ViewModel
import com.example.moviceapp.repo.APIResult
import com.example.moviceapp.repo.BookingRepository
import com.example.moviceapp.repo.Theater
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BookTheaterViewModel @Inject constructor(
    private val repository: BookingRepository
) : ViewModel() {
    suspend fun getTheaters(movieId: Int): List<Theater> =
        when (val result = repository.getTheaters(movieId)) {
            is APIResult.Success -> result.data
            is APIResult.Failure -> emptyList()
        }
}
