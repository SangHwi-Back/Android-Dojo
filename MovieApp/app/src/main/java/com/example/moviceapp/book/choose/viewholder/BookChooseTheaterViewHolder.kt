package com.example.moviceapp.book.choose.viewholder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.moviceapp.R
import com.example.moviceapp.book.BookChooseInfoModel
import com.example.moviceapp.book.BookChooseInfoViewModel
import com.example.moviceapp.databinding.ItemBookChooseTheaterBinding
import com.example.moviceapp.databinding.ItemBookTheaterSelectTheaterBinding
import com.example.moviceapp.repo.Theater

class BookChooseTheaterViewHolder(
    private val viewModel: BookChooseInfoViewModel,
    val parent: ViewGroup,
    val binding: ItemBookChooseTheaterBinding,
) : BookChooseViewHolder(binding) {
    private val adapter = TheaterListAdapter { theater ->
        viewModel.selectTheater(theater)
    }
    override fun bind(model: BookChooseInfoModel) {
        binding.theaterRecyclerView.layoutManager = LinearLayoutManager(parent.context)
        binding.theaterRecyclerView.adapter = adapter
    }
    fun setTheaters(list: List<Theater>) {
        adapter.submitList(list)
    }
    class TheaterListAdapter(
        private val onSelected: (Theater) -> Unit
    ) : ListAdapter<Theater, TheaterViewHolder>(TheaterDiffCallback) {

        private var selectedPosition = RecyclerView.NO_ID.toInt()

        object TheaterDiffCallback : DiffUtil.ItemCallback<Theater>() {
            override fun areItemsTheSame(oldItem: Theater, newItem: Theater) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Theater, newItem: Theater) =
                oldItem == newItem
        }

        override fun submitList(list: List<Theater>?) {
            selectedPosition = RecyclerView.NO_ID.toInt()
            super.submitList(list)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TheaterViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return TheaterViewHolder(
                ItemBookTheaterSelectTheaterBinding.inflate(inflater, parent, false)
            )
        }

        override fun onBindViewHolder(
            holder: TheaterViewHolder,
            @SuppressLint("RecyclerView") position: Int
        ) {
            holder.bind(getItem(position), position == selectedPosition)
            holder.itemView.setOnClickListener {
                val prev = selectedPosition
                selectedPosition = position
                if (prev != RecyclerView.NO_ID.toInt()) notifyItemChanged(prev)
                notifyItemChanged(selectedPosition)
                onSelected(getItem(position))
            }
        }
    }
    class TheaterViewHolder(
        private val binding: ItemBookTheaterSelectTheaterBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(theater: Theater, isSelected: Boolean) {
            binding.nameTextView.text = theater.name
            binding.addressTextView.text = theater.address
            binding.distanceTextView.text =
                binding.root.context.getString(R.string.label_distance_format, theater.distanceKm)

            val bgColor = if (isSelected) R.color.green_accent else R.color.surface_card
            binding.root.setBackgroundColor(
                ContextCompat.getColor(binding.root.context, bgColor)
            )
        }
    }
}