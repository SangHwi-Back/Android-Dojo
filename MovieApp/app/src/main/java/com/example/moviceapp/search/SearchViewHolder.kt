package com.example.moviceapp.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.moviceapp.ScreenAttribute
import com.example.moviceapp.databinding.ItemSearchCategorizedMovieBinding
import com.example.moviceapp.databinding.ItemSearchQueryResultBinding
import com.example.moviceapp.repo.Movie
import com.example.moviceapp.search.category.SearchCategorizedMovieViewHolder
import kotlin.reflect.KClass

abstract class SearchViewHolder(
    binding: ViewBinding
) : RecyclerView.ViewHolder(binding.root) {
    abstract fun bind(entity: SearchViewHolderEntity)
}

object SearchViewHolderFactory {
    fun createViewHolder(
        parent: ViewGroup,
        screenAttribute: ScreenAttribute,
        entity: KClass<out SearchFragmentEntity>,
    ): SearchViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (entity) {
            SearchFragmentEntity.CategorizedMovie::class -> SearchCategorizedMovieViewHolder(parent,
                ItemSearchCategorizedMovieBinding.inflate(inflater, parent, false), screenAttribute)
            else -> SearchQueryResultViewHolder(parent,
                ItemSearchQueryResultBinding.inflate(inflater, parent, false)
            )
        }
    }
}

sealed class SearchViewHolderEntity {
    data class CategorizedMovieEntity(
        var trendingMovies: List<Movie>? = null,
        var browseAllMovies: List<Movie>,
    ) : SearchViewHolderEntity()
    data class QueryResultEntity(
        val queryResults: List<Movie>? = null,
        var page: Int = 0,
    ) : SearchViewHolderEntity()
}
