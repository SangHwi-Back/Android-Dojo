package com.example.moviceapp

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.load
import com.example.moviceapp.databinding.BottomSheetModalBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MovieBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetModalBinding? = null
    private val binding get() = _binding!!

    private lateinit var movie: Movie

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        movie = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.movieImageView.load(movie.posterRes ?: R.drawable.ic_launcher_background)
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

        fun newInstance(movie: Movie) = MovieBottomSheet().apply {
            arguments = Bundle().apply { putParcelable(ARG_MOVIE, movie) }
        }
    }
}