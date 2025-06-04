package com.alejandro.habitjourney.features.settings.presentation.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.features.user.domain.usecase.ChangePasswordUseCase
import com.alejandro.habitjourney.features.user.domain.util.UserValidationUtils
import com.alejandro.habitjourney.features.user.domain.util.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import com.alejandro.habitjourney.features.settings.presentation.state.ChangePasswordUiState
import dagger.hilt.android.qualifiers.ApplicationContext


@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val changePasswordUseCase: ChangePasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun updateCurrentPassword(password: String) {
        _uiState.update { it.copy(currentPassword = password) }
        validateCurrentPassword(password)
    }

    fun updateNewPassword(password: String) {
        _uiState.update { it.copy(newPassword = password) }
        validateNewPassword(password)
        // Re-validate confirm password if it's not empty
        if (_uiState.value.confirmPassword.isNotEmpty()) {
            validateConfirmPassword(_uiState.value.confirmPassword)
        }
    }

    fun updateConfirmPassword(password: String) {
        _uiState.update { it.copy(confirmPassword = password) }
        validateConfirmPassword(password)
    }

    private fun validateCurrentPassword(password: String) {
        _uiState.update {
            it.copy(
                currentPasswordError = if (password.isBlank()) {
                    "La contraseña actual es requerida"
                } else null
            )
        }
    }

    private fun validateNewPassword(password: String) {
        val result = UserValidationUtils.validatePassword(password, context)
        _uiState.update {
            it.copy(
                newPasswordError = if (result is ValidationResult.Error) result.message else null
            )
        }
    }

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

    fun changePassword() {
        val state = _uiState.value

        // Validate all fields
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
                            errorMessage = result.exception.message ?: "Error al cambiar la contraseña"
                        )
                    }
                }
                is NetworkResponse.Loading -> {
                    // No-op
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}