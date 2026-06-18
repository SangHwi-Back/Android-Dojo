package com.example.moviceapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.moviceapp.databinding.FragmentSearchBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment: Fragment(), ThumbnailOnClickListener, BrowseOnClickListener {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    @Inject lateinit var service: MovieService
    lateinit var viewModel: SearchViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        viewModel = SearchViewModel(service)
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