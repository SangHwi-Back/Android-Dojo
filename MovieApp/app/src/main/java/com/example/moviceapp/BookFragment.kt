package com.example.moviceapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.moviceapp.databinding.FragmentBookBinding

class BookFragment : Fragment() {
    private var _binding: FragmentBookBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bookRecyclerGridView.apply {
            layoutManager = GridLayoutManager(context, 3)
            addItemDecoration(GridSpanDecoration(3, 8))
            adapter = ThumbnailAdapter { movie ->
                findNavController().navigate(BookFragmentDirections.actionBookFragmentToBookTheaterFragment(movie))
            }.apply {
                this.submitList(MoviesMock.all)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}