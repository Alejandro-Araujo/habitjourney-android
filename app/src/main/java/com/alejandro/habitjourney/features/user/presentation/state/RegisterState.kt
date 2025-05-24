package com.alejandro.habitjourney.features.user.presentation.state

sealed class RegisterState {
    data object Initial : RegisterState()
    data object Loading : RegisterState()
    data object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}