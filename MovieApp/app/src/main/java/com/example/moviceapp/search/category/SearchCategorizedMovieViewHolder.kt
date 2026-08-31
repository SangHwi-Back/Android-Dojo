package com.example.moviceapp.search.category

import android.view.ViewGroup
import androidx.fragment.app.findFragment
import com.example.moviceapp.ScreenAttribute
import com.example.moviceapp.common.ThumbnailAdapter
import com.example.moviceapp.databinding.ItemSearchCategorizedMovieBinding
import com.example.moviceapp.search.BrowseAllViewAdapter
import com.example.moviceapp.search.RecentChipViewAdapter
import com.example.moviceapp.search.SearchFragment
import com.example.moviceapp.search.SearchViewHolder
import com.example.moviceapp.search.SearchViewHolderEntity

class SearchCategorizedMovieViewHolder (
    val parent: ViewGroup,
    val binding: ItemSearchCategorizedMovieBinding,
    screenAttribute: ScreenAttribute,
) : SearchViewHolder(binding) {// ADAPTERS
    private var recentChipAdapter: RecentChipViewAdapter
    private var thumbnailAdapter: ThumbnailAdapter
    private var browseAllViewAdapter: BrowseAllViewAdapter

    init {
        val fixedWidth = (screenAttribute.screenWidth * 0.32).toInt()
        val fragment = parent.findFragment<SearchFragment>()
        thumbnailAdapter = ThumbnailAdapter(fixedWidth, fragment) // TRENDING NOW
        browseAllViewAdapter = BrowseAllViewAdapter(fragment) // BROWSE ALL
        recentChipAdapter = RecentChipViewAdapter().apply {
            submitList(listOf("Action movies", "Sci-rFi 2026", "Comedy"))
        } // RECENT SEARCHES
        binding.recentSearchChipRecyclerView.adapter = recentChipAdapter
        binding.trendingNowRecyclerView.adapter = thumbnailAdapter
        binding.browseAllRecyclerView.adapter = browseAllViewAdapter
    }

    override fun bind(entity: SearchViewHolderEntity) {
        when (entity) {
            is SearchViewHolderEntity.CategorizedMovieEntity -> {
                thumbnailAdapter.submitList(entity.trendingMovies)
                browseAllViewAdapter.submitList(entity.browseAllMovies)
            }
            else -> return
        }
    }
}