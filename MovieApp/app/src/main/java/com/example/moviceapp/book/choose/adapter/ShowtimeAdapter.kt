package com.example.moviceapp.book.choose.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moviceapp.book.choose.viewholder.SHOWTIME_DATE_VIEW_HOLDER_TYPE
import com.example.moviceapp.book.choose.viewholder.ShowtimeClickHandler
import com.example.moviceapp.book.choose.viewholder.ShowtimeViewHolder
import com.example.moviceapp.book.choose.viewholder.ShowtimeViewHolderFactory
import com.example.moviceapp.repo.ShowtimeSlot

const val SHOWTIME_TIME_VIEW_HOLDER_TYPE = 1

class ShowtimeAdapter(
    private val onDateSelected: (String) -> Unit,
    private val onTimeSelected: (ShowtimeSlot) -> Unit,
) : RecyclerView.Adapter<ShowtimeViewHolder>(), ShowtimeClickHandler {

    var dates: List<String> = listOf()
        set(value) {
            field = value
            selectedDateIndex = -1
            selectedTimeIndex = -1
            notifyDataSetChanged()
        }
    var timeSlots: List<ShowtimeSlot> = listOf()
        set(value) {
            field = value
            selectedTimeIndex = -1
            notifyDataSetChanged()
        }

    private var selectedDateIndex = -1
    private var selectedTimeIndex = -1
    val paddingDatesCount: Int
        get() = if (dates.size % 3 == 0) 0 else (3 - dates.size % 3)

    override fun getItemCount(): Int = dates.size + paddingDatesCount + timeSlots.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ShowtimeViewHolder = ShowtimeViewHolderFactory.createViewHolder(
        parent, viewType, this)

    override fun onBindViewHolder(
        holder: ShowtimeViewHolder,
        position: Int
    ) {
        if (position < dates.size) {
            holder.bind(dates[position], position == selectedDateIndex)
        } else if (position < (dates.size + paddingDatesCount)) {
            holder.bind("", false)
        } else {
            val timeIndex = position - paddingDatesCount - dates.size
            holder.bind(timeSlots[timeIndex].time, timeIndex == selectedTimeIndex)
        }
    }

    override fun getItemViewType(position: Int): Int =
        if (position < (dates.size + paddingDatesCount))
            SHOWTIME_DATE_VIEW_HOLDER_TYPE
        else
            SHOWTIME_TIME_VIEW_HOLDER_TYPE

    override fun onClickDate(date: String) {
        val index = dates.indexOf(date)
        if (index == -1) return
        val prevDate = selectedDateIndex
        val prevTime = selectedTimeIndex
        selectedDateIndex = index
        selectedTimeIndex = -1 // 날짜가 바뀌면 이전에 고른 시간은 더 이상 유효하지 않음
        if (prevDate != -1) notifyItemChanged(prevDate)
        if (prevTime != -1) notifyItemChanged(dates.size + prevTime)
        notifyItemChanged(index)
        onDateSelected(date)
    }

    override fun onClickTime(time: String) {
        val timeIndex = timeSlots.indexOfFirst { it.time == time }
        if (timeIndex == -1) return
        val prev = selectedTimeIndex
        selectedTimeIndex = timeIndex
        if (prev != -1) notifyItemChanged(dates.size + prev)
        notifyItemChanged(dates.size + timeIndex)
        onTimeSelected(timeSlots[timeIndex])
    }
}
