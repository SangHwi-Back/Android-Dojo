package com.example.moviceapp.book

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.moviceapp.R
import com.example.moviceapp.databinding.ItemBookScheduleMovieInfoBinding
import com.example.moviceapp.repo.Movie

class MoviePagerAdapter(
    private val movies: List<Movie>,
) : RecyclerView.Adapter<MoviePagerAdapter.MovieViewHolder>() {

    override fun getItemCount(): Int = movies.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemBookScheduleMovieInfoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    class MovieViewHolder(
        private val binding: ItemBookScheduleMovieInfoBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            binding.movieImageView.load(movie.posterURL ?: R.drawable.ic_launcher_background)
            binding.movieNameTextView.text = movie.title
            binding.movieRuntimeTextView.text = movie.duration
            binding.moviePointTextView.text = movie.rating.toString()
        }
    }
}
