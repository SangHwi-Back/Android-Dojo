package com.example.moviceapp.myinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviceapp.AppException
import com.example.moviceapp.repo.APIResult
import com.example.moviceapp.repo.CardRepository
import com.example.moviceapp.repo.CreatePaymentMethodDto
import com.example.moviceapp.repo.PaymentMethodDto
import com.example.moviceapp.repo.UpdatePaymentMethodDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(
    private val cardRepository: CardRepository
) : ViewModel() {

    private val _cards = MutableStateFlow<List<PaymentMethodDto>>(emptyList())
    val cards: StateFlow<List<PaymentMethodDto>> = _cards

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<AppException?>(null)
    val error: StateFlow<AppException?> = _error

    fun loadCards() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = cardRepository.findAll()

            when (result) {
                is APIResult.Success -> {
                    _cards.value = result.data
                }
                is APIResult.Failure -> _error.value = AppException.Unknown("",result.error)
            }

            _isLoading.value = false
        }
    }

    fun createCard(dto: CreatePaymentMethodDto) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = cardRepository.create(dto)

            when (result) {
                is APIResult.Success<*> -> {
                    // Refresh the list to include the new card
                    loadCards()
                }
                is APIResult.Failure -> _error.value = AppException.Unknown("",result.error)
            }

            _isLoading.value = false
        }
    }

    fun updateCard(id: Int, dto: UpdatePaymentMethodDto) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = cardRepository.update(id, dto)

            when (result) {
                is APIResult.Success<*> -> {
                    // Refresh the list to show updated card
                    loadCards()
                }
                is APIResult.Failure -> _error.value = AppException.Unknown("",result.error)
            }

            _isLoading.value = false
        }
    }

    fun deleteCard(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = cardRepository.remove(id)

            when (result) {
                is APIResult.Success<*> -> {
                    // Refresh the list to remove deleted card
                    loadCards()
                }
                is APIResult.Failure -> _error.value = AppException.Unknown("",result.error)
            }

            _isLoading.value = false
        }
    }
}
