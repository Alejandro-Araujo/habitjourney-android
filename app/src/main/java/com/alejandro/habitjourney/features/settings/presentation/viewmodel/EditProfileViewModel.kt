package com.alejandro.habitjourney.features.settings.presentation.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.features.user.domain.model.User
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import com.alejandro.habitjourney.features.user.domain.usecase.UpdateUserUseCase
import com.alejandro.habitjourney.features.user.domain.util.UserValidationUtils
import com.alejandro.habitjourney.features.user.domain.util.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import com.alejandro.habitjourney.features.settings.presentation.state.EditProfileUiState
import dagger.hilt.android.qualifiers.ApplicationContext


@HiltViewModel
class EditProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val updateUserUseCase: UpdateUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

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

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
        validateName(name)
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
        validateEmail(email)
    }

    private fun validateName(name: String) {
        val result = UserValidationUtils.validateName(name, context)
        _uiState.update {
            it.copy(
                nameError = if (result is ValidationResult.Error) result.message else null
            )
        }
    }

    private fun validateEmail(email: String) {
        val result = UserValidationUtils.validateEmail(email, context)
        _uiState.update {
            it.copy(
                emailError = if (result is ValidationResult.Error) result.message else null
            )
        }
    }

    fun saveProfile() {
        val state = _uiState.value

        // Validate all fields
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
                            errorMessage = result.exception.message ?: "Error al actualizar el perfil"
                        )
                    }
                }
                is NetworkResponse.Loading -> {

                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}