package com.example.moviceapp.search

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.moviceapp.R
import com.example.moviceapp.databinding.BottomSheetModalBinding
import com.example.moviceapp.databinding.SearchRecentListItemBinding
import com.example.moviceapp.repo.Movie
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MovieBottomSheet(
    private val genres: List<String>
) : BottomSheetDialogFragment() {
    private var _binding: BottomSheetModalBinding? = null
    private val binding get() = _binding!!
    lateinit var adapter: MovieBottomSheetChipListAdapter

    private lateinit var movie: Movie

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        movie = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable(ARG_MOVIE, Movie::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            requireArguments().getParcelable(ARG_MOVIE)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetModalBinding.inflate(inflater, container, false)
        adapter = MovieBottomSheetChipListAdapter()
        binding.categoryChipRecyclerView.adapter = adapter
        adapter.submitList(genres)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.movieImageView.load(movie.backdropURL ?: R.drawable.ic_launcher_background)
        binding.nameTextView.text = movie.title
        binding.ratingTextView.text = movie.rating.toString()
        binding.durationTextView.text = movie.duration
        binding.releaseDateTextView.text = movie.releaseDate
        binding.descriptionTextView.text = movie.description
        binding.closeButton.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "MovieBottomSheet"
        private const val ARG_MOVIE = "movie"
        fun newInstance(movie: Movie) = MovieBottomSheet(movie.genres).apply {
            arguments = Bundle().apply { putParcelable(ARG_MOVIE, movie) }
        }
    }
}

class MovieBottomSheetChipListAdapter: ListAdapter<String, MovieBottomSheetChipListAdapter.ViewHodler>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewTYpe: Int): ViewHodler {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = SearchRecentListItemBinding.inflate(layoutInflater, parent, false)
        return ViewHodler(binding)
    }

    override fun onBindViewHolder(holder: ViewHodler, position: Int) {
        holder.binding.numberTextView.text = getItem(position)
    }

    object DiffCallback: DiffUtil.ItemCallback<String>() {
        override fun areContentsTheSame(lh: String, rh: String): Boolean = lh == rh
        override fun areItemsTheSame(lh: String, rh: String): Boolean = lh == rh
    }

    class ViewHodler(val binding: SearchRecentListItemBinding): RecyclerView.ViewHolder(binding.root)
}