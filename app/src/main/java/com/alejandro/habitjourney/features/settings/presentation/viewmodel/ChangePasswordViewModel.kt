package com.alejandro.habitjourney.features.settings.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.exception.ErrorHandler
import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.user.domain.usecase.ChangePasswordUseCase
import com.alejandro.habitjourney.features.user.domain.util.UserValidationUtils
import com.alejandro.habitjourney.features.user.domain.util.ValidationResult
import com.alejandro.habitjourney.features.settings.presentation.state.ChangePasswordUiState
import com.alejandro.habitjourney.features.user.domain.manager.ReauthenticationManager
import com.alejandro.habitjourney.features.user.domain.usecase.ReauthenticateUserUseCase
import com.alejandro.habitjourney.features.user.domain.usecase.ReauthenticateWithGoogleUseCase
import com.alejandro.habitjourney.features.user.presentation.mixin.ReauthenticationMixin
import com.alejandro.habitjourney.navigation.AuthFlowCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para gestionar el cambio de contraseña del usuario.
 *
 * Extiende ReauthenticationMixin para manejar automáticamente la reautenticación
 * cuando sea necesaria para operaciones sensibles.
 *
 * Responsabilidades:
 * - Validar campos de contraseña en tiempo real
 * - Gestionar el proceso de cambio de contraseña
 * - Manejar estados de carga y errores
 * - Proporcionar mensajes de error localizados
 * - Manejar el flujo de reautenticación cuando sea necesario
 */
@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val changePasswordUseCase: ChangePasswordUseCase,
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
){
    private val _uiState = MutableStateFlow(ChangePasswordUiState())

    /**
     * Combina el estado de la UI con el estado de reautenticación
     */
    val combinedState: StateFlow<ChangePasswordUiState> = combine(
        _uiState,
        reauthState
    ) { uiState, reauthState ->
        uiState.copy(
            isLoading = uiState.isLoading || reauthState.isLoading,
            errorMessage = uiState.errorMessage ?: reauthState.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _uiState.value
    )


    /**
     * Actualiza la contraseña actual y valida el campo.
     */
    fun updateCurrentPassword(password: String) {
        _uiState.update { it.copy(currentPassword = password) }
        validateCurrentPassword(password)
    }

    /**
     * Actualiza la nueva contraseña y valida el campo.
     * También revalida la confirmación si ya se ha ingresado.
     */
    fun updateNewPassword(password: String) {
        _uiState.update { it.copy(newPassword = password) }
        validateNewPassword(password)
        if (_uiState.value.confirmPassword.isNotEmpty()) {
            validateConfirmPassword(_uiState.value.confirmPassword)
        }
    }

    /**
     * Actualiza la confirmación de contraseña y valida el campo.
     */
    fun updateConfirmPassword(password: String) {
        _uiState.update { it.copy(confirmPassword = password) }
        validateConfirmPassword(password)
    }

    /**
     * Valida que la contraseña actual no esté vacía.
     */
    private fun validateCurrentPassword(password: String) {
        _uiState.update {
            it.copy(
                currentPasswordError = if (password.isBlank()) {
                    resourceProvider.getString(R.string.error_current_password_required)
                } else null
            )
        }
    }

    /**
     * Valida la nueva contraseña usando las reglas de validación.
     */
    private fun validateNewPassword(password: String) {
        val result = UserValidationUtils.validatePassword(password,context)
        _uiState.update {
            it.copy(
                newPasswordError = if (result is ValidationResult.Error) result.message else null
            )
        }
    }

    /**
     * Valida que la confirmación coincida con la nueva contraseña.
     */
    private fun validateConfirmPassword(password: String) {
        val result = UserValidationUtils.validateConfirmPassword(
            _uiState.value.newPassword,
            password,
            context
        )
        _uiState.update {
            it.copy(
                confirmPasswordError = if (result is ValidationResult.Error) result.message else null
            )
        }
    }

    /**
     * Ejecuta el proceso de cambio de contraseña.
     * Valida todos los campos antes de proceder.
     */
    fun changePassword() {
        val state = _uiState.value
        validateCurrentPassword(state.currentPassword)
        validateNewPassword(state.newPassword)
        validateConfirmPassword(state.confirmPassword)
        if (!state.isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // **CAMBIO 2: Manejamos el NetworkResponse que 'executeWithReauth' devuelve.**
            when (val result = executeWithReauth { changePasswordUseCase(state.currentPassword, state.newPassword) }) {
                is NetworkResponse.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            currentPassword = "",
                            newPassword = "",
                            confirmPassword = ""
                        )
                    }
                }
                is NetworkResponse.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            // El Mixin ya actualiza su 'reauthState.errorMessage'.
                            // Si el error no es de reauth, lo ponemos aquí.
                            errorMessage = errorHandler.getErrorMessage(result.exception)
                        )
                    }
                }
                is NetworkResponse.Loading -> { /* No debería ocurrir aquí */ }
            }
        }
    }


    /**
     * Dispara la reautenticación con Email/Password cuando el usuario confirma en el diálogo.
     */
    fun confirmEmailPasswordReauthFromUi() {
        viewModelScope.launch {
            confirmEmailPasswordReauth()
        }
    }

    /**
     * Limpia el mensaje de error actual.
     * También limpia el error del Mixin.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
        dismissReauthenticationDialog() // Esto también limpia el errorMessage del Mixin
    }

    /**
     * Resetea el estado de éxito para permitir navegación.
     */
    fun resetSuccessState() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}