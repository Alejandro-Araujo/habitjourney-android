package com.alejandro.habitjourney.features.user.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.features.user.domain.usecase.RegisterUseCase
import com.alejandro.habitjourney.features.user.presentation.state.RegisterState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    application: Application
) : AndroidViewModel(application) {

    private val resources = application.resources

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Initial)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _isPasswordVisible = MutableStateFlow(false)
    val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()

    private val _isConfirmPasswordVisible = MutableStateFlow(false)
    val isConfirmPasswordVisible: StateFlow<Boolean> = _isConfirmPasswordVisible.asStateFlow()

    // Estados de error para cada campo
    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError: StateFlow<String?> = _confirmPasswordError.asStateFlow()

    fun onNameChanged(name: String) {
        _name.value = name
        if (_nameError.value != null) {
            _nameError.value = null
        }
    }

    fun onEmailChanged(email: String) {
        _email.value = email
        if (_emailError.value != null) {
            _emailError.value = null
        }
    }

    fun onPasswordChanged(password: String) {
        _password.value = password
        if (_passwordError.value != null) {
            _passwordError.value = null
        }
        // También validar confirmación si ya se escribió
        if (_confirmPassword.value.isNotEmpty() && _confirmPasswordError.value != null) {
            _confirmPasswordError.value = null
        }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _confirmPassword.value = confirmPassword
        if (_confirmPasswordError.value != null) {
            _confirmPasswordError.value = null
        }
    }

    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }

    fun toggleConfirmPasswordVisibility() {
        _isConfirmPasswordVisible.value = !_isConfirmPasswordVisible.value
    }

    private fun validateName(name: String): String? {
        return when {
            name.isEmpty() -> resources.getString(R.string.error_name_empty)
            name.trim().length < 2 -> resources.getString(R.string.error_name_min_length)
            name.trim().length > 50 ->resources.getString(R.string.error_name_max_length)
            !name.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) -> resources.getString(R.string.error_name_invalid_format)
            else -> null
        }
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isEmpty() -> resources.getString(R.string.error_email_empty)
            !isValidEmail(email) -> resources.getString(R.string.error_email_invalid_format)
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> resources.getString(R.string.error_password_empty)
            password.length < 6 -> resources.getString(R.string.error_password_min_length)
            password.length > 128 -> resources.getString(R.string.error_password_max_length)
            !password.matches(Regex(".*[a-zA-Z].*")) -> resources.getString(R.string.error_password_no_letter)
            else -> null
        }
    }

    private fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isEmpty() ->  resources.getString(R.string.error_confirm_password_empty)
            password != confirmPassword -> resources.getString(R.string.error_password_mismatch)
            else -> null
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun validateFields(): Boolean {
        val nameValidation = validateName(_name.value.trim())
        val emailValidation = validateEmail(_email.value.trim())
        val passwordValidation = validatePassword(_password.value)
        val confirmPasswordValidation = validateConfirmPassword(_password.value, _confirmPassword.value)

        _nameError.value = nameValidation
        _emailError.value = emailValidation
        _passwordError.value = passwordValidation
        _confirmPasswordError.value = confirmPasswordValidation

        return nameValidation == null &&
                emailValidation == null &&
                passwordValidation == null &&
                confirmPasswordValidation == null
    }

    fun register() {
        // Validar campos individuales
        if (!validateFields()) {
            return
        }

        _registerState.value = RegisterState.Loading

        viewModelScope.launch {
            val response = registerUseCase(_name.value.trim(), _email.value.trim(), _password.value)
            _registerState.value = when (response) {
                is NetworkResponse.Success -> RegisterState.Success
                is NetworkResponse.Error -> RegisterState.Error(response.exception.message ?:  resources.getString(R.string.error_unknown))
                is NetworkResponse.Loading -> RegisterState.Loading
            }
        }
    }

    fun resetState() {
        _registerState.value = RegisterState.Initial
        _nameError.value = null
        _emailError.value = null
        _passwordError.value = null
        _confirmPasswordError.value = null
    }
}