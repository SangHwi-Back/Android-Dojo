package com.example.moviceapp.myinfo

import androidx.lifecycle.ViewModel
import com.example.moviceapp.repo.Booking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MyInfoViewModel : ViewModel() {
    private var _myBookings = MutableStateFlow(listOf<Booking>())
    val myBookings = _myBookings.asStateFlow()
}