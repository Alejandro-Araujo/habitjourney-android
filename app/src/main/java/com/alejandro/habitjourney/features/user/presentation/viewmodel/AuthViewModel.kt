package com.alejandro.habitjourney.features.user.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.exception.ErrorHandler
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel principal para la gestión del estado de autenticación de la aplicación.
 *
 * Se encarga de observar el estado de inicio de sesión del usuario a través del [UserRepository]
 * y de exponer este estado a la interfaz de usuario. También maneja los errores iniciales
 * de autenticación y el estado de carga.
 *
 * @property userRepository El repositorio de usuario para acceder a la lógica de autenticación.
 * @property errorHandler El manejador de errores para obtener mensajes legibles a partir de excepciones.
 * @property resourceProvider Proveedor de recursos para obtener cadenas localizadas.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val errorHandler: ErrorHandler,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    /**
     * Un [StateFlow] que indica si el usuario está actualmente autenticado.
     * `true` si el usuario ha iniciado sesión, `false` en caso contrario.
     */
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    /**
     * Un [StateFlow] que indica si el estado de autenticación se está verificando.
     * `true` mientras se realiza la comprobación inicial, `false` una vez completada.
     */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    /**
     * Un [StateFlow] que contiene un mensaje de error de autenticación, si lo hay.
     * `null` si no hay errores.
     */
    val authError: StateFlow<String?> = _authError.asStateFlow()


    init {
        viewModelScope.launch {
            userRepository.isLoggedIn().collectLatest { loggedIn ->
                _isLoggedIn.value = loggedIn

                if (_isLoading.value) {
                    _isLoading.value = false
                }
            }
        }
        checkAuthStatus()
    }

    /**
     * Realiza una comprobación inicial del estado de autenticación del usuario.
     *
     * Establece el estado de carga y maneja posibles errores durante la verificación.
     */
    private fun checkAuthStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            try {
                userRepository.isLoggedIn().first()
            } catch (e: Exception) {
                val errorMessage = resourceProvider.getString(R.string.error_auth_initial_check, errorHandler.getErrorMessage(e))
                _authError.value = errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Reinicia el estado de error de autenticación después de un inicio de sesión exitoso.
     */
    fun onLoginSuccess() {
        _authError.value = null
    }
}