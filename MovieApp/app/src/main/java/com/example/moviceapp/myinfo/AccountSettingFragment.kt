package com.example.moviceapp.myinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.moviceapp.R
import com.example.moviceapp.common.CommonDialog
import com.example.moviceapp.databinding.FragmentAccountSettingBinding
import com.google.android.material.textfield.TextInputEditText

class AccountSettingFragment : Fragment() {
    private var _binding: FragmentAccountSettingBinding? = null
    private val binding: FragmentAccountSettingBinding
        get() = _binding!!
    private val viewModel: AccountSettingViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Helper function to create edit dialog
        fun createEditDialog(dataName: DataName, title: String) {
            CommonDialog.newInstance(
                title = title,
                layout = R.layout.dialog_edit_single_text,
                listener = object : CommonDialog.CommonDialogListener {
                    override fun onPositive(view: View) =
                        handleResult(dataName, view)
                }
            ).show(childFragmentManager, "NameEditDialog")
        }
        binding.nameEditButton.setOnClickListener {
            createEditDialog(DataName.NAME, "Name editing")
        }
        binding.emailEditButton.setOnClickListener {
            createEditDialog(DataName.EMAIL, "Email editing")
        }
        binding.phoneEditButton.setOnClickListener {
            createEditDialog(DataName.PHONE, "Phone number editing")
        }
        binding.pushNotificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPushNotification(isChecked)
        }
    }
    private fun handleResult(type: DataName, view: View) {
        view.findViewById<TextInputEditText>(R.id.edit_text_view).text?.let { text ->
            when (type) {
                DataName.NAME, DataName.EMAIL, DataName.PHONE -> {
                    print(text.toString())
                }
            }
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentAccountSettingBinding.inflate(inflater)
        _binding = binding
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    enum class DataName {
        NAME, EMAIL, PHONE
    }
}