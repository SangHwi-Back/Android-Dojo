package com.example.moviceapp.book.choose.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.moviceapp.databinding.ItemBookChooseItemShowtimeDateBinding
import com.example.moviceapp.databinding.ItemBookChooseItemShowtimeTimeBinding

abstract class ShowtimeViewHolder(
    binding: ViewBinding,
    handler: ShowtimeClickHandler,
) : RecyclerView.ViewHolder(binding.root) {
    abstract fun bind(model: String, isSelected: Boolean)
}

object ShowtimeViewHolderFactory {
    fun createViewHolder(parent: ViewGroup, viewType: Int, handler: ShowtimeClickHandler): ShowtimeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == SHOWTIME_DATE_VIEW_HOLDER_TYPE)
            ShowtimeDateViewHolder(handler, ItemBookChooseItemShowtimeDateBinding
                .inflate(inflater, parent, false))
        else
            ShowtimeTimeViewHolder(handler, ItemBookChooseItemShowtimeTimeBinding
                .inflate(inflater, parent, false))
    }
}

interface ShowtimeClickHandler {
    fun onClickDate(date: String)
    fun onClickTime(time: String)
}
