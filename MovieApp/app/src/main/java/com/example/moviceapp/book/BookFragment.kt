package com.example.moviceapp.book

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.moviceapp.common.GridSpanDecoration
import com.example.moviceapp.common.ThumbnailAdapter
import com.example.moviceapp.databinding.FragmentBookBinding
import com.example.moviceapp.repo.Movie
import com.example.moviceapp.search.ThumbnailOnClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookFragment : Fragment(), ThumbnailOnClickListener {
    private var _binding: FragmentBookBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BookViewModel by viewModels()
    private lateinit var adapter: ThumbnailAdapter
    private var allMovies: List<Movie> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ThumbnailAdapter(listener = this)

        binding.bookRecyclerGridView.apply {
            layoutManager = GridLayoutManager(context, 3)
            addItemDecoration(GridSpanDecoration(3, 8))
            adapter = this@BookFragment.adapter
        }

        lifecycleScope.launch {
            allMovies = viewModel.getMovies()
            adapter.submitList(allMovies)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClickMovieFromThumbnail(movie: Movie) {
        findNavController().navigate(
            BookFragmentDirections.actionBookFragmentToBookTheaterFragment(allMovies.toTypedArray())
        )
    }
}
