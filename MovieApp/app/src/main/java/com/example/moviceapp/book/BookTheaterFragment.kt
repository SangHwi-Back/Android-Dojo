package com.example.moviceapp.book

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.moviceapp.R
import com.example.moviceapp.databinding.FragmentBookTheaterBinding
import com.example.moviceapp.databinding.ItemBookTheaterSelectTheaterBinding
import com.example.moviceapp.repo.Theater
import com.example.moviceapp.repo.TheatersMock

class BookTheaterFragment : Fragment() {
    val args: BookTheaterFragmentArgs by navArgs()
    val adapter: TheaterListAdapter = TheaterListAdapter()
    private var _binding: FragmentBookTheaterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookTheaterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.movieImageView.load(args.movie.posterURL ?: R.drawable.ic_launcher_background)
        binding.movieNameTextView.text = args.movie.title
        binding.movieRuntimeTextView.text = args.movie.duration
        binding.moviePointTextView.text = args.movie.rating.toString()

        binding.theaterRecyclerView.layoutManager = LinearLayoutManager(
            requireActivity(),
            LinearLayoutManager.VERTICAL,
            false)
        adapter.onItemTouchListener = { theater ->
            val action = BookTheaterFragmentDirections.Companion
                .actionBookTheaterFragmentToBookScheduleFragment(args.movie, theater)
            findNavController().navigate(action)
        }

        binding.theaterRecyclerView.adapter = adapter
        adapter.submitList(TheatersMock.list)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class TheaterListAdapter : ListAdapter<Theater, TheaterViewHolder>(TheaterDiffCallback) {
        var onItemTouchListener: ((Theater) -> Unit)? = null

        object TheaterDiffCallback : DiffUtil.ItemCallback<Theater>() {
            override fun areItemsTheSame(oldItem: Theater, newItem: Theater): Boolean =
                oldItem.name == newItem.name
            override fun areContentsTheSame(oldItem: Theater, newItem: Theater): Boolean =
                oldItem == newItem
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TheaterViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return TheaterViewHolder(
                ItemBookTheaterSelectTheaterBinding.inflate(inflater, parent, false)
            )
        }

        override fun onBindViewHolder(holder: TheaterViewHolder, position: Int) {
            val theater = getItem(position)
            holder.bind(theater)
            holder.itemView.setOnClickListener {
                onItemTouchListener?.invoke(theater)
            }
        }
    }

    class TheaterViewHolder(
        val binding: ItemBookTheaterSelectTheaterBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(theater: Theater) {
            binding.nameTextView.text = theater.name
            binding.addressTextView.text = theater.address
            binding.distanceTextView.text =
                binding.root.context.getString(R.string.label_distance_format, theater.distanceKm)
        }
    }
}