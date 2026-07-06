package com.example.moviceapp.book

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.moviceapp.R
import com.example.moviceapp.databinding.FragmentBookTheaterBinding
import com.example.moviceapp.databinding.ItemBookTheaterSelectTheaterBinding
import com.example.moviceapp.repo.Movie
import com.example.moviceapp.repo.Theater
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookTheaterFragment : Fragment() {
//    private val args: BookTheaterFragmentArgs by navArgs()
    private val viewModel: BookTheaterViewModel by viewModels()
    private var _binding: FragmentBookTheaterBinding? = null
    private val binding get() = _binding!!
    private lateinit var selectedMovie: Movie
    private var selectedTheater: Theater? = null
    private val theaterAdapter = TheaterListAdapter { theater ->
        selectedTheater = theater
        binding.nextButton.isEnabled = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookTheaterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val movies = args.movies.toList()
//        this.selectedMovie = args.selectedMovie

//        binding.movieViewPager.adapter = MoviePagerAdapter(movies)
//        binding.movieViewPager.setCurrentItem(args.movies.indexOf(selectedMovie), false)
        binding.movieViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
//                selectedMovie = args.movies[position]
                loadTheaters()
            }
        })

        binding.theaterRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.theaterRecyclerView.adapter = theaterAdapter

//        if (movies.isNotEmpty()) loadTheaters()

        binding.nextButton.setOnClickListener {
            val theater = selectedTheater ?: return@setOnClickListener
//            findNavController().navigate(
//                BookTheaterFragmentDirections.actionBookTheaterFragmentToBookScheduleFragment(
//                    args.movies, theater, selectedMovie
//                )
//            )
        }
    }

    private fun loadTheaters() {
        selectedTheater = null
        binding.nextButton.isEnabled = false
        lifecycleScope.launch {
            val theaters = viewModel.getTheaters(selectedMovie.id)
            theaterAdapter.submitList(theaters)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class TheaterListAdapter(
        private val onSelected: (Theater) -> Unit
    ) : ListAdapter<Theater, TheaterViewHolder>(TheaterDiffCallback) {

        private var selectedPosition = RecyclerView.NO_ID.toInt()

        object TheaterDiffCallback : DiffUtil.ItemCallback<Theater>() {
            override fun areItemsTheSame(oldItem: Theater, newItem: Theater) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Theater, newItem: Theater) =
                oldItem == newItem
        }

        override fun submitList(list: List<Theater>?) {
            selectedPosition = RecyclerView.NO_ID.toInt()
            super.submitList(list)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TheaterViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return TheaterViewHolder(
                ItemBookTheaterSelectTheaterBinding.inflate(inflater, parent, false)
            )
        }

        override fun onBindViewHolder(
            holder: TheaterViewHolder,
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
    }

    class TheaterViewHolder(
        private val binding: ItemBookTheaterSelectTheaterBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(theater: Theater, isSelected: Boolean) {
            binding.nameTextView.text = theater.name
            binding.addressTextView.text = theater.address
            binding.distanceTextView.text =
                binding.root.context.getString(R.string.label_distance_format, theater.distanceKm)

            val bgColor = if (isSelected) R.color.green_accent else R.color.surface_card
            binding.root.setBackgroundColor(
                ContextCompat.getColor(binding.root.context, bgColor)
            )
        }
    }
}
