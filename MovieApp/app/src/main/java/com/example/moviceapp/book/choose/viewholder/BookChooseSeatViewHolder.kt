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
import com.example.moviceapp.common.GridSpanDecoration
import com.example.moviceapp.databinding.ItemBookChooseItemSeatBinding
import com.example.moviceapp.databinding.ItemBookChooseSeatBinding
import com.example.moviceapp.repo.SeatSlot

private const val MIN_WIDTH_HEIGHT: Float = 50f
private const val GRID_SPACE: Float = 8f

class BookChooseSeatViewHolder(
    private val viewModel: BookChooseInfoViewModel,
    val parent: ViewGroup,
    val binding: ItemBookChooseSeatBinding,
) : BookChooseViewHolder(binding) {
    private var decoration: GridSpanDecoration? = null
    private val adapter = SeatListAdapter { seat ->
        viewModel.selectSeat(seat)
    }
    init {
        binding.theaterSeatRecyclerView.layoutManager = GridLayoutManager(
            parent.context, 4
        )
        GridSpanDecoration(4, 8).apply {
            this@BookChooseSeatViewHolder.decoration = this
            binding.theaterSeatRecyclerView.addItemDecoration(this)
        }
        binding.theaterSeatRecyclerView.adapter = adapter
    }
    fun setSeats(seats: List<SeatSlot>) {
        val maxRowCount = if (seats.isEmpty()) {
            4
        } else {
            seats.groupBy { it.rowLabel }
                .maxBy { it.value.size }
                .value
                .size
        }
        val density = parent.context.resources.displayMetrics.density
        // item minWidth = 50dp
        val itemWidthPx = (MIN_WIDTH_HEIGHT * density).toInt()
        // GridSpanDecoration gap = 8dp
        val gapPx = (GRID_SPACE * density).toInt()
        // Length of Recyclerview grid width
        val totalWidthPx = maxRowCount * itemWidthPx + (maxRowCount - 1) * gapPx
        // Remove unused ItemDecoration
        decoration?.let { binding.theaterSeatRecyclerView.removeItemDecoration(it) }
        // Set new layout properties
        binding.theaterSeatRecyclerView.layoutParams.width = totalWidthPx
        binding.theaterSeatRecyclerView.layoutManager = GridLayoutManager(
            parent.context, maxRowCount
        )
        GridSpanDecoration(maxRowCount, GRID_SPACE.toInt()).apply {
            this@BookChooseSeatViewHolder.decoration = this
            binding.theaterSeatRecyclerView.addItemDecoration(this)
        }
        adapter.seats = seats
    }
    override fun bind(model: BookChooseInfoModel) {
        // 선택 상태는 SeatListAdapter 가 클릭 시점에 자체적으로 추적한다
    }

    class SeatListAdapter(
        private val onSelected: (SeatSlot) -> Unit
    ) : RecyclerView.Adapter<SeatViewHolder>() {
        var seats: List<SeatSlot> = listOf()
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
        fun bind(seat: SeatSlot, isSelected: Boolean) {
            binding.seatNumber.text = seat.name
            val color = if (isSelected) R.color.seat_selected else R.color.seat_available
            binding.seatNumber.setTextColor(ContextCompat.getColor(binding.root.context, color))
        }
    }
}
