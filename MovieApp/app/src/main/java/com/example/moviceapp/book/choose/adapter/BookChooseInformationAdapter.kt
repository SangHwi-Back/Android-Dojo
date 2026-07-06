package com.example.moviceapp.book.choose.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moviceapp.book.BookChooseInfoViewModel
import com.example.moviceapp.book.BookInfo
import com.example.moviceapp.book.BookShowtime
import com.example.moviceapp.book.choose.viewholder.BookChooseViewHolder
import com.example.moviceapp.book.choose.viewholder.BookChooseViewHolderFactory
import com.example.moviceapp.book.currentItem
import com.example.moviceapp.repo.ShowtimeSlot
import com.example.moviceapp.repo.Theater

class BookChooseInformationAdapter(
    private val viewModel: BookChooseInfoViewModel
) : RecyclerView.Adapter<BookChooseViewHolder>() {
    var theaters: List<Theater> = listOf()
    var showDateList: List<String> = listOf()
    var showTimeList: List<ShowtimeSlot> = listOf()
    var seats: List<String> = listOf()
    override fun getItemCount(): Int = 3
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BookChooseViewHolder {
        val model = viewModel.model.value
        return BookChooseViewHolderFactory.createViewHolder(parent, when (viewType) {
            BookInfo.THEATER.currentItem -> BookChooseViewHolderEntity
                .TheaterEntity(model.selectedTheater, theaters)
            BookInfo.SHOWTIME.currentItem -> BookChooseViewHolderEntity
                .Showtime(model.selectedShowtime, showDateList, showTimeList)
            else -> BookChooseViewHolderEntity
                .Seat(model.selectedSeat, seats)
        })
    }
    override fun onBindViewHolder(
        holder: BookChooseViewHolder,
        position: Int
    ) {
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