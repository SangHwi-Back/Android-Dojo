package com.example.moviceapp.repo

import javax.inject.Inject
import javax.inject.Singleton

interface BookingRepository {
    suspend fun getShowtimeDates(movieId: Int): APIResult<List<String>>
    suspend fun getTheaters(movieId: Int): APIResult<List<Theater>>
    suspend fun getShowtimeSlots(movieId: Int, theaterId: Int, date: String): APIResult<List<ShowtimeSlot>>
}

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val service: BookingService
) : BookingRepository {
    override suspend fun getShowtimeDates(movieId: Int): APIResult<List<String>> =
        service.getShowtimeDates(movieId).toAPIResult()

    override suspend fun getTheaters(movieId: Int): APIResult<List<Theater>> =
        service.getTheaters(movieId).toAPIResult()

    override suspend fun getShowtimeSlots(movieId: Int, theaterId: Int, date: String): APIResult<List<ShowtimeSlot>> =
        service.getShowtimeSlots(movieId, theaterId, date).toAPIResult()
}
