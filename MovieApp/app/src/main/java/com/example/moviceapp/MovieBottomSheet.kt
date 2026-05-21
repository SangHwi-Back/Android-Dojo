package com.example.moviceapp

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MovieBottomSheet : BottomSheetDialogFragment() {
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
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bottom_sheet_modal, container, false)
    }

    companion object {
        const val TAG = "MovieBottomSheet"
        private const val ARG_MOVIE = "movie"

        fun newInstance(movie: Movie) = MovieBottomSheet().apply {
            arguments = Bundle().apply { putParcelable(ARG_MOVIE, movie) }
        }
    }
}