package com.example.moviceapp.myinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.moviceapp.R
import com.example.moviceapp.common.CommonDialog
import com.example.moviceapp.databinding.FragmentAccountSettingBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccountSettingFragment : Fragment() {
    private var _binding: FragmentAccountSettingBinding? = null
    private val binding: FragmentAccountSettingBinding
        get() = _binding!!
    private val viewModel: AccountSettingViewModel by viewModels()
    private val countryCodes = listOf(
        Pair("한국", "+82"),
        Pair("미국", "+1"),
        Pair("일본", "+81"),
    )
    private val editDialogListener: (DataName) -> CommonDialog.CommonDialogListener = { dataName ->
        object : CommonDialog.CommonDialogListener {
            override fun onPositive(view: View) =
                handleResult(dataName, view)

            override fun onCreated(view: View) {
                val editTextView = view.findViewById<TextInputEditText>(R.id.edit_text_view)
                if (dataName == DataName.PHONE && editTextView != null) {
                    // Country code picker logic here
                    val countryCodeButton = view.findViewById<MaterialButton>(R.id.country_code_button)
                    countryCodeButton?.setOnClickListener {
                        val items = countryCodes.map { "${it.first} (${it.second})" }.toTypedArray()
                        AlertDialog.Builder(requireContext())
                            .setItems(items) { _, index ->
                                countryCodeButton.text = countryCodes[index].second
                            }.show()
                    }
                    // Sets user's current phone number logic here
                    editTextView.doOnTextChanged { text, _, _, _ ->
                        // phone number
                        val text = text?.toString() ?: return@doOnTextChanged
                        // country code
                        val countryCode = countryCodeButton.text.toString()
                        // phone number + country code validating and formatting logic here
                        when (val result = viewModel.parsePhoneNumber(text, countryCode)) {
                            is AccountSettingViewModel.ParsingResult.Success -> {
                                view.findViewById<TextView>(R.id.phone_number_text).text = result.phoneNumber
                            }

                            else -> return@doOnTextChanged
                        }
                    }
                    // Sets user's current phone number
                    val phoneNumber = viewModel.currentUser.value?.phone
                    if (phoneNumber != null) {
                        view.findViewById<TextView>(R.id.phone_number_text).text = phoneNumber
                        val split = viewModel.splitCountryCode(phoneNumber)
                        if (split != null) {
                            countryCodeButton.text = split.first
                            editTextView.setText(split.second.filter { it.isDigit() })
                        }
                    }

                }
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            viewModel.currentUser.collect { currentUser ->
                if (currentUser == null) return@collect
                binding.nameTextView.text = currentUser.name
                binding.emailTextView.text = currentUser.email
                binding.phoneTextView.text = currentUser.phone
                binding.pushNotificationSwitch.isChecked = currentUser.pushNotification
            }
        }
        viewModel.getUserMe()
        // Helper function to create edit dialog
        fun createEditDialog(dataName: DataName, title: String) {
            CommonDialog.newInstance(
                title = title,
                layout = if (dataName == DataName.PHONE)
                    R.layout.dialog_edit_phone_text
                else
                    R.layout.dialog_edit_single_text,
                listener = editDialogListener(dataName)
            ).show(childFragmentManager, title)
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
            val value = text.toString()
            when (type) {
                DataName.NAME -> viewModel.setName(value)
                DataName.EMAIL -> viewModel.setEmail(value)
                DataName.PHONE -> {
                    val phoneNumber = view.findViewById<TextInputEditText>(R.id.edit_text_view).text.toString()
                    val countryCode = view.findViewById<MaterialButton>(R.id.country_code_button).text.toString()
                    viewModel.setPhoneNumber(phoneNumber, countryCode)
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