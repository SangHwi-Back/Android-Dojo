package com.example.moviceapp.search.query

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.moviceapp.R
import com.example.moviceapp.databinding.ItemMovieThumbnailCardBinding
import com.example.moviceapp.repo.Movie

class SearchQueryListAdapter : ListAdapter<Movie, SearchQueryListAdapter.SearchQueryViewHolder>(QueryResultDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchQueryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return SearchQueryViewHolder(ItemMovieThumbnailCardBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(
        holder: SearchQueryViewHolder,
        position: Int
    ) = holder.bind(getItem(position))

    class SearchQueryViewHolder(val binding: ItemMovieThumbnailCardBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            binding.nameTextView.text = movie.title
            binding.movieImageView.load(movie.posterUrl ?: R.drawable.ic_launcher_background)
            binding.pointTextView.text = movie.rating.toString()
        }
    }

    private class QueryResultDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean = oldItem == newItem
    }
}