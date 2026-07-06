package com.example.moviceapp.book.choose.viewholder

import com.example.moviceapp.databinding.ItemBookChooseItemShowtimeTimeBinding

// NO USE
//const val SHOWTIME_TIME_VIEW_HOLDER_TYPE = 1
class ShowtimeTimeViewHolder(
    val handler: ShowtimeClickHandler,
    val binding: ItemBookChooseItemShowtimeTimeBinding,
) : ShowtimeViewHolder(binding, handler) {
    override fun bind(model: String) {
        binding.timeButton.text = model
        binding.timeButton.setOnClickListener {
            handler.onClickListener(model)
        }
    }
}