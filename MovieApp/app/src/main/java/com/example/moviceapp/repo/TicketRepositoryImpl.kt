package com.example.moviceapp.repo

import javax.inject.Inject
import javax.inject.Singleton

interface TicketRepository {
    suspend fun createTicketByBookingId(bookingId: Int): APIResult<Ticket>
}
@Singleton
class TicketRepositoryImpl @Inject constructor(
    private val service: TicketService
) : TicketRepository {
    override suspend fun createTicketByBookingId(bookingId: Int): APIResult<Ticket> =
        service.createTicketByBookingId(bookingId).toAPIResult()
}