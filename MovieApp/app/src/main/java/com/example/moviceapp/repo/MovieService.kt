package com.example.moviceapp.repo

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieService {
    @GET("/api/movies")
    fun getMovies(): Call<List<Movie>>

    @GET("/api/movies/featured")
    fun getFeaturedMovies(): Call<List<Movie>>

    @GET("/api/movies/{id}")
    fun getMovieDetail(@Path("id") id: String): Call<Movie>

    @GET("/api/movies")
    fun searchMovies(@Query("q") query: String): Call<List<Movie>>

    @GET("api/movies/random")
    fun randomMovies(): Call<List<Movie>>

    @GET("/api/movies/now-playing")
    fun getNowPlayingMovies(): Call<List<Movie>>
}