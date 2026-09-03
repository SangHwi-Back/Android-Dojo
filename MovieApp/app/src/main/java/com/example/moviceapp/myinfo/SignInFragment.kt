package com.example.moviceapp.myinfo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.moviceapp.BuildConfig
import com.example.moviceapp.R
import com.example.moviceapp.databinding.FragmentSignInBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
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
            val emailPassword = getEmailPasswordOrShowAlert()
            if (emailPassword != null) {
                auth.signInWithEmailAndPassword(
                    emailPassword.first,
                    emailPassword.second
                ).addOnCompleteListener { task ->
                    "signInWithEmail:${if (task.isSuccessful) "success" else "failure"}".let {
                        Log.d(TAG, it, task.exception)
                    }
                    getString(if (task.isSuccessful) R.string.sign_in_success_message else R.string.sign_in_failure_message).let {
                        Toast.makeText(
                            requireContext(), it, Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                return@setOnClickListener
            }
        }

        binding.forgotPasswordButton.setOnClickListener {
            val email = binding.emailEditText.text?.toString()
            if (email.isNullOrBlank()) {
                // Show dialog to attract user to type email
                val alertDialog = AlertDialog.Builder(requireContext())
                alertDialog.setTitle("Reset Password")
                alertDialog.setMessage("To reset password, you should add email address.")

                alertDialog.setPositiveButton(R.string.action_confirm) { dialog, _ ->
                    // Focus on the email input field
                    dialog.dismiss()
                    binding.emailEditText.requestFocus()
                }

                alertDialog.create().show()
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        "forgotResetEmail:${if (task.isSuccessful) "success" else "failure"}".let {
                            Log.d(TAG, it, task.exception)
                        }
                        getString(if (task.isSuccessful)
                            R.string.password_reset_email_sent_message
                        else
                            R.string.sign_in_failure_message
                        ).let {
                            Toast.makeText(
                                requireContext(), it, Toast.LENGTH_SHORT
                            ).show()
                        }
                        if (task.isSuccessful)
                            findNavController().popBackStack()
                    }
            }
        }

        binding.signUpButton.setOnClickListener {
            val emailPassword = getEmailPasswordOrShowAlert()
            if (emailPassword != null) {
                auth.createUserWithEmailAndPassword(
                    emailPassword.first,
                    emailPassword.second
                ).addOnCompleteListener { task ->
                    "createUserWithEmail:${if (task.isSuccessful) "success" else "failure"}".let {
                        Log.d(TAG, it, task.exception)
                    }
                    getString(if (task.isSuccessful) R.string.sign_in_success_message else R.string.sign_in_failure_message).let {
                        Toast.makeText(
                            requireContext(), it, Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                return@setOnClickListener
            }
        }

        binding.googleButton.setOnClickListener {
            Log.d("SignInFragment", "Google sign in clicked")
            signInWithGoogle(true)
        }

        binding.facebookButton.setOnClickListener {
            Log.d("SignInFragment", "Facebook sign in clicked")
            if (callbackManager == null) {
                callbackManager = CallbackManager.Factory.create()
            }
            val manager = callbackManager!!

            requireActivity().activityResultRegistry.register(
                "facebook_login",
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                manager.onActivityResult(result.resultCode, result.resultCode, result.data)
            }

            LoginManager.getInstance().registerCallback(manager, object : FacebookCallback<LoginResult> {
                override fun onCancel() {
                    Log.d(TAG, "Facebook login cancelled")
                }

                override fun onError(error: FacebookException) {
                    Log.e(TAG, "Facebook login error: ${error.message}")
                }

                override fun onSuccess(result: LoginResult) {
                    val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            "createUserWithEmail:${if (task.isSuccessful) "success" else "failure"}".let {
                                Log.d(TAG, it, task.exception)
                            }
                            getString(if (task.isSuccessful) R.string.sign_in_success_message else R.string.sign_in_failure_message).let {
                                Toast.makeText(
                                    requireContext(), it, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            })
            LoginManager.getInstance().logInWithReadPermissions(
                this, manager, listOf("email", "public_profile")
            )
        }
    }

    private fun signInWithGoogle(isAuthorizedBefore: Boolean) {
        // [START create_credential_manager_request]
        // Instantiate a Google sign-in request
        val googleIdOption = GetGoogleIdOption.Builder()
            // Your server's client ID, not your Android client ID.
            .setServerClientId(BuildConfig.GOOGLE_SERVER_CLIENT_ID)
            // Only show accounts previously used to sign in.
            .setFilterByAuthorizedAccounts(isAuthorizedBefore)
            .build()

        // Create the Credential Manager request
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // [END create_credential_manager_request]
        lifecycleScope.launch {
            if (credentialManager == null) {
                credentialManager = CredentialManager.create(requireContext())
            }
            val manager = credentialManager!!
            try {
                // Launch Credential Manager UI
                val result = manager.getCredential(context = requireContext(), request = request)
                // Extract credential from the result returned by Credential Manager
                handleSignIn(result.credential)
            } catch (e: GetCredentialException) {
                if (isAuthorizedBefore) {
                    signInWithGoogle(false)
                } else {
                    Log.e(TAG, "Couldn't retrieve user's credentials: ${e.localizedMessage}")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        credentialManager = null
        callbackManager = null
    }

    fun getEmailPasswordOrShowAlert() : Pair<String, String>? {
        val email = binding.emailEditText.text?.toString()
        val password = binding.passwordEditText.text?.toString()

        if (!email.isNullOrBlank() && !password.isNullOrBlank()) {
            return Pair(email, password)
        } else {
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setMessage(R.string.sign_in_alert_message)
            alertDialog.show()
            return null
        }
    }
    private fun handleSignIn(credential: Credential) {
        // Check if credential is of type Google ID
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            // Create Google ID Token
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

            // Sign in to Firebase with using the token
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w(TAG, "Credential is not of type Google ID!")
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                "createUserWithEmail:${if (task.isSuccessful) "success" else "failure"}".let {
                    Log.d(TAG, it, task.exception)
                }
                getString(if (task.isSuccessful) R.string.sign_in_success_message else R.string.sign_in_failure_message).let {
                    Toast.makeText(
                        requireContext(), it, Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
