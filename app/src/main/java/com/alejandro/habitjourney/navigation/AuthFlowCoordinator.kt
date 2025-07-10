package com.alejandro.habitjourney.navigation

import androidx.credentials.GetCredentialRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

// Definiciones de Request y Result para el coordinador
sealed class AuthRequest {
    abstract val requestId: String

    data class GoogleSignIn(
        override val requestId: String,
        val request: GetCredentialRequest,
        val isOneTapSignIn: Boolean
    ) : AuthRequest()

    data class EmailPasswordSignIn(
        override val requestId: String,
        val email: String
    ) : AuthRequest()

    data class EmailPasswordReauth(
        override val requestId: String,
        val email: String
    ) : AuthRequest()
}

sealed class AuthResult {
    abstract val requestId: String

    data class Success(
        override val requestId: String,
        val credentialToken: String
    ) : AuthResult()

    data class Error(
        override val requestId: String,
        val message: String
    ) : AuthResult()

    data class Cancelled(
        override val requestId: String
    ) : AuthResult()
}

/**
 * Coordinador central para manejar el flujo de autenticación/reautenticación entre ViewModels y la Activity.
 * Usa suspendCancellableCoroutine para permitir que los ViewModels "esperen" el resultado.
 */
@Singleton
class AuthFlowCoordinator @Inject constructor() {

    // Flow para emitir solicitudes de autenticación (observado por MainActivity)
    private val _authRequests = MutableSharedFlow<AuthRequest>(extraBufferCapacity = 1)
    val authRequests: SharedFlow<AuthRequest> = _authRequests.asSharedFlow()

    // Mapa para almacenar las continuaciones de coroutines pendientes
    private val pendingContinuations = mutableMapOf<String, (AuthResult) -> Unit>()

    /**
     * Solicita una acción de autenticación/reautenticación y espera su resultado.
     * Esta es la función clave que llamarán los ViewModels.
     * @return El resultado de la autenticación.
     */
    suspend fun requestAuth(request: AuthRequest): AuthResult {
        return suspendCancellableCoroutine { continuation ->
            // Almacena la continuación en el mapa, asociada a un requestId único
            pendingContinuations[request.requestId] = { result ->
                continuation.resume(result)
            }

            // Si la coroutine se cancela, limpia la continuación del mapa
            continuation.invokeOnCancellation {
                pendingContinuations.remove(request.requestId)
            }

            // Emite la solicitud para que la MainActivity la observe y la maneje
            _authRequests.tryEmit(request)
        }
    }

    /**
     * Maneja el resultado de una solicitud de autenticación recibida desde la Activity.
     * La Activity llamará a este método cuando obtenga un resultado.
     */
    fun handleAuthResult(result: AuthResult) {
        pendingContinuations[result.requestId]?.invoke(result)
        pendingContinuations.remove(result.requestId)
    }

    /**
     * Método de utilidad para generar un ID único para cada solicitud
     */
    fun generateRequestId(): String = UUID.randomUUID().toString()
}