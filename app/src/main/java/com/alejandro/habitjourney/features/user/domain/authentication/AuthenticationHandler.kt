package com.alejandro.habitjourney.features.user.domain.authentication

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.LifecycleCoroutineScope
import com.alejandro.habitjourney.core.utils.logging.AppLogger
import com.alejandro.habitjourney.navigation.AuthFlowCoordinator
import com.alejandro.habitjourney.navigation.AuthRequest
import com.alejandro.habitjourney.navigation.AuthResult
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationHandler @Inject constructor(
    private val credentialManager: CredentialManager,
    private val authFlowCoordinator: AuthFlowCoordinator
) {

    private val pendingGoogleSignInRequests = mutableMapOf<String, PendingCredentialRequest>()

    private data class PendingCredentialRequest(
        val requestId: String,
        val originalRequest: GetCredentialRequest,
        val isOneTapSignIn: Boolean
    )

    fun initialize(
        activity: Activity,
        lifecycleScope: LifecycleCoroutineScope,
        googleSignInLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        // Observar las solicitudes de autenticación
        lifecycleScope.launch {
            authFlowCoordinator.authRequests.collect { request ->
                when (request) {
                    is AuthRequest.GoogleSignIn -> {
                        AppLogger.d("AuthenticationHandler", "Received Google Sign-In request: ${request.requestId}, isOneTap: ${request.isOneTapSignIn}")
                        handleGoogleSignInRequest(activity, request, googleSignInLauncher)
                    }
                    is AuthRequest.EmailPasswordSignIn -> {
                        AppLogger.d("AuthenticationHandler", "Received Email/Password Sign-In request: ${request.requestId}. Not handled by AuthenticationHandler.")
                        authFlowCoordinator.handleAuthResult(
                            AuthResult.Error(request.requestId, "Error: Email/Password Sign-In requests should be handled in ViewModel directly.")
                        )
                    }
                    is AuthRequest.EmailPasswordReauth -> {
                        AppLogger.d("AuthenticationHandler", "Received Email/Password Reauth request: ${request.requestId}. Not handled by AuthenticationHandler.")
                        authFlowCoordinator.handleAuthResult(
                            AuthResult.Error(request.requestId, "Error: Email/Password Reauth requests should be handled in ViewModel directly.")
                        )
                    }
                }
            }
        }
    }

    private fun handleGoogleSignInRequest(
        activity: Activity,
        request: AuthRequest.GoogleSignIn,
        googleSignInLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        launchGoogleCredentialManager(
            activity = activity,
            request = request.request,
            requestId = request.requestId,
            isOneTapSignIn = request.isOneTapSignIn,
            googleSignInLauncher = googleSignInLauncher
        )
    }

    private fun launchGoogleCredentialManager(
        activity: Activity,
        request: GetCredentialRequest,
        requestId: String,
        isOneTapSignIn: Boolean,
        googleSignInLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            try {
                val response = credentialManager.getCredential(
                    context = activity,
                    request = request
                )
                handleCredentialResponse(response, requestId)
            } catch (e: GetCredentialException) {
                if (e.type == "android.credentials.TYPE_INTERACTION_REQUIRED" || e.type == "androidx.credentials.TYPE_INTERACTION_REQUIRED") {
                    AppLogger.d("AuthenticationHandler", "Interaction required for request ID: $requestId. Launching IntentSender.")

                    val pendingIntent: PendingIntent? = extractPendingIntentFromException(e)

                    if (pendingIntent != null) {
                        // Guardar la información de la solicitud pendiente
                        pendingGoogleSignInRequests[requestId] = PendingCredentialRequest(
                            requestId = requestId,
                            originalRequest = request,
                            isOneTapSignIn = isOneTapSignIn
                        )

                        // Lanzar el IntentSender
                        val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent.intentSender)
                            .setFillInIntent(Intent().putExtra("androidx.credentials.BUNDLE_KEY_REQUEST_ID", requestId))
                            .build()
                        googleSignInLauncher.launch(intentSenderRequest)
                    } else {
                        AppLogger.e("AuthenticationHandler", "PendingIntent missing for interaction required exception.", e)
                        handleCredentialException(e, requestId)
                    }
                } else {
                    AppLogger.e("AuthenticationHandler", "Non-interaction GetCredentialException for requestId $requestId: ${e.message}")
                    handleCredentialException(e, requestId)
                }
            } catch (e: Exception) {
                AppLogger.e("AuthenticationHandler", "General exception in launchGoogleCredentialManager for requestId $requestId: ${e.message}")
                handleCredentialException(e, requestId)
            }
        }
    }

    private fun extractPendingIntentFromException(e: GetCredentialException): PendingIntent? {
        return try {
            val method = e.javaClass.getMethod("getPendingIntent")
            method.invoke(e) as? PendingIntent
        } catch (ex: Exception) {
            AppLogger.e("AuthenticationHandler", "Failed to get pending intent via reflection for interaction required exception.", ex)
            null
        }
    }

    fun handleActivityResult(
        activity: Activity,
        resultCode: Int,
        data: Intent?
    ) {
        val requestId = data?.getStringExtra("androidx.credentials.BUNDLE_KEY_REQUEST_ID") ?: run {
            pendingGoogleSignInRequests.keys.firstOrNull()?.also {
                AppLogger.w("AuthenticationHandler", "Recovered requestId: $it from pending map as fallback for ActivityResultLauncher result.")
            }
        } ?: run {
            AppLogger.e("AuthenticationHandler", "Failed to retrieve a valid requestId for the ActivityResultLauncher result. Cannot process.")
            return
        }

        val pendingRequest = pendingGoogleSignInRequests.remove(requestId)
        if (pendingRequest == null) {
            AppLogger.e("AuthenticationHandler", "No pending request found for requestId: $requestId")
            return
        }

        if (resultCode == Activity.RESULT_OK) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                try {
                    val credentialResponse = credentialManager.getCredential(
                        context = activity,
                        request = pendingRequest.originalRequest
                    )
                    handleCredentialResponse(credentialResponse, requestId)
                } catch (e: Exception) {
                    handleCredentialException(e, requestId)
                }
            }
        } else {
            authFlowCoordinator.handleAuthResult(
                AuthResult.Cancelled(requestId)
            )
            AppLogger.d("AuthenticationHandler", "Google Sign-In cancelled with result code: $resultCode for request ID: $requestId")
        }
    }

    private fun handleCredentialResponse(credentialResponse: GetCredentialResponse, requestId: String) {
        val credential = credentialResponse.credential

        try {
            // Verificar el tipo y crear la instancia específica
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                AppLogger.d("AuthenticationHandler", "Google ID Token received for requestId: $requestId")
                authFlowCoordinator.handleAuthResult(
                    AuthResult.Success(requestId, googleIdTokenCredential.idToken)
                )
            } else {
                val errorMessage = "Unexpected credential type: ${credential.type}"
                AppLogger.e("AuthenticationHandler", errorMessage)
                authFlowCoordinator.handleAuthResult(
                    AuthResult.Error(requestId, errorMessage)
                )
            }
        } catch (e: Exception) {
            val errorMessage = "Error processing credential: ${e.message}"
            AppLogger.e("AuthenticationHandler", errorMessage, e)
            authFlowCoordinator.handleAuthResult(
                AuthResult.Error(requestId, errorMessage)
            )
        }
    }

    private fun handleCredentialException(e: Exception, requestId: String) {
        val errorMessage = when (e) {
            is GetCredentialException -> {
                val messageLower = e.message?.lowercase()
                if (messageLower?.contains("user canceled") == true ||
                    messageLower?.contains("user cancelled") == true ||
                    messageLower?.contains("cancellation") == true ||
                    e.type == "androidx.credentials.TYPE_USER_CANCELED") {
                    "User canceled the Credential Manager flow."
                } else if (e is NoCredentialException) {
                    "No valid or available credentials found."
                } else {
                    "Error getting credentials: ${e.message}"
                }
            }
            else -> "Unexpected error during Google authentication: ${e.message}"
        }
        AppLogger.e("AuthenticationHandler", "Credential Manager Error for requestId $requestId: $errorMessage", e)
        authFlowCoordinator.handleAuthResult(
            AuthResult.Error(requestId, errorMessage)
        )
    }

    fun cleanup() {
        pendingGoogleSignInRequests.clear()
    }
}