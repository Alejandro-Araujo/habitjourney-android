package com.alejandro.habitjourney.features.user.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.exception.ErrorHandler
import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.user.domain.usecase.RegisterUseCase
import com.alejandro.habitjourney.features.user.presentation.state.RegisterState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * **ViewModel para la pantalla de registro de usuario.**
 *
 * Gestiona el estado de la UI para el formulario de registro,
 * incluyendo los campos de entrada, la visibilidad de las contraseñas,
 * los mensajes de error de validación y el estado general del proceso de registro.
 * Interactúa con [RegisterUseCase] para realizar la lógica de negocio de registro.
 *
 * @property registerUseCase Caso de uso para la operación de registro.
 * @property errorHandler Manejador de errores para convertir excepciones en mensajes legibles.
 * @property resourceProvider Proveedor de recursos para obtener cadenas localizadas.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val errorHandler: ErrorHandler,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Initial)
    /**
     * **[StateFlow] que representa el estado actual del proceso de registro.**
     * Emite los estados [RegisterState.Initial], [RegisterState.Loading], [RegisterState.Success] o [RegisterState.Error].
     */
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    private val _name = MutableStateFlow("")
    /**
     * **[StateFlow] que contiene el nombre introducido por el usuario.**
     */
    val name: StateFlow<String> = _name.asStateFlow()

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

    private val _confirmPassword = MutableStateFlow("")
    /**
     * **[StateFlow] que contiene la confirmación de la contraseña introducida por el usuario.**
     */
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _isPasswordVisible = MutableStateFlow(false)
    /**
     * **[StateFlow] que controla la visibilidad del texto de la contraseña.**
     * `true` si la contraseña es visible, `false` si está oculta.
     */
    val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()

    private val _isConfirmPasswordVisible = MutableStateFlow(false)
    /**
     * **[StateFlow] que controla la visibilidad del texto de la confirmación de la contraseña.**
     * `true` si la confirmación de la contraseña es visible, `false` si está oculta.
     */
    val isConfirmPasswordVisible: StateFlow<Boolean> = _isConfirmPasswordVisible.asStateFlow()

    private val _nameError = MutableStateFlow<String?>(null)
    /**
     * **[StateFlow] que contiene el mensaje de error para el campo de nombre.**
     * Es `null` si no hay error.
     */
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

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

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    /**
     * **[StateFlow] que contiene el mensaje de error para el campo de confirmación de contraseña.**
     * Es `null` si no hay error.
     */
    val confirmPasswordError: StateFlow<String?> = _confirmPasswordError.asStateFlow()

    /**
     * **Actualiza el nombre y limpia cualquier mensaje de error previo.**
     *
     * @param name El nuevo valor del campo de nombre.
     */
    fun onNameChanged(name: String) {
        _name.value = name
        if (_nameError.value != null) {
            _nameError.value = null
        }
    }

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
     * **Actualiza la contraseña y limpia cualquier mensaje de error previo relacionado.**
     *
     * @param password El nuevo valor del campo de contraseña.
     */
    fun onPasswordChanged(password: String) {
        _password.value = password
        if (_passwordError.value != null) {
            _passwordError.value = null
        }
        if (_confirmPassword.value.isNotEmpty() && _confirmPasswordError.value != null) {
            _confirmPasswordError.value = null
        }
    }

    /**
     * **Actualiza la confirmación de la contraseña y limpia cualquier mensaje de error previo.**
     *
     * @param confirmPassword El nuevo valor del campo de confirmación de contraseña.
     */
    fun onConfirmPasswordChanged(confirmPassword: String) {
        _confirmPassword.value = confirmPassword
        if (_confirmPasswordError.value != null) {
            _confirmPasswordError.value = null
        }
    }

    /**
     * **Alterna la visibilidad del texto en el campo de contraseña.**
     */
    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }

    /**
     * **Alterna la visibilidad del texto en el campo de confirmación de contraseña.**
     */
    fun toggleConfirmPasswordVisibility() {
        _isConfirmPasswordVisible.value = !_isConfirmPasswordVisible.value
    }

    /**
     * **Valida el formato y la longitud del nombre.**
     *
     * @param name El nombre a validar.
     * @return Un mensaje de error [String] si el nombre es inválido, o `null` si es válido.
     */
    private fun validateName(name: String): String? {
        return when {
            name.isEmpty() -> resourceProvider.getString(R.string.error_name_empty)
            name.trim().length < 2 -> resourceProvider.getString(R.string.error_name_min_length)
            name.trim().length > 50 -> resourceProvider.getString(R.string.error_name_max_length)
            !name.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) -> resourceProvider.getString(R.string.error_name_invalid_format)
            else -> null
        }
    }

    /**
     * **Valida el formato del correo electrónico.**
     *
     * @param email El correo electrónico a validar.
     * @return Un mensaje de error [String] si el correo es inválido, o `null` si es válido.
     */
    private fun validateEmail(email: String): String? {
        return when {
            email.isEmpty() -> resourceProvider.getString(R.string.error_email_empty)
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
            password.length > 128 -> resourceProvider.getString(R.string.error_password_max_length)
            !password.matches(Regex(".*[a-zA-Z].*")) -> resourceProvider.getString(R.string.error_password_no_letter)
            else -> null
        }
    }

    /**
     * **Valida que la contraseña de confirmación coincida con la contraseña original.**
     *
     * @param password La contraseña original.
     * @param confirmPassword La contraseña de confirmación a comparar.
     * @return Un mensaje de error [String] si no coinciden o está vacía, o `null` si es válida.
     */
    private fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isEmpty() -> resourceProvider.getString(R.string.error_confirm_password_empty)
            password != confirmPassword -> resourceProvider.getString(R.string.error_password_mismatch)
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
     * **Valida todos los campos del formulario de registro.**
     *
     * Actualiza los [StateFlow]s de error para cada campo (`_nameError`, `_emailError`, `_passwordError`, `_confirmPasswordError`).
     *
     * @return `true` si todos los campos son válidos, `false` en caso contrario.
     */
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

    /**
     * **Inicia el proceso de registro de un nuevo usuario.**
     *
     * Primero valida los campos del formulario. Si son válidos, cambia el estado a [RegisterState.Loading],
     * invoca el [RegisterUseCase] y actualiza el [registerState] con el resultado (éxito o error).
     */
    fun register() {
        if (!validateFields()) {
            return
        }

        _registerState.value = RegisterState.Loading

        viewModelScope.launch {
            val response = registerUseCase(_name.value.trim(), _email.value.trim(), _password.value)
            _registerState.value = when (response) {
                is NetworkResponse.Success -> RegisterState.Success
                is NetworkResponse.Error -> {
                    val errorMessage = errorHandler.getErrorMessage(response.exception)
                    RegisterState.Error(errorMessage)
                }
                is NetworkResponse.Loading -> RegisterState.Loading
            }
        }
    }

    /**
     * **Restablece el estado de la ViewModel a su estado inicial.**
     *
     * Esto limpia los errores y el estado de registro, preparando la ViewModel
     * para una nueva interacción.
     */
    fun resetState() {
        _registerState.value = RegisterState.Initial
        _nameError.value = null
        _emailError.value = null
        _passwordError.value = null
        _confirmPasswordError.value = null
    }
}