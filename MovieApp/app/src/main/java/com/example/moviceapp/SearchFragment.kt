package com.example.moviceapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.moviceapp.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

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
        binding.trendingNowRecyclerView.adapter = ThumbnailAdapter(
            (screenWidth * 0.32).toInt()) {

        }.apply {
            this.submitList(MoviesMock.all)
        }
//        BROWSE ALL
        binding.browseAllRecyclerView.adapter = BrowseAllViewAdapter { modalBottomSheet(it) }.apply {
            submitList(MoviesMock.all)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun modalBottomSheet(movie: Movie) {
        val modal = MovieBottomSheet.newInstance(movie)
        modal.show(childFragmentManager, MovieBottomSheet.TAG)
    }
}