package com.example.moviceapp.book.choose.viewholder

import androidx.core.content.ContextCompat
import com.example.moviceapp.R
import com.example.moviceapp.databinding.ItemBookChooseItemShowtimeDateBinding

const val SHOWTIME_DATE_VIEW_HOLDER_TYPE = 0

class ShowtimeDateViewHolder(
    val handler: ShowtimeClickHandler,
    val binding: ItemBookChooseItemShowtimeDateBinding,
) : ShowtimeViewHolder(binding, handler) {
    override fun bind(model: String, isSelected: Boolean) {
        binding.dateButton.text = model
        val context = binding.root.context
        val tint = if (isSelected) R.color.green_accent else R.color.background_secondary
        binding.dateButton.backgroundTintList = ContextCompat.getColorStateList(context, tint)
        val textColor = if (isSelected) R.color.badge_text else R.color.text_primary
        binding.dateButton.setTextColor(ContextCompat.getColor(context, textColor))
        binding.dateButton.setOnClickListener {
            handler.onClickDate(model)
        }
    }
}
