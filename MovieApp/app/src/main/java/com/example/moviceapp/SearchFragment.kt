package com.example.moviceapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.moviceapp.databinding.FragmentSearchBinding

// https://jtm0609.tistory.com/261
class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private val bookingUpcoming = BookingsMock.upcoming
    private val bookingPast = BookingsMock.past
    private val recentSearches = listOf("Action movies", "Sci-Fi 2026", "Comedy")
    private val movies = MoviesMock.all

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
        binding.recentSearchChipRecyclerView.apply {
            layoutManager = LinearLayoutManager(view.context).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = RecentChipViewAdapter(recentSearches)
        }
//        TRENDING NOW
        binding.trendingNowRecyclerView.apply {
            layoutManager = LinearLayoutManager(view.context).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = TrendingNowViewAdapter(bookingUpcoming + bookingPast)
        }
//        BROWSE ALL
        binding.browseAllRecyclerView.apply {
            layoutManager = LinearLayoutManager(view.context).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = BrowseAllViewAdapter(movies)
        }
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
    class TrendingNowViewAdapter(
        val bookings: List<Booking>,
    ) : RecyclerView.Adapter<TrendingNowViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): TrendingNowViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val listItem = layoutInflater.inflate(
                R.layout.trending_now_list_item, parent, false)
            return TrendingNowViewHolder(listItem)
        }
        override fun onBindViewHolder(
            holder: TrendingNowViewHolder,
            position: Int
        ) = holder.setBooking(bookings[position])
        override fun getItemCount(): Int = bookings.size
    }
    class TrendingNowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setBooking(booking: Booking) {
            itemView.findViewById<TextView>(R.id.name_text_view)
                .text = booking.movie.title
            itemView.findViewById<TextView>(R.id.point_text_view)
                .text = booking.movie.rating.toString()
            itemView.findViewById<ImageView>(R.id.movie_image_view)
                .load(booking.movie.posterRes ?: R.drawable.ic_launcher_background)
        }
    }
    class BrowseAllViewAdapter(
        val movies: List<Movie>,
    ) : RecyclerView.Adapter<BrowseAllViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): BrowseAllViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val listItem = layoutInflater.inflate(
                R.layout.browse_all_list_item, parent, false)
            return BrowseAllViewHolder(listItem)
        }
        override fun onBindViewHolder(
            holder: BrowseAllViewHolder,
            position: Int
        ) = holder.setMovie(movies[position])
        override fun getItemCount(): Int = movies.size
    }
    class BrowseAllViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setMovie(movie: Movie) {
            itemView.findViewById<ImageView>(R.id.movie_image_view)
                .load(movie.posterRes ?: R.drawable.ic_launcher_background)
            itemView.findViewById<TextView>(R.id.name_text_view)
                .text = movie.title
            itemView.findViewById<TextView>(R.id.point_text_view)
                .text = movie.rating.toString()
            itemView.findViewById<TextView>(R.id.play_text_view)
                .text = movie.duration
        }
    }
}