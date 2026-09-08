package com.example.moviceapp.myinfo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.moviceapp.BuildConfig
import com.example.moviceapp.R
import com.example.moviceapp.databinding.FragmentSignInBinding
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

const val TAG = "SignInFragment"

@AndroidEntryPoint
class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val binding: FragmentSignInBinding
        get() = _binding!!
    private val viewModel: SignInViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        FragmentSignInBinding.inflate(inflater, container, false).let {
            _binding = it
            return it.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFacebookLogin()

        binding.closeButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.signInButton.setOnClickListener {
            val (email, password) = getEmailPasswordOrShowAlert()
            if (email.isNotBlank() && password.isNotBlank()) {
                lifecycleScope.launch {
                    viewModel.signInWithEmailAndPasswordButtonTapped(email, password)
                        .handleAuthResult("signInWithEmail", getString(R.string.sign_in_success_message))
                }
            }
        }

        binding.forgotPasswordButton.setOnClickListener {
            val email = binding.emailEditText.text?.toString()
            if (email.isNullOrBlank()) {
                AlertDialog.Builder(requireContext()).apply {
                    setTitle("Reset Password")
                    setMessage("To reset password, you should add email address.")
                    setPositiveButton(R.string.action_confirm) { dialog, _ ->
                        dialog.dismiss()
                        binding.emailEditText.requestFocus()
                    }
                }.create().show()
            } else {
                lifecycleScope.launch {
                    viewModel.forgotPasswordButtonTapped(email)
                        .handleAuthResult("Forgot Reset-Email", getString(R.string.password_reset_email_sent_message))
                }
            }
        }

        binding.signUpButton.setOnClickListener {
            val (email, password) = getEmailPasswordOrShowAlert()
            if (email.isNotBlank() && password.isNotBlank()) {
                lifecycleScope.launch {
                    viewModel.signUpButtonTapped(email, password)
                        .handleAuthResult("Create User With Email", getString(R.string.sign_in_success_message))
                }
            }
        }

        binding.googleButton.setOnClickListener {
            Log.d(TAG, "Google sign in clicked")
            lifecycleScope.launch {
                viewModel.signInWithGoogleButtonTapped(true, requireContext())
                    .handleAuthResult("Google Sign In", getString(R.string.sign_in_success_message))
            }
        }

        binding.facebookButton.setOnClickListener {
            Log.d(TAG, "Facebook sign in clicked")
            LoginManager.getInstance().logInWithReadPermissions(
                this, viewModel.callbackManager, listOf("email", "public_profile")
            )
        }
    }

    private fun setupFacebookLogin() {
        FacebookSdk.setApplicationId(BuildConfig.FACEBOOK_APPLICATION_ID)
        FacebookSdk.setClientToken(BuildConfig.FACEBOOK_CLIENT_TOKEN)
        FacebookSdk.sdkInitialize(requireContext())
        requireActivity().activityResultRegistry.register(
            "facebook_login",
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            viewModel.callbackManager.onActivityResult(result.resultCode, result.resultCode, result.data)
        }

        LoginManager.getInstance().registerCallback(viewModel.callbackManager, object : FacebookCallback<LoginResult> {
            override fun onCancel() {
                Log.d(TAG, "Facebook login cancelled")
            }
            override fun onError(error: FacebookException) {
                Log.e(TAG, "Facebook login error: ${error.message}")
            }
            override fun onSuccess(result: LoginResult) {
                lifecycleScope.launch {
                    viewModel.handleFacebookLoginResult(result)
                        .handleAuthResult("Facebook Sign In", getString(R.string.sign_in_success_message))
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getEmailPasswordOrShowAlert(): Pair<String, String> {
        val email = binding.emailEditText.text?.toString()
        val password = binding.passwordEditText.text?.toString()

        if (!email.isNullOrBlank() && !password.isNullOrBlank()) {
            return Pair(email, password)
        }
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.sign_in_alert_message)
            .show()
        return Pair("", "")
    }

    private fun <T> Result<T>.handleAuthResult(
        methodName: String,
        successMessage: String,
        failureMessage: String = getString(R.string.error_generic),
    ) {
        fold(
            onSuccess = { Log.d(TAG, "$methodName : success") },
            onFailure = { Log.e(TAG, "$methodName : failure", it) }
        )
        val toastMessage = if (isSuccess) successMessage else failureMessage
        Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
    }
}
