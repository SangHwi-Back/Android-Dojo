package com.example.moviceapp.book

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moviceapp.book.BookInfo.SEAT
import com.example.moviceapp.book.BookInfo.SHOWTIME
import com.example.moviceapp.book.BookInfo.THEATER
import com.example.moviceapp.repo.APIResult
import com.example.moviceapp.repo.BookingRepository
import com.example.moviceapp.repo.Movie
import com.example.moviceapp.repo.ShowtimeSlot
import com.example.moviceapp.repo.Theater
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookChooseInfoViewModel @AssistedInject constructor(
    private val repository: BookingRepository,
    @Assisted private val initialMovie: Movie,
    @Assisted private val initialBookInfo: BookInfo = THEATER
) : ViewModel() {
    private var _model = MutableStateFlow(BookChooseInfoModel(initialBookInfo, initialMovie))
    val model: StateFlow<BookChooseInfoModel>
        get() = _model.asStateFlow()
    private var _theaterList = MutableStateFlow<List<Theater>>(listOf())
    val theaterList: StateFlow<List<Theater>>
        get() = _theaterList.asStateFlow()
    private var _showTimeList = MutableStateFlow<List<ShowtimeSlot>>(listOf())
    val showTimeList: StateFlow<List<ShowtimeSlot>>
        get() = _showTimeList.asStateFlow()
    private var _showDateList = MutableStateFlow<List<String>>(listOf())
    val showDateList: StateFlow<List<String>>
        get() = _showDateList.asStateFlow()
    private var _seatList = MutableStateFlow<List<String>>(listOf())
    val seatList: StateFlow<List<String>>
        get() = _seatList.asStateFlow()
    var chooseHandler: BookChooseHandler? = null
    init {
        loadMovieInfo(THEATER)
    }
    /**
     * Change movie model
     */
    fun setMovieAndRefresh(movie: Movie) {
        _model.value = BookChooseInfoModel(THEATER, movie)
    }
    /**
     * Change theater model
     */
    fun selectTheater(theater: Theater) {
        _model.update { it.copy(selectedTheater = theater) }
    }
    /**
     * Change showtime date model
     */
    fun selectShowDate(date: String) {
        _model.update { it.copy(selectedShowtime = BookShowtime(date)) }
    }
    /**
     * Change showtime time model
     */
    fun selectShowtime(date: String, showtime: ShowtimeSlot) {
        _model.update { it.copy(selectedShowtime = BookShowtime(date, showtime)) }
    }
    /**
     * Change seat model
     */
    fun selectSeat(seat: String) {
        _model.update { it.copy(selectedSeat = seat) }
    }
    fun getNextBookInfo(): BookInfo = when (model.value.currentBookInfo) {
        THEATER -> SHOWTIME
        SHOWTIME -> SEAT
        else -> THEATER
    }
    fun goBookInfo(bookInfo: BookInfo? = null) {
        val target = bookInfo ?: getNextBookInfo()
        _model.update { it.copy(currentBookInfo = target) }
        chooseHandler?.goNextAnimated(target)
    }
    fun loadMovieInfo(info: BookInfo, isShowDate: Boolean = false) {
        _model.update { it.copy(currentBookInfo = info) }
        val id = model.value.selectedMovie.id
        viewModelScope.launch {
            when (info) {
                THEATER -> {
                    _theaterList.value = getTheaters(id)
                }
                SHOWTIME -> {
                    if (isShowDate) {
                        _showDateList.value = getShowtimeDates(id)
                        _showTimeList.value = listOf()
                    } else {
                        val theater = model.value.selectedTheater
                        val date = model.value.selectedShowtime?.selectedShowDate
                        if (theater != null && date != null) {
                            _showTimeList.value = getShowtimeSlots(
                                id, theater.id, date)
                        }
                    }
                }
                SEAT -> {
                    // TODO: Fetch seats from repository
                    _seatList.value = listOf("Wait", "For", "A", "While")
                }
            }
        }
    }
    fun actionGoNextButton() {
        val next = getNextBookInfo()
        goBookInfo(next)
        when (next) {
            THEATER -> loadMovieInfo(THEATER)
            SHOWTIME -> loadMovieInfo(SHOWTIME, isShowDate = true)
            SEAT -> loadMovieInfo(SEAT)
        }
    }
    fun actionMoviePageMoved(movie: Movie) {
        setMovieAndRefresh(movie)
        loadMovieInfo(THEATER)
        goBookInfo(THEATER)
    }
    fun actionOnViewCreated() {
        loadMovieInfo(THEATER)
    }
    private suspend fun getShowtimeDates(movieId: Int): List<String> =
        when (val result = repository.getShowtimeDates(movieId)) {
            is APIResult.Success -> result.data
            is APIResult.Failure -> emptyList()
        }
    private suspend fun getShowtimeSlots(movieId: Int, theaterId: Int, date: String): List<ShowtimeSlot> =
        when (val result = repository.getShowtimeSlots(movieId, theaterId, date)) {
            is APIResult.Success -> result.data
            is APIResult.Failure -> emptyList()
        }
    private suspend fun getTheaters(movieId: Int): List<Theater> =
        when (val result = repository.getTheaters(movieId)) {
            is APIResult.Success -> result.data
            is APIResult.Failure -> emptyList()
        }
    @AssistedFactory
    interface MovieAssistedFactory {
        fun create(
            initialMovie: Movie,
            initialBookInfo: BookInfo = THEATER
        ): BookChooseInfoViewModel
    }
    companion object {
        fun provideFactory(
            assistedFactory: MovieAssistedFactory,
            initialMovie: Movie,
            initialBookInfo: BookInfo = THEATER,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return assistedFactory.create(initialMovie, initialBookInfo) as T
                }
            }
        }
    }
}

data class BookChooseInfoModel(
    var currentBookInfo: BookInfo,
    var selectedMovie: Movie,
    var selectedTheater: Theater? = null,
    var selectedShowtime: BookShowtime? = null,
    var selectedSeat: String? = null,
)
data class BookShowtime(
    var selectedShowDate: String,
    var selectedShowtimeSlot: ShowtimeSlot? = null,
)
