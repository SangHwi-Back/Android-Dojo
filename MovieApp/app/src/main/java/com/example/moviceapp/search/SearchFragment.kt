package com.example.moviceapp.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.moviceapp.ScreenAttribute
import com.example.moviceapp.databinding.FragmentSearchBinding
import com.example.moviceapp.repo.Movie
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment: Fragment(), ThumbnailOnClickListener, BrowseOnClickListener {
    @Inject
    lateinit var screenAttribute: ScreenAttribute
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var adapter: SearchContentsViewPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        adapter = SearchContentsViewPagerAdapter(screenAttribute)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.searchContentsViewpager.adapter = adapter
        binding.searchContentsViewpager.isUserInputEnabled = false
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (text.isNullOrEmpty())
                    viewModel.showCategorizedMovieScreen()
                else
                    viewModel.showQueryResultScreen(text.toString())
            }
        })

        viewModel.showCategorizedMovieScreen()

        lifecycleScope.launch {
            viewModel.currentScreen.collect { currentScreen ->
                adapter.setCurrentScreenEntity(currentScreen)
            }
        }
        lifecycleScope.launch {
            viewModel.refreshCategorizedScreen()
            viewModel.showCategorizedMovieScreen()
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

class SearchContentsViewPagerAdapter(val screenAttribute: ScreenAttribute): RecyclerView.Adapter<SearchViewHolder>() {
    private var currentScreenEntity: SearchFragmentEntity =
        SearchFragmentEntity.CategorizedMovie(listOf(), listOf())
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchViewHolder {
        val entity = currentScreenEntity
        return SearchViewHolderFactory.createViewHolder(parent, screenAttribute, when (entity) {
            is SearchFragmentEntity.CategorizedMovie ->
                SearchViewHolderEntity.CategorizedMovieEntity(entity.trending, entity.browseAll)
            is SearchFragmentEntity.QueryResultMovie ->
                SearchViewHolderEntity.QueryResultEntity(entity.movies)
        })
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val entity = when (val e = currentScreenEntity) {
            is SearchFragmentEntity.CategorizedMovie ->
                SearchViewHolderEntity.CategorizedMovieEntity(e.trending, e.browseAll)
            is SearchFragmentEntity.QueryResultMovie ->
                SearchViewHolderEntity.QueryResultEntity(e.movies)
        }
        holder.bind(entity)
    }

    override fun getItemCount(): Int = 1

    override fun getItemViewType(position: Int): Int =
        if (currentScreenEntity is SearchFragmentEntity.QueryResultMovie) 1 else 0

    fun setCurrentScreenEntity(entity: SearchFragmentEntity) {
        currentScreenEntity = entity
        notifyDataSetChanged()
    }
}

sealed class SearchFragmentEntity {
    data class CategorizedMovie(val trending: List<Movie>, val browseAll: List<Movie>) : SearchFragmentEntity()
    data class QueryResultMovie(val movies: List<Movie>) : SearchFragmentEntity()
}
interface ThumbnailOnClickListener {
    fun onClickMovieFromThumbnail(movie: Movie)
}
interface BrowseOnClickListener {
    fun onClickMovieFromBrowseAll(movie: Movie)
}