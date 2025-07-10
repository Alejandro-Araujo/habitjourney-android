package com.alejandro.habitjourney.features.user.domain.exception

import com.alejandro.habitjourney.core.data.remote.exception.AuthException

/**
 * Excepción para indicar que se requiere reautenticación del usuario.
 * Encapsula el mensaje y la excepción original de Firebase si existe.
 */
class ReauthenticationRequiredException(
    override val message: String? = null,
    override val cause: Throwable? = null,
    //val newEmail: String
) : AuthException(message, cause)