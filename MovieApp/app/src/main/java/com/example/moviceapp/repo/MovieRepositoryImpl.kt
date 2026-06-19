package com.example.moviceapp.repo

import retrofit2.Call
import retrofit2.await
import javax.inject.Inject
import javax.inject.Singleton

interface MovieRepository {
    suspend fun getMovies(): List<Movie>
    suspend fun getMovies(path: String): List<Movie>
    suspend fun getMovieDetail(id: String): Movie
}

@Singleton
class MovieRepositoryImpl @Inject constructor(
    private val service: MovieService
) : MovieRepository {
    override suspend fun getMovies(): List<Movie> = when (val result = service.getMovies().toAPIResult()) {
        is APIResult.Success -> result.data
        is APIResult.Failure -> throw result.error
    }
    override suspend fun getMovies(path: String): List<Movie> = when (val result = service.getMovies(path).toAPIResult()) {
        is APIResult.Success -> result.data
        is APIResult.Failure -> throw result.error
    }
    override suspend fun getMovieDetail(id: String): Movie = when (val result = service.getMovieDetail(id).toAPIResult()) {
        is APIResult.Success -> result.data
        is APIResult.Failure -> throw result.error
    }
}

sealed class APIResult<out T> {
    data class Success<T>(val data: T): APIResult<T>()
    data class Failure(val error: Throwable): APIResult<Nothing>()
}

suspend fun <T: Any> Call<T>.toAPIResult(): APIResult<T> {
    return try {
        APIResult.Success(await())
    } catch (t: Throwable) {
        APIResult.Failure(t)
    }
}