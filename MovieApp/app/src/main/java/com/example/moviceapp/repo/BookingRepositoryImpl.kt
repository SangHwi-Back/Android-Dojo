package com.example.moviceapp.repo

import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

interface BookingRepository {
    suspend fun getBookingsFromDate(movieId: String, startDate: Date, endDate: Date): APIResult<List<Booking>>
    suspend fun getBookings(movieId: String): APIResult<List<Booking>>
}

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val service: BookingService
) : BookingRepository {
    override suspend fun getBookingsFromDate(
        movieId: String,
        startDate: Date,
        endDate: Date
    ): APIResult<List<Booking>> =
        service.getBookings(movieId, startDate.toString(), endDate.toString()).toAPIResult()

    override suspend fun getBookings(movieId: String): APIResult<List<Booking>> =
        service.getBookings(movieId).toAPIResult()
}
