package com.example.moviceapp

import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.moviceapp.databinding.FragmentBookSeatBinding
import com.example.moviceapp.databinding.ItemBookSeatSelectBinding

class BookSeatFragment : Fragment() {
    private var _binding: FragmentBookSeatBinding? = null
    val binding get() = _binding!!

    private val adapter = TheaterSeatListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookSeatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.theaterSeatRecyclerView.adapter = adapter
        binding.theaterSeatRecyclerView.addItemDecoration(
            TheaterSeatDecoration(2, 6, 8, 4, 8))
        adapter.submitList(listOf(
            "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "A10", "A11", "A12",
            "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "B10", "B11", "B12",
            "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "C10", "C11", "C12",
            "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9", "D10", "D11", "D12",
            "E1", "E2", "E3", "E4", "E5", "E6", "E7", "E8", "E9", "E10", "E11", "E12",
            "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12",
            "G1", "G2", "G3", "G4", "G5", "G6", "G7", "G8", "G9", "G10", "G11", "G12",
            "H1", "H2", "H3", "H4", "H5", "H6", "H7", "H8", "H9", "H10", "H11", "H12"
        ))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class TheaterSeatListAdapter: ListAdapter<String, TheaterSeatViewHolder>(TheaterSeatDiffCallback) {
    object TheaterSeatDiffCallback: DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
            oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TheaterSeatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBookSeatSelectBinding.inflate(
            inflater, parent, false)
        return TheaterSeatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TheaterSeatViewHolder, position: Int) =
        holder.bind(getItem(position))
}

class TheaterSeatViewHolder(
    val binding: ItemBookSeatSelectBinding
): RecyclerView.ViewHolder(binding.root) {
    fun bind(seat: String) {
        binding.seatNumber.text = seat
    }
}

// MADE BY ME
//class TwoColumnGridSpanDecoration(
//    private val columnCount: Int = 1,
//    private val countPerColumn: Int,
//    private val columnSpacingPx: Int,
//    private val itemSpacingPx: Int,
//) : RecyclerView.ItemDecoration() {
//    override fun getItemOffsets(
//        outRect: Rect,
//        view: View,
//        parent: RecyclerView,
//        state: RecyclerView.State
//    ) {
//        val position = parent.getChildAdapterPosition(view)
//        val parentColumn = position % (columnCount * countPerColumn)
//        val childColumn = position % countPerColumn
//        val paddingBetweenParentColumn = if (parentColumn > 1) parentColumn * columnSpacingPx else 0
//        val spanCount = columnCount * countPerColumn
//        val spacingPx = childColumn * itemSpacingPx + paddingBetweenParentColumn * columnSpacingPx
//        val column = childColumn + (paddingBetweenParentColumn * childColumn)
//        outRect.left = column * spacingPx / spanCount
//        outRect.right = spacingPx - (column + 1) * spacingPx / spanCount
//        outRect.bottom = spacingPx
//    }
//}

// MADE BY AI
class TheaterSeatDecoration(
    private val groupCount: Int,          // 좌석 그룹 수 (ex: 2 → 좌/우)
    private val seatsPerGroup: Int,       // 그룹당 열 수 (ex: 6)
    private val groupSpacingPx: Int,      // 그룹 간 통로 간격
    private val itemSpacingPx: Int,       // 좌석 간 간격
    private val rowSpacingPx: Int,        // 행 간격
) : RecyclerView.ItemDecoration() {

    private val totalSeatsPerRow = groupCount * seatsPerGroup

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position.toLong() == RecyclerView.NO_ID.toInt().toLong()) return

        val indexInRow = position % totalSeatsPerRow   // 행 내 좌석 인덱스 (0~11)
        val groupIndex = indexInRow / seatsPerGroup    // 몇 번째 그룹인지 (0~1)
        val indexInGroup = indexInRow % seatsPerGroup  // 그룹 내 인덱스 (0~5)
        val isLastInGroup = indexInGroup == seatsPerGroup - 1
        val isLastGroup = groupIndex == groupCount - 1

        outRect.left = if (indexInGroup == 0 && groupIndex == 0) 0 else itemSpacingPx / 2
        outRect.right = when {
            isLastInGroup && !isLastGroup -> groupSpacingPx / 2  // 그룹 끝 → 통로
            else -> itemSpacingPx / 2
        }
        outRect.left = when {
            indexInGroup == 0 && groupIndex > 0 -> groupSpacingPx / 2  // 그룹 시작 → 통로
            else -> outRect.left
        }
        outRect.bottom = rowSpacingPx
    }
}
