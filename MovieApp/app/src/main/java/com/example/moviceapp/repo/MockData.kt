package com.example.moviceapp.repo

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// ================================================================
//  데이터 모델
// ================================================================

@Parcelize
data class Movie(
    val id: Int,
    val title: String,
    val duration: String,          // "2h 12m"
    val rating: Double,            // 8.1
    val releaseDate: String,       // "2026-03-15"
    val genres: List<String>,
    val description: String,
    val posterUrl: String? = null,
    val backdropURL: String? = null,
) : Parcelable

@Parcelize
data class Theater(
    val id: Int,
    val name: String,
    val address: String,
    val distanceKm: Double,        // 1.2
) : Parcelable

@Parcelize
data class Ticket(
    val id: Int,
    val booking: Booking,
    @SerializedName("qr_code")
    val qrCode: String,
    @SerializedName("created_at")
    val createdAt: String,
) : Parcelable

@Parcelize
data class Booking(
    val id: Int,
    val movie: Movie,
    val theater: Theater,
    val date: String,
    val time: String,
    val seats: List<SeatSlot>,
) : Parcelable

// POST /api/bookings 요청 body
data class CreateBookingRequest(
    val movie: MovieRef,
    val theater: TheaterRef,
    val date: String,
    val time: String,
    val seatIds: List<Int>,
    val isUpcoming: Boolean = true,
) {
    data class MovieRef(val id: Int)
    data class TheaterRef(val id: Int)
}


// GET /api/showtimes 응답 매핑용 — 특정 movie+theater+date 의 상영 시간 슬롯
data class ShowtimeSlot(
    val id: Int,
    val showDate: String,
    val time: String,
    val hall: String,
    val availableSeats: Int,
)

@Parcelize
data class SeatSlot(
    val id: Int,
    val theater: Theater,
    val hall: String, // Showtime.hall 과 동일한 문자열로 매칭 (예: "IMAX Hall 1")
    val floor: Int,
    val rowLabel: String, // "A", "B", "C" ...
    val rowIndex: Int, // 0, 1, 2 ... 행 정렬/렌더링 순서
    val columnIndex: Int, // 1, 2, 3 ... 행 내 좌석 순서(가로 위치)
    val seatType: String,
    val hasAisleAfterColumn: Boolean, // 이 좌석 오른쪽에 통로
    val hasAisleAfterRow: Boolean, // 이 행 다음에 통로
    val status: String,
    val heldByUserId: String?,
    val heldUntil: Long?,
) : Parcelable {
    val name: String
        get() = "$rowLabel$columnIndex"
}

// ================================================================
//  Mock Data — 뷰 단위로 분리된 object
// ================================================================

/**
 * 영화 목록 (SearchFragment, 수평 ViewPager/RecyclerView 등)
 *
 * 사용 예:
 *   val movies = MoviesMock.nowPlaying
 *   val featured = MoviesMock.featured
 */
object MoviesMock {
    val midnightSonata = Movie(
        id = 5,
        title = "Midnight Sonata",
        duration = "1h 45m",
        rating = 7.9,
        releaseDate = "2026-01-30",
        genres = listOf("Drama", "Romance"),
        description = "A blind pianist rediscovers passion when a mysterious " +
                "composer leaves unfinished scores on his doorstep.",
        posterUrl = "https://picsum.photos/seed/midnight/300/450",
    )

    val ironVeilRising = Movie(
        id = 6,
        title = "Iron Veil: Rising",
        duration = "2h 20m",
        rating = 8.3,
        releaseDate = "2026-06-12",
        genres = listOf("Action", "Adventure"),
        description = "The world's last supersoldier comes out of hiding when a " +
                "shadow organization threatens global infrastructure.",
        posterUrl = "https://picsum.photos/seed/ironveil/300/450",
    )

    /** 곧 개봉 */
    val comingSoon: List<Movie> = listOf(
        midnightSonata,
        ironVeilRising,
    )

}

// ----------------------------------------------------------------

/**
 * 예매 플로우 — 날짜 / 시간표 선택 (BookMovieFragment Step 2)
 *
 * 사용 예:
 *   val dates     = ShowtimeMock.dates
 *   val showtimes = ShowtimeMock.timesForDate("2026-05-15")
 */
@Parcelize
object Showtime : Parcelable {
    @Parcelize
    data class ShowDate(val label: String, val isoDate: String) : Parcelable
    @Parcelize
    data class Showtime(val time: String, val hall: String, val availableSeats: Int) : Parcelable
}
