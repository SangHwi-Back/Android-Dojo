package com.example.moviceapp.repo

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieService {
    @GET("/api/movies")
    fun getMovies(): Call<List<Movie>>

    @GET("/api/movies/{path}")
    fun getMovies(@Path("path") path: String): Call<List<Movie>>

    @GET("/api/movies/{id}")
    fun getMovieDetail(@Path("id") id: String): Call<Movie>

    @GET("/api/{path}")
    fun <T> getAPI(@Path("path") path: String): Call<List<T>>

    @GET("/api/movies")
    fun searchMovies(@Query("q") query: String): Call<List<Movie>>
}