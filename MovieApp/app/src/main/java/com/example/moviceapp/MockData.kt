package com.example.moviceapp

import android.os.Parcelable
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
    val posterRes: Int? = null,    // R.drawable.xxx  (없으면 null → placeholder)
    val backdropRes: Int? = null,
) : Parcelable

@Parcelize
data class Theater(
    val id: Int,
    val name: String,
    val address: String,
    val distanceKm: Double,        // 1.2
) : Parcelable

data class Booking(
    val id: Int,
    val movie: Movie,
    val theater: Theater,
    val date: String,              // "May 15, 2026"
    val time: String,              // "6:45 PM"
    val seats: List<String>,       // ["D4", "D5"]
)

data class UserStats(
    val moviesCount: Int,
    val points: String,            // "1.2K"
    val saved: Int,                // 89  → "$89"
)

data class UserProfile(
    val name: String,
    val isGuest: Boolean,
    val stats: UserStats,
    val upcomingBookings: List<Booking>,
)

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

    val dunePartThree = Movie(
        id = 1,
        title = "Dune: Part Three",
        duration = "2h 45m",
        rating = 9.2,
        releaseDate = "2026-03-15",
        genres = listOf("Sci-Fi", "Adventure"),
        description = "The epic conclusion to the Dune saga follows Paul Atreides " +
                "as he unites with the Fremen people of the desert planet Arrakis.",
    )

    val oceansElevenLegacy = Movie(
        id = 2,
        title = "Ocean's Eleven: Legacy",
        duration = "2h 12m",
        rating = 8.1,
        releaseDate = "2026-02-20",
        genres = listOf("Thriller", "Comedy"),
        description = "A new crew of master thieves attempts the most daring " +
                "casino heist in Las Vegas history.",
    )

    val neonKnights = Movie(
        id = 3,
        title = "Neon Knights",
        duration = "1h 58m",
        rating = 7.6,
        releaseDate = "2026-04-10",
        genres = listOf("Action", "Sci-Fi"),
        description = "In a neon-lit dystopian city, a rogue detective uncovers " +
                "a conspiracy that threatens the last free zone on Earth.",
    )

    val starfallChronicles = Movie(
        id = 4,
        title = "Starfall Chronicles",
        duration = "2h 30m",
        rating = 8.5,
        releaseDate = "2026-05-01",
        genres = listOf("Adventure", "Drama"),
        description = "An astronaut stranded on a dying moon must choose between " +
                "survival and saving an alien civilization.",
    )

    val midnightSonata = Movie(
        id = 5,
        title = "Midnight Sonata",
        duration = "1h 45m",
        rating = 7.9,
        releaseDate = "2026-01-30",
        genres = listOf("Drama", "Romance"),
        description = "A blind pianist rediscovers passion when a mysterious " +
                "composer leaves unfinished scores on his doorstep.",
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
    )

    /** 현재 상영 중 (SearchFragment 수평 리스트) */
    val nowPlaying: List<Movie> = listOf(
        dunePartThree,
        oceansElevenLegacy,
        neonKnights,
        starfallChronicles,
    )

    /** 곧 개봉 */
    val comingSoon: List<Movie> = listOf(
        midnightSonata,
        ironVeilRising,
    )

    /** 상단 피처드 배너 (ViewPager2) */
    val featured: List<Movie> = listOf(
        dunePartThree,
        ironVeilRising,
        starfallChronicles,
    )

    /** 전체 목록 */
    val all: List<Movie> = nowPlaying + comingSoon
}

// ----------------------------------------------------------------

/**
 * 극장 목록 (BookMovieFragment — Select Theater 화면)
 *
 * 사용 예:
 *   val theaters = TheatersMock.list
 */
object TheatersMock {

    val imaxCineplexDowntown = Theater(
        id = 1,
        name = "IMAX Cineplex Downtown",
        address = "123 Main Street, Downtown",
        distanceKm = 1.2,
    )

    val galaxyMultiplex = Theater(
        id = 2,
        name = "Galaxy Multiplex",
        address = "456 Oak Avenue, Midtown",
        distanceKm = 2.8,
    )

    val starlightCinema = Theater(
        id = 3,
        name = "Starlight Cinema",
        address = "789 Park Boulevard, Uptown",
        distanceKm = 4.5,
    )

    val grandScreenArena = Theater(
        id = 4,
        name = "Grand Screen Arena",
        address = "321 River Road, Eastside",
        distanceKm = 6.1,
    )

    /** 거리순 정렬된 전체 극장 리스트 */
    val list: List<Theater> = listOf(
        imaxCineplexDowntown,
        galaxyMultiplex,
        starlightCinema,
        grandScreenArena,
    )
}

// ----------------------------------------------------------------

/**
 * 예매 내역 (MyInfoFragment — Upcoming Bookings)
 *
 * 사용 예:
 *   val bookings = BookingsMock.upcoming
 *   val past     = BookingsMock.past
 */
