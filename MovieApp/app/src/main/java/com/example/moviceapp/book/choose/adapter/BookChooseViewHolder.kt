package com.example.moviceapp.book.choose.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.moviceapp.R
import com.example.moviceapp.book.BookChooseInfoModel
import com.example.moviceapp.book.BookInfo
import com.example.moviceapp.book.choose.viewholder.BookChooseSeatViewHolder
import com.example.moviceapp.book.choose.viewholder.BookChooseShowtimeViewHolder
import com.example.moviceapp.book.choose.viewholder.BookChooseTheaterViewHolder
import com.example.moviceapp.book.currentItem

abstract class BookChooseViewHolder(
    binding: ViewBinding
) : RecyclerView.ViewHolder(binding.root) {
    abstract fun bind(model: BookChooseInfoModel)
}

object BookChooseViewHolderFactory {
    fun createViewHolder(parent: ViewGroup, viewType: Int): BookChooseViewHolder {
        return when(viewType) {
            BookInfo.THEATER.currentItem -> BookChooseTheaterViewHolder(getViewDataBinding(parent,
                R.layout.item_book_choose_theater))
            BookInfo.SHOWTIME.currentItem -> BookChooseShowtimeViewHolder(getViewDataBinding(parent,
                R.layout.item_book_choose_showtime))
            else -> BookChooseSeatViewHolder(getViewDataBinding(parent,
                R.layout.item_book_choose_seat))
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