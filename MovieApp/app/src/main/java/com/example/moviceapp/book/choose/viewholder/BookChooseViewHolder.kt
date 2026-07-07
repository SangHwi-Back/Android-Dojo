package com.example.moviceapp.book.choose.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.moviceapp.book.BookChooseInfoModel
import com.example.moviceapp.book.BookChooseInfoViewModel
import com.example.moviceapp.book.choose.adapter.BookChooseViewHolderEntity
import com.example.moviceapp.databinding.ItemBookChooseSeatBinding
import com.example.moviceapp.databinding.ItemBookChooseShowtimeBinding
import com.example.moviceapp.databinding.ItemBookChooseTheaterBinding

abstract class BookChooseViewHolder(
    binding: ViewBinding
) : RecyclerView.ViewHolder(binding.root) {
    abstract fun bind(model: BookChooseInfoModel)
}

object BookChooseViewHolderFactory {
    fun createViewHolder(
        parent: ViewGroup,
        viewModel: BookChooseInfoViewModel,
        entity: BookChooseViewHolderEntity
    ): BookChooseViewHolder {
        return when (entity) {
            is BookChooseViewHolderEntity.TheaterEntity -> BookChooseTheaterViewHolder(
                parent,
                ItemBookChooseTheaterBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                viewModel
            ).apply {
                setTheaters(entity.theaters)
            }
            is BookChooseViewHolderEntity.Showtime -> BookChooseShowtimeViewHolder(
                parent,
                ItemBookChooseShowtimeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                viewModel
            ).apply {
                setShowtimeList(entity.showTimeList)
                setShowDateList(entity.showDateList)
            }
            is BookChooseViewHolderEntity.Seat -> BookChooseSeatViewHolder(
                parent,
                ItemBookChooseSeatBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                viewModel
            ).apply {
                setSeats(entity.seats)
            }
        }
    }
}
