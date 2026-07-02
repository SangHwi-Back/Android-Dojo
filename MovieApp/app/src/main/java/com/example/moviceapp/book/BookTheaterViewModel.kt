package com.example.moviceapp.book

import androidx.lifecycle.ViewModel
import com.example.moviceapp.repo.APIResult
import com.example.moviceapp.repo.BookingRepository
import com.example.moviceapp.repo.Theater
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BookTheaterViewModel @Inject constructor(
    val repository: BookingRepository
) : ViewModel() {
    suspend fun getTheater(movieId: Int): Theater? =
        when (val result = repository.getTheater(movieId)) {
            is APIResult.Success -> result.data
            is APIResult.Failure -> null
        }
}
