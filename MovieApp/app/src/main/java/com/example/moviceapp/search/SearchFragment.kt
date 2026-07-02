package com.example.moviceapp.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.moviceapp.common.ThumbnailAdapter
import com.example.moviceapp.common.screenWidth
import com.example.moviceapp.databinding.FragmentSearchBinding
import com.example.moviceapp.repo.APIResult
import com.example.moviceapp.repo.Movie
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment: Fragment(), ThumbnailOnClickListener, BrowseOnClickListener {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()

    // ADAPTERS
    lateinit var recentChipAdapter: RecentChipViewAdapter
    lateinit var thumbnailAdapter: ThumbnailAdapter
    lateinit var browseAllViewAdapter: BrowseAllViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fixedWidth = (screenWidth * 0.32).toInt()

        thumbnailAdapter = ThumbnailAdapter(fixedWidth, this) // TRENDING NOW
        browseAllViewAdapter = BrowseAllViewAdapter(this) // BROWSE ALL
        recentChipAdapter = RecentChipViewAdapter().apply {
            submitList(listOf("Action movies", "Sci-rFi 2026", "Comedy"))
        } // RECENT SEARCHES

        binding.recentSearchChipRecyclerView.adapter = recentChipAdapter
        binding.trendingNowRecyclerView.adapter = thumbnailAdapter
        binding.browseAllRecyclerView.adapter = browseAllViewAdapter

        lifecycleScope.launch {
            thumbnailAdapter.submitList(when (val result = viewModel.getFeaturedMovies()) {
                is APIResult.Success<List<Movie>> -> result.data
                else -> emptyList<Movie>()
            })
            browseAllViewAdapter.submitList(when (val result = viewModel.getMovies()) {
                is APIResult.Success<List<Movie>> -> result.data
                else -> emptyList<Movie>()
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClickMovieFromThumbnail(movie: Movie) =
        onClickMovie(movie)

    override fun onClickMovieFromBrowseAll(movie: Movie) =
        onClickMovie(movie)

    private fun onClickMovie(movie: Movie) {
        val modal = MovieBottomSheet.newInstance(movie)
        modal.show(childFragmentManager, MovieBottomSheet.TAG)
    }
}
interface ThumbnailOnClickListener {
    fun onClickMovieFromThumbnail(movie: Movie)
}
interface BrowseOnClickListener {
    fun onClickMovieFromBrowseAll(movie: Movie)
}