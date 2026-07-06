package com.example.moviceapp.book.choose.viewholder

import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.moviceapp.book.BookChooseInfoModel
import com.example.moviceapp.book.BookChooseInfoViewModel
import com.example.moviceapp.book.BookInfo
import com.example.moviceapp.book.choose.adapter.ShowtimeAdapter
import com.example.moviceapp.databinding.ItemBookChooseShowtimeBinding
import com.example.moviceapp.repo.ShowtimeSlot

class BookChooseShowtimeViewHolder(
    val parent: ViewGroup,
    val binding: ItemBookChooseShowtimeBinding,
    private val viewModel: BookChooseInfoViewModel,
) : BookChooseViewHolder(binding) {
    private val dateAdapter = ShowtimeAdapter(
        onDateSelected = { date ->
            viewModel.selectShowDate(date)
            viewModel.loadMovieInfo(BookInfo.SHOWTIME, isShowDate = false)
        },
        onTimeSelected = { slot ->
            val date = viewModel.model.value.selectedShowtime?.selectedShowDate
            if (date != null) viewModel.selectShowtime(date, slot)
        }
    )
    init {
        binding.showtimeRecyclerView.layoutManager = GridLayoutManager(parent.context, 3)
        binding.showtimeRecyclerView.adapter = dateAdapter
    }
    fun setShowDateList(list: List<String>) {
        dateAdapter.dates = list
    }
    fun setShowtimeList(list: List<ShowtimeSlot>) {
        dateAdapter.timeSlots = list
    }
    override fun bind(model: BookChooseInfoModel) {
        // 선택 상태는 ShowtimeAdapter 가 클릭 시점에 자체적으로 추적한다
    }
}
