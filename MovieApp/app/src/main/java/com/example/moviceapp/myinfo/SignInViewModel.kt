package com.example.moviceapp.myinfo

import android.content.Context
import androidx.activity.result.contract.ActivityResultContracts
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.example.moviceapp.BuildConfig
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.*
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class SignInViewModel: ViewModel() {
    private val auth: FirebaseAuth
    private var credentialManager: CredentialManager? = null
    private var callbackManager = CallbackManager.Factory.create()

    init {
        auth = Firebase.auth
    }

    suspend fun signUpButtonTapped(email: String, password: String) : Result<AuthResult> {
        return suspendCancellableCoroutine { continuation ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    handleContinuation(continuation, SignInViewModelTask.Success(task))
                }
        }
    }

    suspend fun signInWithEmailAndPasswordButtonTapped(email: String, password: String) : Result<AuthResult> {
        return suspendCancellableCoroutine { continuation ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    handleContinuation(continuation, SignInViewModelTask.Success(task))
                }
        }
    }

    suspend fun forgotPasswordButtonTapped(email: String) : Result<Void> {
        return suspendCancellableCoroutine { continuation ->
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    handleContinuation(continuation, SignInViewModelTask.Success(task))
                }
        }
    }

    private fun <T> handleContinuation(continuation: CancellableContinuation<Result<T>>, task: SignInViewModelTask) {
        when (task) {
            is SignInViewModelTask.Success<*> -> {
                continuation.resume(Result.success(task.task) as Result<T>)
            }
            is SignInViewModelTask.Failure -> {
                continuation.resume(Result.failure(Exception(task.exception)))
            }
        }
    }

    fun signInWithFacebook(activity: FragmentActivity, facebookCallback: FacebookCallback<LoginResult>) : Pair<LoginManager, CallbackManager> {
        activity.activityResultRegistry.register(
            "facebook_login",
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            callbackManager.onActivityResult(result.resultCode, result.resultCode, result.data)
        }

        LoginManager.getInstance().registerCallback(callbackManager, facebookCallback)
        return Pair(LoginManager.getInstance(), callbackManager)
    }

    suspend fun handleFacebookLoginResult(result: LoginResult) : Result<AuthResult> {
        return suspendCancellableCoroutine { continuation ->
            val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    handleContinuation(continuation, SignInViewModelTask.Success(task))
                }
        }
    }

    suspend fun signInWithGoogleButtonTapped(isAuthorizedBefore: Boolean, context: Context) : Result<AuthResult>  {
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
        if (credentialManager == null) {
            credentialManager = CredentialManager.create(context)
        }
        val manager = credentialManager!!
        try {
            // Launch Credential Manager UI
            val result = manager.getCredential(context, request)
            // Extract credential from the result returned by Credential Manager
            return suspendCancellableCoroutine { continuation ->
                if (result.credential is CustomCredential && result.credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    // Create Google ID Token
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)

                    // Sign in to Firebase with using the token
                    val credential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            handleContinuation(continuation, SignInViewModelTask.Success(task))
                        }
                } else {
                    handleContinuation(continuation, SignInViewModelTask.Failure(Exception("Invalid credential type")))
                }
            }
        } catch (e: GetCredentialException) {
            return if (isAuthorizedBefore) {
                signInWithGoogleButtonTapped(false, context)
            } else {
                suspendCancellableCoroutine { continuation ->
                    handleContinuation(continuation, SignInViewModelTask.Failure(Exception("Google sign-in failed", e)))
                }
            }
        }
    }
}

private sealed class SignInViewModelTask {
    data class Success<T>(val task: Task<T>): SignInViewModelTask()
    data class Failure(val exception: Exception): SignInViewModelTask()
}