package com.example.moviceapp.book.choose.viewholder

import androidx.core.content.ContextCompat
import com.example.moviceapp.R
import com.example.moviceapp.databinding.ItemBookChooseItemShowtimeTimeBinding

class ShowtimeTimeViewHolder(
    val handler: ShowtimeClickHandler,
    val binding: ItemBookChooseItemShowtimeTimeBinding,
) : ShowtimeViewHolder(binding, handler) {
    override fun bind(model: String, isSelected: Boolean) {
        binding.timeButton.text = model
        val context = binding.root.context
        val tint = if (isSelected) R.color.green_accent else R.color.background_secondary
        binding.timeButton.backgroundTintList = ContextCompat.getColorStateList(context, tint)
        val textColor = if (isSelected) R.color.badge_text else R.color.text_primary
        binding.timeButton.setTextColor(ContextCompat.getColor(context, textColor))
        binding.timeButton.setOnClickListener {
            handler.onClickTime(model)
        }
    }
}
