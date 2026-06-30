package com.example.moviceapp.repo

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BookingService {
    @GET("/api/bookings/schedules/date")
    suspend fun getBookings(
        @Query("movie_id") movieId: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): Call<List<Booking>>

    @GET("/api/bookings/schedules")
    suspend fun getBookings(
        @Query("movie_id") movieId: String
    ): Call<List<Booking>>
}