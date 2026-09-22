package com.example.moviceapp

import android.view.View
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class AppViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableSharedFlow<String>(replay = 0)
    val error: SharedFlow<String> = _error

    private val _visibilityFloatingActionButton = MutableStateFlow(View.VISIBLE)
    val visibilityFloatingActionButton: StateFlow<Int> = _visibilityFloatingActionButton

    fun showLoading() {
        _isLoading.value = true
    }

    fun hideLoading() {
        _isLoading.value = false
    }

    fun setVisibilityFloatingActionButton(visibility: Int) {
        _visibilityFloatingActionButton.value = visibility
    }

    suspend fun emitError(message: String) {
        _error.emit(message)
    }
}
