package com.example.moviceapp.repo

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BookingService {
    @GET("/api/showtimes/dates")
    fun getShowtimeDates(@Query("movieId") movieId: Int): Call<List<String>>

    @GET("/api/showtimes/theaters")
    fun getTheaters(@Query("movieId") movieId: Int): Call<List<Theater>>

    @GET("/api/showtimes")
    fun getShowtimeSlots(
        @Query("movieId") movieId: Int,
        @Query("theaterId") theaterId: Int,
        @Query("date") date: String,
    ): Call<List<ShowtimeSlot>>
}
