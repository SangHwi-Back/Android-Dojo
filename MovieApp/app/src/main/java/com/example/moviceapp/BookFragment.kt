package com.example.moviceapp

import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.moviceapp.SearchFragment.BrowseAllViewHolder
import com.example.moviceapp.databinding.FragmentBookBinding

class BookFragment : Fragment() {
    private lateinit var binding: FragmentBookBinding
    private val movies = MoviesMock.all
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookBinding.inflate(inflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.width
        binding.bookRecyclerGridView.apply {
            adapter = MovieViewAdapter(movies)
        }
    }
    class MovieViewAdapter(
        val movies: List<Movie>
    ): RecyclerView.Adapter<MovieViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MovieViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val listItem = layoutInflater.inflate(
                R.layout.trending_now_list_item, parent, false)
            listItem.layoutParams.width = (DisplayMetrics().widthPixels - 16) / 3
            return MovieViewHolder(listItem)
        }
        override fun onBindViewHolder(
            holder: MovieViewHolder,
            position: Int
        ) = holder.setMovie(movies[position])
        override fun getItemCount(): Int = movies.size
    }
    class MovieViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun setMovie(movie: Movie) {
            itemView.findViewById<ImageView>(R.id.movie_image_view)
                .load(movie.posterRes ?: R.drawable.ic_launcher_background)
            itemView.findViewById<TextView>(R.id.name_text_view)
                .text = movie.title
            itemView.findViewById<TextView>(R.id.point_text_view)
                .text = movie.rating.toString()
        }
    }
}