package com.example.moviceapp.book.choose.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moviceapp.book.BookShowtime
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
            notifyDataSetChanged()
        }
    var timeSlots: List<ShowtimeSlot> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    // 선택 "인덱스"를 캐시하지 않고, 선택된 "값"만 캐시한다.
    // 인덱스는 selectedDateIndex/selectedTimeIndex getter에서 매번 dates/timeSlots 기준으로 다시 계산되므로,
    // dates가 나중에 채워지든 currentShowtime이 나중에 도착하든 순서에 상관없이 항상 최신 상태로 수렴한다.
    private var currentShowtime: BookShowtime? = null

    private val selectedDateIndex: Int
        get() = currentShowtime?.selectedShowDate?.let { dates.indexOf(it) } ?: -1

    private val selectedTimeIndex: Int
        get() = currentShowtime?.selectedShowtimeSlot?.let { timeSlots.indexOf(it) } ?: -1

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
        if (date.isEmpty()) return
        onDateSelected(date)
    }

    override fun onClickTime(time: String) {
        if (time.isEmpty()) return
        val timeSlot = timeSlots.firstOrNull { it.time == time }
        if (timeSlot != null) {
            onTimeSelected(timeSlot)
        }
    }

    fun setShowtime(showtime: BookShowtime?) {
        currentShowtime = showtime
        notifyDataSetChanged()
    }
}
