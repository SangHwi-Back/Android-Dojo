package com.example.moviceapp.repo

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Path

interface TicketService {
    @POST("/api/tickets/{bookingId}")
    fun createTicketByBookingId(@Path("bookingId") bookingId: Int): Call<Ticket>
}