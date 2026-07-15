package com.example.moviceapp.book.choose.viewholder

import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.moviceapp.book.BookChooseInfoModel
import com.example.moviceapp.book.BookChooseInfoViewModel
import com.example.moviceapp.book.choose.adapter.ShowtimeAdapter
import com.example.moviceapp.databinding.ItemBookChooseShowtimeBinding
import com.example.moviceapp.repo.ShowtimeSlot

class BookChooseShowtimeViewHolder(
    private val viewModel: BookChooseInfoViewModel,
    val parent: ViewGroup,
    val binding: ItemBookChooseShowtimeBinding,
) : BookChooseViewHolder(binding) {
    private val dateAdapter = ShowtimeAdapter(
        onDateSelected = { date -> viewModel.actionSetDateButton(date) },
        onTimeSelected = { slot -> viewModel.actionSetTimeButton(slot) }
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
