package com.alejandro.habitjourney.features.user.presentation.mixin

import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.exception.ErrorHandler
import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.user.domain.manager.ReauthenticationManager
import com.alejandro.habitjourney.features.user.domain.usecase.ReauthenticateUserUseCase
import com.alejandro.habitjourney.features.user.domain.usecase.ReauthenticateWithGoogleUseCase
import com.alejandro.habitjourney.features.user.presentation.state.ReauthenticationState
import com.alejandro.habitjourney.features.user.presentation.state.ReauthenticationType
import com.alejandro.habitjourney.navigation.AuthFlowCoordinator
import com.alejandro.habitjourney.navigation.AuthRequest
import com.alejandro.habitjourney.navigation.AuthResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Mixin para agregar funcionalidad de reautenticación a ViewModels.
 * Proporciona métodos suspendidos para manejar reautenticación de manera limpia.
 */
abstract class ReauthenticationMixin(
    private val reauthManager: ReauthenticationManager,
    private val authFlowCoordinator: AuthFlowCoordinator,
    val resourceProvider: ResourceProvider,
    private val errorHandler: ErrorHandler,
    private val reauthenticateWithGoogleUseCase: ReauthenticateWithGoogleUseCase,
    private val reauthenticateUserUseCase: ReauthenticateUserUseCase,
    private val googleWebClientId: String
) : ViewModel() {

    protected val _reauthState = MutableStateFlow(ReauthenticationState())
    val reauthState: StateFlow<ReauthenticationState> = _reauthState.asStateFlow()

    private var _pendingActionForReauth: (suspend () -> NetworkResponse<*>) ? = null
    private var _reauthCompletion: CompletableDeferred<NetworkResponse<*>>? = null


    /**
     * Ejecuta una acción que puede requerir reautenticación, suspendiendo hasta que el flujo completo termine.
     *
     * @param action La acción a ejecutar.
     * @return El NetworkResponse final después de un posible flujo de reautenticación y reintento.
     */
    protected suspend fun <T> executeWithReauth(action: suspend () -> NetworkResponse<T>): NetworkResponse<T> {
        return when (val initialResult = action()) {
            is NetworkResponse.Success -> initialResult
            is NetworkResponse.Error -> {
                if (reauthManager.requiresReauthentication(initialResult.exception)) {
                    val completion = CompletableDeferred<NetworkResponse<T>>()

                    _reauthCompletion = completion as CompletableDeferred<NetworkResponse<*>>
                    _pendingActionForReauth = action

                    handleReauthenticationRequired()

                    completion.await()
                } else {
                    handleError(initialResult.exception)
                    initialResult
                }
            }
            is NetworkResponse.Loading -> initialResult
        }
    }


    /**
     * Maneja el proceso de reautenticación requerida.
     * La acción pendiente se obtiene de _pendingActionForReauth.
     */
    private suspend fun handleReauthenticationRequired() {
        val authType = reauthManager.getReauthenticationType()

        if (authType == null) {
            val errorResponse = NetworkResponse.Error(Exception(resourceProvider.getString(R.string.error_cannot_determine_auth_method)))
            _reauthCompletion?.complete(errorResponse)
            resetReauthProcess()
            return
        }

        _reauthState.update {
            it.copy(showDialog = true, type = authType, isLoading = false, errorMessage = null)
        }

        if (authType == ReauthenticationType.GOOGLE) {
            performGoogleReauth()
        }
    }

    /**
     * Inicia la reautenticación de Google.
     */
    private suspend fun performGoogleReauth() {
        val requestId = authFlowCoordinator.generateRequestId()
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(googleWebClientId)
            .setAutoSelectEnabled(false)
            .build()

        val credentialRequest = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

        _reauthState.update { it.copy(isLoading = true, errorMessage = null) }

        when (val authResult = authFlowCoordinator.requestAuth(AuthRequest.GoogleSignIn(requestId, credentialRequest, false))) {
            is AuthResult.Success -> {
                when (val reauthResult = reauthenticateWithGoogleUseCase(authResult.credentialToken)) {
                    is NetworkResponse.Success -> retryPendingAction()
                    is NetworkResponse.Error -> {
                        val errorMessage = errorHandler.getErrorMessage(reauthResult.exception)
                        setReauthError(errorMessage)
                        _reauthCompletion?.complete(reauthResult)
                        resetReauthProcess()
                    }
                    is NetworkResponse.Loading -> {}
                }
            }
            is AuthResult.Error -> {
                setReauthError(authResult.message)
                val errorResponse = NetworkResponse.Error(Exception(authResult.message))
                _reauthCompletion?.complete(errorResponse)
                resetReauthProcess()
            }
            is AuthResult.Cancelled -> {
                _reauthState.update { it.copy(isLoading = false) }
                val errorResponse = NetworkResponse.Error(Exception("Reauthentication cancelled by user."))
                _reauthCompletion?.complete(errorResponse)
                resetReauthProcess()
            }
        }
    }

    /**
     * Ejecuta la reautenticación con Email/Password.
     * Esta función es llamada desde el ViewModel concreto (ej. EditProfileViewModel)
     * una vez que el usuario ha introducido la contraseña.
     * Contiene la lógica para reintentar la acción pendiente.
     */
    fun confirmEmailPasswordReauth() {
        viewModelScope.launch {
            val password = _reauthState.value.passwordInput
            if (password.isBlank()) {
                _reauthState.update { it.copy(passwordError = resourceProvider.getString(R.string.error_password_required)) }
                return@launch
            }

            _reauthState.update { it.copy(isLoading = true, passwordError = null) }

            val currentUser = reauthManager.firebaseAuth.currentUser
            if (currentUser?.email == null) {
                val error = NetworkResponse.Error(Exception(resourceProvider.getString(R.string.error_no_email_associated)))
                setReauthError(error.exception.message ?: "")
                _reauthCompletion?.complete(error)
                resetReauthProcess()
                return@launch
            }

            val credential = EmailAuthProvider.getCredential(currentUser.email!!, password)
            when (val reauthResult = reauthenticateUserUseCase(credential)) {
                is NetworkResponse.Success -> {
                    retryPendingAction()
                }
                is NetworkResponse.Error -> {
                    val errorMessage = errorHandler.getErrorMessage(reauthResult.exception)
                    setReauthError(errorMessage)
                    _reauthCompletion?.complete(reauthResult)
                    resetReauthProcess()
                }
                is NetworkResponse.Loading -> {}
            }
        }
    }

    /**
     * Reintenta la acción pendiente y completa el Deferred con el resultado.
     */
    private suspend fun retryPendingAction() {
        val action = _pendingActionForReauth
        if (action == null) {
            val error = NetworkResponse.Error(Exception(resourceProvider.getString(R.string.error_no_pending_action)))
            _reauthCompletion?.complete(error)
            resetReauthProcess()
            return
        }

        val retryResult = action()
        _reauthCompletion?.complete(retryResult)

        if (retryResult is NetworkResponse.Success) {
            _reauthState.update { it.copy(isSuccessfullReauthAndAction = true) }
        } else if (retryResult is NetworkResponse.Error) {
            handleError(retryResult.exception)
        }

        resetReauthProcess(isSuccess = retryResult is NetworkResponse.Success)
    }

    /**
     * Actualiza la entrada de contraseña para reautenticación.
     */
    fun updateReauthPasswordInput(password: String) {
        _reauthState.update {
            it.copy(passwordInput = password, passwordError = null)
        }
    }

    /**
     * Establece un mensaje de error en el estado de reautenticación.
     */
    private fun setReauthError(message: String) {
        _reauthState.update {
            it.copy(
                isLoading = false,
                errorMessage = message
            )
        }
    }

    /**
     * Maneja errores generales.
     */
    private fun handleError(exception: Throwable) {
        val errorMessage = errorHandler.getErrorMessage(exception)
        setReauthError(errorMessage)
    }

    fun dismissReauthenticationDialog() {
        val errorResponse = NetworkResponse.Error(Exception("Reauthentication cancelled by user."))
        _reauthCompletion?.complete(errorResponse)
        resetReauthProcess()
    }

    private fun resetReauthProcess(isSuccess: Boolean = false) {
        if(isSuccess){
            viewModelScope.launch {
                kotlinx.coroutines.delay(500)
                _reauthState.update { ReauthenticationState() }
            }
        } else {
            _reauthState.update { it.copy(showDialog = false, isLoading = false) }
        }
        _pendingActionForReauth = null
        _reauthCompletion = null
    }
}