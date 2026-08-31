package com.example.moviceapp.search

import android.view.ViewGroup
import com.example.moviceapp.databinding.ItemSearchQueryResultBinding
import com.example.moviceapp.search.query.SearchQueryListAdapter

class SearchQueryResultViewHolder(
    val parent: ViewGroup,
    val binding: ItemSearchQueryResultBinding,
) : SearchViewHolder(binding) {
    private val adapter = SearchQueryListAdapter()

    init {
        binding.queryResultRecyclerView.adapter = adapter
    }

    override fun bind(entity: SearchViewHolderEntity) {
        if (entity !is SearchViewHolderEntity.QueryResultEntity) return
        "24 results for \"${entity.queryResults?.size ?: 0}\"".also {
            binding.queryResultTextView.text = it
        }
        adapter.submitList(entity.queryResults)
    }
}