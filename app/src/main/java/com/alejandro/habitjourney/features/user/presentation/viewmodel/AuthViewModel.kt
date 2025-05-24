package com.alejandro.habitjourney.features.user.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine // Importar combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository // Solo necesitamos el UserRepository
) : ViewModel() {

    // isLoggedIn ahora se deriva del Flow del UserRepository
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    init {
        // Observar el estado de login del repositorio
        viewModelScope.launch {
            // Combinar isLoggedIn del repositorio con nuestro isLoading para
            // decidir el estado final de _isLoggedIn.
            // Opcionalmente, puedes usar simplemente userRepository.isLoggedIn() si isLoading no afecta _isLoggedIn
            userRepository.isLoggedIn().collectLatest { loggedIn ->
                _isLoggedIn.value = loggedIn
                // Una vez que sabemos el estado de login, podemos decir que no estamos cargando inicialmente
                // Si el isLoading se maneja en otro lado (ej. durante login/registro), quita el finally en checkAuthStatus
                if (_isLoading.value) { // Solo cambiar a false si aún está en true al inicio
                    _isLoading.value = false
                }
            }
        }
        checkAuthStatus() // Llamada inicial para verificar el estado
    }

    // `checkAuthStatus` puede ser más simple ahora, ya que `isLoggedIn` del repo ya observa el token
    private fun checkAuthStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            try {
                // No necesitamos validar token aquí. El `isLoggedIn()` del repositorio ya lo hace.
                // Solo esperamos a que el flow emita su primer valor para quitar el estado de carga inicial.
                userRepository.isLoggedIn().first() // Esperar a que el flow emita su primer valor
            } catch (e: Exception) {
                // Manejar errores si la lectura inicial de preferencias falla (poco probable)
                _authError.value = "Error inicial de autenticación: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authError.value = null
            try {
                // El `logout()` del repositorio ya limpia localmente y notifica al backend si es necesario
                val response = userRepository.logout()
                response.onError { exception ->
                    // Si hay un error al hacer logout en el backend, registrarlo o mostrarlo
                    _authError.value = "Error al cerrar sesión: ${exception.message}"
                }
                // userRepository.logout() ya limpia el token, lo que hará que isLoggedIn.value se actualice
                // y se redirija al login. No necesitamos _isLoggedIn.value = false aquí.
            } catch (e: Exception) {
                _authError.value = "Error inesperado al cerrar sesión: ${e.message}"
            }
        }
    }

    // Este método se llamará desde LoginScreen/RegisterScreen después de una operación exitosa
    // El token ya se guarda en el UserRepository.login/register, así que no es necesario pasarlo aquí.
    fun onLoginSuccess() {
        _authError.value = null // Limpiar cualquier error de autenticación anterior
        // El `_isLoggedIn.value` se actualizará automáticamente a través del flow del userRepository
        // al guardar el token en userPreferences.
    }

    fun clearAuthError() {
        _authError.value = null
    }
}