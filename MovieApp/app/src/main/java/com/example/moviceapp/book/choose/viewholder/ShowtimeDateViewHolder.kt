package com.example.moviceapp.book.choose.viewholder

import android.view.View
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

        binding.dateButton.backgroundTintList = ContextCompat.getColorStateList(
            binding.root.context, if (model.isEmpty())
                R.color.background_primary
            else if (isSelected)
                R.color.green_accent
            else
                R.color.background_secondary
        )

        binding.dateButton.setTextColor(ContextCompat.getColor(
            binding.root.context, if (model.isEmpty())
                R.color.background_primary
            else if (isSelected)
                R.color.badge_text
            else
                R.color.text_primary
        ))

        binding.dateButton.isClickable = model.isNotEmpty()

        if (model.isNotEmpty()) {
            binding.dateButton.setOnClickListener { handler.onClickDate(model) }
        }
    }
}
