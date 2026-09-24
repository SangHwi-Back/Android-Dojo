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

    private val _error = MutableSharedFlow<Throwable>(replay = 0)
    val error: SharedFlow<Throwable> = _error

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

    /**
     * Handle AppException and emit user-friendly error message
     */
    suspend fun handleException(exception: Throwable) =
        _error.emit(when (exception) {
            is AppException -> exception
            else -> exception.toAppException()
        })

    /**
     * Handle AppException with custom message override
     */
    suspend fun handleException(exception: AppException, customMessage: String) =
        _error.emit(exception)
}
