package com.alejandro.habitjourney.features.settings.presentation.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.exception.ErrorHandler
import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.settings.domain.repository.SettingsRepository
import com.alejandro.habitjourney.features.settings.presentation.state.Language
import com.alejandro.habitjourney.features.settings.presentation.state.SettingsUiState
import com.alejandro.habitjourney.features.settings.presentation.state.ThemeMode
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import com.alejandro.habitjourney.features.user.domain.usecase.DeleteUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel principal para la pantalla de configuración.
 *
 * Responsabilidades:
 * - Gestionar configuraciones de tema y idioma
 * - Manejar cierre de sesión y eliminación de cuenta
 * - Cargar datos del usuario actual
 * - Coordinar navegación entre pantallas de configuración
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val errorHandler: ErrorHandler,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
        loadSettings()
    }

    /**
     * Carga los datos del usuario actual desde el repositorio local.
     */
    private fun loadUserData() {
        viewModelScope.launch {
            userRepository.getLocalUser().collect { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
    }

    /**
     * Carga las configuraciones actuales de la aplicación.
     */
    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getAppSettings().collect { settings ->
                _uiState.update {
                    it.copy(
                        currentTheme = mapToThemeMode(settings.theme),
                        currentLanguage = Language.fromCode(settings.language)
                    )
                }
            }
        }
    }

    /**
     * Actualiza el tema de la aplicación.
     * Aplica el cambio tanto en el repositorio como en AppCompatDelegate.
     */
    fun updateTheme(themeMode: ThemeMode) {
        viewModelScope.launch {
            val themeString = when (themeMode) {
                ThemeMode.LIGHT -> "light"
                ThemeMode.DARK -> "dark"
                ThemeMode.SYSTEM -> "system"
            }
            settingsRepository.updateTheme(themeString)

            // Aplicar el cambio a nivel de aplicación
            val appCompatThemeMode = when (themeMode) {
                ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(appCompatThemeMode)

            _uiState.update { it.copy(currentTheme = themeMode) }
        }
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = userRepository.logout()) {
                is NetworkResponse.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            navigateToAuth = true
                        )
                    }
                }
                is NetworkResponse.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = resourceProvider.getString(
                                R.string.error_logout_failed,
                                errorHandler.getErrorMessage(result.exception)
                            )
                        )
                    }
                }
                is NetworkResponse.Loading -> {
                    // Estado ya manejado arriba
                }
            }
        }
    }

    /**
     * Elimina permanentemente la cuenta del usuario.
     * Requiere confirmación del usuario.
     */
    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = deleteUserUseCase()) {
                is NetworkResponse.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            navigateToAuth = true
                        )
                    }
                }
                is NetworkResponse.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = resourceProvider.getString(
                                R.string.error_deleting_account,
                                errorHandler.getErrorMessage(result.exception)
                            )
                        )
                    }
                }
                is NetworkResponse.Loading -> {
                    // Estado ya manejado arriba
                }
            }
        }
    }

    /**
     * Limpia el mensaje de estado actual.
     */
    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    /**
     * Marca la navegación como manejada después de navegar a auth.
     */
    fun onNavigationHandled() {
        _uiState.update { it.copy(navigateToAuth = false) }
    }

    /**
     * Convierte string de tema a enum ThemeMode.
     */
    private fun mapToThemeMode(theme: String): ThemeMode {
        return when (theme) {
            "light" -> ThemeMode.LIGHT
            "dark" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }
}