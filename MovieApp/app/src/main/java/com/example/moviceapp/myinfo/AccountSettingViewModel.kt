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

    fun setPhoneNumber(phoneNumber: String, countryCode: String) {
        _currentUser.value?.let { user ->
            when (val result = parsePhoneNumber(phoneNumber, countryCode)) {
                is ParsingResult.Success -> {
                    val updatedUser = user.copy(phone = result.phoneNumber)
                    viewModelScope.launch { _currentUser.value = updateUser(updatedUser) }
                }
                is ParsingResult.Failure -> return@let
            }
        }
    }

    fun splitCountryCode(phoneNumber: String): Pair<String, String>? {
        val countryCodeStart = phoneNumber.indexOf('+')
        val countryCodeEnd = phoneNumber.indexOf('-')
        if (countryCodeStart == -1 || countryCodeEnd == -1 || countryCodeEnd <= countryCodeStart)
            return null

        val countryCode = phoneNumber.substring(countryCodeStart, countryCodeEnd) // "+82"
        val number = phoneNumber.substring(countryCodeEnd + 1).filter { it.isDigit() } // "010-1234-5678"
        return Pair(countryCode, number)
    }

    fun setPushNotification(isChecked: Boolean) {
        _currentUser.value?.let { user ->
            val updatedUser = user.copy(pushNotification = isChecked)
            viewModelScope.launch { _currentUser.value = updateUser(updatedUser) }
        }
    }

    /**
     * Parses phone number with country code
     * @param phoneNumber The raw phone number string
     * @return Result<String> containing the parsed phone number or error
     */
    fun parsePhoneNumber(phoneNumber: String, countryCode: String): ParsingResult {
        return try {
            // Remove all non-digit characters from the input
            val digitsOnly = phoneNumber.filter { it.isDigit() }
            val countryCodeNumber = countryCode.filter { it.isDigit() }

            // Check if we already have the country code in the number
            val normalizedNumber = if (digitsOnly.startsWith(countryCodeNumber)) {
                digitsOnly // Already has country code
            } else {
                // Add country code prefix
                countryCodeNumber + digitsOnly
            }

            // Validate that we have a reasonable phone number length
            if (normalizedNumber.length !in 7..15) {
                return ParsingResult.Failure(IllegalArgumentException("Invalid phone number length"))
            }
            // Format the phone number with hyphens for better readability
            val formattedPhoneNumber = formatPhoneNumber(normalizedNumber, countryCode)

            // Return the parsed phone number with country code
            ParsingResult.Success(formattedPhoneNumber)
        } catch (e: Exception) {
            ParsingResult.Failure(e)
        }
    }

    /**
     * Formats a phone number with hyphens based on the stored country code
     */
    private fun formatPhoneNumber(phoneNumber: String, countryCode: String): String {
        // Remove country code prefix for formatting purposes
        val digitsOnly = phoneNumber.filter { it.isDigit() }

        // Remove country code from the beginning if present
        val countryCodeDigits = countryCode.filter { it.isDigit() }
        val numberWithoutCountryCode = if (digitsOnly.startsWith(countryCodeDigits)) {
            digitsOnly.substring(countryCodeDigits.length)
        } else {
            digitsOnly
        }

        return when (countryCodeDigits.length) {
            // For US/Canada format (+1) - XXX-XXX-XXXX
            1 -> {
                if (numberWithoutCountryCode.length >= 11) {
                    val areaCode = numberWithoutCountryCode.substring(0, 3)
                    val exchange = numberWithoutCountryCode.substring(3, 6)
                    val subscriber = numberWithoutCountryCode.substring(6, 10)
                    "$countryCode-$areaCode-$exchange-$subscriber"
                } else {
                    numberWithoutCountryCode
                }
            }
            // For Korean format (+82) - XXX-XXXX-XXXX (or XXX-XXX-XXXX depending on length)
            2 -> {
                if (numberWithoutCountryCode.length >= 10) {
                    val areaCode = numberWithoutCountryCode.substring(0, 3)
                    val exchange = numberWithoutCountryCode.substring(3, 7)
                    val subscriber = numberWithoutCountryCode.substring(7, 11)
                    "$countryCode-$areaCode-$exchange-$subscriber"
                } else if (numberWithoutCountryCode.length >= 7) {
                    val areaCode = numberWithoutCountryCode.substring(0, 3)
                    val exchange = numberWithoutCountryCode.substring(3, 6)
                    val subscriber = numberWithoutCountryCode.substring(6)
                    "$countryCode-$areaCode-$exchange-$subscriber"
                } else {
                    numberWithoutCountryCode
                }
            }
            // Default formatting for other countries (no hyphens)
            else -> numberWithoutCountryCode
        }
    }

    sealed class ParsingResult {
        data class Success(val phoneNumber: String) : ParsingResult()
        data class Failure(val exception: Exception) : ParsingResult()
    }
}