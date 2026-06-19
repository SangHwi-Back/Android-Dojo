package com.example.moviceapp.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.moviceapp.search.RecentChipViewAdapter
import com.example.moviceapp.common.ThumbnailAdapter
import com.example.moviceapp.databinding.FragmentSearchBinding
import com.example.moviceapp.repo.Movie
import com.example.moviceapp.repo.MoviesMock
import com.example.moviceapp.common.screenWidth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment: Fragment(), ThumbnailOnClickListener, BrowseOnClickListener {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        RECENT SEARCHES
        binding.recentSearchChipRecyclerView.adapter = RecentChipViewAdapter().apply {
            submitList(listOf("Action movies", "Sci-rFi 2026", "Comedy"))
        }
//        TRENDING NOW
        val fixedWidth = (screenWidth * 0.32).toInt()
        binding.trendingNowRecyclerView.adapter = ThumbnailAdapter(fixedWidth, this).apply {
            this.submitList(MoviesMock.all)
        }
        lifecycleScope.launch {
            val movies = viewModel.getMovies()
            print("size of movies : ${movies.size}")
        }
//        BROWSE ALL
        binding.browseAllRecyclerView.adapter = BrowseAllViewAdapter(this).apply {
            submitList(MoviesMock.all)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClickMovieFromThumbnail(movie: Movie) {
        TODO("Not yet implemented")
    }

    override fun onClickMovieFromBrowseAll(movie: Movie) {
        val modal = MovieBottomSheet.Companion.newInstance(movie)
        modal.show(childFragmentManager, MovieBottomSheet.Companion.TAG)
    }
}
interface ThumbnailOnClickListener {
    fun onClickMovieFromThumbnail(movie: Movie)
}
interface BrowseOnClickListener {
    fun onClickMovieFromBrowseAll(movie: Movie)
}