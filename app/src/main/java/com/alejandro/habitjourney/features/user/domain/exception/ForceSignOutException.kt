package com.alejandro.habitjourney.features.user.domain.exception

import com.alejandro.habitjourney.core.data.remote.exception.AuthException

/**
 * Excepción para indicar que la sesión del usuario ha sido invalidada
 * y que se requiere un inicio de sesión completo para restablecerla.
 * Esto es común después de operaciones sensibles de Firebase como la actualización de email.
 */
class ForceSignOutException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : AuthException(message, cause)