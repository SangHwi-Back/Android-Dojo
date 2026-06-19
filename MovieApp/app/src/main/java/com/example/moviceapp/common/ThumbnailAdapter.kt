package com.example.moviceapp.common

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.moviceapp.R
import com.example.moviceapp.databinding.ItemMovieThumbnailCardBinding
import com.example.moviceapp.repo.Movie
import com.example.moviceapp.search.ThumbnailOnClickListener

class ThumbnailAdapter(
    private val fixedWidth: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    private val listener: ThumbnailOnClickListener,
) : ListAdapter<Movie, ThumbnailViewHolder>(MovieDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        val binding = ItemMovieThumbnailCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        // 수평 스크롤일 때만 폭을 고정
        if (fixedWidth != ViewGroup.LayoutParams.MATCH_PARENT) {
            binding.root.layoutParams = binding.root.layoutParams.apply {
                width = fixedWidth
            }
        }
        return ThumbnailViewHolder(binding) {
            listener.onClickMovieFromThumbnail(it)
        }
    }

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) =
        holder.bind(getItem(position))

    private class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Movie, newItem: Movie) = oldItem == newItem
    }
}

class ThumbnailViewHolder(
    private val binding: ItemMovieThumbnailCardBinding,
    private val setOnClickViewListener: (Movie) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: Movie) {
        binding.nameTextView.text = item.title
        binding.pointTextView.text = item.rating.toString()
        binding.movieImageView.load(item.posterURL ?: R.drawable.ic_launcher_background)
        binding.root.setOnClickListener { setOnClickViewListener(item) }
    }
}

class GridSpanDecoration(
    private val spanCount: Int,
    private val spacingPx: Int
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        outRect.left = column * spacingPx / spanCount
        outRect.right = spacingPx - (column + 1) * spacingPx / spanCount
        outRect.bottom = spacingPx
    }
}
