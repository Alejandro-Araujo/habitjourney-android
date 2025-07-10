package com.alejandro.habitjourney.features.user.presentation.viewmodel

import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.exception.ErrorHandler
import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.core.utils.logging.AppLogger
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.user.domain.usecase.GoogleSignInUseCase
import com.alejandro.habitjourney.features.user.domain.usecase.LoginUseCase
import com.alejandro.habitjourney.features.user.presentation.state.LoginState
import com.alejandro.habitjourney.navigation.AuthFlowCoordinator
import com.alejandro.habitjourney.navigation.AuthRequest
import com.alejandro.habitjourney.navigation.AuthResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
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
 * Soporta tanto login tradicional como Google Sign-In.
 *
 * @property loginUseCase Caso de uso para la operación de inicio de sesión.
 * @property googleSignInUseCase Caso de uso para la operación de inicio de sesión con Google.
 * @property errorHandler Manejador de errores para convertir excepciones en mensajes legibles.
 * @property resourceProvider Proveedor de recursos para obtener cadenas localizadas.
 * @property authFlowCoordinator Coordinador para iniciar flujos de autenticación en la Activity.
 * @property googleWebClientId ID de cliente web de Google para la configuración de OAuth.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val errorHandler: ErrorHandler,
    private val resourceProvider: ResourceProvider,
    private val authFlowCoordinator: AuthFlowCoordinator,
    private val googleWebClientId: String
)  : ViewModel() {

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

    private val _isGoogleSignInLoading = MutableStateFlow(false)
    /**
     * **[StateFlow] que indica si el proceso de Google Sign-In está en curso.**
     * Separado del estado general para mostrar loading específico en el botón de Google.
     */
    val isGoogleSignInLoading: StateFlow<Boolean> = _isGoogleSignInLoading.asStateFlow()

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
     * **Inicia el proceso de inicio de sesión tradicional (email/password).**
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
                else -> LoginState.Initial
            }
        }
    }

    /**
     * **Inicia el proceso de Google Sign-In desde el botón.**
     *
     * Se encarga de construir la solicitud para Credential Manager y de coordinar
     * con la Activity para lanzar el flujo de autenticación de Google.
     * Una vez obtenido el token, lo pasa al `GoogleSignInUseCase` para la autenticación real.
     */
    fun signInWithGoogleButton() {
        _isGoogleSignInLoading.value = true
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val requestId = authFlowCoordinator.generateRequestId()
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(googleWebClientId)
                .setAutoSelectEnabled(false)
                .build()

            val getCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            when (val authResult = authFlowCoordinator.requestAuth(
                AuthRequest.GoogleSignIn(requestId, getCredentialRequest, false) // `false` para One-Tap
            )) {
                is AuthResult.Success -> {
                    // Si la UI de Credential Manager devuelve un token exitosamente,
                    // lo pasamos al UseCase para que realice la autenticación con el backend.
                    when (val signInResult = googleSignInUseCase(authResult.credentialToken)) {
                        is NetworkResponse.Success -> _loginState.value = LoginState.Success
                        is NetworkResponse.Error -> {
                            val errorMessage = errorHandler.getErrorMessage(signInResult.exception)
                            _loginState.value = LoginState.Error(errorMessage)
                        }
                        // NetworkResponse.Loading no debería ocurrir aquí, ya que el UseCase es síncrono en su NetworkResponse final.
                        else -> LoginState.Initial
                    }
                }
                is AuthResult.Error -> {
                    // Manejo de errores específicos del flujo de Credential Manager.
                    val errorMessage = authResult.message
                    if (errorMessage == resourceProvider.getString(R.string.error_google_signin_cancelled)) {
                        AppLogger.d("LoginViewModel", "Google Sign-In cancelado por el usuario (botón).")
                        _loginState.value = LoginState.Initial
                    } else {
                        _loginState.value = LoginState.Error(errorMessage)
                    }
                }
                is AuthResult.Cancelled -> {
                    // El usuario canceló explícitamente el flujo de Credential Manager.
                    AppLogger.d("LoginViewModel", "Google Sign-In cancelado por el usuario (botón).")
                    _loginState.value = LoginState.Initial
                }
            }
            _isGoogleSignInLoading.value = false
        }
    }

    /**
     * **Inicia el proceso de Google Sign-In con One-Tap.**
     *
     * Similar a `signInWithGoogleButton()`, pero con `setAutoSelectEnabled(true)` para One-Tap.
     */
    fun triggerOneTapSignIn() {
        _isGoogleSignInLoading.value = true
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val requestId = authFlowCoordinator.generateRequestId()
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(googleWebClientId)
                .setAutoSelectEnabled(true)
                .build()

            val getCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            when (val authResult = authFlowCoordinator.requestAuth(
                AuthRequest.GoogleSignIn(requestId, getCredentialRequest, true)
            )) {
                is AuthResult.Success -> {
                    // Si One-Tap devuelve un token exitosamente,
                    // lo pasamos al UseCase para la autenticación en el backend.
                    when (val signInResult = googleSignInUseCase(authResult.credentialToken)) {
                        is NetworkResponse.Success -> _loginState.value = LoginState.Success
                        is NetworkResponse.Error -> {
                            val errorMessage = errorHandler.getErrorMessage(signInResult.exception)
                            _loginState.value = LoginState.Error(errorMessage)
                        }
                        else -> LoginState.Initial
                    }
                }
                is AuthResult.Error -> {
                    // Manejo de errores específicos del flujo de Credential Manager.
                    val errorMessage = authResult.message
                    if (errorMessage == resourceProvider.getString(R.string.error_google_signin_cancelled)) {
                        AppLogger.d("LoginViewModel", "Google Sign-In cancelado por el usuario (One-Tap).")
                        _loginState.value = LoginState.Initial
                    } else {
                        _loginState.value = LoginState.Error(errorMessage)
                    }
                }
                is AuthResult.Cancelled -> {
                    // El usuario canceló explícitamente el flujo de Credential Manager.
                    AppLogger.d("LoginViewModel", "Google Sign-In cancelado por el usuario (One-Tap).")
                    _loginState.value = LoginState.Initial
                }
            }
            _isGoogleSignInLoading.value = false
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
        _email.value = ""
        _password.value = ""
        _emailError.value = null
        _passwordError.value = null
        _isPasswordVisible.value = false
        _isGoogleSignInLoading.value = false
    }
}
