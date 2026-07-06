package com.example.moviceapp.book.choose.viewholder

import com.example.moviceapp.databinding.ItemBookChooseItemShowtimeDateBinding

const val SHOWTIME_DATE_VIEW_HOLDER_TYPE = 0

class ShowtimeDateViewHolder(
    val handler: ShowtimeClickHandler,
    val binding: ItemBookChooseItemShowtimeDateBinding,
) : ShowtimeViewHolder(binding, handler) {
    override fun bind(model: String) {
        binding.dateButton.text = model
        binding.dateButton.setOnClickListener {
            handler.onClickListener(model)
        }
    }
}