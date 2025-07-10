package com.alejandro.habitjourney.core.data.remote.exception


/**
 * Excepción personalizada para errores relacionados con la autenticación.
 * Permite encapsular un mensaje de error y, opcionalmente, la causa original.
 */
open class AuthException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Exception(message, cause)