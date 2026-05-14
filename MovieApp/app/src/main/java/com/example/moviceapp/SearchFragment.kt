package com.example.moviceapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moviceapp.databinding.FragmentSearchBinding

// https://jtm0609.tistory.com/261
class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recentSearchChipRecyclerView
    }

    class SearchRecentChipViewAdapter: RecyclerView.Adapter<SearchRecentChipViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): SearchRecentChipViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val listItem = layoutInflater.inflate(
                R.layout.search_recent_list_item, parent, false)
            return SearchRecentChipViewHolder(listItem)
        }

        override fun onBindViewHolder(
            holder: SearchRecentChipViewHolder,
            position: Int
        ) {
            holder.nameText.text = "Hello!"
        }

        override fun getItemCount(): Int = 1
    }

    class SearchRecentChipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.name_text_view)
    }

    class TrendingNowViewAdapter : RecyclerView.Adapter<TrendingNowViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): TrendingNowViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val listItem = layoutInflater.inflate(
                R.layout.trending_now_list_item, parent, false)
            return TrendingNowViewHolder(listItem)
        }

        override fun onBindViewHolder(
            holder: TrendingNowViewHolder,
            position: Int
        ) {
            holder.nameText.text = "Hello!"
        }

        override fun getItemCount(): Int = 1
    }

    class TrendingNowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.name_text_view)
    }
}