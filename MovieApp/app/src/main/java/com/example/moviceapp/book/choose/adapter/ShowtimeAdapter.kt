package com.example.moviceapp.book.choose.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moviceapp.book.choose.viewholder.ShowtimeClickHandler
import com.example.moviceapp.book.choose.viewholder.ShowtimeViewHolder
import com.example.moviceapp.book.choose.viewholder.ShowtimeViewHolderFactory
import com.example.moviceapp.repo.ShowtimeSlot

class ShowtimeAdapter: RecyclerView.Adapter<ShowtimeViewHolder>(), ShowtimeClickHandler {
    var dates: List<String> = listOf()
    var timeSlots: List<ShowtimeSlot> = listOf()
    override fun getItemCount(): Int = dates.size + timeSlots.size
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ShowtimeViewHolder = ShowtimeViewHolderFactory.createViewHolder(
        parent, viewType, this)
    override fun onBindViewHolder(
        holder: ShowtimeViewHolder,
        position: Int
    ) {
        holder.bind(if (position < dates.size)
            dates[position]
        else
            timeSlots[position].time
        )
    }
    override fun getItemViewType(position: Int): Int =
        if (position < dates.size) 0 else 1
    override fun onClickListener(date: String) {
        TODO("Not yet implemented")
    }
}