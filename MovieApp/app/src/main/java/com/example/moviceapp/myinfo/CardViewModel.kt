package com.example.moviceapp.myinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviceapp.repo.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(
    private val cardRepository: CardRepository
) : ViewModel() {

    private val _cards = MutableStateFlow<List<PaymentMethodDto>>(emptyList())
    val cards: StateFlow<List<PaymentMethodDto>> = _cards.asStateFlow()
    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadCards() {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = cardRepository.findAll()) {
                is APIResult.Success -> _cards.value = result.data
                is APIResult.Failure -> _error.value = result.error
            }

            _isLoading.value = false
        }
    }

    fun createCard(dto: CreatePaymentMethodDto) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = cardRepository.create(dto)) {
                is APIResult.Success<*> -> {
                    // Refresh the list to include the new card
                    loadCards()
                }
                is APIResult.Failure -> _error.value = result.error
            }

            _isLoading.value = false
        }
    }

    fun updateCard(id: Int, dto: UpdatePaymentMethodDto) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = cardRepository.update(id, dto)) {
                is APIResult.Success<*> -> {
                    // Refresh the list to show updated card
                    loadCards()
                }
                is APIResult.Failure -> _error.value = result.error
            }

            _isLoading.value = false
        }
    }

    fun deleteCard(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = cardRepository.remove(id)) {
                is APIResult.Success<*> -> {
                    // Refresh the list to remove deleted card
                    loadCards()
                }
                is APIResult.Failure -> _error.value = result.error
            }

            _isLoading.value = false
        }
    }
}