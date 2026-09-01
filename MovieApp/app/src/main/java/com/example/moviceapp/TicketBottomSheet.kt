package com.example.moviceapp

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.moviceapp.databinding.ItemBookChooseItemSeatBinding
import com.example.moviceapp.databinding.TicketBottomSheetModalBinding
import com.example.moviceapp.repo.Booking
import com.example.moviceapp.repo.SeatSlot
import com.example.moviceapp.repo.Ticket
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TicketBottomSheet : BottomSheetDialogFragment() {
    private var bookingId: Int = -1
    private val viewModel: TicketBottomSheetViewModel by viewModels()
    private var _binding: TicketBottomSheetModalBinding? = null
    private val adapter = TicketBottomSheetSeatsAdapter()
    val binding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookingId = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getInt(ARG_BOOKING_ID)
        } else {
            @Suppress("DEPRECATION")
            requireArguments().getInt(ARG_BOOKING_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TicketBottomSheetModalBinding.inflate(inflater, container, false)
        return binding.root.apply {
            lifecycleScope.launch {
                viewModel.ticket.collect { bind(it) }
            }
            lifecycleScope.launch {
                viewModel.getTicketByBookingId(bookingId)
            }
        }
    }

    private fun bind(ticket: Ticket?) {
        if (ticket == null) return
        binding.movieNameTextView.text = ticket.booking.movie.title
        binding.theaterNameTextView.text = ticket.booking.theater.name
        binding.seatsRecyclerView.adapter = adapter
        adapter.submitList(ticket.booking.seats)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val TAG = "TicketBottomSheet"
        private const val ARG_BOOKING_ID = "booking_id"
        fun newInstance(booking: Booking) = TicketBottomSheet().apply {
            arguments = Bundle().apply { putInt(ARG_BOOKING_ID, booking.id) }
        }
    }
}

private class TicketBottomSheetSeatsAdapter: ListAdapter<SeatSlot, TicketBottomSheetSeatsAdapter.SeatViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return SeatViewHolder(ItemBookChooseItemSeatBinding.inflate(inflater, parent, false))
    }
    override fun onBindViewHolder(holder: SeatViewHolder, position: Int) = holder.bind(getItem(position))
    class SeatViewHolder(val binding: ItemBookChooseItemSeatBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(seatSlot: SeatSlot) {
            binding.seatNumber.text = seatSlot.name
        }
    }
    object DiffCallback: DiffUtil.ItemCallback<SeatSlot>() {
        override fun areItemsTheSame(oldItem: SeatSlot, newItem: SeatSlot): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: SeatSlot, newItem: SeatSlot): Boolean = oldItem.id == newItem.id
    }
}