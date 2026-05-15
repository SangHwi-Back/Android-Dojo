package com.example.moviceapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.moviceapp.databinding.FragmentSearchBinding

// https://jtm0609.tistory.com/261
class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        RECENT SEARCHES
        binding.recentSearchChipRecyclerView.adapter = RecentChipViewAdapter(
            listOf("Action movies", "Sci-rFi 2026", "Comedy"))
//        TRENDING NOW
        binding.trendingNowRecyclerView.adapter = ThumbnailAdapter(
            (screenWidth * (2.toFloat()/3.toFloat())).toInt()).apply {
                this.submitList(MoviesMock.all)
            }
//        BROWSE ALL
        binding.browseAllRecyclerView.adapter = BrowseAllViewAdapter(
            MoviesMock.all, { modalBottomSheet(it) })
    }

    class RecentChipViewAdapter(
        val recentSearches: List<String>
    ): RecyclerView.Adapter<SearchRecentChipViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): SearchRecentChipViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val listItem = layoutInflater.inflate(
                R.layout.search_recent_list_item, parent, false)
            return SearchRecentChipViewHolder(listItem)
        }
        override fun onBindViewHolder(
            holder: SearchRecentChipViewHolder,
            position: Int
        ) {
            holder.nameText.text = recentSearches[position]
        }
        override fun getItemCount(): Int = recentSearches.size
    }
    class SearchRecentChipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.name_text_view)
    }
    class BrowseAllViewAdapter(
        val movies: List<Movie>,
        val onClickListener: (Movie) -> Unit,
    ) : RecyclerView.Adapter<BrowseAllViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): BrowseAllViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val listItem = layoutInflater.inflate(
                R.layout.browse_all_list_item, parent, false)
            return BrowseAllViewHolder(listItem, onClickListener)
        }
        override fun onBindViewHolder(
            holder: BrowseAllViewHolder,
            position: Int
        ) = holder.bind(movies[position])
        override fun getItemCount(): Int = movies.size
    }
    class BrowseAllViewHolder(itemView: View, val onClickListener: (Movie) -> Unit) : RecyclerView.ViewHolder(itemView) {
        fun bind(movie: Movie) {
            itemView.findViewById<ImageView>(R.id.movie_image_view)
                .load(movie.posterRes ?: R.drawable.ic_launcher_background)
            itemView.findViewById<TextView>(R.id.name_text_view)
                .text = movie.title
            itemView.findViewById<TextView>(R.id.point_text_view)
                .text = movie.rating.toString()
            itemView.findViewById<TextView>(R.id.play_text_view)
                .text = movie.duration
            itemView.setOnClickListener {
                onClickListener(movie)
            }
        }
    }
    private fun modalBottomSheet(movie: Movie) {
        val modal = MovieBottomSheet(movie)
        modal.show(childFragmentManager, MovieBottomSheet.TAG)
    }
}