package com.example.moviceapp.repo

import retrofit2.Call
import retrofit2.await

sealed class APIResult<out T> {
    data class Success<T>(val data: T): APIResult<T>()
    data class Failure(val error: Throwable): APIResult<Nothing>()
}

suspend fun <T: Any> Call<T>.toAPIResult(): APIResult<T> {
    return try {
        APIResult.Success(await())
    } catch (e: Exception) {
        APIResult.Failure(Throwable(e.toString()))
    } catch (t: Throwable) {
        APIResult.Failure(t)
    }
}