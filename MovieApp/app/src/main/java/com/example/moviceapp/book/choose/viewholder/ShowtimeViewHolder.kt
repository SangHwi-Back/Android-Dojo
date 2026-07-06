package com.example.moviceapp.book.choose.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.moviceapp.R

abstract class ShowtimeViewHolder(
    binding: ViewBinding,
    handler: ShowtimeClickHandler,
) : RecyclerView.ViewHolder(binding.root) {
    abstract fun bind(model: String)
}

object ShowtimeViewHolderFactory {
    fun createViewHolder(parent: ViewGroup, viewType: Int, handler: ShowtimeClickHandler): ShowtimeViewHolder {
        return when (viewType) {
            SHOWTIME_DATE_VIEW_HOLDER_TYPE -> ShowtimeDateViewHolder(handler,getViewDataBinding(
                parent,R.layout.item_book_choose_item_showtime_date))
            else -> ShowtimeTimeViewHolder(handler, getViewDataBinding(
                parent,R.layout.item_book_choose_item_showtime_time))
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

interface ShowtimeClickHandler {
    fun onClickListener(date: String)
}