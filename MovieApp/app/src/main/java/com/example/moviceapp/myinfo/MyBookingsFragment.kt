package com.example.moviceapp.myinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.moviceapp.R
import com.example.moviceapp.databinding.FragmentMyBookingsBinding
import com.example.moviceapp.databinding.ItemMyBookingsMovieBinding
import com.example.moviceapp.repo.Booking
import kotlinx.coroutines.launch

class MyBookingsFragment : Fragment() {
    private var _binding: FragmentMyBookingsBinding? = null
    val binding get() = _binding!!

    private lateinit var myBookingsAdapter: MyBookingsAdapter
    private val viewModel: MyInfoViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyBookingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myBookingsAdapter = MyBookingsAdapter()
        binding.myBookingsRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        binding.myBookingsRecyclerView.adapter = myBookingsAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.myBookings.collect {
                myBookingsAdapter.submitList(it)
            }
        }
    }
}

class MyBookingsAdapter : ListAdapter<Booking, MyBookingsAdapter.MyBookingsViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBookingsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemMyBookingsMovieBinding.inflate(layoutInflater, parent, false)
        return MyBookingsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyBookingsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object DiffCallback : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(oldItem: Booking, newItem: Booking): Boolean =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Booking, newItem: Booking): Boolean =
            oldItem == newItem
    }

    class MyBookingsViewHolder(val binding: ItemMyBookingsMovieBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(booking: Booking) {
            binding.movieImageView.load(
                booking.movie.posterUrl ?: R.drawable.ic_launcher_background)
            binding.nameTextView.text = booking.movie.title
            binding.theaterTextView.text = booking.theater.name
            (booking.date + booking.time).also { binding.showTimeTextView.text = it }
            binding.seatTextView.text = booking.seats.toString()
        }
    }
}