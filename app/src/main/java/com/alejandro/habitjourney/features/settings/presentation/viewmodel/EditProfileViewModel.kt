package com.alejandro.habitjourney.features.settings.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.exception.ErrorHandler
import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.user.domain.usecase.UpdateUserUseCase
import com.alejandro.habitjourney.features.user.domain.util.UserValidationUtils
import com.alejandro.habitjourney.features.user.domain.util.ValidationResult
import com.alejandro.habitjourney.features.settings.presentation.state.EditProfileUiState
import com.alejandro.habitjourney.features.user.domain.exception.ForceSignOutException
import com.alejandro.habitjourney.features.user.domain.manager.ReauthenticationManager
import com.alejandro.habitjourney.features.user.domain.usecase.GetLocalUserUseCase
import com.alejandro.habitjourney.features.user.domain.usecase.IsEmailVerifiedUseCase
import com.alejandro.habitjourney.features.user.domain.usecase.LogoutUseCase
import com.alejandro.habitjourney.features.user.domain.usecase.ReauthenticateWithGoogleUseCase
import com.alejandro.habitjourney.features.user.domain.usecase.ReauthenticateUserUseCase
import com.alejandro.habitjourney.features.user.domain.usecase.SendEmailVerificationUseCase
import com.alejandro.habitjourney.features.user.presentation.mixin.ReauthenticationMixin
import com.alejandro.habitjourney.navigation.AuthFlowCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para gestionar la edición del perfil del usuario.
 * Hereda de ReauthenticationMixin para manejar reautenticación automáticamente.
 */
