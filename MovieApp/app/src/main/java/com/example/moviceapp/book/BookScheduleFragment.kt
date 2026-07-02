package com.example.moviceapp.book

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.moviceapp.R
import com.example.moviceapp.databinding.FragmentBookScheduleBinding
import com.example.moviceapp.databinding.ItemBookScheduleDateBinding
import com.example.moviceapp.databinding.ItemBookScheduleTimeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class BookScheduleFragment : Fragment() {
    private val args: BookScheduleFragmentArgs by navArgs()
    private var _binding: FragmentBookScheduleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BookScheduleViewModel by viewModels()

    private var selectedDate: String? = null
    private var selectedTime: String? = null

    private val dateAdapter = DateGridAdapter { dateItem ->
        selectedDate = dateItem.isoDate
        loadTimes(currentMovieId, args.theater.id, dateItem.isoDate)
    }
    private val timeAdapter = TimeGridAdapter { time ->
        selectedTime = time
        updateConfirmButtonState()
    }

    private var currentMovieId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val movies = args.movies.toList()

        binding.movieViewPager.adapter = MoviePagerAdapter(movies)
        binding.movieViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                loadDates(movies[position].id)
            }
        })

        binding.dateRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.dateRecyclerView.adapter = dateAdapter

        binding.timeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.timeRecyclerView.adapter = timeAdapter

        if (movies.isNotEmpty()) loadDates(movies[0].id)

        binding.confirmButton.setOnClickListener {
            val movie = args.movies.first { it.id == currentMovieId }
            val theater = args.theater

            findNavController().navigate(BookScheduleFragmentDirections
                .actionBookScheduleFragmentToBookSeatFragment(movie, theater)
            )
        }
    }

    private fun loadDates(movieId: Int) {
        currentMovieId = movieId
        selectedDate = null
        selectedTime = null
        binding.selectTimeTitleTextView.visibility = View.GONE
        timeAdapter.submitList(emptyList())
        updateConfirmButtonState()

        lifecycleScope.launch {
            val dates = viewModel.getShowtimeDates(movieId)
            dateAdapter.submitList(dates.map { DateItem(it, formatDate(it)) })
        }
    }

    private fun loadTimes(movieId: Int, theaterId: Int, date: String) {
        selectedTime = null
        updateConfirmButtonState()

        lifecycleScope.launch {
            val slots = viewModel.getShowtimeSlots(movieId, theaterId, date)
            binding.selectTimeTitleTextView.visibility =
                if (slots.isEmpty()) View.GONE else View.VISIBLE
            timeAdapter.submitList(slots.map { it.time })
        }
    }

    private fun updateConfirmButtonState() {
        binding.confirmButton.isEnabled = selectedDate != null && selectedTime != null
    }

    private fun formatDate(isoDate: String): String {
        return try {
            val date = LocalDate.parse(isoDate, DateTimeFormatter.ISO_LOCAL_DATE)
            date.format(DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH))
        } catch (_: Exception) {
            isoDate
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class DateItem(val isoDate: String, val label: String)

    class DateGridAdapter(
        private val onSelected: (DateItem) -> Unit
    ) : ListAdapter<DateItem, DateGridAdapter.DateViewHolder>(DateDiffCallback) {

        private var selectedPosition = RecyclerView.NO_ID.toInt()

        object DateDiffCallback : DiffUtil.ItemCallback<DateItem>() {
            override fun areItemsTheSame(oldItem: DateItem, newItem: DateItem) =
                oldItem.isoDate == newItem.isoDate
            override fun areContentsTheSame(oldItem: DateItem, newItem: DateItem) = oldItem == newItem
        }

        override fun submitList(list: List<DateItem>?) {
            selectedPosition = RecyclerView.NO_ID.toInt()
            super.submitList(list)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
            val binding = ItemBookScheduleDateBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return DateViewHolder(binding)
        }

        override fun onBindViewHolder(
            holder: DateViewHolder,
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

        class DateViewHolder(
            private val binding: ItemBookScheduleDateBinding
        ) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: DateItem, isSelected: Boolean) {
                binding.dateButton.text = item.label
                val tint = if (isSelected) R.color.green_accent else R.color.background_secondary
                binding.dateButton.backgroundTintList =
                    binding.root.context.getColorStateList(tint)
                val textColor = if (isSelected) R.color.badge_text else R.color.text_primary
                binding.dateButton.setTextColor(
                    binding.root.context.getColor(textColor)
                )
            }
        }
    }

    class TimeGridAdapter(
        private val onSelected: (String) -> Unit
    ) : ListAdapter<String, TimeGridAdapter.TimeViewHolder>(TimeDiffCallback) {

        private var selectedPosition = RecyclerView.NO_ID.toInt()

        object TimeDiffCallback : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
            override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        }

        override fun submitList(list: List<String>?) {
            selectedPosition = RecyclerView.NO_ID.toInt()
            super.submitList(list)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
            val binding = ItemBookScheduleTimeBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return TimeViewHolder(binding)
        }

        override fun onBindViewHolder(
            holder: TimeViewHolder,
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

        class TimeViewHolder(
            private val binding: ItemBookScheduleTimeBinding
        ) : RecyclerView.ViewHolder(binding.root) {
            fun bind(time: String, isSelected: Boolean) {
                binding.timeButton.text = time
                val tint = if (isSelected) R.color.green_accent else R.color.background_secondary
                binding.timeButton.backgroundTintList =
                    binding.root.context.getColorStateList(tint)
                val textColor = if (isSelected) R.color.badge_text else R.color.text_primary
                binding.timeButton.setTextColor(
                    binding.root.context.getColor(textColor)
                )
            }
        }
    }
}
