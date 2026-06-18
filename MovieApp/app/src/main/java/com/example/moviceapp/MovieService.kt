package com.example.moviceapp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface MovieService {
    @GET("/api/movies")
    fun getMovies(): Call<List<Movie>>

    @GET("/api/{path}")
    fun <T> getAPI(@Path("path") path: String): Call<List<T>>
}