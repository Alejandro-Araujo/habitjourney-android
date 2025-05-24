package com.alejandro.habitjourney.features.user.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.features.user.domain.usecase.LoginUseCase
import com.alejandro.habitjourney.features.user.presentation.state.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    application: Application
) : AndroidViewModel(application) {

    private val resources = application.resources

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isPasswordVisible = MutableStateFlow(false)
    val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()

    // Estados de error para cada campo
    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    fun onEmailChanged(email: String) {
        _email.value = email
        // Limpiar error cuando el usuario empiece a escribir
        if (_emailError.value != null) {
            _emailError.value = null
        }
    }

    fun onPasswordChanged(password: String) {
        _password.value = password
        // Limpiar error cuando el usuario empiece a escribir
        if (_passwordError.value != null) {
            _passwordError.value = null
        }
    }

    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isEmpty() ->  resources.getString(R.string.error_email_empty)
            !isValidEmail(email) -> resources.getString(R.string.error_email_invalid_format)
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> resources.getString(R.string.error_password_empty)
            password.length < 6 -> resources.getString(R.string.error_password_min_length)
            else -> null
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun validateFields(): Boolean {
        val emailValidation = validateEmail(_email.value)
        val passwordValidation = validatePassword(_password.value)

        _emailError.value = emailValidation
        _passwordError.value = passwordValidation

        return emailValidation == null && passwordValidation == null
    }

    fun login() {
        // Validar campos individuales
        if (!validateFields()) {
            return
        }

        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val response = loginUseCase(_email.value, _password.value)
            _loginState.value = when (response) {
                is NetworkResponse.Success -> LoginState.Success
                is NetworkResponse.Error ->  LoginState.Error(response.exception.message ?: resources.getString(R.string.error_unknown))
                is NetworkResponse.Loading -> LoginState.Loading
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Initial
        _emailError.value = null
        _passwordError.value = null
    }
}