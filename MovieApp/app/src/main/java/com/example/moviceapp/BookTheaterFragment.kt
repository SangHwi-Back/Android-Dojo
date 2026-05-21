package com.example.moviceapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.moviceapp.databinding.FragmentBookTheaterBinding
import com.example.moviceapp.databinding.ItemBookTheaterSelectTheaterBinding

class BookTheaterFragment : Fragment() {
    val args: BookTheaterFragmentArgs by navArgs()
    val adapter: RecyclerViewAdapter = RecyclerViewAdapter()
    lateinit var binding: FragmentBookTheaterBinding
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookTheaterBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.movieImageView.load(args.movie.posterRes ?: R.drawable.ic_launcher_background)
        binding.movieNameTextView.text = args.movie.title
        binding.movieRuntimeTextView.text = args.movie.duration
        binding.moviePointTextView.text = args.movie.rating.toString()

        binding.theaterRecyclerView.layoutManager = LinearLayoutManager(
            requireActivity(),
            LinearLayoutManager.VERTICAL,
            false)
        binding.theaterRecyclerView.adapter = adapter
        adapter.setItems(TheatersMock.list)
    }

    class RecyclerViewAdapter: RecyclerView.Adapter<TheaterViewHolder>() {
        private var items = mutableListOf<Theater>()
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): TheaterViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return TheaterViewHolder(ItemBookTheaterSelectTheaterBinding.inflate(inflater))
        }
        override fun onBindViewHolder(
            holder: TheaterViewHolder,
            position: Int
        ) {
            holder.bind(items[position])
        }
        override fun getItemCount(): Int = items.size
        fun setItems(items: List<Theater>) {
            this.items = items.toMutableList()
            notifyDataSetChanged()
        }
    }
    class TheaterViewHolder(
        val binding: ItemBookTheaterSelectTheaterBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(theater: Theater) {
            binding.nameTextView.text = theater.name
            binding.addressTextView.text = theater.address
            binding.distanceTextView.text = theater.distanceKm.toString()
        }
    }
}