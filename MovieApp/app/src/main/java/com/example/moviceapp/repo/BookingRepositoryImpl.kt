package com.example.moviceapp.repo

import com.example.moviceapp.book.BookRequest
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

interface BookingRepository {
    suspend fun getBookingsFromDate(movieId: String, startDate: Date, endDate: Date): APIResult<List<Booking>>
    suspend fun getBookings(movieId: String): APIResult<List<Booking>>
    suspend fun get(request: BookRequest): APIResult<List<Booking>>
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

    override suspend fun get(request: BookRequest): APIResult<List<Booking>> =
        when (request) {
            is BookRequest.Id -> getBookings(request.movieId)
            is BookRequest.Period -> getBookingsFromDate(
                request.movieId,
                request.startDate,
                request.endDate,
            )
        }
}
