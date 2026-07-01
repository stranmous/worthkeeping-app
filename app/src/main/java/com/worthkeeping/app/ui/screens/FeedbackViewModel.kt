package com.worthkeeping.app.ui.screens

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worthkeeping.app.data.FeedbackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedbackViewModel(
    private val repository: FeedbackRepository = FeedbackRepository()
) : ViewModel() {

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _submitSuccess = MutableStateFlow<Boolean?>(null)
    val submitSuccess: StateFlow<Boolean?> = _submitSuccess.asStateFlow()

    fun updateMessage(newMessage: String) {
        _message.value = newMessage
    }

    fun submitFeedback(appVersion: String) {
        val currentMessage = _message.value
        if (currentMessage.isBlank()) return

        _isSubmitting.value = true
        _submitSuccess.value = null

        viewModelScope.launch {
            val androidVersion = Build.VERSION.RELEASE ?: "Unknown"
            val success = repository.submitFeedback(currentMessage, androidVersion, appVersion)
            _isSubmitting.value = false
            _submitSuccess.value = success
            if (success) {
                _message.value = ""
            }
        }
    }

    fun resetState() {
        _submitSuccess.value = null
    }
}
