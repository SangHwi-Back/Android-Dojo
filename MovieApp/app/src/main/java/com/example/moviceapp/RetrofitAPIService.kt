package com.example.moviceapp

import retrofit2.Call
import retrofit2.http.GET

interface RetrofitAPIService {
    @GET("/api/movie")
    fun getMovies(): Call<Array<Movie>>
}