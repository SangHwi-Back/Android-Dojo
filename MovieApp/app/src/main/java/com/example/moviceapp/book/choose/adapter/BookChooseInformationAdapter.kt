package com.example.moviceapp.book.choose.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moviceapp.book.BookChooseInfoViewModel
import com.example.moviceapp.book.BookInfo
import com.example.moviceapp.book.BookShowtime
import com.example.moviceapp.book.choose.viewholder.BookChooseSeatViewHolder
import com.example.moviceapp.book.choose.viewholder.BookChooseShowtimeViewHolder
import com.example.moviceapp.book.choose.viewholder.BookChooseTheaterViewHolder
import com.example.moviceapp.book.choose.viewholder.BookChooseViewHolder
import com.example.moviceapp.book.choose.viewholder.BookChooseViewHolderFactory
import com.example.moviceapp.book.currentItem
import com.example.moviceapp.book.toBookInfo
import com.example.moviceapp.repo.ShowtimeSlot
import com.example.moviceapp.repo.Theater

class BookChooseInformationAdapter(
    private val viewModel: BookChooseInfoViewModel
) : RecyclerView.Adapter<BookChooseViewHolder>() {
    var theaters: List<Theater> = listOf()
        set(value) {
            field = value
            notifyItemChanged(BookInfo.THEATER.currentItem)
        }
    var showDateList: List<String> = listOf()
        set(value) {
            field = value
            notifyItemChanged(BookInfo.SHOWTIME.currentItem)
        }
    var showTimeList: List<ShowtimeSlot> = listOf()
        set(value) {
            field = value
            notifyItemChanged(BookInfo.SHOWTIME.currentItem)
        }
    var seats: List<String> = listOf()
        set(value) {
            field = value
            notifyItemChanged(BookInfo.SEAT.currentItem)
        }
    override fun getItemCount(): Int = 3
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BookChooseViewHolder {
        val model = viewModel.model.value
        // viewType range is 0~2. viewType.toBookInfo shouldn't be null.
        return BookChooseViewHolderFactory.createViewHolder(parent, viewModel, when (viewType.toBookInfo) {
            BookInfo.THEATER -> BookChooseViewHolderEntity
                .TheaterEntity(model.selectedTheater, theaters)
            BookInfo.SHOWTIME -> BookChooseViewHolderEntity
                .Showtime(model.selectedShowtime, showDateList, showTimeList)
            else -> BookChooseViewHolderEntity
                .Seat(model.selectedSeat, seats)
        })
    }
    override fun onBindViewHolder(
        holder: BookChooseViewHolder,
        position: Int
    ) {
        // 데이터가 새로 도착할 때마다 실제 화면에 반영 (onCreateViewHolder 는 최초 1회만 호출됨)
        when (holder) {
            is BookChooseTheaterViewHolder -> holder.setTheaters(theaters)
            is BookChooseShowtimeViewHolder -> {
                holder.setShowDateList(showDateList)
                holder.setShowtimeList(showTimeList)
            }
            is BookChooseSeatViewHolder -> holder.setSeats(seats)
        }
        holder.bind(viewModel.model.value)
    }
    override fun getItemViewType(position: Int): Int = position
}

sealed class BookChooseViewHolderEntity {
    data class TheaterEntity(
        var selectedTheater: Theater? = null,
        var theaters: List<Theater>,
    ) : BookChooseViewHolderEntity()
    data class Showtime(
        val selectedShowtime: BookShowtime? = null,
        var showDateList: List<String>,
        var showTimeList: List<ShowtimeSlot>,
    ) : BookChooseViewHolderEntity()
    data class Seat(
        val seat: String? = null,
        var seats: List<String>,
    ) : BookChooseViewHolderEntity()
}
