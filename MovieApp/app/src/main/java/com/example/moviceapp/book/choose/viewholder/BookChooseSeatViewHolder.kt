package com.example.moviceapp.book.choose.viewholder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moviceapp.R
import com.example.moviceapp.book.BookChooseInfoModel
import com.example.moviceapp.book.BookChooseInfoViewModel
import com.example.moviceapp.databinding.ItemBookChooseItemSeatBinding
import com.example.moviceapp.databinding.ItemBookChooseSeatBinding

class BookChooseSeatViewHolder(
    val parent: ViewGroup,
    val binding: ItemBookChooseSeatBinding,
    private val viewModel: BookChooseInfoViewModel,
) : BookChooseViewHolder(binding) {
    private val adapter = SeatListAdapter { seat ->
        viewModel.selectSeat(seat)
    }
    init {
        binding.theaterSeatRecyclerView.layoutManager = GridLayoutManager(parent.context, 4)
        binding.theaterSeatRecyclerView.adapter = adapter
    }
    fun setSeats(seats: List<String>) {
        adapter.seats = seats
    }
    override fun bind(model: BookChooseInfoModel) {
        // 선택 상태는 SeatListAdapter 가 클릭 시점에 자체적으로 추적한다
    }

    class SeatListAdapter(
        private val onSelected: (String) -> Unit
    ) : RecyclerView.Adapter<SeatViewHolder>() {
        var seats: List<String> = listOf()
            set(value) {
                field = value
                selectedPosition = -1
                notifyDataSetChanged()
            }
        private var selectedPosition = -1

        override fun getItemCount(): Int = seats.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return SeatViewHolder(ItemBookChooseItemSeatBinding.inflate(inflater, parent, false))
        }

        override fun onBindViewHolder(
            holder: SeatViewHolder,
            @SuppressLint("RecyclerView") position: Int
        ) {
            val seat = seats[position]
            holder.bind(seat, position == selectedPosition)
            holder.itemView.setOnClickListener {
                val prev = selectedPosition
                selectedPosition = position
                if (prev != -1) notifyItemChanged(prev)
                notifyItemChanged(selectedPosition)
                onSelected(seat)
            }
        }
    }

    class SeatViewHolder(
        private val binding: ItemBookChooseItemSeatBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(seat: String, isSelected: Boolean) {
            binding.seatNumber.text = seat
            val color = if (isSelected) R.color.seat_selected else R.color.seat_available
            binding.seatNumber.setTextColor(ContextCompat.getColor(binding.root.context, color))
        }
    }
}
