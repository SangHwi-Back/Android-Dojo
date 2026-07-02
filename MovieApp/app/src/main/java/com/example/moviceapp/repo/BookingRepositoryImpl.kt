package com.example.moviceapp.repo

import javax.inject.Inject
import javax.inject.Singleton

interface BookingRepository {
    suspend fun getShowtimeDates(movieId: Int): APIResult<List<String>>
    suspend fun getTheater(movieId: Int): APIResult<Theater>
}

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val service: BookingService
) : BookingRepository {
    override suspend fun getShowtimeDates(movieId: Int): APIResult<List<String>> =
        service.getShowtimeDates(movieId).toAPIResult()
    override suspend fun getTheater(movieId: Int): APIResult<Theater> =
        service.getTheaters(movieId).toAPIResult()
}
