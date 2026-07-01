package com.example.moviceapp.book

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.moviceapp.R
import com.example.moviceapp.databinding.ItemBookScheduleMovieInfoBinding
import com.example.moviceapp.repo.Movie

class MoviePagerAdapter(
    private val movies: List<Movie>
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

/**
 *  4121* curl -o actions-runner-osx-x64-2.335.1.tar.gz -L https://github.com/actions/runner/releases/download/v2.335.1/actions-runner-osx-x64-2.335.1.tar.gz
 *  4122* echo "b2fe57b2ae5b0bc1605f9fc0723c07eedf06167321d3478ce0440f15e5b0a010  actions-runner-osx-x64-2.335.1.tar.gz" | shasum -a 256 -c
 *  4123* tar xzf ./actions-runner-osx-x64-2.335.1.tar.gz
 *  4124* ./config.sh --url https://github.com/SangHwi-Back/Android-Dojo --token APXAQSB3YPCBD5ZJ2OPKJULKIUT7E
 *  4125* ./run.sh
 */
