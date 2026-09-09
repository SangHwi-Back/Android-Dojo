package com.example.moviceapp.myinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.moviceapp.databinding.FragmentAccountSettingBinding

class AccountSettingFragment : Fragment() {
    private var _binding: FragmentAccountSettingBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentAccountSettingBinding.inflate(inflater)
        this._binding = binding
        return binding.root
    }
}