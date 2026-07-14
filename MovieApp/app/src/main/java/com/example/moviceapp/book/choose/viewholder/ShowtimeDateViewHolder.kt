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
    private val model: String = ""

    override fun bind(model: String, isSelected: Boolean) {
        val context = binding.root.context
        val tint = if (model.isEmpty())
            R.color.background_primary
        else if (isSelected)
            R.color.green_accent
        else
            R.color.background_secondary
        val textColor = if (model.isEmpty())
            R.color.background_primary
        else if (isSelected)
            R.color.badge_text
        else
            R.color.text_primary
        binding.dateButton.isClickable = model.isNotEmpty()
        binding.dateButton.text = model
        binding.dateButton.backgroundTintList = ContextCompat.getColorStateList(context, tint)
        binding.dateButton.setTextColor(ContextCompat.getColor(context, textColor))
        if (model.isNotEmpty()) {
            binding.dateButton.setOnClickListener {
                handler.onClickDate(model)
            }
        }
    }
}
