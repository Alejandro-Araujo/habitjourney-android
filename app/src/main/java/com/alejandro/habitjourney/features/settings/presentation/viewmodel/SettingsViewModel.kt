package com.alejandro.habitjourney.features.settings.presentation.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.exception.ErrorHandler
import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.settings.domain.usecase.GetAppSettingsUseCase
import com.alejandro.habitjourney.features.settings.domain.usecase.UpdateThemeUseCase
import com.alejandro.habitjourney.features.settings.presentation.state.Language
import com.alejandro.habitjourney.features.settings.presentation.state.SettingsUiState
import com.alejandro.habitjourney.features.settings.presentation.state.ThemeMode
import com.alejandro.habitjourney.features.user.domain.manager.ReauthenticationManager
import com.alejandro.habitjourney.features.user.domain.usecase.DeleteUserUseCase
import com.alejandro.habitjourney.features.user.domain.usecase.GetLocalUserUseCase
import com.alejandro.habitjourney.features.user.domain.usecase.LogoutUseCase
import com.alejandro.habitjourney.features.user.domain.usecase.ReauthenticateUserUseCase
import com.alejandro.habitjourney.features.user.domain.usecase.ReauthenticateWithGoogleUseCase
import com.alejandro.habitjourney.features.user.presentation.mixin.ReauthenticationMixin
import com.alejandro.habitjourney.navigation.AuthFlowCoordinator
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
    private val getLocalUserUseCase: GetLocalUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val getAppSettingsUseCase: GetAppSettingsUseCase,
    private val updateThemeUseCase: UpdateThemeUseCase,
    private val errorHandler: ErrorHandler,
    resourceProvider: ResourceProvider,
    reauthenticationManager: ReauthenticationManager,
    authFlowCoordinator: AuthFlowCoordinator,
    reauthenticateWithGoogleUseCase: ReauthenticateWithGoogleUseCase,
    reauthenticateUserUseCase: ReauthenticateUserUseCase,
    googleWebClientId: String
) : ReauthenticationMixin(
    reauthenticationManager,
    authFlowCoordinator,
    resourceProvider,
    errorHandler,
    reauthenticateWithGoogleUseCase,
    reauthenticateUserUseCase,
    googleWebClientId
) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    /**
     * Combina el estado de la UI específica de Settings con el estado de reautenticación del Mixin.
     */
    val combinedState: StateFlow<SettingsUiState> = combine(
        _uiState,
        reauthState
    ) { uiState, reauthMixinState ->
        uiState.copy(
            isLoading = uiState.isLoading || reauthMixinState.isLoading,
            message = uiState.message ?: reauthMixinState.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _uiState.value
    )

    init {
        loadUserData()
        loadSettings()
    }

    /**
     * Carga los datos del usuario actual usando el caso de uso correspondiente.
     */
    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getLocalUserUseCase().collect { user ->
                _uiState.update { it.copy(user = user, isLoading = false) }
            }
        }
    }

    /**
     * Carga las configuraciones actuales de la aplicación usando el caso de uso correspondiente.
     */
    private fun loadSettings() {
        viewModelScope.launch {
            getAppSettingsUseCase().collect { settings ->
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
     * Actualiza el tema de la aplicación usando el caso de uso correspondiente.
     * Aplica el cambio tanto en el repositorio como en AppCompatDelegate.
     */
    fun updateTheme(themeMode: ThemeMode) {
        viewModelScope.launch {
            val themeString = when (themeMode) {
                ThemeMode.LIGHT -> "light"
                ThemeMode.DARK -> "dark"
                ThemeMode.SYSTEM -> "system"
            }

            // Usar el caso de uso en lugar del repositorio directamente
            updateThemeUseCase(themeString)

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
     * Cierra la sesión del usuario actual usando el caso de uso correspondiente.
     */
    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }

            when (val result = logoutUseCase()) {
                is NetworkResponse.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            navigateToAuth = true,
                            message = resourceProvider.getString(R.string.logout_success)
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
                    // Estado de carga ya manejado arriba
                }
            }
        }
    }

    /**
     * Elimina permanentemente la cuenta del usuario.
     * Usa el ReauthenticationMixin para manejar la reautenticación si es necesaria.
     */
    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }

            // **CAMBIO 2: Manejamos el NetworkResponse que 'executeWithReauth' devuelve.**
            when (val result = executeWithReauth { deleteUserUseCase() }) {
                is NetworkResponse.Success -> {
                    // ¡Éxito! La cuenta fue eliminada (con posible reautenticación).
                    // Actualizamos el estado para navegar fuera.
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            navigateToAuth = true,
                            message = resourceProvider.getString(R.string.user_deleted_successfully)
                        )
                    }
                }
                is NetworkResponse.Error -> {
                    // La operación falló.
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            // El Mixin ya se encarga de mostrar el error en el diálogo de re-auth.
                            // Este mensaje es un fallback por si el error ocurre fuera de ese flujo.
                            message = errorHandler.getErrorMessage(result.exception)
                        )
                    }
                }
                is NetworkResponse.Loading -> { /* No debería ocurrir aquí */ }
            }
        }
    }

    /**
     * Limpia el mensaje de estado actual del UI State principal y también limpia los errores del Mixin.
     */
    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
        dismissReauthenticationDialog()
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