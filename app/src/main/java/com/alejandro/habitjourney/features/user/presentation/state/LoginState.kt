package com.alejandro.habitjourney.features.user.presentation.state


sealed class LoginState {
    data object Initial : LoginState()
    data object Loading : LoginState()
    data object Success : LoginState()
    data class Error(val message: String) : LoginState()
}