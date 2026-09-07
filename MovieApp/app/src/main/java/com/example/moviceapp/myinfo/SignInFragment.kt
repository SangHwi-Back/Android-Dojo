package com.example.moviceapp.myinfo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.credentials.CredentialManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.moviceapp.R
import com.example.moviceapp.databinding.FragmentSignInBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

const val TAG = "SignInFragment"
@AndroidEntryPoint
class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    val binding: FragmentSignInBinding
        get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private var credentialManager: CredentialManager? = null
    private var callbackManager: CallbackManager? = null
    private val emailEmptyAlertDialog: AlertDialog
        get() {
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setTitle("Reset Password")
            alertDialog.setMessage("To reset password, you should add email address.")

            alertDialog.setPositiveButton(R.string.action_confirm) { dialog, _ ->
                // Focus on the email input field
                dialog.dismiss()
                binding.emailEditText.requestFocus()
            }
            return alertDialog.create()
        }

    private val viewModel: SignInViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        FragmentSignInBinding.inflate(inflater, container, false).let {
            _binding = it
            return it.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.closeButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.signInButton.setOnClickListener {
            val (email, password) = getEmailPasswordOrShowAlert()
            if (email.isNotBlank() && password.isNotBlank()) {
                lifecycleScope.launch {
                    viewModel
                        .signInWithEmailAndPasswordButtonTapped(email, password)
                        .handleAuthResult(
                            "signInWithEmail",
                            getString(R.string.sign_in_success_message)
                        )
                }
            }
        }

        binding.forgotPasswordButton.setOnClickListener {
            val email = binding.emailEditText.text?.toString()
            if (email.isNullOrBlank()) {
                // Show dialog to attract user to type email
                emailEmptyAlertDialog.show()
            } else {
                lifecycleScope.launch {
                    viewModel.forgotPasswordButtonTapped(email)
                        .handleAuthResult(
                            "Forgot Reset-Email",
                            getString(R.string.password_reset_email_sent_message)
                        )
                }
            }
        }

        binding.signUpButton.setOnClickListener {
            val (email, password) = getEmailPasswordOrShowAlert()
            if (email.isNotBlank() && password.isNotBlank()) {
                lifecycleScope.launch {
                    viewModel.signUpButtonTapped(email, password)
                        .handleAuthResult(
                            "Create User With Email",
                            getString(R.string.sign_in_success_message)
                        )
                }
            }
        }

        binding.googleButton.setOnClickListener {
            Log.d("SignInFragment", "Google sign in clicked")
            lifecycleScope.launch {
                viewModel.signInWithGoogleButtonTapped(true, requireContext())
                    .handleAuthResult(
                        "Google Sign In",
                        getString(R.string.sign_in_success_message)
                    )
            }
        }

        binding.facebookButton.setOnClickListener {
            Log.d("SignInFragment", "Facebook sign in clicked")
            viewModel
                .signInWithFacebook(requireActivity(), object : FacebookCallback<LoginResult> {
                    override fun onCancel() { Log.d(TAG, "Facebook login cancelled") }
                    override fun onError(error: FacebookException) { Log.e(TAG, "Facebook login error: ${error.message}") }
                    override fun onSuccess(result: LoginResult) {
                        lifecycleScope.launch {
                            viewModel.handleFacebookLoginResult(result)
                                .handleAuthResult(
                                    "Facebook Sign In",
                                    getString(R.string.sign_in_success_message)
                                )
                        }
                    }
                })
                .let { (loginManager, callbackManager) ->
                    loginManager.logInWithReadPermissions(
                        this, callbackManager, listOf("email", "public_profile"))
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        credentialManager = null
        callbackManager = null
    }

    private fun getEmailPasswordOrShowAlert() : Pair<String, String> {
        val email = binding.emailEditText.text?.toString()
        val password = binding.passwordEditText.text?.toString()

        if (!email.isNullOrBlank() && !password.isNullOrBlank()) {
            return Pair(email, password)
        } else {
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setMessage(R.string.sign_in_alert_message)
            alertDialog.show()
            return Pair("", "")
        }
    }

    private fun <T> Result<T>.handleAuthResult(
        methodName: String,
        successMessage: String,
        failureMessage: String = getString(R.string.error_generic),
    ) {
        var debugMessage = "$methodName : "
        var toastMessage = ""
        var exception: Exception? = null
        this.fold(
            onSuccess = {
                debugMessage += "success"
                toastMessage = successMessage
            },
            onFailure = {
                debugMessage += "failure"
                toastMessage = failureMessage
                exception = Exception(it)
            }
        )

        Log.d(TAG, debugMessage, exception)

        Toast.makeText(
            requireContext(), toastMessage, Toast.LENGTH_SHORT
        ).show()
    }
}
