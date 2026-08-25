package com.example.moviceapp.book.choose.viewholder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moviceapp.R
import com.example.moviceapp.book.BookChooseInfoModel
import com.example.moviceapp.book.BookChooseInfoViewModel
import com.example.moviceapp.databinding.ItemBookChooseItemSeatBinding
import com.example.moviceapp.databinding.ItemBookChooseSeatBinding
import com.example.moviceapp.repo.SeatSlot

private const val MIN_SEAT_WIDTH: Float = 50f
private const val ROW_AISLE_HEIGHT: Float = 20f
// 좌석 1칸 = SEAT_SPAN 단위, 통로 1칸 = AISLE_SPAN 단위 (너비 비율)
private const val SEAT_SPAN = 3
private const val AISLE_SPAN = 1

sealed class SeatGridItem {
    data class Seat(val slot: SeatSlot) : SeatGridItem()
    object ColumnAisle : SeatGridItem()
    object RowAisle : SeatGridItem()
}

private fun buildGridItems(seats: List<SeatSlot>): Pair<List<SeatGridItem>, Int> {
    if (seats.isEmpty())
        return emptyList<SeatGridItem>() to 4 * SEAT_SPAN
    val byRow = seats.groupBy { it.rowIndex }.toSortedMap()
    // 가장 긴 행을 기준으로 spanCount 계산
    val refRow = byRow.values.maxByOrNull { it.size }?.sortedBy { it.columnIndex }
    if (refRow == null)
        return emptyList<SeatGridItem>() to 4 * SEAT_SPAN
    val spanCount = refRow.sumOf {
        SEAT_SPAN + (if (it.hasAisleAfterColumn) AISLE_SPAN else 0) // 좌석과 통로 사이의 간격
    }.let {
        + 2 * AISLE_SPAN // 양 옆 Span
    }
    val items = mutableListOf<SeatGridItem>()
    byRow.forEach { (_, rowSeats) ->
        // 맨 왼쪽 통로
        items.add(SeatGridItem.ColumnAisle)
        // 왼쪽 좌석부터 중간 통로
        rowSeats.sortedBy { it.columnIndex }.forEachIndexed { _, seat ->
            items.add(SeatGridItem.Seat(seat))
            if (seat.hasAisleAfterColumn) items.add(SeatGridItem.ColumnAisle)
        }
        // 맨 오른쪽 통로
        items.add(SeatGridItem.ColumnAisle)
        // 좌석 아래 통로
        if (rowSeats.any { it.hasAisleAfterRow }) {
            items.add(SeatGridItem.RowAisle)
        }
    }
    return items to spanCount
}

class BookChooseSeatViewHolder(
    private val viewModel: BookChooseInfoViewModel,
    val parent: ViewGroup,
    val binding: ItemBookChooseSeatBinding,
) : BookChooseViewHolder(binding) {
    private val adapter = SeatListAdapter { seat ->
        viewModel.selectSeat(seat)
    }
    init {
        binding.theaterSeatRecyclerView.adapter = adapter
    }
    fun setSeats(seats: List<SeatSlot>) {
        val (items, spanCount) = buildGridItems(seats)
        val density = parent.context.resources.displayMetrics.density
        val totalWidthPx = (spanCount * MIN_SEAT_WIDTH * density / SEAT_SPAN).toInt()

        binding.theaterSeatRecyclerView.layoutParams.width = totalWidthPx
        val lm = GridLayoutManager(parent.context, spanCount)
        lm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int) = adapter.getSpanSize(position, spanCount)
        }
        binding.theaterSeatRecyclerView.layoutManager = lm
        adapter.items = items
    }
    override fun bind(model: BookChooseInfoModel) {}

    class SeatListAdapter(
        private val onSelected: (SeatSlot) -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var items: List<SeatGridItem> = listOf()
            set(value) {
                field = value
                selectedPosition = -1
                notifyDataSetChanged()
            }
        private var selectedPosition = -1

        override fun getItemCount() = items.size
        override fun getItemViewType(position: Int) = when (items[position]) {
            is SeatGridItem.Seat     -> 0
            SeatGridItem.ColumnAisle -> 1
            SeatGridItem.RowAisle    -> 2
        }

        fun getSpanSize(position: Int, spanCount: Int) = when (items.getOrNull(position)) {
            SeatGridItem.RowAisle    -> spanCount
            SeatGridItem.ColumnAisle -> AISLE_SPAN
            else                     -> SEAT_SPAN
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val density = parent.context.resources.displayMetrics.density
            return when (viewType) {
                0 -> SeatViewHolder(
                    ItemBookChooseItemSeatBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
                1 -> AisleViewHolder(View(parent.context).apply {
                    layoutParams = RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        (MIN_SEAT_WIDTH * density).toInt()
                    )
                })
                else -> AisleViewHolder(View(parent.context).apply {
                    layoutParams = RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        (ROW_AISLE_HEIGHT * density).toInt()
                    )
                })
            }
        }

        override fun onBindViewHolder(
            holder: RecyclerView.ViewHolder,
            @SuppressLint("RecyclerView") position: Int
        ) {
            if (holder !is SeatViewHolder) return
            val item = items[position] as SeatGridItem.Seat
            holder.bind(item.slot, position == selectedPosition)
            holder.itemView.setOnClickListener {
                val prev = selectedPosition
                selectedPosition = position
                if (prev != -1) notifyItemChanged(prev)
                notifyItemChanged(selectedPosition)
                onSelected(item.slot)
            }
        }
    }

    class AisleViewHolder(view: View) : RecyclerView.ViewHolder(view)

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
