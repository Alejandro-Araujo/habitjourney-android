package com.alejandro.habitjourney.features.user.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.exception.ErrorHandler
import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.user.domain.usecase.LoginUseCase
import com.alejandro.habitjourney.features.user.presentation.state.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * **ViewModel para la pantalla de inicio de sesión.**
 *
 * Gestiona el estado de la UI para el formulario de inicio de sesión,
 * incluyendo los campos de entrada, la visibilidad de la contraseña,
 * los mensajes de error de validación y el estado general del proceso de autenticación.
 * Interactúa con [LoginUseCase] para realizar la lógica de negocio de inicio de sesión.
 *
 * @property loginUseCase Caso de uso para la operación de inicio de sesión.
 * @property errorHandler Manejador de errores para convertir excepciones en mensajes legibles.
 * @property resourceProvider Proveedor de recursos para obtener cadenas localizadas.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val errorHandler: ErrorHandler,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    /**
     * **[StateFlow] que representa el estado actual del proceso de inicio de sesión.**
     * Emite los estados [LoginState.Initial], [LoginState.Loading], [LoginState.Success] o [LoginState.Error].
     */
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _email = MutableStateFlow("")
    /**
     * **[StateFlow] que contiene el correo electrónico introducido por el usuario.**
     */
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    /**
     * **[StateFlow] que contiene la contraseña introducida por el usuario.**
     */
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isPasswordVisible = MutableStateFlow(false)
    /**
     * **[StateFlow] que controla la visibilidad del texto de la contraseña.**
     * `true` si la contraseña es visible, `false` si está oculta.
     */
    val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    /**
     * **[StateFlow] que contiene el mensaje de error para el campo de correo electrónico.**
     * Es `null` si no hay error.
     */
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    /**
     * **[StateFlow] que contiene el mensaje de error para el campo de contraseña.**
     * Es `null` si no hay error.
     */
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    /**
     * **Actualiza el correo electrónico y limpia cualquier mensaje de error previo.**
     *
     * @param email El nuevo valor del campo de correo electrónico.
     */
    fun onEmailChanged(email: String) {
        _email.value = email
        if (_emailError.value != null) {
            _emailError.value = null
        }
    }

    /**
     * **Actualiza la contraseña y limpia cualquier mensaje de error previo.**
     *
     * @param password El nuevo valor del campo de contraseña.
     */
    fun onPasswordChanged(password: String) {
        _password.value = password
        if (_passwordError.value != null) {
            _passwordError.value = null
        }
    }

    /**
     * **Alterna la visibilidad del texto en el campo de contraseña.**
     */
    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }

    /**
     * **Valida el formato del correo electrónico.**
     *
     * @param email El correo electrónico a validar.
     * @return Un mensaje de error [String] si el correo es inválido, o `null` si es válido.
     */
    private fun validateEmail(email: String): String? {
        return when {
            email.isEmpty() ->  resourceProvider.getString(R.string.error_email_empty)
            !isValidEmail(email) -> resourceProvider.getString(R.string.error_email_invalid_format)
            else -> null
        }
    }

    /**
     * **Valida la fortaleza de la contraseña.**
     *
     * @param password La contraseña a validar.
     * @return Un mensaje de error [String] si la contraseña es inválida, o `null` si es válida.
     */
    private fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> resourceProvider.getString(R.string.error_password_empty)
            password.length < 6 -> resourceProvider.getString(R.string.error_password_min_length)
            else -> null
        }
    }

    /**
     * **Comprueba si una cadena de texto tiene un formato de correo electrónico válido.**
     *
     * @param email La cadena a verificar.
     * @return `true` si el formato es válido, `false` en caso contrario.
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * **Valida todos los campos del formulario de inicio de sesión.**
     *
     * Actualiza los [StateFlow]s de error para cada campo (`_emailError`, `_passwordError`).
     *
     * @return `true` si todos los campos son válidos, `false` en caso contrario.
     */
    private fun validateFields(): Boolean {
        val emailValidation = validateEmail(_email.value)
        val passwordValidation = validatePassword(_password.value)

        _emailError.value = emailValidation
        _passwordError.value = passwordValidation

        return emailValidation == null && passwordValidation == null
    }

    /**
     * **Inicia el proceso de inicio de sesión.**
     *
     * Primero valida los campos del formulario. Si son válidos, cambia el estado a [LoginState.Loading],
     * invoca el [LoginUseCase] y actualiza el [loginState] con el resultado (éxito o error).
     */
    fun login() {
        if (!validateFields()) {
            return
        }

        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val response = loginUseCase(_email.value, _password.value)
            _loginState.value = when (response) {
                is NetworkResponse.Success -> LoginState.Success
                is NetworkResponse.Error  -> {
                    val errorMessage = errorHandler.getErrorMessage(response.exception)
                    LoginState.Error(errorMessage)
                }
                is NetworkResponse.Loading -> LoginState.Loading
            }
        }
    }

    /**
     * **Restablece el estado de la ViewModel a su estado inicial.**
     *
     * Esto limpia los errores y el estado de inicio de sesión, preparando la ViewModel
     * para una nueva interacción.
     */
    fun resetState() {
        _loginState.value = LoginState.Initial
        _emailError.value = null
        _passwordError.value = null
    }
}