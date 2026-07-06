package com.example.moviceapp.book.choose.viewholder

import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.moviceapp.book.BookChooseInfoModel
import com.example.moviceapp.book.choose.adapter.ShowtimeAdapter
import com.example.moviceapp.databinding.ItemBookChooseShowtimeBinding

class BookChooseShowtimeViewHolder(
    val parent: ViewGroup,
    val binding: ItemBookChooseShowtimeBinding
) : BookChooseViewHolder(binding) {
    private val dateAdapter = ShowtimeAdapter()
    init {
        binding.showtimeRecyclerView.layoutManager = GridLayoutManager(parent.context, 3)
        binding.showtimeRecyclerView.adapter = dateAdapter
    }
    override fun bind(model: BookChooseInfoModel) {
        TODO("Not yet implemented")
    }
}