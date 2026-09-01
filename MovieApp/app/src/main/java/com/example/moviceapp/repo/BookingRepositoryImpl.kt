package com.example.moviceapp.repo

import com.example.moviceapp.book.BookChooseInfoModel
import javax.inject.Inject
import javax.inject.Singleton

interface BookingRepository {
    suspend fun getShowtimeDates(movieId: Int): APIResult<List<String>>
    suspend fun getTheaters(movieId: Int): APIResult<List<Theater>>
    suspend fun getShowtimeSlots(movieId: Int, theaterId: Int, date: String): APIResult<List<ShowtimeSlot>>
    suspend fun getSeatSlots(theaterId: Int, date: String, hall: String): APIResult<List<SeatSlot>>
    suspend fun postBooking(booking: BookChooseInfoModel): APIResult<Booking>
    suspend fun getBookings(): APIResult<List<Booking>>
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

    override suspend fun getSeatSlots(theaterId: Int, date: String, hall: String): APIResult<List<SeatSlot>> =
        service.getSeatSlots(theaterId, date, hall).toAPIResult()

    override suspend fun postBooking(booking: BookChooseInfoModel): APIResult<Booking> {
        val theaterId = booking.selectedTheater?.id ?: return APIResult.Failure(Error("Invalid theater id"))
        val selectedDate = booking.selectedShowtime?.selectedShowDate ?: return APIResult.Failure(Error("Invalid selected date"))
        val selectedTime = booking.selectedShowtime?.selectedShowtimeSlot?.time ?: return APIResult.Failure(Error("Invalid selected time"))
        val seatIds = booking.selectedSeat?.id ?: return APIResult.Failure(Error("Invalid seat id"))
        return service.bookMovie(CreateBookingRequest(
            movie = CreateBookingRequest.MovieRef(booking.selectedMovie.id),
            theater = CreateBookingRequest.TheaterRef(theaterId),
            date = selectedDate,
            time = selectedTime,
            seatIds = listOf(seatIds)
        )).toAPIResult()
    }
    override suspend fun getBookings(): APIResult<List<Booking>> =
        service.getBookings().toAPIResult()
}
