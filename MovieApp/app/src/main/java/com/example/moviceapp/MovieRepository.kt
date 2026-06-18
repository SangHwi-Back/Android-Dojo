package com.example.moviceapp

import retrofit2.Call
import retrofit2.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepository @Inject constructor(private val service: MovieService) {
    suspend fun getMovies(): List<Movie> = when (val result = service.getMovies().toAPIResult()) {
        is APIResult.Success -> {
            result.data
        }
        is APIResult.Failure -> {
            listOf()
        }
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