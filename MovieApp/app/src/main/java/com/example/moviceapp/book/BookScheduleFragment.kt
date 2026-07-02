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
    private val dateAdapter = DateGridAdapter()
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
        lifecycleScope.launch {
            val dates = viewModel.getShowtimeDates(movieId)
            currentMovieId = movieId
            dateAdapter.submitList(dates.map { formatDate(it) })
        }
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

    class DateGridAdapter : ListAdapter<String, DateGridAdapter.DateViewHolder>(DateDiffCallback) {

        private var selectedPosition = RecyclerView.NO_ID.toInt()

        object DateDiffCallback : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
            override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
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
            }
        }

        class DateViewHolder(
            private val binding: ItemBookScheduleDateBinding
        ) : RecyclerView.ViewHolder(binding.root) {
            fun bind(label: String, isSelected: Boolean) {
                binding.dateButton.text = label
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
}
