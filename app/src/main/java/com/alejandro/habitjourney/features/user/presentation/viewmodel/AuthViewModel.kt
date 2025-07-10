package com.alejandro.habitjourney.features.user.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.core.data.remote.exception.ErrorHandler
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.user.domain.usecase.IsLoggedInUseCase
import com.alejandro.habitjourney.features.user.domain.usecase.ObserveSessionInconsistencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * ViewModel principal para la gestión del estado de autenticación de la aplicación.
 *
 * Se encarga de observar el estado de inicio de sesión del usuario a través del [IsLoggedInUseCase]
 * y de exponer este estado a la interfaz de usuario. También maneja los errores iniciales
 * de autenticación y el estado de carga.
 *
 * @property isLoggedInUseCase El caso de uso para verificar el estado de autenticación.
 * @property observeSessionInconsistencyUseCase Caso de uso para verificar la consintencia de la sesión de usuario.
 * @property errorHandler El manejador de errores para obtener mensajes legibles a partir de excepciones.
 * @property resourceProvider Proveedor de recursos para obtener cadenas localizadas.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val isLoggedInUseCase: IsLoggedInUseCase,
    private val observeSessionInconsistencyUseCase: ObserveSessionInconsistencyUseCase,
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

    private val _forceLogoutEvent = MutableSharedFlow<Unit>()
    val forceLogoutEvent: SharedFlow<Unit> = _forceLogoutEvent.asSharedFlow()


    init {
        viewModelScope.launch {
            isLoggedInUseCase().collectLatest { loggedIn ->
                _isLoggedIn.value = loggedIn
                if (_isLoading.value) {
                    _isLoading.value = false
                }
            }
        }

        viewModelScope.launch {
            observeSessionInconsistencyUseCase().collect { isInconsistent ->
                if (isInconsistent) {
                    _forceLogoutEvent.emit(Unit)
                }
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