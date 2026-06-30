package com.example.moviceapp.repo

import retrofit2.Call
import retrofit2.await
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

interface MovieRepository {
    suspend fun getMovies(): APIResult<List<Movie>>
    suspend fun getMovies(path: String): APIResult<List<Movie>>
    suspend fun getMovieDetail(id: String): APIResult<Movie>
}

@Singleton
class MovieRepositoryImpl @Inject constructor(
    private val service: MovieService
) : MovieRepository {
    override suspend fun getMovies(): APIResult<List<Movie>> =
        service.getMovies().toAPIResult()
    override suspend fun getMovies(path: String): APIResult<List<Movie>> =
        service.getMovies(path).toAPIResult()
    override suspend fun getMovieDetail(id: String): APIResult<Movie> =
        service.getMovieDetail(id).toAPIResult()
}
