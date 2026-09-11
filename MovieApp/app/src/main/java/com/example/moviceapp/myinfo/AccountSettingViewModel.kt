package com.example.moviceapp.myinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviceapp.repo.APIResult
import com.example.moviceapp.repo.MovieAppUserRepository
import com.example.moviceapp.repo.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountSettingViewModel @Inject constructor(
    val userRepository: MovieAppUserRepository
) : ViewModel() {
    var _currentUser = MutableStateFlow<UserEntity?>(null)
    var currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    suspend fun getUserMe(): UserEntity? {
        return when (val result = userRepository.getUser()) {
            is APIResult.Success -> result.data
            is APIResult.Failure -> null
        }
    }

    suspend fun updateUser(user: UserEntity): UserEntity {
        return when (val result = userRepository.updateUser(user)) {
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