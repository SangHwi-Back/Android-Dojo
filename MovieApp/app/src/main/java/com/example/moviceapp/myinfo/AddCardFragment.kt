package com.example.moviceapp.myinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.moviceapp.databinding.FragmentAddCardBinding

class AddCardFragment : Fragment() {
    private var _binding: FragmentAddCardBinding? = null
    private val binding get() = _binding!!
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentAddCardBinding.inflate(inflater)
        _binding = binding
        return binding.root
    }
}