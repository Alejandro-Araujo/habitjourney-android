package com.alejandro.habitjourney.features.settings.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.exception.ErrorHandler
import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.user.domain.usecase.ChangePasswordUseCase
import com.alejandro.habitjourney.features.user.domain.util.UserValidationUtils
import com.alejandro.habitjourney.features.user.domain.util.ValidationResult
import com.alejandro.habitjourney.features.settings.presentation.state.ChangePasswordUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para gestionar el cambio de contraseña del usuario.
 *
 * Responsabilidades:
 * - Validar campos de contraseña en tiempo real
 * - Gestionar el proceso de cambio de contraseña
 * - Manejar estados de carga y errores
 * - Proporcionar mensajes de error localizados
 */
@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val errorHandler: ErrorHandler,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

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

        // Validar todos los campos
        validateCurrentPassword(state.currentPassword)
        validateNewPassword(state.newPassword)
        validateConfirmPassword(state.confirmPassword)

        if (!state.isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = changePasswordUseCase(state.currentPassword, state.newPassword)) {
                is NetworkResponse.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                    }
                }
                is NetworkResponse.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = resourceProvider.getString(
                                R.string.error_changing_password,
                                errorHandler.getErrorMessage(result.exception)
                            )
                        )
                    }
                }
                is NetworkResponse.Loading -> {
                    // Estado ya manejado
                }
            }
        }
    }

    /**
     * Limpia el mensaje de error actual.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}