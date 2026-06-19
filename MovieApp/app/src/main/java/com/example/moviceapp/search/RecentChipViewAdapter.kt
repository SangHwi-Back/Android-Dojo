package com.example.moviceapp.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.moviceapp.R

class RecentChipViewAdapter : ListAdapter<String, RecentChipViewAdapter.SearchRecentChipViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchRecentChipViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView = layoutInflater.inflate(R.layout.search_recent_list_item, parent, false)
        return SearchRecentChipViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SearchRecentChipViewHolder, position: Int) {
        holder.nameText.text = getItem(position)
    }

    class SearchRecentChipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.number_text_view)
    }
}