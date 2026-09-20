package com.example.moviceapp.myinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviceapp.repo.APIResult
import com.example.moviceapp.repo.MovieAppUserRepository
import com.example.moviceapp.repo.UserEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AccountSettingViewModel @Inject constructor(
    val userRepository: MovieAppUserRepository
) : ViewModel() {
    var _currentUser = MutableStateFlow<UserEntity?>(null)
    var currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    fun getUserMe() {
        viewModelScope.launch {
            var tokenResult = Firebase.auth.currentUser?.getIdToken(false)?.await()
            if (tokenResult == null)
                tokenResult = Firebase.auth.currentUser?.getIdToken(true)?.await()

            val token = tokenResult?.token ?: return@launch

            when (val result = userRepository.getUser("Bearer $token")) {
                is APIResult.Success ->
                    _currentUser.value = result.data
                is APIResult.Failure ->
                    _currentUser.value = null
            }
        }
    }

    suspend fun updateUser(user: UserEntity): UserEntity {
        var tokenResult = Firebase.auth.currentUser?.getIdToken(false)?.await()
        if (tokenResult == null)
            tokenResult = Firebase.auth.currentUser?.getIdToken(true)?.await()
        val token = tokenResult?.token?.let { "Bearer $it" }
        return when (val result = userRepository.updateUser(token, user)) {
            is APIResult.Success -> result.data
            is APIResult.Failure -> user
        }
    }

    fun setName(name: String) {
        _currentUser.value?.let { user ->
            val updatedUser = user.copy(name = name)
            viewModelScope.launch { _currentUser.value = updateUser(updatedUser) }
        }
    }

    fun setEmail(email: String) {
        _currentUser.value?.let { user ->
            val updatedUser = user.copy(email = email)
            viewModelScope.launch { _currentUser.value = updateUser(updatedUser) }
        }
    }

    fun setPhoneNumber(phoneNumber: String) {
        _currentUser.value?.let { user ->
            val updatedUser = user.copy(phone = phoneNumber)
            viewModelScope.launch { _currentUser.value = updateUser(updatedUser) }
        }
    }

    fun setPushNotification(isChecked: Boolean) {
        _currentUser.value?.let { user ->
            val updatedUser = user.copy(pushNotification = isChecked)
            viewModelScope.launch { _currentUser.value = updateUser(updatedUser) }
        }
    }
}