@HiltViewModel
class EditProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getLocalUserUseCase: GetLocalUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val sendEmailVerificationUseCase: SendEmailVerificationUseCase,
    private val isEmailVerifiedUseCase: IsEmailVerifiedUseCase,
    private val errorHandler: ErrorHandler,
    reauthManager: ReauthenticationManager,
    authFlowCoordinator: AuthFlowCoordinator,
    resourceProvider: ResourceProvider,
    reauthenticateWithGoogleUseCase: ReauthenticateWithGoogleUseCase,
    reauthenticateUserUseCase: ReauthenticateUserUseCase,
    private val googleWebClientId: String
) : ReauthenticationMixin(
    reauthManager,
    authFlowCoordinator,
    resourceProvider,
    errorHandler,
    reauthenticateWithGoogleUseCase,
    reauthenticateUserUseCase,
    googleWebClientId
) {

    private val _editProfileUiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = combine(
        _editProfileUiState,
        reauthState,
        isEmailVerifiedUseCase()
    ) { editProfileState, reauth, isEmailVerified ->
        editProfileState.copy(
            isLoading = editProfileState.isLoading || reauth.isLoading,
            errorMessage = editProfileState.errorMessage ?: reauth.errorMessage,
            reauthState = reauth,
            isEmailVerified = isEmailVerified
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EditProfileUiState()
    )

    init {
        loadUserData()
    }

    /**
     * Carga los datos actuales del usuario desde el repositorio local.
     */
    private fun loadUserData() {
        viewModelScope.launch {
            _editProfileUiState.update { it.copy(isLoading = true, errorMessage = null) }
            getLocalUserUseCase().first()?.let { user ->
                _editProfileUiState.update {
                    it.copy(
                        name = user.name,
                        email = user.email,
                        originalName = user.name,
                        originalEmail = user.email,
                        isLoading = false
                    )
                }
            } ?: _editProfileUiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = resourceProvider.getString(R.string.error_loading_user_data)
                )
            }
        }
    }

    /**
     * Actualiza el nombre del usuario y valida el campo en tiempo real.
     */
    fun updateName(name: String) {
        _editProfileUiState.update { it.copy(name = name) }
        validateName(name)
    }

    /**
     * Actualiza el email del usuario y valida el campo en tiempo real.
     */
    fun updateEmail(email: String) {
        _editProfileUiState.update { it.copy(email = email) }
        validateEmail(email)
    }

    /**
     * Valida el nombre usando las reglas de validación centralizadas.
     */
    private fun validateName(name: String) {
        val result = UserValidationUtils.validateName(name, context)
        _editProfileUiState.update {
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
        _editProfileUiState.update {
            it.copy(
                emailError = if (result is ValidationResult.Error) result.message else null
            )
        }
    }

    /**
     * Guarda los cambios del perfil del usuario.
     * Usa executeWithReauth para manejar automáticamente la reautenticación.
     */
    fun saveProfile() {
        val state = _editProfileUiState.value
        validateName(state.name)
        validateEmail(state.email)
        if (!state.isValid) return

        viewModelScope.launch {
            _editProfileUiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    emailVerificationSent = false
                )
            }

            try {
                val emailWillChange = state.email != state.originalEmail

                when (val result = executeWithReauth { updateUserUseCase(state.name, state.email) }) {
                    is NetworkResponse.Success -> {
                        _editProfileUiState.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = true,
                                originalName = state.name,
                                originalEmail = state.email,
                                emailVerificationSent = false
                            )
                        }
                    }

                    is NetworkResponse.Error -> {
                        _editProfileUiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = errorHandler.getErrorMessage(result.exception)
                            )
                        }
                    }

                    is NetworkResponse.Loading -> {}
                }
            } catch (e: ForceSignOutException) {
                // Este catch maneja el caso cuando el email cambió
                _editProfileUiState.update {
                    it.copy(
                        isLoading = false,
                        forceSignOut = true,
                        showForceSignOutDialog = true,
                        emailVerificationSent = true,
                        errorMessage = null
                    )
                }
            }
        }
    }

    /**
     * Coordina la ejecución del signOut desde la capa de presentación
     */
    fun executeForceSignOut() {
        viewModelScope.launch {
            _editProfileUiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = logoutUseCase()) {
                is NetworkResponse.Success -> {
                    _editProfileUiState.update {
                        it.copy(
                            isLoading = false,
                            forceSignOut = false,
                            showForceSignOutDialog = false,
                            logoutCompleted = true,
                            errorMessage = null
                        )
                    }
                    // El logout fue exitoso, la navegación se manejará en la Screen
                }
                is NetworkResponse.Error -> {
                    _editProfileUiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = errorHandler.getErrorMessage(result.exception)
                        )
                    }
                }
                is NetworkResponse.Loading -> {}
            }
        }
    }


    /**
     * Dispara la reautenticación con Email/Password cuando el usuario confirma en el diálogo.
     * Esta función simplemente llama al Mixin.
     */
    fun confirmEmailPasswordReauthFromUi() {
        confirmEmailPasswordReauth()
    }

    /**
     * Envía el email de verificación si el email no está verificado.
     */
    fun sendVerificationEmail() {
        viewModelScope.launch {
            _editProfileUiState.update { it.copy(isLoading = true, errorMessage = null, emailVerificationSent = false) }
            when (val result = sendEmailVerificationUseCase()) {
                is NetworkResponse.Success -> {
                    _editProfileUiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true, // Indica que la operación de envío fue exitosa
                            emailVerificationSent = true, // Indica que el email de verificación fue enviado
                            errorMessage = resourceProvider.getString(R.string.email_verification_sent_success) // Mensaje para Snackbar
                        )
                    }
                }
                is NetworkResponse.Error -> {
                    _editProfileUiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = errorHandler.getErrorMessage(result.exception)
                        )
                    }
                }
                is NetworkResponse.Loading -> { }
            }
        }
    }

    /**
     * Limpia el mensaje de error actual.
     */
    fun clearError() {
        _editProfileUiState.update { it.copy(errorMessage = null) }
        dismissReauthenticationDialog()
    }

    /**
     * Resetea el estado de éxito para permitir navegación o nuevos cambios.
     */
    fun resetSuccessState() {
        _editProfileUiState.update { it.copy(isSuccess = false, emailVerificationSent = false) }
    }

    /**
     * Resetea el estado de forceSignOut y el diálogo asociado.
     */
    fun resetForceSignOutState() {
        _editProfileUiState.update {
            it.copy(
                forceSignOut = false,
                showForceSignOutDialog = false,
                logoutCompleted = false,
                errorMessage = null
            )
        }
    }

    /**
     * Resetea el estado de logout completado
     */
    fun resetLogoutState() {
        _editProfileUiState.update {
            it.copy(logoutCompleted = false)
        }
    }
}