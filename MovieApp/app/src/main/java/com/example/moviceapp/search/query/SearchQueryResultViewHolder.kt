package com.example.moviceapp.search

import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.moviceapp.common.GridSpanDecoration
import com.example.moviceapp.databinding.ItemSearchQueryResultBinding
import com.example.moviceapp.search.query.SearchQueryListAdapter

class SearchQueryResultViewHolder(
    val parent: ViewGroup,
    val binding: ItemSearchQueryResultBinding,
) : SearchViewHolder(binding) {
    private val adapter = SearchQueryListAdapter()

    init {
        binding.queryResultRecyclerView.layoutManager =
            GridLayoutManager(parent.context, 3)
        binding.queryResultRecyclerView.addItemDecoration(
            GridSpanDecoration(2, 8))
        binding.queryResultRecyclerView.adapter = adapter
    }

    override fun bind(entity: SearchViewHolderEntity) {
        if (entity !is SearchViewHolderEntity.QueryResultEntity) return
        "24 results for \"${entity.queryResults?.size ?: 0}\"".also {
            binding.queryResultTextView.text = it }
        adapter.submitList(entity.queryResults)
    }
}