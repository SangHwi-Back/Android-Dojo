package com.example.moviceapp.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.moviceapp.R
import com.example.moviceapp.databinding.ItemBrowseAllListBinding
import com.example.moviceapp.repo.Movie

class BrowseAllViewAdapter(
    val listener: BrowseOnClickListener,
) : ListAdapter<Movie, BrowseAllViewAdapter.BrowseAllViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean =
            oldItem.title == newItem.title
        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowseAllViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemBrowseAllListBinding.inflate(layoutInflater, parent, false)
        return BrowseAllViewHolder(binding) { listener.onClickMovieFromBrowseAll(it) }
    }

    override fun onBindViewHolder(holder: BrowseAllViewHolder, position: Int) =
        holder.bind(getItem(position))

    class BrowseAllViewHolder(
        val binding: ItemBrowseAllListBinding,
        val onClickListener: (Movie) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            binding.movieImageView.load(
                movie.posterURL ?: R.drawable.ic_launcher_background)
            binding.nameTextView.text = movie.title
            binding.pointTextView.text = movie.rating.toString()
            binding.playTextView.text = movie.duration
            binding.root.setOnClickListener { onClickListener(movie) }
        }
    }
}