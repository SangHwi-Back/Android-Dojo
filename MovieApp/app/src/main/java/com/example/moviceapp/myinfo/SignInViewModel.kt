package com.example.moviceapp.myinfo

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import com.example.moviceapp.BuildConfig
import com.facebook.CallbackManager
import com.facebook.login.LoginResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class SignInViewModel @Inject constructor() : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private var credentialManager: CredentialManager? = null
    val callbackManager: CallbackManager = CallbackManager.Factory.create()

    suspend fun signUpButtonTapped(email: String, password: String): Result<AuthResult> =
        suspendCancellableCoroutine { continuation ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) continuation.resume(Result.success(task.result))
                    else continuation.resume(Result.failure(task.exception ?: Exception("Unknown error")))
                }
        }

    suspend fun signInWithEmailAndPasswordButtonTapped(email: String, password: String): Result<AuthResult> =
        suspendCancellableCoroutine { continuation ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) continuation.resume(Result.success(task.result))
                    else continuation.resume(Result.failure(task.exception ?: Exception("Unknown error")))
                }
        }

    suspend fun forgotPasswordButtonTapped(email: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) continuation.resume(Result.success(Unit))
                    else continuation.resume(Result.failure(task.exception ?: Exception("Unknown error")))
                }
        }

    suspend fun handleFacebookLoginResult(loginResult: LoginResult): Result<AuthResult> =
        suspendCancellableCoroutine { continuation ->
            val credential = FacebookAuthProvider.getCredential(loginResult.accessToken.token)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) continuation.resume(Result.success(task.result))
                    else continuation.resume(Result.failure(task.exception ?: Exception("Unknown error")))
                }
        }

    suspend fun signInWithGoogleButtonTapped(isAuthorizedBefore: Boolean, context: Context): Result<AuthResult> {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(BuildConfig.GOOGLE_SERVER_CLIENT_ID)
            .setFilterByAuthorizedAccounts(isAuthorizedBefore)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        if (credentialManager == null) {
            credentialManager = CredentialManager.create(context)
        }
        val manager = credentialManager!!

        return try {
            val credentialResult = manager.getCredential(context, request)
            val credential = credentialResult.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                suspendCancellableCoroutine { continuation ->
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) continuation.resume(Result.success(task.result))
                            else continuation.resume(Result.failure(task.exception ?: Exception("Unknown error")))
                        }
                }
            } else {
                Result.failure(Exception("Invalid credential type"))
            }
        } catch (e: GetCredentialCancellationException) {
            // 사용자 취소 또는 계정 재인증 실패 — 재시도 없이 즉시 실패 반환
            Result.failure(e)
        } catch (_: GetCredentialException) {
            if (isAuthorizedBefore) {
                signInWithGoogleButtonTapped(false, context)
            } else {
                // GetGoogleIdOption(One Tap) 이 모두 실패하면 표준 계정 선택 팝업으로 폴백
                signInWithGoogleStandardPicker(context)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun signInWithGoogleStandardPicker(context: Context): Result<AuthResult> {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_SERVER_CLIENT_ID).build()
            )
            .build()

        return try {
            val credentialResult = credentialManager!!.getCredential(context, request)
            val credential = credentialResult.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                suspendCancellableCoroutine { continuation ->
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) continuation.resume(Result.success(task.result))
                            else continuation.resume(Result.failure(task.exception ?: Exception("Unknown error")))
                        }
                }
            } else {
                Result.failure(Exception("Invalid credential type"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}