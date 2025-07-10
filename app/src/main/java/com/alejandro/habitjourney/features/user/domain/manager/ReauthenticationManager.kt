package com.alejandro.habitjourney.features.user.domain.manager

import com.alejandro.habitjourney.core.data.remote.exception.AuthException
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.user.domain.exception.ReauthenticationRequiredException
import com.alejandro.habitjourney.features.user.presentation.state.ReauthenticationType
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.GoogleAuthProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager para manejar la lógica de reautenticación de forma centralizada.
 */
@Singleton
class ReauthenticationManager @Inject constructor(
    val firebaseAuth: FirebaseAuth,
    private val resourceProvider: ResourceProvider
) {

    /**
     * Determina el tipo de reautenticación requerida basándose en el proveedor de autenticación actual.
     */
    fun getReauthenticationType(): ReauthenticationType? {
        val currentUser = firebaseAuth.currentUser ?: return null

        val mainProvider = currentUser.providerData.find {
            it.providerId != "firebase"
        }?.providerId

        return when (mainProvider) {
            EmailAuthProvider.PROVIDER_ID -> ReauthenticationType.EMAIL_PASSWORD
            GoogleAuthProvider.PROVIDER_ID -> ReauthenticationType.GOOGLE
            else -> {
                if (currentUser.email != null && currentUser.providerData.any {
                        it.providerId == EmailAuthProvider.PROVIDER_ID
                    }) {
                    ReauthenticationType.EMAIL_PASSWORD
                } else {
                    null
                }
            }
        }
    }

    /**
     * Verifica si una excepción específica requiere reautenticación.
     */
    fun requiresReauthentication(exception: Throwable): Boolean {
        return when (exception) {
            is FirebaseAuthRecentLoginRequiredException -> true
            is ReauthenticationRequiredException -> true
            is AuthException -> exception.cause is FirebaseAuthRecentLoginRequiredException
            else -> {
                val message = exception.message?.lowercase() ?: ""
                message.contains("recent login required") ||
                        message.contains("requires recent authentication")
            }
        }
    }

    /**
     * Verifica si el usuario actual tiene un método de autenticación específico vinculado
     */
    fun hasAuthMethod(providerId: String): Boolean {
        return firebaseAuth.currentUser?.providerData?.any {
            it.providerId == providerId
        } ?: false
    }

    /**
     * Obtiene todos los métodos de autenticación vinculados
     */
    fun getLinkedAuthMethods(): List<String> {
        return firebaseAuth.currentUser?.providerData?.map {
            it.providerId
        }?.filter { it != "firebase" } ?: emptyList()
    }
}