object BookingsMock {

    val booking1 = Booking(
        id = 1001,
        movie = MoviesMock.dunePartThree,
        theater = TheatersMock.imaxCineplexDowntown,
        date = "May 15, 2026",
        time = "6:45 PM",
        seats = listOf("D4", "D5"),
    )

    val booking2 = Booking(
        id = 1002,
        movie = MoviesMock.neonKnights,
        theater = TheatersMock.galaxyMultiplex,
        date = "May 20, 2026",
        time = "9:30 PM",
        seats = listOf("E2", "E3", "E4"),
    )

    val booking3 = Booking(
        id = 1003,
        movie = MoviesMock.oceansElevenLegacy,
        theater = TheatersMock.starlightCinema,
        date = "May 28, 2026",
        time = "7:00 PM",
        seats = listOf("B1", "B2"),
    )

    val booking4 = Booking(
        id = 1004,
        movie = MoviesMock.starfallChronicles,
        theater = TheatersMock.imaxCineplexDowntown,
        date = "April 5, 2026",
        time = "3:00 PM",
        seats = listOf("F5", "F6", "F7"),
    )

    /** 다가오는 예매 (MyInfoFragment 상단 카드) */
    val upcoming: List<Booking> = listOf(booking1, booking2, booking3)

    /** 지난 예매 (My Bookings 전체 목록) */
    val past: List<Booking> = listOf(booking4)

    /** 전체 */
    val all: List<Booking> = upcoming + past
}

// ----------------------------------------------------------------

/**
 * 유저 프로필 (MyInfoFragment 상단 프로필 카드 + 통계)
 *
 * 사용 예:
 *   val profile = UserMock.profile
 *   val stats   = UserMock.profile.stats
 */
object UserMock {

    val profile = UserProfile(
        name = "Guest User",
        isGuest = true,
        stats = UserStats(
            moviesCount = 12,
            points = "1.2K",
            saved = 89,
        ),
        upcomingBookings = BookingsMock.upcoming,
    )
}

// ----------------------------------------------------------------

/**
 * 영화 상세 Bottom Sheet (MovieDetailSheet)
 * — MoviesMock.all 에서 id로 찾거나 아래 헬퍼 사용
 *
 * 사용 예:
 *   val movie = MovieDetailMock.find(1)   // Dune: Part Three
 */
object MovieDetailMock {

    fun find(id: Int): Movie? = MoviesMock.all.find { it.id == id }

    /** Sheet 에 바로 넘길 수 있는 샘플 */
    val sample: Movie = MoviesMock.dunePartThree
}

// ----------------------------------------------------------------

/**
 * 예매 플로우 — 날짜 / 시간표 선택 (BookMovieFragment Step 2)
 *
 * 사용 예:
 *   val dates     = ShowtimeMock.dates
 *   val showtimes = ShowtimeMock.timesForDate("2026-05-15")
 */
object ShowtimeMock {

    data class ShowDate(val label: String, val isoDate: String)
    data class Showtime(val time: String, val hall: String, val availableSeats: Int)

    val dates: List<ShowDate> = listOf(
        ShowDate("Today",  "2026-05-14"),
        ShowDate("Thu 15", "2026-05-15"),
        ShowDate("Fri 16", "2026-05-16"),
        ShowDate("Sat 17", "2026-05-17"),
        ShowDate("Sun 18", "2026-05-18"),
        ShowDate("Mon 19", "2026-05-19"),
        ShowDate("Tue 20", "2026-05-20"),
    )

    private val showtimeMap: Map<String, List<Showtime>> = mapOf(
        "2026-05-14" to listOf(
            Showtime("12:00 PM", "IMAX Hall 1", 42),
            Showtime("3:30 PM",  "Hall 2",      28),
            Showtime("6:45 PM",  "IMAX Hall 1", 15),
            Showtime("9:30 PM",  "Hall 3",      60),
        ),
        "2026-05-15" to listOf(
            Showtime("11:00 AM", "Hall 2",      50),
            Showtime("2:15 PM",  "IMAX Hall 1", 33),
            Showtime("6:45 PM",  "IMAX Hall 1", 8),
            Showtime("9:00 PM",  "Hall 3",      45),
        ),
        "2026-05-16" to listOf(
            Showtime("1:00 PM",  "Hall 2",      55),
            Showtime("4:30 PM",  "IMAX Hall 1", 20),
            Showtime("7:45 PM",  "IMAX Hall 1", 12),
        ),
        "2026-05-17" to listOf(
            Showtime("10:30 AM", "Hall 3",      70),
            Showtime("1:45 PM",  "IMAX Hall 1", 40),
            Showtime("5:00 PM",  "Hall 2",      25),
            Showtime("8:15 PM",  "IMAX Hall 1", 18),
        ),
    )

    fun timesForDate(isoDate: String): List<Showtime> =
        showtimeMap[isoDate] ?: showtimeMap.values.first()
}
