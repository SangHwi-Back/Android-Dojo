package com.example.moviceapp.book.choose.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moviceapp.book.BookChooseInfoViewModel

class BookChooseInformationAdapter(
    private val viewModel: BookChooseInfoViewModel
) : RecyclerView.Adapter<BookChooseViewHolder>() {
    override fun getItemCount(): Int = 3
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BookChooseViewHolder {
        return BookChooseViewHolderFactory.createViewHolder(parent, viewType)
    }
    override fun onBindViewHolder(
        holder: BookChooseViewHolder,
        position: Int
    ) {
        holder.bind(viewModel.model.value)
    }
}

/**
 * class MoviePagerAdapter(
 *     private val movies: List<Movie>,
 * ) : RecyclerView.Adapter<MoviePagerAdapter.MovieViewHolder>() {
 *
 *     override fun getItemCount(): Int = movies.size
 *
 *     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
 *         val binding = ItemBookScheduleMovieInfoBinding.inflate(
 *             LayoutInflater.from(parent.context), parent, false
 *         )
 *         return MovieViewHolder(binding)
 *     }
 *
 *     override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
 *         holder.bind(movies[position])
 *     }
 *
 *     class MovieViewHolder(
 *         private val binding: ItemBookScheduleMovieInfoBinding
 *     ) : RecyclerView.ViewHolder(binding.root) {
 *         fun bind(movie: Movie) {
 *             binding.movieImageView.load(movie.posterURL ?: R.drawable.ic_launcher_background)
 *             binding.movieNameTextView.text = movie.title
 *             binding.movieRuntimeTextView.text = movie.duration
 *             binding.moviePointTextView.text = movie.rating.toString()
 *         }
 *     }
 * }
 *
 */