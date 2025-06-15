package com.alejandro.habitjourney.features.settings.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.exception.ErrorHandler
import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import com.alejandro.habitjourney.features.user.domain.usecase.UpdateUserUseCase
import com.alejandro.habitjourney.features.user.domain.util.UserValidationUtils
import com.alejandro.habitjourney.features.user.domain.util.ValidationResult
import com.alejandro.habitjourney.features.settings.presentation.state.EditProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para gestionar la edición del perfil del usuario.
 *
 * Responsabilidades:
 * - Cargar los datos actuales del usuario
 * - Validar cambios en nombre y email en tiempo real
 * - Gestionar el proceso de actualización del perfil
 * - Manejar estados de carga y errores
 */
@HiltViewModel
class EditProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val updateUserUseCase: UpdateUserUseCase,
    private val errorHandler: ErrorHandler,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    /**
     * Carga los datos actuales del usuario desde el repositorio local.
     */
    private fun loadUserData() {
        viewModelScope.launch {
            userRepository.getLocalUser().first()?.let { user ->
                _uiState.update {
                    it.copy(
                        name = user.name,
                        email = user.email,
                        originalName = user.name,
                        originalEmail = user.email
                    )
                }
            }
        }
    }

    /**
     * Actualiza el nombre del usuario y valida el campo en tiempo real.
     */
    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
        validateName(name)
    }

    /**
     * Actualiza el email del usuario y valida el campo en tiempo real.
     */
    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
        validateEmail(email)
    }

    /**
     * Valida el nombre usando las reglas de validación centralizadas.
     */
    private fun validateName(name: String) {
        val result = UserValidationUtils.validateName(name, context)
        _uiState.update {
            it.copy(
                nameError = if (result is ValidationResult.Error) result.message else null
            )
        }
    }

    /**
     * Valida el email usando las reglas de validación centralizadas.
     */
    private fun validateEmail(email: String) {
        val result = UserValidationUtils.validateEmail(email, context)
        _uiState.update {
            it.copy(
                emailError = if (result is ValidationResult.Error) result.message else null
            )
        }
    }

    /**
     * Guarda los cambios del perfil del usuario.
     * Valida todos los campos antes de proceder con la actualización.
     */
    fun saveProfile() {
        val state = _uiState.value

        // Validar todos los campos antes de guardar
        validateName(state.name)
        validateEmail(state.email)

        if (!state.isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = updateUserUseCase(state.name, state.email)) {
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
                                R.string.error_updating_profile,
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
     * Limpia el mensaje de error actual.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}