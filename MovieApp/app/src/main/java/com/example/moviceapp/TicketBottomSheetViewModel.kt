package com.example.moviceapp

import androidx.lifecycle.ViewModel
import com.example.moviceapp.repo.APIResult
import com.example.moviceapp.repo.Ticket
import com.example.moviceapp.repo.TicketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TicketBottomSheetViewModel @Inject constructor(
    var repository: TicketRepository
) : ViewModel() {
    private var _ticket: MutableStateFlow<Ticket?> = MutableStateFlow(null)
    val ticket = _ticket.asStateFlow()
    suspend fun getTicketByBookingId(bookingId: Int) {
        _ticket.value = when (val result = repository.createTicketByBookingId(bookingId)) {
            is APIResult.Success -> {
                result.data }
            is APIResult.Failure -> {
                null }
        }
    }
}