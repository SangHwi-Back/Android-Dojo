package com.example.moviceapp.repo

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BookingService {
    @GET("/api/showtimes/dates")
    fun getShowtimeDates(@Query("movieId") movieId: Int): Call<List<String>>

    // TODO: 서버가 하나의 Movie 에서 여러 개의 Theater 를 매핑하면 그에 따른 Repository, ViewModel 업데이트 진행
    @GET("/api/bookings/theater")
    fun getTheaters(@Query("movieId") movieId: Int): Call<Theater>
}
