package com.example.moviceapp.book.choose.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.moviceapp.R
import com.example.moviceapp.book.BookChooseInfoModel
import com.example.moviceapp.book.choose.adapter.BookChooseViewHolderEntity

abstract class BookChooseViewHolder(
    binding: ViewBinding
) : RecyclerView.ViewHolder(binding.root) {
    abstract fun bind(model: BookChooseInfoModel)
}

object BookChooseViewHolderFactory {
    fun createViewHolder(parent: ViewGroup, entity: BookChooseViewHolderEntity): BookChooseViewHolder {
        return when (entity) {
            is BookChooseViewHolderEntity.TheaterEntity -> BookChooseTheaterViewHolder(parent, getViewDataBinding(parent,
                R.layout.item_book_choose_theater)).apply {
                    setTheaters(entity.theaters)
            }
            is BookChooseViewHolderEntity.Showtime -> BookChooseShowtimeViewHolder(parent, getViewDataBinding(parent,
                R.layout.item_book_choose_showtime)).apply {
                    setShowtimeList(entity.showTimeList)
                    setShowDateList(entity.showDateList)
            }
            is BookChooseViewHolderEntity.Seat -> BookChooseSeatViewHolder(parent, getViewDataBinding(parent,
                R.layout.item_book_choose_seat)).apply {
                    setSeats(entity.seats)
            }
        }
    }

    private fun <T: ViewDataBinding> getViewDataBinding(parent: ViewGroup, layoutRes: Int): T {
        return DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            layoutRes,
            parent,
            false
        )
    }
}