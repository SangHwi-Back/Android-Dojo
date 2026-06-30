package com.example.moviceapp.book

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.*
import com.example.moviceapp.R
import com.example.moviceapp.databinding.FragmentBookScheduleBinding
import com.example.moviceapp.databinding.ItemBookScheduleDateBinding
import com.example.moviceapp.databinding.ItemBookScheduleTimeBinding
import com.example.moviceapp.repo.ShowtimeMock
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.*

class BookScheduleFragment : Fragment() {

    private val args: BookScheduleFragmentArgs by navArgs()
    private var _binding: FragmentBookScheduleBinding? = null
    private val binding get() = _binding!!
    private var selectedDate: ShowtimeMock.ShowDate? = null
    private var selectedTime: ShowtimeMock.Showtime? = null

    private val dateAdapter = DateAdapter { selectedDate ->
        timeAdapter.submitList(ShowtimeMock.timesForDate(selectedDate.isoDate))
        this.selectedDate = ShowtimeMock.ShowDate(selectedDate.dayNum.toString(), selectedDate.isoDate)
    }
    private val timeAdapter = TimeAdapter { selectedTime ->
        this.selectedTime = selectedTime
        if (this.selectedDate != null)
            findNavController().navigate(
                BookScheduleFragmentDirections
                .actionBookScheduleFragmentToBookSeatFragment(args.movie, args.theater))
    }
    lateinit var topAppBar: MaterialToolbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        topAppBar = view.findViewById(R.id.top_appBar)
        topAppBar.title = args.movie.title

        // 날짜 RecyclerView (가로 스크롤)
        binding.dateRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false
        )
        binding.dateRecyclerView.adapter = dateAdapter

        // 시간 RecyclerView (3열 그리드)
        binding.timeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.timeRecyclerView.adapter = timeAdapter

        // 오늘부터 7일 날짜 생성
        val dates = generateDates(7)
        dateAdapter.submitList(dates)

        // 첫 번째 날짜의 시간표 기본 로드
        timeAdapter.submitList(ShowtimeMock.timesForDate(dates.first().isoDate))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        topAppBar.title = getString(R.string.app_name)
    }

    // ── 오늘부터 count일 간의 DateItem 목록 생성 ──────────────────────
    private fun generateDates(count: Int): List<DateItem> {
        val dayOfWeekFmt = SimpleDateFormat("EEE", Locale.ENGLISH)
        val isoFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return (0 until count).map { offset ->
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset) }
            DateItem(
                dayOfWeek  = dayOfWeekFmt.format(cal.time),
                dayNum     = cal.get(Calendar.DAY_OF_MONTH),
                isoDate    = isoFmt.format(cal.time),
                isSelected = offset == 0
            )
        }
    }

    // ── 데이터 모델 ───────────────────────────────────────────────────
    data class DateItem(
        val dayOfWeek: String,
        val dayNum: Int,
        val isoDate: String,
        val isSelected: Boolean = false,
    )

    // ── 날짜 Adapter ──────────────────────────────────────────────────
    class DateAdapter(
        private val onDateSelected: (DateItem) -> Unit,
    ) : ListAdapter<DateItem, DateAdapter.DateViewHolder>(DateDiffCallback) {

        private var selectedIndex = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
            val binding = ItemBookScheduleDateBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return DateViewHolder(binding)
        }

        override fun onBindViewHolder(holder: DateViewHolder, @SuppressLint("RecyclerView") position: Int) {
            holder.bind(getItem(position), position == selectedIndex)
            holder.itemView.setOnClickListener {
                val prev = selectedIndex
                selectedIndex = position
                notifyItemChanged(prev)
                notifyItemChanged(selectedIndex)
                onDateSelected(getItem(selectedIndex))
            }
        }

        object DateDiffCallback : DiffUtil.ItemCallback<DateItem>() {
            override fun areItemsTheSame(oldItem: DateItem, newItem: DateItem) =
                oldItem.isoDate == newItem.isoDate
            override fun areContentsTheSame(oldItem: DateItem, newItem: DateItem) =
                oldItem == newItem
        }

        class DateViewHolder(
            private val binding: ItemBookScheduleDateBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(item: DateItem, isSelected: Boolean) {
                binding.dayOfWeekTextView.text = item.dayOfWeek
                binding.dayNumberTextView.text = item.dayNum.toString()

                val context = binding.root.context
                if (isSelected) {
                    binding.root.background =
                        ContextCompat.getDrawable(context, R.drawable.bg_date_chip_selected)
                    binding.dayOfWeekTextView.setTextColor(
                        ContextCompat.getColor(context, R.color.background_primary)
                    )
                    binding.dayNumberTextView.setTextColor(
                        ContextCompat.getColor(context, R.color.background_primary)
                    )
                } else {
                    binding.root.background =
                        ContextCompat.getDrawable(context, R.drawable.bg_date_chip_unselected)
                    binding.dayOfWeekTextView.setTextColor(
                        ContextCompat.getColor(context, R.color.text_secondary)
                    )
                    binding.dayNumberTextView.setTextColor(
                        ContextCompat.getColor(context, R.color.text_primary)
                    )
                }
            }
        }
    }

    // ── 시간 Adapter ──────────────────────────────────────────────────
    class TimeAdapter(
        val onTimeSelected: (ShowtimeMock.Showtime) -> Unit
    ) : ListAdapter<ShowtimeMock.Showtime, TimeAdapter.TimeViewHolder>(TimeDiffCallback) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
            val binding = ItemBookScheduleTimeBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return TimeViewHolder(binding)
        }

        override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
            holder.bind(getItem(position))
            holder.itemView.setOnClickListener {
                onTimeSelected(getItem(position))
            }
        }

        object TimeDiffCallback : DiffUtil.ItemCallback<ShowtimeMock.Showtime>() {
            override fun areItemsTheSame(
                oldItem: ShowtimeMock.Showtime,
                newItem: ShowtimeMock.Showtime
            ) = oldItem.time == newItem.time

            override fun areContentsTheSame(
                oldItem: ShowtimeMock.Showtime,
                newItem: ShowtimeMock.Showtime
            ) = oldItem == newItem
        }

        class TimeViewHolder(
            private val binding: ItemBookScheduleTimeBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(item: ShowtimeMock.Showtime) {
                binding.timeTextView.text = item.time
            }
        }
    }
}